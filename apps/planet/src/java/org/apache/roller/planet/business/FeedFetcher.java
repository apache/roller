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

package org.apache.roller.planet.business;

import org.apache.roller.RollerException;


/**
 * A FeedFetcher is what is responsible for actually fetching subscriptions from
 * their urls and persisting them to the db for the application to make use of.
 */
public interface FeedFetcher {
    
    /** 
     * Refresh all subscriptions by fetching and parsing feeds. 
     */
    public void refreshEntries(String cacheDirPath) throws RollerException;
    
}
