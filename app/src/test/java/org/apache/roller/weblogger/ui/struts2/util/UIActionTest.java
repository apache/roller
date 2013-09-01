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


import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;


public class UIActionTest extends TestCase {

    public UIActionTest() {
    }

    public static Test suite() {
        return new TestSuite(UIActionTest.class);
    }

    public void testCleanTextEmpty() {
        assertEquals(null,UIAction.cleanText(null));
        assertEquals("",UIAction.cleanText(""));
    }

    public void testCleanExpressions() {
        assertEquals(null,UIAction.cleanText(null));
        assertEquals("",UIAction.cleanText(""));
        assertEquals("a",UIAction.cleanText("a"));
        assertEquals("$",UIAction.cleanText("$"));
        assertEquals("%",UIAction.cleanText("%"));
        assertEquals("%$",UIAction.cleanText("%$"));
        assertEquals("{",UIAction.cleanText("{"));
        assertEquals("}",UIAction.cleanText("}"));
        assertEquals("",UIAction.cleanText("${"));
        assertEquals("",UIAction.cleanText("%{"));
        assertEquals("text$",UIAction.cleanText("text$"));
        assertEquals("text%",UIAction.cleanText("text%"));
        assertEquals("text something   more", UIAction.cleanText("text something ${ more } ${ and more } more"));
        assertEquals("text something   more", UIAction.cleanText("text something %{ more } %{ and more } more"));
        assertEquals("text  more", UIAction.cleanText("text %{ something ${ more } ${ and more } } more"));
        assertEquals("text {1} text {2} more", UIAction.cleanText("text {1} text${2} {2} more"));
        assertEquals("text  text {2} more", UIAction.cleanText("text %{1} text${2} {2} more"));
        assertEquals("already { clean }", UIAction.cleanText("already { clean }"));
        assertEquals("$signs but not immediately followed by { braces }", UIAction.cleanText("$signs but not immediately followed by { braces }"));
        assertEquals("%signs but not immediately followed by { braces }", UIAction.cleanText("%signs but not immediately followed by { braces }"));
        assertEquals("clean", UIAction.cleanText("${part %{ } }clean${%anything}"));
        assertEquals("clean", UIAction.cleanText("%{part ${} }clean${%anything}"));
    }

    public void testCleanTextHtml() {
        assertEquals("&lt;i&gt;some text&lt;/i&gt;",UIAction.cleanText("<i>some text</i>"));
        assertEquals("&lt;i&gt;some &lt;/i&gt;",UIAction.cleanText("<i>some ${text}</i>"));   // combined
    }

}
