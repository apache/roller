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
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.  For additional information regarding
 * copyright in this work, please see the NOTICE file in the top level
 * directory of this distribution.
 */

package org.apache.roller.weblogger.webservices.atomprotocol;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamReader;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Keeps the XML examples in the AtomPub guide ({@code docs/atompub/examples})
 * in step with the code.
 *
 * <ul>
 * <li>Response examples are written by {@link AtomWriter} from the same wire
 * model the collections build, indented, and compared with the committed
 * files. Run with {@code -Datompub.docs.update=true} to rewrite them.</li>
 * <li>Request examples ({@code *-request.xml}) are written by hand; they must
 * be accepted by {@link AtomReader}.</li>
 * <li>All examples are checked against the RFC 4287 / RFC 5023 grammars, and
 * every Roller extension element against {@code docs/atompub/roller-atompub.rnc}.</li>
 * </ul>
 */
public class AtomPubDocExamplesTest {

    private static final String ROLLER = "https://example.com/roller";
    private static final String APP = ROLLER + "/roller-services/app";
    private static final String BLOG = APP + "/myblog";
    private static final Date PUBLISHED = Date.from(Instant.parse("2026-10-10T12:00:00Z"));
    private static final Date UPDATED = Date.from(Instant.parse("2026-10-10T12:30:00Z"));

    private static final String ROLLER_SCHEMA = "roller-atompub.rnc";

    private static final Path DOCS = locateDocs();

    @Test
    public void generatedExamplesMatchTheDocs() throws Exception {
        boolean update = Boolean.getBoolean("atompub.docs.update");
        List<String> stale = new ArrayList<>();
        for (Map.Entry<String, byte[]> example : generatedExamples().entrySet()) {
            String expected = indent(example.getValue());
            Path file = DOCS.resolve("examples").resolve(example.getKey());
            if (update) {
                Files.write(file, expected.getBytes(StandardCharsets.UTF_8));
            } else if (!Files.exists(file)
                    || !expected.equals(new String(Files.readAllBytes(file), StandardCharsets.UTF_8))) {
                stale.add(example.getKey());
            }
        }
        assertTrue(stale.isEmpty(), "AtomPub doc examples are out of date: " + stale
                + ". Regenerate with: mvn -pl app test -Dtest=AtomPubDocExamplesTest"
                + " -Datompub.docs.update=true");
    }

    @Test
    public void requestExamplesAreAcceptedByTheServer() throws Exception {
        AtomEntry entry = read("entry-request.xml");
        assertEquals("Hello AtomPub", entry.getTitle());
        assertEquals(2, entry.getCategories().size());

        AtomEntry category = read("category-request.xml");
        assertEquals("Travel", category.getTitle());
        assertEquals("https://example.com/images/travel.png", category.getExtension("image"));

        AtomEntry template = read("template-request.xml");
        assertEquals("custom", template.getExtension("action"));
        assertTrue(template.getMobileRendition().contains("sidebar-mobile"));

        AtomEntry status = read("comment-status-request.xml");
        assertEquals("approved", status.getExtension("status"));
    }

    @Test
    public void examplesConformToTheSchemas() throws Exception {
        try (Stream<Path> files = Files.list(DOCS.resolve("examples"))) {
            for (Path file : (Iterable<Path>) files.filter(f -> f.toString().endsWith(".xml"))::iterator) {
                byte[] xml = Files.readAllBytes(file);
                String root = parse(xml).name;
                String schema = root.endsWith("service") ? "/atompub/app-service.rnc"
                        : root.endsWith("categories") ? null : "/atompub/atom.rnc";
                if (schema != null) {
                    List<String> errors = RelaxNgValidator.validateResource(schema, xml);
                    assertTrue(errors.isEmpty(), file.getFileName() + ": " + errors);
                }
                for (byte[] extension : extensionElements(parse(xml))) {
                    List<String> errors;
                    try (InputStream rnc = Files.newInputStream(DOCS.resolve(ROLLER_SCHEMA))) {
                        errors = RelaxNgValidator.validate(rnc, ROLLER_SCHEMA, extension);
                    }
                    assertTrue(errors.isEmpty(), file.getFileName() + ": "
                            + new String(extension, StandardCharsets.UTF_8) + " " + errors);
                }
            }
        }
    }

    // ------------------------------------------------------------ examples

