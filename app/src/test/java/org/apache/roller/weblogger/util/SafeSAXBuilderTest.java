/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 *  contributor license agreements.  The ASF licenses this file to You
 *  under the Apache License, Version 2.0 (the "License"); you may not
 *  use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.  For additional information regarding
 *  copyright in this work, please see the NOTICE file in the top level
 *  directory of this distribution.
 */
package org.apache.roller.weblogger.util;

import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import org.jdom2.Document;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * The parser contract, checked directly rather than through a caller.
 */
public class SafeSAXBuilderTest {

    private static final String ORDINARY =
            "<?xml version=\"1.0\"?><opml version=\"1.1\"><head><title>t</title>"
                    + "</head><body><outline text=\"a\"/></body></opml>";

    /** Ordinary XML, carrying no declarations, still parses. */
    @Test
    public void ordinaryDocumentsStillParse() throws Exception {
        Document doc = new SafeSAXBuilder().build(new StringReader(ORDINARY));
        assertNotNull(doc.getRootElement());
        assertEquals("opml", doc.getRootElement().getName());
    }

    /** Any document type declaration is refused, whatever it points at. */
    @Test
    public void anyDoctypeIsRefused() {
        String withInternalSubset = "<?xml version=\"1.0\"?>"
                + "<!DOCTYPE opml [<!ELEMENT opml ANY>]>"
                + "<opml version=\"1.1\"><body/></opml>";
        assertThrows(Exception.class,
                () -> new SafeSAXBuilder().build(new StringReader(withInternalSubset)),
                "a document type declaration was accepted");
    }

    /** A declared external entity is refused, and its file is never read into the document. */
    @Test
    public void externalEntitiesAreNotRead(@TempDir Path dir) throws Exception {
        Path marker = dir.resolve("marker.txt");
        Files.write(marker, "MARKER-CONTENT".getBytes(StandardCharsets.UTF_8));
        String withEntity = "<?xml version=\"1.0\"?>"
                + "<!DOCTYPE opml [<!ENTITY m SYSTEM \"" + marker.toUri() + "\">]>"
                + "<opml version=\"1.1\"><body><outline text=\"&m;\"/></body></opml>";
        Exception e = assertThrows(Exception.class,
                () -> new SafeSAXBuilder().build(new StringReader(withEntity)));
        assertFalse(String.valueOf(e.getMessage()).contains("MARKER-CONTENT"));
    }

    /** Readers come from the shared parser configuration. */
    @Test
    public void readersComeFromTheSharedConfiguration() {
        assertSame(SecureXmlParsers.JDOM_READERS, new SafeSAXBuilder().getXMLReaderFactory());
    }
}
