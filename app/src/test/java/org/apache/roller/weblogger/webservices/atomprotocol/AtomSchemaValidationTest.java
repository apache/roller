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

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.xml.sax.ErrorHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXParseException;

import com.thaiopensource.util.PropertyMapBuilder;
import com.thaiopensource.validate.ValidateProperty;
import com.thaiopensource.validate.ValidationDriver;
import com.thaiopensource.validate.rng.CompactSchemaReader;

/**
 * Validates the XML produced by {@link AtomWriter} against the official RELAX NG
 * schemas from RFC 4287 (Atom) and RFC 5023 (AtomPub), using Jing. This checks
 * that Roller's AtomPub wire format actually conforms to the specifications,
 * not just that it is well-formed.
 */
public class AtomSchemaValidationTest {

    private static final Date PUBLISHED = Date.from(Instant.parse("2026-06-03T12:34:56Z"));
    private static final Date UPDATED = Date.from(Instant.parse("2026-06-04T01:02:03Z"));

    /** Validate xml against a classpath RELAX NG Compact schema; return errors (empty == valid). */
    private List<String> validate(String schemaResource, byte[] xml) throws Exception {
        List<String> errors = new ArrayList<>();
        ErrorHandler handler = new ErrorHandler() {
            @Override public void warning(SAXParseException e) { /* ignore warnings */ }
            @Override public void error(SAXParseException e) { errors.add(e.getMessage()); }
            @Override public void fatalError(SAXParseException e) { errors.add(e.getMessage()); }
        };
        PropertyMapBuilder props = new PropertyMapBuilder();
        props.put(ValidateProperty.ERROR_HANDLER, handler);
        ValidationDriver driver =
                new ValidationDriver(props.toPropertyMap(), CompactSchemaReader.getInstance());

        try (InputStream schema = getClass().getResourceAsStream(schemaResource)) {
            assertTrue(driver.loadSchema(new InputSource(schema)),
                    "schema " + schemaResource + " failed to compile: " + errors);
        }
        driver.validate(new InputSource(new ByteArrayInputStream(xml)));
        return errors;
    }

    private byte[] writeEntry(AtomEntry entry) throws Exception {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        new AtomWriter().writeEntry(out, entry);
        return out.toByteArray();
    }

    private AtomEntry textEntry() {
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

    private AtomEntry mediaEntry() {
        AtomEntry entry = new AtomEntry();
        entry.setId("http://example.com/app/blog/resource/snapshot.png");
        entry.setTitle("snapshot.png");
        entry.setUpdated(UPDATED);
        entry.setEdited(UPDATED);
        entry.setDraft(false);

        AtomContent content = new AtomContent();
        content.setType("image/png");
        content.setSrc("http://example.com/blog/mediaresource/snapshot.png");
        entry.setContent(content);

        entry.getLinks().add(new AtomLink("alternate", "http://example.com/blog/snapshot.png"));
        entry.getLinks().add(new AtomLink("edit", "http://example.com/app/blog/resource/snapshot.png.media-link"));
        entry.getLinks().add(new AtomLink("edit-media", "http://example.com/app/blog/resource/snapshot.png"));
        return entry;
    }

    @Test
    public void testTextEntryConformsToAtomSchema() throws Exception {
        List<String> errors = validate("/atompub/atom.rnc", writeEntry(textEntry()));
        assertTrue(errors.isEmpty(), "Atom entry should be schema-valid but: " + errors);
    }

    @Test
    public void testMediaEntryConformsToAtomSchema() throws Exception {
        List<String> errors = validate("/atompub/atom.rnc", writeEntry(mediaEntry()));
        assertTrue(errors.isEmpty(), "media link entry should be schema-valid but: " + errors);
    }

    @Test
    public void testFeedConformsToAtomSchema() throws Exception {
        AtomFeed feed = new AtomFeed();
        feed.setId("urn:feed:1");
        feed.setTitle("My Blog");
        feed.setUpdated(UPDATED);
        feed.getLinks().add(new AtomLink("alternate", "http://example.com/blog"));
        feed.getEntries().add(textEntry());
        feed.getEntries().add(textEntry());

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        new AtomWriter().writeFeed(out, feed);

        List<String> errors = validate("/atompub/atom.rnc", out.toByteArray());
        assertTrue(errors.isEmpty(), "Atom feed should be schema-valid but: " + errors);
    }

    @Test
    public void testServiceDocConformsToAppSchema() throws Exception {
        AtomServiceDoc service = new AtomServiceDoc();
        AtomWorkspace workspace = new AtomWorkspace();
        workspace.setTitle("My Weblog");
        service.getWorkspaces().add(workspace);

        AtomCollection entries = new AtomCollection();
        entries.setTitle("Weblog Entries");
        entries.setHref("http://example.com/app/blog/entries");
        entries.setAccepts(Arrays.asList("application/atom+xml;type=entry"));
        AtomCategories fixed = new AtomCategories();
        fixed.setFixed(true);
        fixed.setScheme("http://example.com/cats");
        AtomCategory cat = new AtomCategory();
        cat.setTerm("tech");
        cat.setLabel("tech");
        fixed.getCategories().add(cat);
        entries.getCategories().add(fixed);
        entries.getCategories().add(new AtomCategories()); // free-form
        workspace.getCollections().add(entries);

        AtomCollection media = new AtomCollection();
        media.setTitle("Media Files: default");
        media.setHref("http://example.com/app/blog/resources/default");
        media.setAccepts(Arrays.asList("image/png", "image/jpeg"));
        workspace.getCollections().add(media);

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        new AtomWriter().writeServiceDoc(out, service);

        List<String> errors = validate("/atompub/app-service.rnc", out.toByteArray());
        assertTrue(errors.isEmpty(), "service document should be schema-valid but: " + errors);
    }
}