    private static Map<String, byte[]> generatedExamples() throws Exception {
        Map<String, byte[]> examples = new LinkedHashMap<>();
        examples.put("service.xml", write(w -> w.writeServiceDoc(w.out, serviceDoc())));
        examples.put("entry-response.xml", write(w -> w.writeEntry(w.out, weblogEntry())));
        examples.put("entries-feed.xml", write(w -> w.writeFeed(w.out, entriesFeed())));
        examples.put("media-link-entry.xml", write(w -> w.writeEntry(w.out, mediaLinkEntry())));
        examples.put("categories.xml", write(w -> w.writeCategoriesDoc(w.out, categories())));
        examples.put("category-entry.xml", write(w -> w.writeEntry(w.out, categoryEntry())));
        examples.put("template-entry.xml", write(w -> w.writeEntry(w.out, templateEntry())));
        examples.put("comment-entry.xml", write(w -> w.writeEntry(w.out, commentEntry())));
        examples.put("comments-feed.xml", write(w -> w.writeFeed(w.out, commentsFeed())));
        return examples;
    }

    private static AtomServiceDoc serviceDoc() {
        AtomServiceDoc service = new AtomServiceDoc();
        AtomWorkspace workspace = new AtomWorkspace();
        workspace.setTitle("My Weblog");
        service.getWorkspaces().add(workspace);

        AtomCollection entries = collection("Weblog Entries", BLOG + "/entries",
                "application/atom+xml;type=entry");
        AtomCategories inline = categories();
        entries.getCategories().add(inline);
        AtomCategories byReference = new AtomCategories();
        byReference.setHref(BLOG + "/categories.atomcat");
        entries.getCategories().add(byReference);
        entries.getCategories().add(new AtomCategories());
        workspace.getCollections().add(entries);

        AtomCollection media = collection("Media Files: default", BLOG + "/resources/default",
                "image/*");
        media.getAccepts().add("application/pdf");
        workspace.getCollections().add(media);
        workspace.getCollections().add(collection("Templates", BLOG + "/templates",
                "application/atom+xml;type=entry"));
        workspace.getCollections().add(collection("Categories", BLOG + "/categories",
                "application/atom+xml;type=entry"));
        workspace.getCollections().add(collection("Comments", BLOG + "/comments", ""));
        return service;
    }

    private static AtomEntry weblogEntry() {
        AtomEntry entry = new AtomEntry();
        entry.setId(ROLLER + "/myblog/entry/hello_atompub");
        entry.setTitle("Hello AtomPub");
        entry.setPublished(PUBLISHED);
        entry.setUpdated(UPDATED);
        entry.setEdited(UPDATED);
        entry.setContent(CollectionSupport.text("html", "<p>Posted with <b>AtomPub</b>.</p>"));
        AtomPerson author = new AtomPerson();
        author.setName("dave");
        author.setEmail("dave@example.com");
        entry.getAuthors().add(author);
        AtomCategory category = new AtomCategory();
        category.setTerm("Java");
        category.setScheme(ROLLER + "/myblog/");
        entry.getCategories().add(category);
        AtomCategory tag = new AtomCategory();
        tag.setTerm("atompub");
        entry.getCategories().add(tag);
        entry.getLinks().add(new AtomLink("alternate", ROLLER + "/myblog/entry/hello_atompub"));
        entry.getLinks().add(new AtomLink("edit", BLOG + "/entry/4a7d1ed4"));
        return entry;
    }

    private static AtomFeed entriesFeed() {
        AtomFeed feed = new AtomFeed();
        feed.setId(BLOG + "/entries/0");
        feed.setTitle("My Weblog");
        feed.setUpdated(UPDATED);
        feed.getLinks().add(link("alternate", ROLLER + "/myblog"));
        feed.getLinks().add(link("next", BLOG + "/entries/20"));
        feed.getEntries().add(weblogEntry());
        return feed;
    }

    private static AtomEntry mediaLinkEntry() {
        AtomEntry entry = new AtomEntry();
        entry.setId(BLOG + "/resource/default/sunset.jpg");
        entry.setTitle("sunset.jpg");
        entry.setUpdated(UPDATED);
        entry.setEdited(UPDATED);
        AtomContent content = new AtomContent();
        content.setType("image/jpeg");
        content.setSrc(ROLLER + "/myblog/mediaresource/2b9c4f0e");
        entry.setContent(content);
        entry.getLinks().add(link("alternate", ROLLER + "/myblog/mediaresource/2b9c4f0e"));
        entry.getLinks().add(link("edit", BLOG + "/resource/default/sunset.jpg.media-link"));
        entry.getLinks().add(link("edit-media", BLOG + "/resource/default/sunset.jpg"));
        return entry;
    }

    private static AtomCategories categories() {
        AtomCategories cats = new AtomCategories();
        cats.setFixed(true);
        cats.setScheme(ROLLER + "/myblog/");
        for (String name : Arrays.asList("General", "Java", "Travel")) {
            AtomCategory cat = new AtomCategory();
            cat.setTerm(name);
            cat.setLabel(name);
            cats.getCategories().add(cat);
        }
        return cats;
    }

