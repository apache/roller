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

import java.io.InputStream;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.util.Date;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

/**
 * Parses an incoming AtomPub request body (an atom:entry) into the wire model
 * using the JDK StAX API. Replaces ROME's Atom parser.
 *
 * <p>DTD processing and external entities are disabled to protect against XXE
 * attacks.
 */
public class AtomReader {

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
     * Parse an atom:entry from the given stream. The author is intentionally not
     * read; the server sets the entry's creator from the authenticated user.
     */
    public AtomEntry parseEntry(InputStream in) throws AtomException {
        XMLStreamReader r = null;
        try {
            r = factory.createXMLStreamReader(in, "UTF-8");
            AtomEntry entry = new AtomEntry();
            while (r.hasNext()) {
                if (r.next() != XMLStreamConstants.START_ELEMENT) {
                    continue;
                }
                String ns = r.getNamespaceURI();
                String name = r.getLocalName();
                if (ATOM_NS.equals(ns)) {
                    switch (name) {
                        case "id":
                            entry.setId(r.getElementText());
                            break;
                        case "title":
                            entry.setTitle(r.getElementText());
                            break;
                        case "summary":
                            entry.setSummary(readContent(r));
                            break;
                        case "content":
                            if (entry.getContent() == null) {
                                entry.setContent(readContent(r));
                            }
                            break;
                        case "published":
                            entry.setPublished(parseDate(r.getElementText()));
                            break;
                        case "updated":
                            entry.setUpdated(parseDate(r.getElementText()));
                            break;
                        case "category":
                            entry.getCategories().add(readCategory(r));
                            break;
                        default:
                            break;
                    }
                } else if (APP_NS.equals(ns) && "draft".equals(name)) {
                    String value = r.getElementText();
                    entry.setDraft(value != null && value.trim().equalsIgnoreCase("yes"));
                }
            }
            return entry;
        } catch (XMLStreamException ex) {
            throw new AtomException("Error parsing Atom entry", ex);
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

    private AtomContent readContent(XMLStreamReader r) throws XMLStreamException {
        AtomContent content = new AtomContent();
        content.setType(r.getAttributeValue(null, "type"));
        String src = r.getAttributeValue(null, "src");
        content.setSrc(src);
        if (src == null) {
            content.setValue(r.getElementText());
        }
        return content;
    }

    private AtomCategory readCategory(XMLStreamReader r) {
        AtomCategory cat = new AtomCategory();
        cat.setTerm(r.getAttributeValue(null, "term"));
        cat.setScheme(r.getAttributeValue(null, "scheme"));
        cat.setLabel(r.getAttributeValue(null, "label"));
        return cat;
    }
}
