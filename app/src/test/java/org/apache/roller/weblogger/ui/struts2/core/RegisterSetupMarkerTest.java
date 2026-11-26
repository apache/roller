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
import org.apache.roller.weblogger.ui.core.security.BootstrapSecurity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * Registration records that setup is done in a single properties row. Writing
 * it when the row is already there must update it, not insert a second one,
 * or the registration fails on the duplicate key.
 */
class RegisterSetupMarkerTest {

    @BeforeEach
    void setUp() throws Exception {
        TestUtils.setupWeblogger();
    }

    @Test
    void recordingSetupTwiceKeepsOneRow() throws Exception {
        PropertiesManager properties = WebloggerFactory.getWeblogger().getPropertiesManager();

        Register.markSetupCompleted();
        WebloggerFactory.getWeblogger().flush();
        TestUtils.endSession(true);

        Register.markSetupCompleted();
        WebloggerFactory.getWeblogger().flush();
        TestUtils.endSession(true);

        RuntimeConfigProperty marker = properties.getProperty(BootstrapSecurity.COMPLETION_PROPERTY);
        assertNotNull(marker);
        assertEquals("true", marker.getValue());
    }
}
