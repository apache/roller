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
package org.apache.roller.weblogger.ui.struts2.core;

import org.apache.roller.weblogger.TestUtils;
import org.apache.roller.weblogger.business.PropertiesManager;
import org.apache.roller.weblogger.business.WebloggerFactory;
import org.apache.roller.weblogger.pojos.RuntimeConfigProperty;
import org.apache.roller.weblogger.pojos.User;
import org.apache.roller.weblogger.pojos.Weblog;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.spy;

/**
 * Saving the create-weblog form follows the site setting for user weblog
 * creation, as opening the form does.
 */
class CreateWeblogSettingTest {

    private static final String SETTING = "site.allowUserWeblogCreation";
    private static final String HANDLE = "createsettingtest";

    private String userName;
    private String savedValue;

    @BeforeEach
    void setUp() throws Exception {
        TestUtils.setupWeblogger();
        userName = TestUtils.setupUser("createsettinguser").getUserName();
        savedValue = setting().getValue();
        TestUtils.endSession(true);
    }

    @AfterEach
    void tearDown() throws Exception {
        setSetting(savedValue);
        Weblog weblog = WebloggerFactory.getWeblogger().getWeblogManager().getWeblogByHandle(HANDLE);
        if (weblog != null) {
            TestUtils.teardownWeblog(weblog.getId());
        }
        TestUtils.teardownUser(userName);
        TestUtils.endSession(true);
    }

    @Test
    void saveCreatesNoWeblogWhenTheSiteSettingIsOff() throws Exception {
        setSetting("false");

        CreateWeblog action = action();
        assertEquals("disabled", action.save());
        TestUtils.endSession(true);

        assertNull(WebloggerFactory.getWeblogger().getWeblogManager().getWeblogByHandle(HANDLE));
    }

    @Test
    void saveCreatesTheWeblogWhenTheSiteSettingIsOn() throws Exception {
        setSetting("true");

        CreateWeblog action = action();
        assertEquals("success", action.save());
        TestUtils.endSession(true);

        assertNotNull(WebloggerFactory.getWeblogger().getWeblogManager().getWeblogByHandle(HANDLE));
    }

    private CreateWeblog action() throws Exception {
        CreateWeblog action = spy(new CreateWeblog());
        doAnswer(call -> call.getArgument(0)).when(action).getText(anyString());
        doAnswer(call -> call.getArgument(0)).when(action).getText(anyString(), anyString());
        doAnswer(call -> call.getArgument(0)).when(action).getText(anyString(), anyList());
        doAnswer(call -> call.getArgument(0)).when(action)
                .getText(anyString(), anyString(), anyString());
        User user = WebloggerFactory.getWeblogger().getUserManager().getUserByUserName(userName);
        action.setAuthenticatedUser(user);
        CreateWeblogBean bean = action.getBean();
        bean.setHandle(HANDLE);
        bean.setName("Create setting test");
        bean.setEmailAddress("createsetting@example.com");
        bean.setTheme("basic");
        bean.setLocale("en_US");
        bean.setTimeZone("America/New_York");
        return action;
    }

    private static RuntimeConfigProperty setting() throws Exception {
        return WebloggerFactory.getWeblogger().getPropertiesManager().getProperty(SETTING);
    }

    private static void setSetting(String value) throws Exception {
        PropertiesManager properties = WebloggerFactory.getWeblogger().getPropertiesManager();
        RuntimeConfigProperty property = properties.getProperty(SETTING);
        property.setValue(value);
        properties.saveProperty(property);
        TestUtils.endSession(true);
    }
}
