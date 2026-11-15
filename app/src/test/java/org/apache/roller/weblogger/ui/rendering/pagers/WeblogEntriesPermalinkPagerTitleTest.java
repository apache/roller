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
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or
 * implied.  See the License for the specific language governing
 * permissions and limitations under the License.  For additional
 * information regarding copyright in this work, please see the NOTICE
 * file in the top level directory of this distribution.
 */

package org.apache.roller.weblogger.ui.rendering.pagers;

import java.sql.Timestamp;

import org.apache.roller.weblogger.TestUtils;
import org.apache.roller.weblogger.business.WeblogEntryManager;
import org.apache.roller.weblogger.business.WebloggerFactory;
import org.apache.roller.weblogger.pojos.User;
import org.apache.roller.weblogger.pojos.Weblog;
import org.apache.roller.weblogger.pojos.WeblogEntry;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * The next and previous links on a permalink page show the neighbouring
 * entries' titles as text.
 */
public class WeblogEntriesPermalinkPagerTitleTest {

    private User user;
    private Weblog weblog;

    @BeforeEach
    public void setUp() throws Exception {
        TestUtils.setupWeblogger();
        user = TestUtils.setupUser("pagerTitleUser");
        weblog = TestUtils.setupWeblog("pagerTitleWeblog", user);
        TestUtils.endSession(true);
    }

    @AfterEach
    public void tearDown() throws Exception {
        TestUtils.teardownWeblog(weblog.getId());
        TestUtils.teardownUser(user.getUserName());
        TestUtils.endSession(true);
    }

    private void entry(String anchor, String title, long pubTime) throws Exception {
        WeblogEntry entry = TestUtils.setupWeblogEntry(anchor, weblog, user);
        entry.setTitle(title);
        entry.setPubTime(new Timestamp(pubTime));
        WeblogEntryManager mgr = WebloggerFactory.getWeblogger().getWeblogEntryManager();
        mgr.saveWeblogEntry(entry);
        TestUtils.endSession(true);
    }

    private WeblogEntriesPermalinkPager pagerFor(String anchor) throws Exception {
        Weblog managed = TestUtils.getManagedWebsite(weblog);
        return new WeblogEntriesPermalinkPager(
                WebloggerFactory.getWeblogger().getUrlStrategy(),
                managed, null, null, anchor, null, null, null, 0);
    }

    @Test
    public void neighbouringTitlesAreShownAsText() throws Exception {
        long now = System.currentTimeMillis();
        entry("older", "<em>Older</em>", now - 3 * 60_000L);
        entry("middle", "Middle", now - 2 * 60_000L);
        entry("newer", "A &amp; B <b>bold</b>", now - 60_000L);

        WeblogEntriesPermalinkPager pager = pagerFor("middle");

        // markup is dropped, entities stay encoded exactly once
        assertEquals("Older", pager.getPrevName());
        assertEquals("A &amp; B bold", pager.getNextName());
    }

    @Test
    public void encodedMarkupStaysEncoded() throws Exception {
        long now = System.currentTimeMillis();
        entry("older", "&lt;em&gt; tags", now - 2 * 60_000L);
        entry("middle", "Middle", now - 60_000L);

        assertEquals("&lt;em&gt; tags", pagerFor("middle").getPrevName());
    }
}
