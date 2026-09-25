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
package org.apache.roller.weblogger.ui.struts2.editor;

import java.io.InputStream;
import java.util.List;
import java.util.Set;
import javax.xml.parsers.DocumentBuilderFactory;

import org.junit.jupiter.api.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

class EntryTrackbackRemovalTest {

    @Test
    void entryActionsDoNotAllowTrackbackMethod() throws Exception {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
        factory.setFeature("http://xml.org/sax/features/external-general-entities", false);
        factory.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
        factory.setXIncludeAware(false);
        factory.setExpandEntityReferences(false);

        try (InputStream input = getClass().getClassLoader().getResourceAsStream("struts.xml")) {
            assertNotNull(input);
            Document config = factory.newDocumentBuilder().parse(input);
            NodeList actions = config.getElementsByTagName("action");
            Set<String> expectedActions = Set.of("entryAdd", "entryEdit");
            int actionsChecked = 0;

            for (int i = 0; i < actions.getLength(); i++) {
                Element action = (Element) actions.item(i);
                if (expectedActions.contains(action.getAttribute("name"))) {
                    NodeList allowedMethods = action.getElementsByTagName("allowed-methods");
                    assertEquals(1, allowedMethods.getLength());
                    String methods = allowedMethods.item(0).getTextContent();
                    assertFalse(List.of(methods.trim().split("\\s*,\\s*")).contains("trackback"));
                    actionsChecked++;
                }
            }

            assertEquals(expectedActions.size(), actionsChecked);
        }
    }

    @Test
    void outboundTrackbackImplementationIsRemoved() {
        assertThrows(NoSuchMethodException.class,
                () -> EntryEdit.class.getMethod("trackback"));
        assertThrows(ClassNotFoundException.class,
                () -> Class.forName("org.apache.roller.weblogger.util.Trackback"));
        assertThrows(ClassNotFoundException.class,
                () -> Class.forName(
                        "org.apache.roller.weblogger.util.TrackbackNotAllowedException"));
    }
}
