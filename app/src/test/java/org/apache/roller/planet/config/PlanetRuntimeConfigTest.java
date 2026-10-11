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
package org.apache.roller.planet.config;

import org.apache.roller.weblogger.business.PropertiesManager;
import org.apache.roller.weblogger.business.Weblogger;
import org.apache.roller.weblogger.business.WebloggerFactory;
import org.apache.roller.weblogger.pojos.RuntimeConfigProperty;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

class PlanetRuntimeConfigTest {

    private MockedStatic<WebloggerFactory> factory;
    private PropertiesManager properties;

    @BeforeEach
    void setUp() {
        factory = mockStatic(WebloggerFactory.class);
        properties = mock(PropertiesManager.class);
        Weblogger weblogger = mock(Weblogger.class);
        when(weblogger.getPropertiesManager()).thenReturn(properties);
        factory.when(WebloggerFactory::getWeblogger).thenReturn(weblogger);
    }

    @AfterEach
    void tearDown() {
        factory.close();
    }

    @Test
    void savedPropertyIsReturned() throws Exception {
        when(properties.getProperty("planet.site.name"))
                .thenReturn(new RuntimeConfigProperty("planet.site.name", "My Planet"));

        assertEquals("My Planet", PlanetRuntimeConfig.getProperty("planet.site.name"));
    }

    @Test
    void unsavedPropertyFallsBackToItsDefault() throws Exception {
        when(properties.getProperty("planet.site.name")).thenReturn(null);

        assertEquals("Roller Planet", PlanetRuntimeConfig.getProperty("planet.site.name"));
    }

    @Test
    void unknownPropertyIsNull() throws Exception {
        when(properties.getProperty("planet.no.such.property")).thenReturn(null);

        assertNull(PlanetRuntimeConfig.getProperty("planet.no.such.property"));
    }
}
