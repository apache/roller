/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 *  contributor license agreements.  The ASF licenses this file to You
 * under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or
 * implied.  See the License for the specific language governing
 * permissions and limitations under the License.  For additional
 * information regarding copyright in this work, please see the NOTICE
 * file in the top level directory of this distribution.
 */
package org.apache.roller.weblogger.util;

import java.io.StringReader;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.atomic.AtomicInteger;

import org.jdom2.Document;
import org.jdom2.input.SAXBuilder;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * The parser contract, checked directly rather than through a caller.
 *
 * <p>Each case that asserts a resource is not resolved also renders the same
 * document through a plain {@link SAXBuilder} first, and asserts that one does
 * resolve it. Without that reference the assertions would still pass if the
 * document were simply malformed, or if the parser were refusing it for some
 * unrelated reason.
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

    /** A declared file is not read. */
    @Test
    public void aDeclaredFileIsNotRead() throws Exception {
        Path secret = Files.createTempFile("roller-saxbuilder-probe", ".txt");
        Files.write(secret, "PROBE-CONTENT-4d21".getBytes(StandardCharsets.UTF_8));

        String doc = "<?xml version=\"1.0\"?>"
                + "<!DOCTYPE r [<!ENTITY probe SYSTEM \"" + secret.toUri() + "\">]>"
                + "<r>&probe;</r>";

        // Reference: the unhardened parser does read it.
        String reference = renderWith(new SAXBuilder(), doc);
        assertTrue(reference.contains("PROBE-CONTENT-4d21"),
                "control failed: the plain parser did not read the declared file, so "
                        + "the assertion below shows nothing:\n" + reference);

        String hardened = renderWith(new SafeSAXBuilder(), doc);
        assertFalse(hardened.contains("PROBE-CONTENT-4d21"),
                "a declared file was read:\n" + hardened);

        Files.deleteIfExists(secret);
    }

    /** A declared URL is not requested. */
    @Test
    public void aDeclaredUrlIsNotRequested() throws Exception {
        try (ServerSocket listener = new ServerSocket(0)) {
            listener.setSoTimeout(1500);
            AtomicInteger connections = new AtomicInteger();
            Thread accepting = new Thread(() -> {
                while (!Thread.currentThread().isInterrupted()) {
                    try (Socket s = listener.accept()) {
                        connections.incrementAndGet();
                    } catch (Exception stop) {
                        return;
                    }
                }
            });
            accepting.setDaemon(true);
            accepting.start();

            String url = "http://127.0.0.1:" + listener.getLocalPort() + "/probe.dtd";
            String doc = "<?xml version=\"1.0\"?>"
                    + "<!DOCTYPE r SYSTEM \"" + url + "\"><r>x</r>";

            // Reference: the unhardened parser does request it.
            renderWith(new SAXBuilder(), doc);
            Thread.sleep(300);
            int afterPlain = connections.get();
            assertTrue(afterPlain > 0,
                    "control failed: the plain parser made no request, so the "
                            + "assertion below shows nothing");

            renderWith(new SafeSAXBuilder(), doc);
            Thread.sleep(300);
            accepting.interrupt();

            assertEquals(afterPlain, connections.get(),
                    "the hardened parser requested a declared URL");
        }
    }

    /**
     * @return the document's text content, or a marker naming the failure, so a
     *         parser that refuses the document and one that reads nothing from
     *         it are not confused with each other
     */
    private String renderWith(SAXBuilder builder, String xml) {
        try {
            Document doc = builder.build(new StringReader(xml));
            return String.valueOf(doc.getRootElement().getValue());
        } catch (Exception refused) {
            return "refused: " + refused.getClass().getSimpleName();
        }
    }
}
