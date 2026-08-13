/*
* Licensed to the Apache Software Foundation (ASF) under one or more
* contributor license agreements.  The ASF licenses this file to You
* under the Apache License, Version 2.0 (the "License"); you may not
* use this file except in compliance with the License.
* You may obtain a copy of the License at
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
package org.apache.roller.weblogger.webservices.atomprotocol;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;

import org.junit.jupiter.api.Test;

/**
 * Unit tests for the StAX-based {@link AtomReader}.
 */
public class AtomReaderTest {

    private AtomEntry parse(String xml) throws AtomException {
        return new AtomReader().parseEntry(
                new ByteArrayInputStream(xml.getBytes(StandardCharsets.UTF_8)));
    }

    @Test
    public void testParseFullEntry() throws Exception {
        String xml =
            "<?xml version='1.0' encoding='UTF-8'?>"
            + "<entry xmlns='http://www.w3.org/2005/Atom'"
            + "       xmlns:app='http://www.w3.org/2007/app'>"
            + "  <id>urn:entry:1</id>"
            + "  <title>Hello World</title>"
            + "  <published>2026-06-03T12:34:56Z</published>"
            + "  <updated>2026-06-04T01:02:03Z</updated>"
            + "  <summary type='html'>A summary</summary>"
            + "  <content type='html'>&lt;b&gt;Body&lt;/b&gt; &amp; more</content>"
            + "  <category term='tech' scheme='http://example.com/cats'/>"
            + "  <category term='java'/>"
            + "  <app:control><app:draft>yes</app:draft></app:control>"
            + "</entry>";

        AtomEntry entry = parse(xml);

        assertEquals("urn:entry:1", entry.getId());
        assertEquals("Hello World", entry.getTitle());
        assertNotNull(entry.getContent());
        assertEquals("html", entry.getContent().getType());
        assertEquals("<b>Body</b> & more", entry.getContent().getValue());
        assertNotNull(entry.getSummary());
        assertEquals("A summary", entry.getSummary().getValue());
        assertEquals(Date.from(Instant.parse("2026-06-03T12:34:56Z")), entry.getPublished());
        assertEquals(Date.from(Instant.parse("2026-06-04T01:02:03Z")), entry.getUpdated());
        assertTrue(entry.isDraft());

        assertEquals(2, entry.getCategories().size());
        AtomCategory cat = entry.getCategories().get(0);
        assertEquals("tech", cat.getTerm());
        assertEquals("http://example.com/cats", cat.getScheme());
        AtomCategory tag = entry.getCategories().get(1);
        assertEquals("java", tag.getTerm());
        assertNull(tag.getScheme());
    }

    @Test
    public void testDraftDefaultsToFalseWhenNoControl() throws Exception {
        String xml =
            "<entry xmlns='http://www.w3.org/2005/Atom'>"
            + "<title>No control</title></entry>";
        assertFalse(parse(xml).isDraft());
    }

    @Test
    public void testDraftNoIsNotDraft() throws Exception {
        String xml =
            "<entry xmlns='http://www.w3.org/2005/Atom'"
            + "       xmlns:app='http://www.w3.org/2007/app'>"
            + "<app:control><app:draft>no</app:draft></app:control></entry>";
        assertFalse(parse(xml).isDraft());
    }

    @Test
    public void testContentWithSrcHasNoValue() throws Exception {
        String xml =
            "<entry xmlns='http://www.w3.org/2005/Atom'>"
            + "<content type='image/png' src='http://example.com/a.png'/></entry>";
        AtomEntry entry = parse(xml);
        assertEquals("image/png", entry.getContent().getType());
        assertEquals("http://example.com/a.png", entry.getContent().getSrc());
        assertNull(entry.getContent().getValue());
    }

    @Test
    public void testParseDateAcceptsZuluAndOffset() {
        assertEquals(Date.from(Instant.parse("2026-06-03T12:34:56Z")),
                AtomReader.parseDate("2026-06-03T12:34:56Z"));
        // 14:34:56+02:00 is the same instant as 12:34:56Z
        assertEquals(Date.from(Instant.parse("2026-06-03T12:34:56Z")),
                AtomReader.parseDate("2026-06-03T14:34:56+02:00"));
    }

    @Test
    public void testParseDateReturnsNullForGarbage() {
        assertNull(AtomReader.parseDate(null));
        assertNull(AtomReader.parseDate("   "));
        assertNull(AtomReader.parseDate("not-a-date"));
    }

    @Test
    public void testExternalEntityIsRejected() {
        // A DOCTYPE with an external entity must not be processed (XXE guard).
        String xml =
            "<?xml version='1.0'?>"
            + "<!DOCTYPE entry [ <!ENTITY xxe SYSTEM 'file:///etc/passwd'> ]>"
            + "<entry xmlns='http://www.w3.org/2005/Atom'><title>&xxe;</title></entry>";
        assertThrows(AtomException.class, () -> parse(xml));
    }
}
