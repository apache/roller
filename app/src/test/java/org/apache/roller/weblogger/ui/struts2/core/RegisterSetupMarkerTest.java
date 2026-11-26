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

import java.sql.Connection;
import java.sql.PreparedStatement;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.apache.roller.weblogger.TestUtils;
import org.apache.roller.weblogger.business.PropertiesManager;
import org.apache.roller.weblogger.business.WebloggerFactory;
import org.apache.roller.weblogger.business.startup.WebloggerStartup;
import org.apache.roller.weblogger.pojos.RuntimeConfigProperty;
import org.apache.roller.weblogger.ui.core.RollerContext;
import org.apache.roller.weblogger.ui.core.security.BootstrapSecurity;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.springframework.security.crypto.password.NoOpPasswordEncoder;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

/**
 * Once setup is recorded in the database, later registrations must still
 * save their user: they used to insert the setup marker again and fail on
 * the duplicate key when the session was flushed.
 */
class RegisterSetupMarkerTest {

    private static final String REGISTRATION = "users.registration.enabled";
    private static final String[] USERS = {"junitregistersecond", "junitregisterthird"};

    private RuntimeConfigProperty savedMarker;
    private String savedRegistration;
    private MockedStatic<RollerContext> context;

    @BeforeEach
    void setUp() throws Exception {
        TestUtils.setupWeblogger();
        PropertiesManager properties = properties();
        RuntimeConfigProperty marker = properties.getProperty(BootstrapSecurity.COMPLETION_PROPERTY);
        savedMarker = marker == null ? null : new RuntimeConfigProperty(marker.getName(), marker.getValue());
        RuntimeConfigProperty registration = properties.getProperty(REGISTRATION);
        savedRegistration = registration == null ? null : registration.getValue();
        setProperty(REGISTRATION, "true");
        TestUtils.endSession(true);

        context = mockStatic(RollerContext.class);
        context.when(RollerContext::getPasswordEncoder).thenReturn(NoOpPasswordEncoder.getInstance());
    }

    @AfterEach
    void tearDown() throws Exception {
        context.close();
        for (String user : USERS) {
            if (WebloggerFactory.getWeblogger().getUserManager().getUserByUserName(user, null) != null) {
                TestUtils.teardownUser(user);
            }
        }
        if (savedRegistration != null) {
            setProperty(REGISTRATION, savedRegistration);
        }
        if (savedMarker != null) {
            setProperty(savedMarker.getName(), savedMarker.getValue());
        }
        TestUtils.endSession(true);
        if (savedMarker == null) {
            try (Connection con = WebloggerStartup.getDatabaseProvider().getConnection();
                 PreparedStatement delete = con.prepareStatement(
                         "delete from roller_properties where name = ?")) {
                delete.setString(1, BootstrapSecurity.COMPLETION_PROPERTY);
                delete.executeUpdate();
            }
        }
    }

    @Test
    void registrationsAfterSetupIsRecordedSaveTheirUsers() throws Exception {
        Register.markSetupCompleted();
        TestUtils.endSession(true);

        try (MockedStatic<BootstrapSecurity> bootstrap = mockStatic(BootstrapSecurity.class)) {
            bootstrap.when(BootstrapSecurity::isCompleted).thenReturn(true);
            for (String user : USERS) {
                Register action = register(user);
                String result = action.save();
                assertEquals("success", result,
                        () -> user + " " + action.getActionErrors() + " " + action.getFieldErrors());
                TestUtils.endSession(true);
            }
        }

        for (String user : USERS) {
            assertNotNull(WebloggerFactory.getWeblogger().getUserManager().getUserByUserName(user), user);
        }
        assertEquals("true", properties().getProperty(BootstrapSecurity.COMPLETION_PROPERTY).getValue());
    }

    private static PropertiesManager properties() {
        return WebloggerFactory.getWeblogger().getPropertiesManager();
    }

    private static void setProperty(String name, String value) throws Exception {
        PropertiesManager properties = properties();
        RuntimeConfigProperty property = properties.getProperty(name);
        if (property == null) {
            property = new RuntimeConfigProperty(name, value);
        } else {
            property.setValue(value);
        }
        properties.saveProperty(property);
    }

    private static Register register(String userName) {
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getSession()).thenReturn(mock(HttpSession.class));

        Register action = spy(new Register());
        doAnswer(call -> call.getArgument(0)).when(action).getText(anyString());
        action.setServletRequest(request);
        action.getBean().setUserName(userName);
        action.getBean().setScreenName(userName);
        action.getBean().setFullName(userName);
        action.getBean().setEmailAddress(userName + "@example.com");
        action.getBean().setPasswordText("s3cret-Passw0rd");
        action.getBean().setPasswordConfirm("s3cret-Passw0rd");
        return action;
    }
}
