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

import java.lang.reflect.Field;
import jakarta.persistence.Cache;

import org.apache.roller.weblogger.TestUtils;
import org.apache.roller.weblogger.business.WebloggerFactory;
import org.apache.roller.weblogger.business.WeblogEntryManager;
import org.apache.roller.weblogger.pojos.User;
import org.apache.roller.weblogger.pojos.Weblog;
import org.apache.roller.weblogger.pojos.WeblogEntry;
import org.apache.roller.weblogger.pojos.WeblogEntryComment;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Comments stay out of EclipseLink's shared cache. Background search index
 * operations read comments on their own threads, and a read that overlaps a
 * delete could otherwise leave the deleted comment in the shared cache, where
 * lookups by id would still find it (ROL-2185).
 */
public class CommentSharedCacheTest {

    private User user;
    private Weblog weblog;

    @BeforeEach
    public void setUp() throws Exception {
        TestUtils.setupWeblogger();
        user = TestUtils.setupUser("commentcacheuser");
        weblog = TestUtils.setupWeblog("commentcacheblog", user);
        TestUtils.endSession(true);
    }

    @AfterEach
    public void tearDown() throws Exception {
        TestUtils.teardownWeblog(weblog.getId());
        TestUtils.teardownUser(user.getUserName());
        TestUtils.endSession(true);
    }

    @Test
    public void commentsAreNotKeptInTheSharedCache() throws Exception {
        WeblogEntry entry = TestUtils.setupWeblogEntry("commentcache",
                TestUtils.getManagedWebsite(weblog), TestUtils.getManagedUser(user));
        WeblogEntryComment comment = TestUtils.setupComment("cached?", entry);
        TestUtils.endSession(true);

        WeblogEntryManager mgr = WebloggerFactory.getWeblogger().getWeblogEntryManager();
        mgr.getWeblogEntry(entry.getId());
        mgr.getComment(comment.getId());
        TestUtils.endSession(false);

        Cache cache = strategy().getEntityManager(false).getEntityManagerFactory().getCache();
        // entries are still cached, so this checks the comment setting, not a disabled cache
        assertTrue(cache.contains(WeblogEntry.class, entry.getId()));
        assertFalse(cache.contains(WeblogEntryComment.class, comment.getId()));
        TestUtils.endSession(false);
    }

    private static JPAPersistenceStrategy strategy() throws Exception {
        Field field = JPAWebloggerImpl.class.getDeclaredField("strategy");
        field.setAccessible(true);
        return (JPAPersistenceStrategy) field.get(WebloggerFactory.getWeblogger());
    }
}
