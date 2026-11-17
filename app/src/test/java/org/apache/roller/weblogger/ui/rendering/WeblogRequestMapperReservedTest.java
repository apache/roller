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

package org.apache.roller.weblogger.ui.rendering;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class WeblogRequestMapperReservedTest {

    @Test
    void pathsRollerServesItselfAreReserved() {
        // the paths Spring Security serves or leaves out of security.access rules
        for (String name : new String[] {"roller-ui", "images", "theme", "webjars", "oauth2"}) {
            assertTrue(WeblogRequestMapper.isReservedHandle(name), name);
        }
        assertTrue(WeblogRequestMapper.isReservedHandle("WebJars"), "regardless of case");
        assertFalse(WeblogRequestMapper.isReservedHandle("family"));
        assertFalse(WeblogRequestMapper.isReservedHandle(null));
    }
}
