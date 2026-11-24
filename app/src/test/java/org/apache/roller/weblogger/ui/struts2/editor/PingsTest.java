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

import com.opensymphony.xwork2.ActionContext;
import com.opensymphony.xwork2.LocalizedTextProvider;
import com.opensymphony.xwork2.config.ConfigurationManager;
import com.opensymphony.xwork2.config.providers.XWorkConfigurationProvider;
import com.opensymphony.xwork2.inject.Container;
import com.opensymphony.xwork2.util.ValueStack;
import com.opensymphony.xwork2.util.ValueStackFactory;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class PingsTest {

    // the ping server's reply is shown as text, never looked up as a message key
    @Test
    public void pingServerErrorIsShownAsText() {
        ConfigurationManager configurationManager = new ConfigurationManager("xwork");
        configurationManager.addContainerProvider(new XWorkConfigurationProvider());
        Container container = configurationManager.getConfiguration().getContainer();
        ValueStack stack = container.getInstance(ValueStackFactory.class).createValueStack();
        ActionContext.setContext(new ActionContext(stack.getContext()));
        try {
            container.getInstance(LocalizedTextProvider.class)
                    .addDefaultResourceBundle("ApplicationResources");
            Pings action = new Pings();
            container.inject(action);
            stack.push(action);

            action.addPingServerError("addActionMessage('marker'+7*191).x <b>");

            assertTrue(action.getActionMessages().isEmpty(), "the reply was evaluated");
            List<String> errors = List.copyOf(action.getActionErrors());
            assertEquals(2, errors.size());
            assertTrue(errors.get(0).startsWith("The ping was transmitted"));
            assertEquals("addActionMessage('marker'+7*191).x &lt;b&gt;", errors.get(1));
        } finally {
            ActionContext.setContext(null);
        }
    }
}
