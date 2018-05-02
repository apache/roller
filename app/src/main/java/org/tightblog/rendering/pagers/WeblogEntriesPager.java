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
 *
 * Source file modified from the original ASF source; all changes made
 * are also under Apache License.
 */
package org.tightblog.rendering.pagers;

import org.tightblog.pojos.WeblogEntry;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * Pager for an ordered list of weblog entries.
 */
public interface WeblogEntriesPager {

    /**
     * URL to return to pager home.
     */
    String getHomeLink();

    /**
     * Label to use for pager home.
     */
    String getHomeLabel();

    /**
     * URL for next sublist of items held by the pager, null if at end of list
     */
    String getNextLink();

    /**
     * Label to use for next link.
     */
    String getNextLabel();

    /**
     * URL for previous sublist of items held by the pager, null if at beginning of list
     */
    String getPrevLink();

    /**
     * Label to use for previous link.
     */
    String getPrevLabel();

    /**
     * A map of all entries provided by the Pager.
     * <p>
     * The collection is grouped by days of entries.  Each value is a list of
     * entry objects keyed by the date they were published.
     */
    Map<LocalDate, List<WeblogEntry>> getEntries();

    List<WeblogEntry> getItems();
}
