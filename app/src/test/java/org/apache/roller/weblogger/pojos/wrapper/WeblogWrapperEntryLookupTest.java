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

package org.apache.roller.weblogger.pojos.wrapper;

import org.apache.roller.weblogger.pojos.Weblog;
import org.apache.roller.weblogger.pojos.WeblogEntry;
import org.apache.roller.weblogger.pojos.WeblogEntry.PubStatus;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Templates look up entries by anchor through the weblog wrapper. Only
 * published entries come back.
 */
public class WeblogWrapperEntryLookupTest {

    private static WeblogWrapper weblogWithEntry(PubStatus status) {
        WeblogEntry entry = new WeblogEntry();
        entry.setAnchor("the-anchor");
        entry.setStatus(status);
        Weblog weblog = mock(Weblog.class);
        when(weblog.getWeblogEntry("the-anchor")).thenReturn(entry);
        return WeblogWrapper.wrap(weblog, null);
    }

    @Test
    public void publishedEntryIsReturned() {
        WeblogEntryWrapper found = weblogWithEntry(PubStatus.PUBLISHED).getWeblogEntry("the-anchor");
        assertNotNull(found);
        assertEquals("the-anchor", found.getAnchor());
    }

    @Test
    public void unpublishedEntriesAreNotReturned() {
        for (PubStatus status : new PubStatus[] {PubStatus.DRAFT, PubStatus.PENDING, PubStatus.SCHEDULED}) {
            assertNull(weblogWithEntry(status).getWeblogEntry("the-anchor"), status.name());
        }
    }

    @Test
    public void missingEntryIsNull() {
        assertNull(weblogWithEntry(PubStatus.PUBLISHED).getWeblogEntry("other"));
    }
}
