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

package org.apache.roller.weblogger.ui.struts2.util;

import java.io.InputStream;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import com.opensymphony.xwork2.config.ConfigurationManager;
import com.opensymphony.xwork2.config.providers.XWorkConfigurationProvider;
import com.opensymphony.xwork2.interceptor.ParametersInterceptor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * The params interceptor in rollerStack takes the parameter names that the
 * UI forms post, and no others.
 */
class UIActionParameterNamesTest {

    /** Exposes the name check that the interceptor applies to each parameter. */
    static class CheckedParameters extends ParametersInterceptor {
        boolean accepts(String name) {
            return acceptableName(name);
        }
    }

    private CheckedParameters params;

    @BeforeEach
    void setUp() throws Exception {
        ConfigurationManager configurationManager = new ConfigurationManager("xwork");
        configurationManager.addContainerProvider(new XWorkConfigurationProvider());
        params = new CheckedParameters();
        configurationManager.getConfiguration().getContainer().inject(params);
        params.setAcceptParamNames(rollerStackParam("acceptParamNames"));
        params.setExcludeParams(rollerStackParam("excludeParams"));
    }

    /** Read a param of the params interceptor in rollerStack from struts.xml. */
    private static String rollerStackParam(String name) throws Exception {
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        dbf.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
        DocumentBuilder builder = dbf.newDocumentBuilder();
        Document doc;
        try (InputStream in = UIActionParameterNamesTest.class.getResourceAsStream("/struts.xml")) {
            assertNotNull(in, "struts.xml on the classpath");
            doc = builder.parse(in);
        }
        NodeList stacks = doc.getElementsByTagName("interceptor-stack");
        for (int i = 0; i < stacks.getLength(); i++) {
            Element stack = (Element) stacks.item(i);
            if (!"rollerStack".equals(stack.getAttribute("name"))) {
                continue;
            }
            NodeList refs = stack.getElementsByTagName("interceptor-ref");
            for (int j = 0; j < refs.getLength(); j++) {
                Element ref = (Element) refs.item(j);
                if (!"params".equals(ref.getAttribute("name"))) {
                    continue;
                }
                NodeList values = ref.getElementsByTagName("param");
                for (int k = 0; k < values.getLength(); k++) {
                    Element value = (Element) values.item(k);
                    if (name.equals(value.getAttribute("name"))) {
                        return value.getTextContent().trim();
                    }
                }
            }
        }
        throw new AssertionError("rollerStack params has no " + name);
    }

    @Test
    void acceptsNamesTheFormsPost() {
        for (String name : new String[] {
            "weblog", "salt", "entryId", "bean.passwordText", "bean.emailAddress",
            "bean.id", "group.handle", "group.title", "folder.name",
            "uploadedFilesFileName", "opmlFileContentType"
        }) {
            assertTrue(params.accepts(name), name);
        }
    }

    @Test
    void rejectsOtherNames() {
        for (String name : new String[] {
            "bean.category.name", "bean['id']", "bean[0]", "bean.ids[0]",
            "bean.list[0].name", "bean['tags'].size", "folder.parent.name",
            "entry.title", "pingTarget.pingUrl", "group.planet.title",
            "method:save", "action:foo"
        }) {
            assertFalse(params.accepts(name), name);
        }
    }
}
