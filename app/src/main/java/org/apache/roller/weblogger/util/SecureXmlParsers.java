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

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParserFactory;

import org.apache.commons.xml.secure.SecureSAXParserFactory;
import org.apache.xmlrpc.util.SAXParsers;
import org.jdom2.JDOMException;
import org.jdom2.input.sax.XMLReaderJDOMFactory;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;

/**
 * The XML parser configuration Roller uses for every document it parses itself.
 *
 * <p>Parsers come from Apache Commons Secure XML, which configures the JAXP
 * implementation so that nothing a document names is resolved, and throws
 * rather than continuing if a setting cannot be applied. On top of that,
 * Roller refuses any document type declaration: none of the documents Roller
 * reads needs one.
 */
public final class SecureXmlParsers {

    /** Xerces feature name, honoured by the JDK's own parser. */
    static final String DISALLOW_DOCTYPE =
            "http://apache.org/xml/features/disallow-doctype-decl";

    /** Supplies JDOM with readers from {@link #newSAXParserFactory()}. */
    public static final XMLReaderJDOMFactory JDOM_READERS = new XMLReaderJDOMFactory() {
        @Override
        public XMLReader createXMLReader() throws JDOMException {
            try {
                return newSAXParserFactory().newSAXParser().getXMLReader();
            } catch (ParserConfigurationException | SAXException e) {
                throw new JDOMException("Could not create an XML reader", e);
            }
        }

        @Override
        public boolean isValidating() {
            return false;
        }
    };

    private SecureXmlParsers() {
    }

    /**
     * Returns a namespace-aware, non-validating SAX parser factory that
     * resolves nothing a document names and refuses document type
     * declarations.
     *
     * @throws IllegalStateException if the configuration cannot be applied
     */
    public static SAXParserFactory newSAXParserFactory() {
        SAXParserFactory factory = SecureSAXParserFactory.newNSInstance();
        factory.setValidating(false);
        try {
            factory.setFeature(DISALLOW_DOCTYPE, true);
        } catch (ParserConfigurationException | SAXException e) {
            throw new IllegalStateException(
                    "The XML parser does not support refusing document type declarations", e);
        }
        return factory;
    }

    /**
     * Makes the XML-RPC library parse requests with {@link #newSAXParserFactory()}.
     *
     * @throws IllegalStateException if the configuration cannot be applied
     */
    public static void installForXmlRpc() {
        SAXParsers.setSAXParserFactory(newSAXParserFactory());
    }
}
