/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.  For additional information regarding
 * copyright in this work, please see the NOTICE file in the top level
 * directory of this distribution.
 */

package org.apache.roller.weblogger.util;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import javax.xml.parsers.SAXParserFactory;

import org.apache.commons.xml.secure.SecureSAXParserFactory;
import org.apache.xmlrpc.util.SAXParsers;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.xml.sax.Attributes;
import org.xml.sax.helpers.DefaultHandler;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SecureXmlParsersTest {

    private static final String NAMESPACED =
            "<?xml version=\"1.0\"?><feed xmlns=\"http://www.w3.org/2005/Atom\"><title>t</title></feed>";

    private static final String WITH_DOCTYPE =
            "<?xml version=\"1.0\"?><!DOCTYPE feed [<!ELEMENT feed ANY>]><feed/>";

    @Test
    void factoryIsNamespaceAwareAndNonValidating() {
        SAXParserFactory factory = SecureXmlParsers.newSAXParserFactory();
        assertTrue(factory.isNamespaceAware());
        assertFalse(factory.isValidating());
    }

    @Test
    void ordinaryNamespacedDocumentsParse() throws Exception {
        RootRecorder root = new RootRecorder();
        SecureXmlParsers.newSAXParserFactory().newSAXParser().parse(bytes(NAMESPACED), root);
        assertEquals("http://www.w3.org/2005/Atom", root.uri);
        assertEquals("feed", root.localName);
    }

    @Test
    void documentTypeDeclarationsAreRefused() {
        assertThrows(Exception.class, () -> SecureXmlParsers.newSAXParserFactory()
                .newSAXParser().parse(bytes(WITH_DOCTYPE), new DefaultHandler()));
    }

    /** The Commons layer on its own does not read a declared external entity. */
    @Test
    void commonsLayerDoesNotReadExternalEntities(@TempDir Path dir) throws Exception {
        Path marker = dir.resolve("marker.txt");
        Files.write(marker, "MARKER-CONTENT".getBytes(StandardCharsets.UTF_8));
        String withEntity = "<?xml version=\"1.0\"?>"
                + "<!DOCTYPE feed [<!ENTITY m SYSTEM \"" + marker.toUri() + "\">]>"
                + "<feed>&m;</feed>";
        StringBuilder text = new StringBuilder();
        try {
            SecureSAXParserFactory.newNSInstance().newSAXParser().parse(bytes(withEntity),
                    new DefaultHandler() {
                        @Override
                        public void characters(char[] ch, int start, int length) {
                            text.append(ch, start, length);
                        }
                    });
        } catch (Exception refused) {
            // Refusing the document outright is also acceptable.
        }
        assertFalse(text.toString().contains("MARKER-CONTENT"));
    }

    @Test
    void xmlRpcUsesTheSameConfiguration() throws Exception {
        SAXParserFactory previous = SAXParsers.getSAXParserFactory();
        try {
            SecureXmlParsers.installForXmlRpc();
            SAXParserFactory installed = SAXParsers.getSAXParserFactory();
            assertTrue(installed.isNamespaceAware());
            assertThrows(Exception.class, () -> installed.newSAXParser()
                    .parse(bytes(WITH_DOCTYPE), new DefaultHandler()));
        } finally {
            SAXParsers.setSAXParserFactory(previous);
        }
    }

    private static ByteArrayInputStream bytes(String xml) {
        return new ByteArrayInputStream(xml.getBytes(StandardCharsets.UTF_8));
    }

    private static final class RootRecorder extends DefaultHandler {
        String uri;
        String localName;

        @Override
        public void startElement(String uri, String localName, String qName, Attributes attributes) {
            if (this.localName == null) {
                this.uri = uri;
                this.localName = localName;
            }
        }
    }
}
