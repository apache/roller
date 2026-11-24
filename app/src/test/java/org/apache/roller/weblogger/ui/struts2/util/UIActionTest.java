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

import java.util.Collections;

import com.opensymphony.xwork2.ActionContext;
import com.opensymphony.xwork2.LocalizedTextProvider;
import com.opensymphony.xwork2.config.ConfigurationManager;
import com.opensymphony.xwork2.config.providers.XWorkConfigurationProvider;
import com.opensymphony.xwork2.inject.Container;
import com.opensymphony.xwork2.util.ValueStack;
import com.opensymphony.xwork2.util.ValueStackFactory;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;


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

    // the <s:text> tag formats its <s:param> values through these overloads
    @Test
    public void testGetTextWithValueStackEscapesStringArgs() {
        ConfigurationManager configurationManager = new ConfigurationManager("xwork");
        configurationManager.addContainerProvider(new XWorkConfigurationProvider());
        Container container = configurationManager.getConfiguration().getContainer();
        ValueStack stack = container.getInstance(ValueStackFactory.class).createValueStack();
        ActionContext.setContext(new ActionContext(stack.getContext()));
        try {
            container.getInstance(LocalizedTextProvider.class)
                    .addDefaultResourceBundle("ApplicationResources");
            UIAction action = new UIAction() { };
            container.inject(action);

            // mediaFileView.filesNamed=Files named like <b>{0}</b>
            assertEquals("Files named like <b>&lt;i&gt;x&lt;/i&gt; &amp; 'y'</b>",
                    action.getText("mediaFileView.filesNamed", "mediaFileView.filesNamed",
                            Collections.singletonList("<i>x</i> & 'y'"), stack));
            assertEquals("Files named like <b>&lt;i&gt;x&lt;/i&gt;</b>",
                    action.getText("mediaFileView.filesNamed", "mediaFileView.filesNamed",
                            new String[] {"<i>x</i>"}, stack));
            // numbers and other non-string values are passed through
            assertEquals("Found <b>3</b> matching results.",
                    action.getText("mediaFileView.matchingResults", "mediaFileView.matchingResults",
                            Collections.singletonList(3), stack));
        } finally {
            ActionContext.setContext(null);
        }
    }

    // the page title comes from struts.xml or the action itself, never from
    // the request, because it is looked up as a message key
    @Test
    public void testPageTitleIsNotARequestParameter() {
        UIAction action = new org.apache.roller.weblogger.ui.struts2.core.Install();
        for (String name : new String[] {"pageTitle", "PageTitle", "top.pageTitle",
                "[0].pageTitle", "pageTitle[0]"}) {
            assertFalse(action.acceptableParameterName(name), name);
        }
        assertTrue(action.acceptableParameterName("bean.title"));
        assertTrue(action.acceptableParameterName("weblog"));
    }

    @Test
    public void testPageTitleOnlyLooksUpPlainKeys() {
        assertTrue(UIAction.isPageTitleKey("weblogEdit.title.newEntry"));
        assertTrue(UIAction.isPageTitleKey("installer.error.connection.pageTitle"));
        assertFalse(UIAction.isPageTitleKey(null));
        assertFalse(UIAction.isPageTitleKey("addActionMessage('x').x"));
        assertFalse(UIAction.isPageTitleKey("a b.c"));
        assertFalse(UIAction.isPageTitleKey("#a.b"));
    }

}
