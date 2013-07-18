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

package org.apache.roller.weblogger.util.cache;

import org.apache.roller.weblogger.pojos.WeblogBookmark;
import org.apache.roller.weblogger.pojos.WeblogEntryComment;
import org.apache.roller.weblogger.pojos.WeblogBookmarkFolder;
import org.apache.roller.weblogger.pojos.WeblogReferrer;
import org.apache.roller.weblogger.pojos.User;
import org.apache.roller.weblogger.pojos.WeblogCategory;
import org.apache.roller.weblogger.pojos.WeblogEntry;
import org.apache.roller.weblogger.pojos.WeblogTemplate;
import org.apache.roller.weblogger.pojos.Weblog;


/**
 * Represents someone that wants to receive notifications about cache
 * invalidation events.
 *
 * A CacheHandler can be registered with the CacheManager and then will
 * receive all the various object invalidation events happening in the
 * system.  Typically classes which are using a cache will want to implement
 * this interface so that they can know when to remove items from their cache.
 */
public interface CacheHandler {
    
    public void invalidate(WeblogEntry entry);
    
    public void invalidate(Weblog website);
    
    public void invalidate(WeblogBookmark bookmark);
    
    public void invalidate(WeblogBookmarkFolder folder);

    public void invalidate(WeblogEntryComment comment);

    public void invalidate(WeblogReferrer referer);

    public void invalidate(User user);

    public void invalidate(WeblogCategory category);

    public void invalidate(WeblogTemplate template);
    
}
