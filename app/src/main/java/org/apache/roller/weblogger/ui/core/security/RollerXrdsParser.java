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
 * limitations under the License.
 */
package org.apache.roller.weblogger.ui.core.security;

import java.io.IOException;
import java.io.StringReader;
import java.util.List;
import java.util.Set;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.roller.weblogger.util.SecureXmlParsers;
import org.openid4java.OpenIDException;
import org.openid4java.discovery.DiscoveryException;
import org.openid4java.discovery.xrds.XrdsParser;
import org.openid4java.discovery.xrds.XrdsParserImpl;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

/**
 * The XRDS parser openid4java uses for OpenID discovery, configured in
 * {@code openid4java.properties}.
 *
 * <p>Each document is first read with {@link SecureXmlParsers}, the parser
 * configuration Roller uses for the XML it reads itself, which refuses
 * document type declarations. XRDS documents don't need one. Documents that
 * pass are then handed to openid4java's own parser.
 */
public class RollerXrdsParser implements XrdsParser {

    private final XrdsParser delegate = new XrdsParserImpl();

    @Override
    public List parseXrds(String input, Set targetTypes) throws DiscoveryException {
        check(input);
        return delegate.parseXrds(input, targetTypes);
    }

    static void check(String input) throws DiscoveryException {
        try {
            SecureXmlParsers.newSAXParserFactory().newSAXParser()
                    .parse(new InputSource(new StringReader(input)), new DefaultHandler());
        } catch (ParserConfigurationException | SAXException | IOException e) {
            throw new DiscoveryException("Could not read the XRDS document",
                    OpenIDException.XRDS_PARSING_ERROR, e);
        }
    }
}
