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

import javax.xml.XMLConstants;

import org.jdom2.input.SAXBuilder;
import org.jdom2.input.sax.XMLReaders;

/**
 * A {@link SAXBuilder} that treats a document strictly as data.
 *
 * <p>An XML document can name resources for the parser to go and read: a
 * document type declaration can point at an external subset, and entity
 * declarations can point at files or URLs. Resolving those makes the parser act
 * on behalf of whoever wrote the document, which is only appropriate when the
 * document is Roller's own.
 *
 * <p>Roller parses documents from user input and from its own menu, theme and
 * configuration descriptors alike. Rather than track which parser is on which
 * side, every retained JDOM parser is built here, and none of them resolve
 * anything. Roller's own descriptors carry no document type declaration, so the
 * strict setting costs them nothing.
 *
 * <p>The settings overlap deliberately. Refusing the declaration outright is
 * what does the work; the remaining ones close the same door at the layers
 * beneath, so a parser configured elsewhere, or a JAXP implementation with
 * different defaults, does not quietly reopen it.
 */
public class SafeSAXBuilder extends SAXBuilder {

    /** Xerces feature names, honoured by the JDK's own parser. */
    private static final String DISALLOW_DOCTYPE =
            "http://apache.org/xml/features/disallow-doctype-decl";
    private static final String EXTERNAL_PARAMETER_ENTITIES =
            "http://xml.org/sax/features/external-parameter-entities";
    private static final String LOAD_EXTERNAL_DTD =
            "http://apache.org/xml/features/nonvalidating/load-external-dtd";

    public SafeSAXBuilder() {
        super(XMLReaders.NONVALIDATING);

        // Secure processing is set explicitly rather than relied on. It is on
        // by default in current JDKs, but that default limits resource
        // consumption; it does not by itself stop external resolution.
        setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);

        // A document that declares a doctype is refused. Everything an entity
        // could name has to be declared first, so this is the setting the rest
        // stand behind.
        setFeature(DISALLOW_DOCTYPE, true);

        setFeature(EXTERNAL_PARAMETER_ENTITIES, false);
        setFeature(LOAD_EXTERNAL_DTD, false);

        // JDOM maps this setting to the external-general-entities parser
        // feature, so it supplies that part of the policy without duplicate
        // configuration.
        setExpandEntities(false);
    }
}
