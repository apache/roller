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

package org.apache.roller.weblogger.ui.core;

/**
 * Static collection of request parameter names.
 */
public class RequestConstants {
    
    public static final String ANCHOR            = "entry";
    public static final String BOOKMARK_ID       = "bookmarkId";
    public static final String FOLDER_ID         = "folderId";
    public static final String PAGE_ID           = "pageId";
    public static final String PARENT_ID         = "parentId";
    public static final String PINGTARGET_ID     = "pingtargetId";
    public static final String REFERRER_ID       = "referrerId";
    public static final String USERNAME          = "username";
    public static final String WEBLOG            = "weblog";
    public static final String WEBLOG_ID         = "websiteId";
    public static final String WEBLOGCATEGORY    = "cat";
    public static final String WEBLOGCATEGORY_ID = "categoryId";
    public static final String WEBLOGENTRY_ID    = "entryId";
    
    // this should ONLY be used in file-upload situations where
    // weblog handle cannot be conveyed as a request parameter
    public static final String WEBLOG_SESSION_STASH = "weblog_session_stash";
}
