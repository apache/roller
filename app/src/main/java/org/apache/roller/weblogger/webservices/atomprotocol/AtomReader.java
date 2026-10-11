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

import java.io.InputStream;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.util.Date;

import javax.servlet.http.HttpServletResponse;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

/**
 * Parses an incoming AtomPub request body (an atom:entry) into the wire model
 * using the JDK StAX API. Replaces ROME's Atom parser.
 *
 * <p>DTD processing and external entities are disabled, and an entry that
 * declares a DOCTYPE is refused, to protect against XXE attacks. Parse errors
 * are reported as HTTP 400.
 */
public class AtomReader {

    private static final String XHTML_NS = "http://www.w3.org/1999/xhtml";

    private final XMLInputFactory factory;

    public AtomReader() {
        factory = XMLInputFactory.newInstance();
        factory.setProperty(XMLInputFactory.SUPPORT_DTD, Boolean.FALSE);
        factory.setProperty(XMLInputFactory.IS_SUPPORTING_EXTERNAL_ENTITIES, Boolean.FALSE);
    }

    static Date parseDate(String text) {
        if (text == null || text.isBlank()) {
            return null;
        }
        String trimmed = text.trim();
        try {
            return Date.from(OffsetDateTime.parse(trimmed).toInstant());
        } catch (Exception ignored) {
            try {
                return Date.from(Instant.parse(trimmed));
            } catch (Exception ignored2) {
                return null;
            }
        }
    }

    /**
     * Parse an atom:entry from the given stream. Only direct children of the
     * entry are read, so metadata inside nested elements such as atom:source
     * is ignored. The author is intentionally not read; the server sets the
     * entry's creator from the authenticated user.
     */
    public AtomEntry parseEntry(InputStream in) throws AtomException {
        XMLStreamReader r = null;
        try {
            r = factory.createXMLStreamReader(in, "UTF-8");
            AtomEntry entry = new AtomEntry();
            moveToRootElement(r);
            while (r.nextTag() == XMLStreamConstants.START_ELEMENT) {
                readEntryChild(r, entry);
            }
            return entry;
        } catch (XMLStreamException ex) {
            throw new AtomException("Error parsing Atom entry",
                    HttpServletResponse.SC_BAD_REQUEST, ex);
        } finally {
            if (r != null) {
                try {
                    r.close();
                } catch (XMLStreamException ignored) {
                    // nothing useful to do on close failure
                }
            }
        }
    }

    private static void moveToRootElement(XMLStreamReader r)
            throws XMLStreamException, AtomException {
        while (r.hasNext()) {
            int event = r.next();
            if (event == XMLStreamConstants.DTD) {
                throw new AtomException("DOCTYPE is not allowed in an Atom entry",
                        HttpServletResponse.SC_BAD_REQUEST, null);
            }
            if (event == XMLStreamConstants.START_ELEMENT) {
                return;
            }
        }
        throw new XMLStreamException("No root element");
    }

    /**
     * Reads one child of atom:entry. On return the reader is on the child's
     * END_ELEMENT.
     */
    private void readEntryChild(XMLStreamReader r, AtomEntry entry) throws XMLStreamException {
        String ns = r.getNamespaceURI();
        String name = r.getLocalName();
        if (ATOM_NS.equals(ns)) {
            switch (name) {
                case "id":
                    entry.setId(r.getElementText());
                    return;
                case "title":
                    entry.setTitle(readContent(r).getValue());
                    return;
                case "summary":
                    entry.setSummary(readContent(r));
                    return;
                case "content":
                    AtomContent content = readContent(r);
                    if (entry.getContent() == null) {
                        entry.setContent(content);
                    }
                    return;
                case "published":
                    entry.setPublished(parseDate(r.getElementText()));
                    return;
                case "updated":
                    entry.setUpdated(parseDate(r.getElementText()));
                    return;
                case "category":
                    entry.getCategories().add(readCategory(r));
                    skipElement(r);
                    return;
                default:
                    break;
            }
        } else if (APP_NS.equals(ns) && "control".equals(name)) {
            while (r.nextTag() == XMLStreamConstants.START_ELEMENT) {
                if (APP_NS.equals(r.getNamespaceURI()) && "draft".equals(r.getLocalName())) {
                    String value = r.getElementText();
                    entry.setDraft(value != null && value.trim().equalsIgnoreCase("yes"));
                } else {
                    skipElement(r);
                }
            }
            return;
        } else if (ROLLER_NS.equals(ns)) {
            if ("rendition".equals(name)) {
                String type = r.getAttributeValue(null, "type");
                String value = r.getElementText();
                if ("mobile".equals(type)) {
                    entry.setMobileRendition(value);
                }
            } else {
                // Roller extension values are text; markup must be escaped
                entry.setExtension(name, r.getElementText());
            }
            return;
        } else if (THREAD_NS.equals(ns) && "in-reply-to".equals(name)) {
            entry.setInReplyToRef(r.getAttributeValue(null, "ref"));
            entry.setInReplyToHref(r.getAttributeValue(null, "href"));
            skipElement(r);
            return;
        }
        skipElement(r);
    }

