/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.roller.weblogger.business.search;

import org.apache.roller.weblogger.pojos.WeblogEntry;
import org.junit.jupiter.api.Test;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doCallRealMethod;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * A saved entry is re-indexed if it is published and removed otherwise.
 */
class UpdateEntryIndexTest {

    @Test
    void publishedEntryIsReIndexed() throws Exception {
        IndexManager manager = manager();
        WeblogEntry entry = entry(true);
        manager.updateEntryIndex(entry);
        verify(manager).addEntryReIndexOperation(entry);
        verify(manager, never()).removeEntryIndexOperation(any());
    }

    @Test
    void unpublishedEntryIsRemoved() throws Exception {
        IndexManager manager = manager();
        WeblogEntry entry = entry(false);
        manager.updateEntryIndex(entry);
        verify(manager).removeEntryIndexOperation(entry);
        verify(manager, never()).addEntryReIndexOperation(any());
    }

    private static IndexManager manager() throws Exception {
        IndexManager manager = mock(IndexManager.class);
        doCallRealMethod().when(manager).updateEntryIndex(any());
        return manager;
    }

    private static WeblogEntry entry(boolean published) {
        WeblogEntry entry = mock(WeblogEntry.class);
        when(entry.isPublished()).thenReturn(published);
        return entry;
    }
}
