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

package org.apache.roller.weblogger.business.search.lucene;

import java.nio.file.Files;
import java.nio.file.Path;

import org.apache.roller.weblogger.business.WeblogEntryManager;
import org.apache.roller.weblogger.business.Weblogger;
import org.apache.roller.weblogger.business.runnable.ThreadManager;
import org.apache.roller.weblogger.config.WebloggerConfig;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.MockedStatic;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.CALLS_REAL_METHODS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * The <code>.index-inconsistent</code> marker exists while Roller runs and is
 * removed at a clean shutdown, so finding it at startup means the last run
 * ended early and the index may be incomplete.
 */
class IndexConsistencyMarkerTest {

    @TempDir
    Path indexDir;

    private LuceneIndexManager manager(ThreadManager threads) {
        Weblogger roller = mock(Weblogger.class);
        when(roller.getThreadManager()).thenReturn(threads);
        when(roller.getWeblogEntryManager()).thenReturn(mock(WeblogEntryManager.class));
        WebloggerConfig.init();
        try (MockedStatic<WebloggerConfig> config = mockStatic(WebloggerConfig.class, CALLS_REAL_METHODS)) {
            config.when(() -> WebloggerConfig.getProperty("search.index.dir"))
                    .thenReturn(indexDir.toString());
            return new LuceneIndexManager(roller);
        }
    }

    private Path marker() {
        return indexDir.resolve(".index-inconsistent");
    }

    @Test
    void markerSurvivesTheIndexBeingReplaced() throws Exception {
        // no index yet, so startup deletes what is there and rebuilds
        ThreadManager threads = mock(ThreadManager.class);
        LuceneIndexManager first = manager(threads);
        first.initialize();
        assertTrue(first.isInconsistentAtStartup());
        verify(threads).executeInBackground(any());
        assertTrue(Files.exists(marker()), "the marker must stay while this run is going");

        // Roller is stopped before the rebuild finishes: no shutdown()
        ThreadManager nextThreads = mock(ThreadManager.class);
        LuceneIndexManager next = manager(nextThreads);
        try {
            next.initialize();
            assertTrue(next.isInconsistentAtStartup());
            verify(nextThreads).executeInBackground(any());
            assertTrue(Files.exists(marker()));
        } finally {
            next.shutdown();
        }
    }

    @Test
    void cleanShutdownRemovesTheMarker() throws Exception {
        LuceneIndexManager first = manager(mock(ThreadManager.class));
        first.initialize();
        first.shutdown();
        assertFalse(Files.exists(marker()));

        ThreadManager nextThreads = mock(ThreadManager.class);
        LuceneIndexManager next = manager(nextThreads);
        try {
            next.initialize();
            assertFalse(next.isInconsistentAtStartup());
            verify(nextThreads, never()).executeInBackground(any());
        } finally {
            next.shutdown();
        }
    }
}