    /** Moves the reader from a START_ELEMENT to its matching END_ELEMENT. */
    private static void skipElement(XMLStreamReader r) throws XMLStreamException {
        int depth = 1;
        while (depth > 0) {
            int event = r.next();
            if (event == XMLStreamConstants.START_ELEMENT) {
                depth++;
            } else if (event == XMLStreamConstants.END_ELEMENT) {
                depth--;
            }
        }
    }

    /**
     * Reads an Atom text construct or atom:content. For type="xhtml" the value
     * is the markup inside the wrapping xhtml:div (RFC 4287 3.1.1.3).
     */
    private AtomContent readContent(XMLStreamReader r) throws XMLStreamException {
        AtomContent content = new AtomContent();
        String type = r.getAttributeValue(null, "type");
        content.setType(type);
        String src = r.getAttributeValue(null, "src");
        content.setSrc(src);
        if (src != null) {
            skipElement(r);
        } else if ("xhtml".equals(type)) {
            content.setValue(readXhtml(r));
        } else {
            content.setValue(r.getElementText());
        }
        return content;
    }

    /**
     * Serializes the children of the current element as markup without
     * namespace declarations. A single wrapping xhtml:div is left out. Empty
     * elements are written as {@code <br/>}, never {@code <br></br>}.
     */
    private String readXhtml(XMLStreamReader r) throws XMLStreamException {
        StringBuilder out = new StringBuilder();
        int depth = 1;
        boolean skipDiv = false;
        boolean openTag = false;
        while (depth > 0) {
            int event = r.next();
            if (event == XMLStreamConstants.START_ELEMENT) {
                depth++;
                if (openTag) {
                    out.append('>');
                    openTag = false;
                }
                if (depth == 2 && XHTML_NS.equals(r.getNamespaceURI())
                        && "div".equals(r.getLocalName()) && out.toString().isBlank()) {
                    skipDiv = true;
                    continue;
                }
                out.append('<').append(r.getLocalName());
                for (int i = 0; i < r.getAttributeCount(); i++) {
                    out.append(' ').append(r.getAttributeLocalName(i)).append("=\"")
                            .append(escape(r.getAttributeValue(i), true)).append('"');
                }
                openTag = true;
            } else if (event == XMLStreamConstants.END_ELEMENT) {
                depth--;
                if (depth == 0 || (depth == 1 && skipDiv)) {
                    continue;
                }
                if (openTag) {
                    out.append("/>");
                    openTag = false;
                } else {
                    out.append("</").append(r.getLocalName()).append('>');
                }
            } else if (event == XMLStreamConstants.CHARACTERS
                    || event == XMLStreamConstants.CDATA
                    || event == XMLStreamConstants.SPACE) {
                if (depth > 1 || !skipDiv) {
                    if (openTag) {
                        out.append('>');
                        openTag = false;
                    }
                    out.append(escape(r.getText(), false));
                }
            }
        }
        return out.toString().trim();
    }

    private static String escape(String text, boolean attribute) {
        StringBuilder sb = new StringBuilder(text.length());
        for (char c : text.toCharArray()) {
            switch (c) {
                case '&':
                    sb.append("&amp;");
                    break;
                case '<':
                    sb.append("&lt;");
                    break;
                case '>':
                    sb.append("&gt;");
                    break;
                case '"':
                    sb.append(attribute ? "&quot;" : "\"");
                    break;
                default:
                    sb.append(c);
            }
        }
        return sb.toString();
    }

    private AtomCategory readCategory(XMLStreamReader r) {
        AtomCategory cat = new AtomCategory();
        cat.setTerm(r.getAttributeValue(null, "term"));
        cat.setScheme(r.getAttributeValue(null, "scheme"));
        cat.setLabel(r.getAttributeValue(null, "label"));
        return cat;
    }
}
