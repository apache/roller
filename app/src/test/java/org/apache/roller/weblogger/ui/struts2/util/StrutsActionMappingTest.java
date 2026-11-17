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
package org.apache.roller.weblogger.ui.struts2.util;

import java.util.Collections;
import java.util.Set;
import java.util.TreeSet;
import jakarta.servlet.ServletContext;

import org.apache.struts2.config.RuntimeConfiguration;
import org.apache.struts2.dispatcher.Dispatcher;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;

/**
 * Loads the Struts configuration the webapp uses (struts.xml,
 * struts.properties and the plugins on the classpath) and checks that
 * struts.xml is the only source of action mappings.
 */
class StrutsActionMappingTest {

    private Dispatcher dispatcher;

    @BeforeEach
    void setUp() {
        // building action objects through Spring needs a running webapp; the
        // mappings themselves don't depend on which factory creates actions
        dispatcher = new Dispatcher(mock(ServletContext.class),
                Collections.singletonMap("struts.objectFactory", "struts"));
        dispatcher.init();
    }

    @AfterEach
    void tearDown() {
        dispatcher.cleanup();
        Dispatcher.setInstance(null);
    }

    @Test
    void actionsAreMappedOnlyInStrutsXmlNamespaces() {
        RuntimeConfiguration config =
                dispatcher.getConfigurationManager().getConfiguration().getRuntimeConfiguration();

        Set<String> namespaces = new TreeSet<>(config.getActionConfigs().keySet());
        namespaces.remove("");  // struts-default's empty default namespace

        Set<String> expected = new TreeSet<>();
        Collections.addAll(expected,
                "/roller-ui",
                "/roller-ui/admin",
                "/roller-ui/authoring",
                "/roller-ui/authoring/overlay",
                "/roller-ui/install");

        assertEquals(expected, namespaces);
    }
}
