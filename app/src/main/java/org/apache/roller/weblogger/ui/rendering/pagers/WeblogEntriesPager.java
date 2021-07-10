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

package org.apache.roller.weblogger.ui.rendering.pagers;

import java.util.Collection;
import java.util.Date;
import java.util.Map;
import org.apache.roller.weblogger.pojos.wrapper.WeblogEntryWrapper;

/**
 * Pager for weblog entries, handles latest, single-entry, month and day views.
 * Collection returned is a list of lists of entries, where each list of 
 * entries represents one day.
 */
public interface WeblogEntriesPager {  
        
    /**
     * A map of entries representing this collection.
     *
     * The collection is grouped by days of entries.  Each value is a list of
     * entry objects keyed by the date they were published.
     */
    Map<Date, ? extends Collection<WeblogEntryWrapper>> getEntries();
        
    /**
     * Link value for returning to pager home
     */
    String getHomeLink();

    /**
     * Name of pager home.
     */
    String getHomeName();

    /**
     * Link value for next page in current collection view
     */
    String getNextLink();

    /**
     * Name for next page in current collection view
     */
    String getNextName();

    /**
     * Link value for prev page in current collection view
     */
    String getPrevLink();

    /**
     * Link value for prev page in current collection view
     */
    String getPrevName();
    
        /**
     * Link value for next collection view
     */
    String getNextCollectionLink();
    
    /**
     * Name for next collection view
     */
    String getNextCollectionName();
    
    /**
     * Link value for prev collection view
     */
    String getPrevCollectionLink();
    
    /**
     * Name for prev collection view
     */
    String getPrevCollectionName();
    
}