    private static AtomEntry categoryEntry() {
        AtomEntry entry = new AtomEntry();
        entry.setId(BLOG + "/category/7c3e9a10");
        entry.setTitle("Travel");
        entry.setUpdated(UPDATED);
        entry.setSummary(CollectionSupport.text("text", "Trips and places"));
        entry.setExtension("image", "https://example.com/images/travel.png");
        entry.setExtension("position", "2");
        entry.setExtension("inUse", "true");
        entry.getLinks().add(link("edit", BLOG + "/category/7c3e9a10"));
        return entry;
    }

    private static AtomEntry templateEntry() {
        AtomEntry entry = new AtomEntry();
        entry.setId(BLOG + "/template/0e5d8b62");
        entry.setTitle("sidebar");
        entry.setSummary(CollectionSupport.text("text", "The sidebar"));
        entry.setUpdated(UPDATED);
        entry.setEdited(UPDATED);
        entry.setContent(CollectionSupport.text("text",
                "<div class=\"sidebar\">$model.weblog.name</div>"));
        entry.setMobileRendition("<div class=\"sidebar-mobile\">$model.weblog.name</div>");
        entry.setExtension("action", "custom");
        entry.setExtension("link", "sidebar");
        entry.setExtension("navbar", "true");
        entry.setExtension("hidden", "false");
        entry.setExtension("required", "false");
        entry.getLinks().add(link("edit", BLOG + "/template/0e5d8b62"));
        return entry;
    }

    private static AtomEntry commentEntry() {
        AtomEntry entry = new AtomEntry();
        entry.setId(BLOG + "/comment/8f0e2a6c");
        entry.setTitle("Comment on Hello AtomPub");
        entry.setPublished(PUBLISHED);
        entry.setUpdated(PUBLISHED);
        AtomPerson author = new AtomPerson();
        author.setName("Pat Reader");
        author.setEmail("pat@example.org");
        author.setUri("https://pat.example.org/");
        entry.getAuthors().add(author);
        entry.setContent(CollectionSupport.text("text", "Nice post!"));
        entry.setInReplyToRef(ROLLER + "/myblog/entry/hello_atompub");
        entry.setInReplyToHref(BLOG + "/entry/4a7d1ed4");
        entry.setExtension("status", "pending");
        entry.setExtension("remoteHost", "192.0.2.10");
        entry.getLinks().add(link("edit", BLOG + "/comment/8f0e2a6c"));
        entry.getLinks().add(link("related", ROLLER + "/myblog/entry/hello_atompub"));
        return entry;
    }

    private static AtomFeed commentsFeed() {
        AtomFeed feed = new AtomFeed();
        feed.setId(BLOG + "/comments/0");
        feed.setTitle("My Weblog comments");
        feed.setUpdated(UPDATED);
        feed.getLinks().add(link("next", BLOG + "/comments/20?status=pending"));
        feed.getEntries().add(commentEntry());
        return feed;
    }

    private static AtomCollection collection(String title, String href, String accept) {
        AtomCollection collection = new AtomCollection();
        collection.setTitle(title);
        collection.setHref(href);
        collection.getAccepts().add(accept);
        return collection;
    }

    private static AtomLink link(String rel, String href) {
        return CollectionSupport.link(rel, href);
    }

    // ------------------------------------------------------------- helpers

    @FunctionalInterface
    private interface Writing {
        void run(WriterOut w) throws AtomException;
    }

    /** An AtomWriter with the stream it writes to. */
    private static final class WriterOut extends AtomWriter {
        final ByteArrayOutputStream out = new ByteArrayOutputStream();
    }

    private static byte[] write(Writing writing) throws AtomException {
        WriterOut w = new WriterOut();
        writing.run(w);
        return w.out.toByteArray();
    }

    private static AtomEntry read(String name) throws IOException, AtomException {
        try (InputStream in = Files.newInputStream(DOCS.resolve("examples").resolve(name))) {
            return new AtomReader().parseEntry(in);
        }
    }

    /** docs/atompub, from the app module (Maven) or the repository root (IDE). */
    private static Path locateDocs() {
        Path cwd = Paths.get("").toAbsolutePath();
        for (Path base : Arrays.asList(cwd.getParent(), cwd)) {
            if (base != null && Files.isDirectory(base.resolve("docs/atompub"))) {
                return base.resolve("docs/atompub");
            }
        }
        throw new IllegalStateException("Cannot find docs/atompub from " + cwd);
    }

    // ----------------------------------------- a small XML tree and printer

    /** An element: qualified name, attributes and namespace declarations, children. */
    private static final class Node {
        final String name;
        final List<String[]> attributes = new ArrayList<>();
        final List<Object> children = new ArrayList<>();
        String namespace;

