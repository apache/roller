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
import static org.apache.roller.weblogger.webservices.atomprotocol.AtomConstants.ROLLER_NS;
import static org.apache.roller.weblogger.webservices.atomprotocol.AtomConstants.THREAD_NS;

import java.io.OutputStream;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.Map;

import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

/**
 * Serializes the AtomPub wire model ({@link AtomEntry}, {@link AtomFeed},
 * {@link AtomServiceDoc}) to XML using the JDK StAX API. Replaces the ROME and
 * Propono serialization the AtomPub server previously relied on.
 *
 * <p>Atom entries and feeds are written with the Atom namespace as the default
 * namespace (so atom elements are unprefixed) and the APP namespace bound to the
 * {@code app} prefix. Service documents use the reverse: APP as the default
 * namespace and {@code atom} for atom elements.
 */
public class AtomWriter {

    private static final DateTimeFormatter RFC3339 =
            DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss'Z'").withZone(ZoneOffset.UTC);

    private final XMLOutputFactory factory = XMLOutputFactory.newInstance();

    static String formatDate(Date date) {
        return date == null ? null : RFC3339.format(date.toInstant());
    }

    public void writeEntry(OutputStream out, AtomEntry entry) throws AtomException {
        writeDocument(out, ATOM_NS, "entry", "app", APP_NS, "Error serializing Atom entry",
                w -> writeEntryBody(w, entry));
    }

    /** Writes an APP category document (RFC 5023 section 7). */
    public void writeCategoriesDoc(OutputStream out, AtomCategories cats) throws AtomException {
        writeDocument(out, APP_NS, "categories", "atom", ATOM_NS,
                "Error serializing category document", w -> {
            w.writeAttribute("fixed", cats.isFixed() ? "yes" : "no");
            if (cats.getScheme() != null) {
                w.writeAttribute("scheme", cats.getScheme());
            }
            for (AtomCategory cat : cats.getCategories()) {
                writeCategory(w, cat);
            }
        });
    }

    public void writeFeed(OutputStream out, AtomFeed feed) throws AtomException {
        writeDocument(out, ATOM_NS, "feed", "app", APP_NS, "Error serializing Atom feed", w -> {
            writeAtomText(w, "id", feed.getId());
            writeAtomText(w, "title", feed.getTitle());
            writeAtomText(w, "updated", formatDate(feed.getUpdated()));
            for (AtomLink link : feed.getLinks()) {
                writeLink(w, link);
            }
            for (AtomEntry entry : feed.getEntries()) {
                w.writeStartElement(ATOM_NS, "entry");
                writeEntryBody(w, entry);
                w.writeEndElement();
            }
        });
    }

    public void writeServiceDoc(OutputStream out, AtomServiceDoc service) throws AtomException {
        writeDocument(out, APP_NS, "service", "atom", ATOM_NS, "Error serializing service document", w -> {
            for (AtomWorkspace workspace : service.getWorkspaces()) {
                w.writeStartElement(APP_NS, "workspace");
                writeAtomText(w, "title", workspace.getTitle());
                for (AtomCollection collection : workspace.getCollections()) {
                    writeCollection(w, collection);
                }
                w.writeEndElement();
            }
        });
    }

    /** Writes the body of a document element. */
    @FunctionalInterface
    private interface ElementBody {
        void write(XMLStreamWriter w) throws XMLStreamException;
    }

    /**
     * Writes a UTF-8 document whose root element is in the default namespace
     * {@code rootNs}, with {@code otherNs} bound to {@code prefix}.
     */
    private void writeDocument(OutputStream out, String rootNs, String root,
            String prefix, String otherNs, String errorMessage, ElementBody body)
            throws AtomException {
        XMLStreamWriter w = null;
        try {
            w = factory.createXMLStreamWriter(out, "UTF-8");
            w.writeStartDocument("UTF-8", "1.0");
            w.setDefaultNamespace(rootNs);
            w.setPrefix(prefix, otherNs);
            w.writeStartElement(rootNs, root);
            w.writeDefaultNamespace(rootNs);
            w.writeNamespace(prefix, otherNs);
            if (ATOM_NS.equals(rootNs)) {
                // Extension namespaces used by entries (see writeEntryBody)
                w.setPrefix("roller", ROLLER_NS);
                w.setPrefix("thr", THREAD_NS);
                w.writeNamespace("roller", ROLLER_NS);
                w.writeNamespace("thr", THREAD_NS);
            }
            body.write(w);
            w.writeEndElement();
            w.writeEndDocument();
            w.flush();
        } catch (XMLStreamException ex) {
            throw new AtomException(errorMessage, ex);
        } finally {
            if (w != null) {
                try {
                    w.close();
                } catch (XMLStreamException ignored) {
                    // nothing useful to do on close failure
                }
            }
        }
    }

