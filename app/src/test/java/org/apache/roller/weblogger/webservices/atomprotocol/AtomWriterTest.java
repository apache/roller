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

import static org.apache.roller.weblogger.webservices.atomprotocol.AtomConstants.APP_NS;
import static org.apache.roller.weblogger.webservices.atomprotocol.AtomConstants.ATOM_NS;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.time.Instant;
import java.util.Arrays;
import java.util.Date;

import javax.xml.parsers.DocumentBuilderFactory;

import org.junit.jupiter.api.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

/**
 * Unit tests for the StAX-based {@link AtomWriter}. The emitted XML is parsed
 * back with a namespace-aware DOM parser so the structure and namespaces can be
 * asserted directly.
 */
public class AtomWriterTest {

    private static final Date PUBLISHED = Date.from(Instant.parse("2026-06-03T12:34:56Z"));
    private static final Date UPDATED = Date.from(Instant.parse("2026-06-04T01:02:03Z"));

    private Document parse(byte[] xml) throws Exception {
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        dbf.setNamespaceAware(true);
        return dbf.newDocumentBuilder().parse(new ByteArrayInputStream(xml));
    }

    private String text(Document doc, String ns, String local) {
        NodeList nl = doc.getElementsByTagNameNS(ns, local);
        return nl.getLength() == 0 ? null : nl.item(0).getTextContent();
    }

    private Element element(Document doc, String ns, String local) {
        NodeList nl = doc.getElementsByTagNameNS(ns, local);
        return nl.getLength() == 0 ? null : (Element) nl.item(0);
    }

    private AtomEntry sampleEntry() {
        AtomEntry entry = new AtomEntry();
        entry.setId("urn:entry:1");
        entry.setTitle("Hello World");
        entry.setPublished(PUBLISHED);
        entry.setUpdated(UPDATED);
        entry.setEdited(UPDATED);
        entry.setDraft(false);

        AtomContent content = new AtomContent();
        content.setType("html");
        content.setValue("<b>Body</b> & more");
        entry.setContent(content);

        AtomContent summary = new AtomContent();
        summary.setType("html");
        summary.setValue("A summary");
        entry.setSummary(summary);

        AtomPerson author = new AtomPerson();
        author.setName("alice");
        author.setEmail("alice@example.com");
        entry.getAuthors().add(author);

        AtomCategory cat = new AtomCategory();
        cat.setTerm("tech");
        cat.setScheme("http://example.com/cats");
        entry.getCategories().add(cat);
        AtomCategory tag = new AtomCategory();
        tag.setTerm("java");
        entry.getCategories().add(tag);

        entry.getLinks().add(new AtomLink("alternate", "http://example.com/blog/1"));
        entry.getLinks().add(new AtomLink("edit", "http://example.com/app/blog/entry/1"));
        return entry;
    }

    @Test
    public void testWriteEntryStructure() throws Exception {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        new AtomWriter().writeEntry(out, sampleEntry());
        Document doc = parse(out.toByteArray());

        assertEquals("entry", doc.getDocumentElement().getLocalName());
        assertEquals(ATOM_NS, doc.getDocumentElement().getNamespaceURI());

        assertEquals("urn:entry:1", text(doc, ATOM_NS, "id"));
        assertEquals("Hello World", text(doc, ATOM_NS, "title"));
        assertEquals(AtomWriter.formatDate(PUBLISHED), text(doc, ATOM_NS, "published"));
        assertEquals(AtomWriter.formatDate(UPDATED), text(doc, ATOM_NS, "updated"));

        Element content = element(doc, ATOM_NS, "content");
        assertEquals("html", content.getAttribute("type"));
        // markup is escaped on the wire but DOM gives us back the original text
        assertEquals("<b>Body</b> & more", content.getTextContent());

        assertEquals("A summary", text(doc, ATOM_NS, "summary"));
        assertEquals("alice", text(doc, ATOM_NS, "name"));

        // draft "no" plus app:edited present
        assertEquals("no", text(doc, APP_NS, "draft"));
        assertEquals(AtomWriter.formatDate(UPDATED), text(doc, APP_NS, "edited"));
    }

    @Test
    public void testWriteEntryDraftYes() throws Exception {
        AtomEntry entry = sampleEntry();
        entry.setDraft(true);
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        new AtomWriter().writeEntry(out, entry);
        assertEquals("yes", text(parse(out.toByteArray()), APP_NS, "draft"));
    }