        Node(String name) {
            this.name = name;
        }
    }

    private static Node parse(byte[] xml) throws Exception {
        XMLInputFactory factory = XMLInputFactory.newInstance();
        factory.setProperty(XMLInputFactory.SUPPORT_DTD, Boolean.FALSE);
        XMLStreamReader r = factory.createXMLStreamReader(new ByteArrayInputStream(xml), "UTF-8");
        List<Node> stack = new ArrayList<>();
        Node root = null;
        while (r.hasNext()) {
            int event = r.next();
            if (event == XMLStreamConstants.START_ELEMENT) {
                Node node = new Node(qname(r.getPrefix(), r.getLocalName()));
                node.namespace = r.getNamespaceURI();
                for (int i = 0; i < r.getNamespaceCount(); i++) {
                    String prefix = r.getNamespacePrefix(i);
                    node.attributes.add(new String[] {
                        prefix == null || prefix.isEmpty() ? "xmlns" : "xmlns:" + prefix,
                        r.getNamespaceURI(i)});
                }
                for (int i = 0; i < r.getAttributeCount(); i++) {
                    node.attributes.add(new String[] {
                        qname(r.getAttributePrefix(i), r.getAttributeLocalName(i)),
                        r.getAttributeValue(i)});
                }
                if (stack.isEmpty()) {
                    root = node;
                } else {
                    stack.get(stack.size() - 1).children.add(node);
                }
                stack.add(node);
            } else if (event == XMLStreamConstants.END_ELEMENT) {
                stack.remove(stack.size() - 1);
            } else if ((event == XMLStreamConstants.CHARACTERS || event == XMLStreamConstants.CDATA)
                    && !stack.isEmpty() && !r.isWhiteSpace()) {
                stack.get(stack.size() - 1).children.add(r.getText());
            }
        }
        return root;
    }

    private static String qname(String prefix, String local) {
        return prefix == null || prefix.isEmpty() ? local : prefix + ":" + local;
    }

    /** Re-indents a document: two spaces per level, text-only elements on one line. */
    private static String indent(byte[] xml) throws Exception {
        StringBuilder out = new StringBuilder("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
        print(parse(xml), 0, out);
        return out.toString();
    }

    private static void print(Node node, int depth, StringBuilder out) {
        String pad = "  ".repeat(depth);
        out.append(pad).append('<').append(node.name);
        for (String[] attribute : node.attributes) {
            out.append(' ').append(attribute[0]).append("=\"")
                    .append(escape(attribute[1], true)).append('"');
        }
        if (node.children.isEmpty()) {
            out.append("/>\n");
            return;
        }
        boolean textOnly = node.children.stream().allMatch(c -> c instanceof String);
        if (textOnly) {
            out.append('>');
            for (Object text : node.children) {
                out.append(escape((String) text, false));
            }
            out.append("</").append(node.name).append(">\n");
            return;
        }
        out.append(">\n");
        for (Object child : node.children) {
            if (child instanceof Node) {
                print((Node) child, depth + 1, out);
            } else {
                out.append(pad).append("  ").append(escape((String) child, false)).append('\n');
            }
        }
        out.append(pad).append("</").append(node.name).append(">\n");
    }

    private static String escape(String text, boolean attribute) {
        String escaped = text.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
        return attribute ? escaped.replace("\"", "&quot;") : escaped;
    }

    /** Each roller:* or thr:* element, serialized alone with its namespace declared. */
    private static List<byte[]> extensionElements(Node node) {
        List<byte[]> found = new ArrayList<>();
        collectExtensions(node, found);
        return found;
    }

    private static void collectExtensions(Node node, List<byte[]> found) {
        for (Object child : node.children) {
            if (!(child instanceof Node)) {
                continue;
            }
            Node element = (Node) child;
            if (AtomConstants.ROLLER_NS.equals(element.namespace)
                    || AtomConstants.THREAD_NS.equals(element.namespace)) {
                StringBuilder xml = new StringBuilder();
                String prefix = element.name.substring(0, element.name.indexOf(':'));
                xml.append('<').append(element.name).append(" xmlns:").append(prefix)
                        .append("=\"").append(element.namespace).append('"');
                for (String[] attribute : element.attributes) {
                    if (!attribute[0].startsWith("xmlns")) {
                        xml.append(' ').append(attribute[0]).append("=\"")
                                .append(escape(attribute[1], true)).append('"');
                    }
                }
                xml.append('>');
                for (Object text : element.children) {
                    xml.append(escape(String.valueOf(text), false));
                }
                xml.append("</").append(element.name).append('>');
                found.add(xml.toString().getBytes(StandardCharsets.UTF_8));
            } else {
                collectExtensions(element, found);
            }
        }
    }
}
