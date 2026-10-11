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
 * limitations under the License.  For additional information regarding
 * copyright in this work, please see the NOTICE file in the top level
 * directory of this distribution.
 */

package org.apache.roller.weblogger.webservices.xmlrpc;

import org.apache.roller.weblogger.WebloggerException;
import org.apache.roller.weblogger.business.Weblogger;
import org.apache.roller.weblogger.business.WebloggerFactory;
import org.apache.roller.weblogger.business.WeblogEntryManager;
import org.apache.roller.weblogger.business.WeblogManager;
import org.apache.roller.weblogger.pojos.User;
import org.apache.roller.weblogger.pojos.WeblogPermission;
import org.apache.xmlrpc.common.XmlRpcNotAuthorizedException;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

public class BaseAPIHandlerTest {

    @Test
    public void testWeblogLookupFailureReturnsNeutralAuthorizationFault()
            throws Exception {
        Weblogger weblogger = mock(Weblogger.class);
        WeblogManager manager = mock(WeblogManager.class);
        when(weblogger.getWeblogManager()).thenReturn(manager);
        when(manager.getWeblogByHandle("unavailable"))
                .thenThrow(new WebloggerException("database details"));

        try (MockedStatic<WebloggerFactory> factory =
                     mockStatic(WebloggerFactory.class)) {
            factory.when(WebloggerFactory::getWeblogger).thenReturn(weblogger);
            XmlRpcNotAuthorizedException fault = assertThrows(
                    XmlRpcNotAuthorizedException.class,
                    () -> new TestHandler().validateWeblogForTest(
                            "unavailable", mock(User.class)));
            assertFalse(fault.getMessage().contains("database details"));
        }
    }

    @Test
    public void testEntryLookupFailureReturnsNeutralAuthorizationFault()
            throws Exception {
        Weblogger weblogger = mock(Weblogger.class);
        WeblogEntryManager manager = mock(WeblogEntryManager.class);
        when(weblogger.getWeblogEntryManager()).thenReturn(manager);
        when(manager.getWeblogEntry("unavailable"))
                .thenThrow(new WebloggerException("database details"));

        try (MockedStatic<WebloggerFactory> factory =
                     mockStatic(WebloggerFactory.class)) {
            factory.when(WebloggerFactory::getWeblogger).thenReturn(weblogger);
            XmlRpcNotAuthorizedException fault = assertThrows(
                    XmlRpcNotAuthorizedException.class,
                    () -> new TestHandler().getEntryForWriteForTest(
                            "unavailable", mock(User.class)));
            assertFalse(fault.getMessage().contains("database details"));
        }
    }

    private static class TestHandler extends BaseAPIHandler {
        private static final long serialVersionUID = 1L;

        void validateWeblogForTest(String handle, User user) throws Exception {
            validateWeblog(handle, user, WeblogPermission.EDIT_DRAFT);
        }

        void getEntryForWriteForTest(String id, User user) throws Exception {
            getEntryForWrite(id, user);
        }
    }
}
