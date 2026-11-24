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

package org.apache.roller.weblogger.business.jpa;

import net.oauth.OAuth;
import net.oauth.OAuthAccessor;
import net.oauth.OAuthConsumer;
import net.oauth.OAuthMessage;
import net.oauth.OAuthProblemException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.roller.weblogger.TestUtils;
import org.apache.roller.weblogger.business.WebloggerFactory;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test Weblogger Bookmark Management.
 */
public class JPAOAuthManagerTest  {
    public static Log log = LogFactory.getLog(JPAOAuthManagerTest.class);

    @BeforeEach
    public void setUp() throws Exception {
        
        // setup weblogger
        TestUtils.setupWeblogger();
        
        try {
            TestUtils.endSession(true);
        } catch (Exception ex) {
            log.error(ex);
            throw new Exception("Test setup failed", ex);
        }
    }

    @AfterEach
    public void tearDown() throws Exception {
        
        try {
            TestUtils.endSession(true);
        } catch (Exception ex) {
            log.error("ERROR in tearDown", ex);
            throw new Exception("Test teardown failed", ex);
        }
    }

    @Test
    public void testCRUD() throws Exception {
        JPAOAuthManagerImpl omgr = (JPAOAuthManagerImpl)
            WebloggerFactory.getWeblogger().getOAuthManager();

        String consumerKey = "1111";
        OAuthConsumer consumer = omgr.addConsumer("dummyusername", consumerKey);
        TestUtils.endSession(true);

        consumer = omgr.getConsumerByKey(consumer.consumerKey);
        assertNotNull(consumer);
        assertEquals(consumerKey, consumer.consumerKey);

        OAuthAccessor accessor = new OAuthAccessor(consumer);
        accessor.setProperty("userId", "dummyusername");
        omgr.addAccessor(accessor);
        TestUtils.endSession(true);

        accessor = omgr.getAccessorByKey(consumerKey);
        assertNotNull(accessor);

        omgr.removeAccessor(accessor);
        TestUtils.endSession(true);
        assertNull(omgr.getAccessorByKey(consumerKey));

        omgr.removeConsumer(consumer);
        TestUtils.endSession(true);
        assertNull(omgr.getConsumerByKey(consumerKey));
    }

    @Test
    public void testAuthorizeRequestTokenTransition() throws Exception {
        JPAOAuthManagerImpl omgr = (JPAOAuthManagerImpl)
            WebloggerFactory.getWeblogger().getOAuthManager();

        String consumerKey = "authorization-consumer";
        String requestToken = "pending-request-token";
        OAuthConsumer consumer = omgr.addConsumer("authorization-owner", consumerKey);

        OAuthAccessor accessor = new OAuthAccessor(consumer);
        accessor.requestToken = requestToken;
        accessor.tokenSecret = "pending-token-secret";
        omgr.addAccessor(accessor);
        TestUtils.endSession(true);

        assertFalse(omgr.authorizeRequestToken(consumerKey, "another-token", "alice"));
        assertTrue(omgr.authorizeRequestToken(consumerKey, requestToken, "alice"));
        assertTrue(omgr.authorizeRequestToken(consumerKey, requestToken, "alice"));
        assertFalse(omgr.authorizeRequestToken(consumerKey, requestToken, "bob"));
        TestUtils.endSession(true);

        OAuthAccessor authorized = omgr.getAccessorByToken(requestToken);
        assertNotNull(authorized);
        assertEquals("alice", authorized.getProperty("userId"));
        assertEquals(Boolean.TRUE, authorized.getProperty("authorized"));

        omgr.removeAccessor(authorized);
        omgr.removeConsumer(consumer);
        TestUtils.endSession(true);
    }

    @Test
    void temporaryCredentialsCanBeReissuedAndExchanged() throws Exception {
        JPAOAuthManagerImpl manager = (JPAOAuthManagerImpl)
                WebloggerFactory.getWeblogger().getOAuthManager();
        String owner = "oauth-client-user";
        String key = "oauth-client-key";
        OAuthConsumer consumer = manager.addConsumer(owner, key);
        TestUtils.endSession(true);

        OAuthAccessor first = new OAuthAccessor(consumer);
        manager.generateRequestToken(first);
        TestUtils.endSession(true);

        assertNotNull(manager.getAccessorByToken(first.requestToken));
        TestUtils.endSession(false);

        OAuthAccessor second = new OAuthAccessor(consumer);
        manager.generateRequestToken(second);
        TestUtils.endSession(true);

        String pendingToken = second.requestToken;
        assertNotEquals(first.requestToken, pendingToken);
        assertNotEquals(first.tokenSecret, second.tokenSecret);
        assertNull(manager.getAccessorByToken(first.requestToken));
        assertNotNull(manager.getAccessorByToken(pendingToken));
        assertTrue(manager.authorizeRequestToken(key, pendingToken, owner));
        TestUtils.endSession(true);

        OAuthAccessor approved = manager.getAccessorByToken(pendingToken);
        manager.generateAccessToken(approved);
        TestUtils.endSession(true);
        assertNotNull(approved.accessToken);
        assertNull(manager.getAccessorByToken(pendingToken));

        OAuthAccessor active = manager.getAccessorByToken(approved.accessToken);
        OAuthMessage atomRequest = approved.newRequestMessage("GET",
                "https://roller.example/roller-services/app", OAuth.newList("oauth_signature_method", "HMAC-SHA1"));
        manager.getValidator().validateMessage(atomRequest, manager.getAccessor(atomRequest));
        assertEquals(owner, active.consumer.getProperty("userId"));
        TestUtils.endSession(false);

        assertThrows(OAuthProblemException.class,
                () -> manager.generateRequestToken(new OAuthAccessor(consumer)));
        TestUtils.endSession(false);

        assertEquals(approved.accessToken, manager.getAccessorByKey(key).accessToken);
        manager.removeAccessor(active);
        manager.removeConsumer(consumer);
        TestUtils.endSession(true);
    }
}
