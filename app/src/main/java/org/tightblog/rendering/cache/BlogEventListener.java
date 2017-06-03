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
package org.tightblog.rendering.cache;

import org.tightblog.pojos.WeblogBookmark;
import org.tightblog.pojos.WeblogEntryComment;
import org.tightblog.pojos.User;
import org.tightblog.pojos.WeblogCategory;
import org.tightblog.pojos.WeblogEntry;
import org.tightblog.pojos.Weblog;
import org.tightblog.pojos.WeblogTemplate;

/**
 * Represents someone that wants to receive notifications about changed
 * items.
 * <p>
 * A BlogEventListener can be registered with the CacheManager and then will
 * receive all the various object invalidation events happening in the
 * system.
 */
public interface BlogEventListener {

    void invalidate(WeblogEntry entry);

    void invalidate(Weblog website);

    void invalidate(WeblogBookmark bookmark);

    void invalidate(WeblogEntryComment comment);

    void invalidate(User user);

    void invalidate(WeblogCategory category);

    void invalidate(WeblogTemplate template);
}