    private void writeCollection(XMLStreamWriter w, AtomCollection collection)
            throws XMLStreamException {
        w.writeStartElement(APP_NS, "collection");
        if (collection.getHref() != null) {
            w.writeAttribute("href", collection.getHref());
        }
        writeAtomText(w, "title", collection.getTitle());
        for (String accept : collection.getAccepts()) {
            w.writeStartElement(APP_NS, "accept");
            w.writeCharacters(accept);
            w.writeEndElement();
        }
        for (AtomCategories cats : collection.getCategories()) {
            if (cats.getHref() != null) {
                w.writeEmptyElement(APP_NS, "categories");
                w.writeAttribute("href", cats.getHref());
                continue;
            }
            w.writeStartElement(APP_NS, "categories");
            w.writeAttribute("fixed", cats.isFixed() ? "yes" : "no");
            if (cats.getScheme() != null) {
                w.writeAttribute("scheme", cats.getScheme());
            }
            for (AtomCategory cat : cats.getCategories()) {
                writeCategory(w, cat);
            }
            w.writeEndElement();
        }
        w.writeEndElement();
    }

    private void writeEntryBody(XMLStreamWriter w, AtomEntry entry) throws XMLStreamException {
        writeAtomText(w, "id", entry.getId());
        writeAtomText(w, "title", entry.getTitle());
        writeAtomText(w, "published", formatDate(entry.getPublished()));
        writeAtomText(w, "updated", formatDate(entry.getUpdated()));
        for (AtomPerson author : entry.getAuthors()) {
            w.writeStartElement(ATOM_NS, "author");
            writeAtomText(w, "name", author.getName());
            writeAtomText(w, "uri", author.getUri());
            writeAtomText(w, "email", author.getEmail());
            w.writeEndElement();
        }
        for (AtomCategory cat : entry.getCategories()) {
            writeCategory(w, cat);
        }
        if (entry.getSummary() != null) {
            writeContent(w, "summary", entry.getSummary());
        }
        if (entry.getContent() != null) {
            writeContent(w, "content", entry.getContent());
        }
        for (AtomLink link : entry.getLinks()) {
            writeLink(w, link);
        }
        if (entry.getInReplyToRef() != null) {
            w.writeEmptyElement(THREAD_NS, "in-reply-to");
            w.writeAttribute("ref", entry.getInReplyToRef());
            if (entry.getInReplyToHref() != null) {
                w.writeAttribute("href", entry.getInReplyToHref());
            }
        }
        if (entry.getMobileRendition() != null) {
            w.writeStartElement(ROLLER_NS, "rendition");
            w.writeAttribute("type", "mobile");
            w.writeCharacters(entry.getMobileRendition());
            w.writeEndElement();
        }
        for (Map.Entry<String, String> ext : entry.getExtensions().entrySet()) {
            w.writeStartElement(ROLLER_NS, ext.getKey());
            w.writeCharacters(ext.getValue());
            w.writeEndElement();
        }
        // APP extensions (RFC 5023): app:edited is a child of atom:entry
        // (section 10.2); app:draft goes inside app:control (section 13.1)
        if (entry.getEdited() != null) {
            w.writeStartElement(APP_NS, "edited");
            w.writeCharacters(formatDate(entry.getEdited()));
            w.writeEndElement();
        }
        w.writeStartElement(APP_NS, "control");
        w.writeStartElement(APP_NS, "draft");
        w.writeCharacters(entry.isDraft() ? "yes" : "no");
        w.writeEndElement();
        w.writeEndElement();
    }

    private void writeContent(XMLStreamWriter w, String name, AtomContent content)
            throws XMLStreamException {
        w.writeStartElement(ATOM_NS, name);
        if (content.getType() != null) {
            w.writeAttribute("type", content.getType());
        }
        if (content.getSrc() != null) {
            w.writeAttribute("src", content.getSrc());
        } else if (content.getValue() != null) {
            w.writeCharacters(content.getValue());
        }
        w.writeEndElement();
    }

    private void writeLink(XMLStreamWriter w, AtomLink link) throws XMLStreamException {
        w.writeStartElement(ATOM_NS, "link");
        if (link.getRel() != null) {
            w.writeAttribute("rel", link.getRel());
        }
        if (link.getHref() != null) {
            w.writeAttribute("href", link.getHref());
        }
        if (link.getType() != null) {
            w.writeAttribute("type", link.getType());
        }
        w.writeEndElement();
    }

    private void writeCategory(XMLStreamWriter w, AtomCategory cat) throws XMLStreamException {
        w.writeStartElement(ATOM_NS, "category");
        if (cat.getTerm() != null) {
            w.writeAttribute("term", cat.getTerm());
        }
        if (cat.getScheme() != null) {
            w.writeAttribute("scheme", cat.getScheme());
        }
        if (cat.getLabel() != null) {
            w.writeAttribute("label", cat.getLabel());
        }
        w.writeEndElement();
    }

    private void writeAtomText(XMLStreamWriter w, String name, String text)
            throws XMLStreamException {
        if (text == null) {
            return;
        }
        w.writeStartElement(ATOM_NS, name);
        w.writeCharacters(text);
        w.writeEndElement();
    }
}
