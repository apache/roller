/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.apache.roller.weblogger.ui.struts2.util;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;


public class UIActionTest  {

    @Test
    public void testCleanTextEmpty() {
        assertEquals(null,UIAction.cleanTextKey(null));
        assertEquals("",UIAction.cleanTextKey(""));
        assertEquals(null,UIAction.cleanTextArg(null));
        assertEquals("",UIAction.cleanTextArg(""));
    }

    @Test
    public void testCleanTextKey() {
        assertEquals(null,UIAction.cleanTextKey(null));
        assertEquals("",UIAction.cleanTextKey(""));
        assertEquals("a",UIAction.cleanTextKey("a"));
        assertEquals("$",UIAction.cleanTextKey("$"));
        assertEquals("%",UIAction.cleanTextKey("%"));
        assertEquals("%$",UIAction.cleanTextKey("%$"));
        assertEquals("{",UIAction.cleanTextKey("{"));
        assertEquals("}",UIAction.cleanTextKey("}"));
        assertEquals("",UIAction.cleanTextKey("${"));
        assertEquals("",UIAction.cleanTextKey("%{"));
        assertEquals("text$",UIAction.cleanTextKey("text$"));
        assertEquals("text%",UIAction.cleanTextKey("text%"));
        assertEquals("", UIAction.cleanTextKey("something ${foo} more"));
        assertEquals("", UIAction.cleanTextKey("something %{foo} more"));
        assertEquals("", UIAction.cleanTextKey("something %{foo} more"));
    }

    @Test
    public void testCleanTextArg() {
        assertEquals("&lt;i&gt;some text&lt;/i&gt;",UIAction.cleanTextArg("<i>some text</i>"));
        assertEquals("&lt;i&gt;some ${text}&lt;/i&gt;",UIAction.cleanTextArg("<i>some ${text}</i>"));
    }

}