    @Test
    public void testWriteEntryCategoriesSchemeAndTag() throws Exception {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        new AtomWriter().writeEntry(out, sampleEntry());
        Document doc = parse(out.toByteArray());

        NodeList cats = doc.getElementsByTagNameNS(ATOM_NS, "category");
        assertEquals(2, cats.getLength());
        Element tech = (Element) cats.item(0);
        assertEquals("tech", tech.getAttribute("term"));
        assertEquals("http://example.com/cats", tech.getAttribute("scheme"));
        Element java = (Element) cats.item(1);
        assertEquals("java", java.getAttribute("term"));
        // a tag carries no scheme attribute at all
        assertFalse(java.hasAttribute("scheme"));
    }

    @Test
    public void testWriteEntryEditLink() throws Exception {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        new AtomWriter().writeEntry(out, sampleEntry());
        Document doc = parse(out.toByteArray());

        NodeList links = doc.getElementsByTagNameNS(ATOM_NS, "link");
        assertEquals(2, links.getLength());
        boolean foundEdit = false;
        for (int i = 0; i < links.getLength(); i++) {
            Element link = (Element) links.item(i);
            if ("edit".equals(link.getAttribute("rel"))) {
                assertEquals("http://example.com/app/blog/entry/1", link.getAttribute("href"));
                foundEdit = true;
            }
        }
        assertTrue(foundEdit);
    }

    @Test
    public void testWriteFeed() throws Exception {
        AtomFeed feed = new AtomFeed();
        feed.setId("urn:feed:1");
        feed.setTitle("My Blog");
        feed.setUpdated(UPDATED);
        feed.getLinks().add(new AtomLink("alternate", "http://example.com/blog"));
        feed.getEntries().add(sampleEntry());
        feed.getEntries().add(sampleEntry());

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        new AtomWriter().writeFeed(out, feed);
        Document doc = parse(out.toByteArray());

        assertEquals("feed", doc.getDocumentElement().getLocalName());
        assertEquals(ATOM_NS, doc.getDocumentElement().getNamespaceURI());
        assertEquals("My Blog", text(doc, ATOM_NS, "title"));
        assertEquals(2, doc.getElementsByTagNameNS(ATOM_NS, "entry").getLength());
    }

    @Test
    public void testWriteServiceDoc() throws Exception {
        AtomServiceDoc service = new AtomServiceDoc();
        AtomWorkspace workspace = new AtomWorkspace();
        workspace.setTitle("My Weblog");
        service.getWorkspaces().add(workspace);

        AtomCollection collection = new AtomCollection();
        collection.setTitle("Weblog Entries");
        collection.setHref("http://example.com/app/blog/entries");
        collection.setAccepts(Arrays.asList("application/atom+xml;type=entry"));
        AtomCategories cats = new AtomCategories();
        cats.setFixed(true);
        cats.setScheme("http://example.com/cats");
        AtomCategory cat = new AtomCategory();
        cat.setTerm("tech");
        cat.setLabel("tech");
        cats.getCategories().add(cat);
        collection.getCategories().add(cats);
        workspace.getCollections().add(collection);

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        new AtomWriter().writeServiceDoc(out, service);
        Document doc = parse(out.toByteArray());

        assertEquals("service", doc.getDocumentElement().getLocalName());
        assertEquals(APP_NS, doc.getDocumentElement().getNamespaceURI());
        assertNotNull(element(doc, APP_NS, "workspace"));

        Element coll = element(doc, APP_NS, "collection");
        assertEquals("http://example.com/app/blog/entries", coll.getAttribute("href"));
        // titles are atom:title (Atom namespace) even inside the service doc
        assertEquals("My Weblog", text(doc, ATOM_NS, "title"));
        assertEquals("application/atom+xml;type=entry", text(doc, APP_NS, "accept"));

        Element categories = element(doc, APP_NS, "categories");
        assertEquals("yes", categories.getAttribute("fixed"));
        assertEquals("http://example.com/cats", categories.getAttribute("scheme"));
        assertEquals("tech", element(doc, ATOM_NS, "category").getAttribute("term"));
    }

    @Test
    public void testEntryRoundTripThroughReader() throws Exception {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        new AtomWriter().writeEntry(out, sampleEntry());

        AtomEntry parsed = new AtomReader().parseEntry(new ByteArrayInputStream(out.toByteArray()));

        assertEquals("urn:entry:1", parsed.getId());
        assertEquals("Hello World", parsed.getTitle());
        assertEquals("<b>Body</b> & more", parsed.getContent().getValue());
        assertEquals("A summary", parsed.getSummary().getValue());
        assertEquals(PUBLISHED, parsed.getPublished());
        assertEquals(UPDATED, parsed.getUpdated());
        assertFalse(parsed.isDraft());
        assertEquals(2, parsed.getCategories().size());
        assertEquals("http://example.com/cats", parsed.getCategories().get(0).getScheme());
        assertNull(parsed.getCategories().get(1).getScheme());
    }
}
