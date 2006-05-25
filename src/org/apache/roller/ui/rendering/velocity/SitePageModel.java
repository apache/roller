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
package org.apache.roller.ui.rendering.velocity;

import java.util.List;
import java.util.Map;

/**
 *
 */
public class SitePageModel {
    
    /**
     * Get most collection of Website objects,
     * in reverse chrono order by creationDate.
     * @param offset   Offset into results (for paging)
     * @param len      Max number of results to return
     */
    public List getWeblogs(int offset, int len) {
        return null;
    }
    
    /**
     * Get most collection of most commented Website objects,
     * in descending order by number of comments.
     * @param sinceDays Only consider weblogs updated in the last sinceDays
     * @param offset   Offset into results (for paging)
     * @param len      Max number of results to return
     */
    public List getCommentedWeblogs(int sinceDays ,int offset, int len) {
        return null;
    }
    
    /**
     * Get most recent WeblogEntry objects across all weblogs,
     * in reverse chrono order by pubTime.
     * @param cats     To limit results to list of category names
     * @param offset   Offset into results (for paging)
     * @param len      Max number of results to return
     */
    public List getWeblogEntries(List cats, int offset, int len) {
        return null;
    }
    
    /**
     * Get most commented WeblogEntry objects across all weblogs,
     * in descending order by number of comments.
     * @param sinceDays Only consider weblogs updated in the last sinceDays
     * @param cats     To limit results to list of category names
     * @param offset   Offset into results (for paging)
     * @param len      Max number of results to return
     */
    public List getCommentedWeblogEntries(List cats, int sinceDays, int offset, int len) {
        return null;
    }
    
    /**
     * Get most recent WeblogEntry objects across all weblogs,
     * in reverse chrono order by pubTime.
     * @param username   Limit entries to those created by this user
     * @param offset     Offset into results (for paging)
     * @param len        Max number of results to return
     */
    public List getUserWeblogEntries(String username, int offset, int len) {
        return null;
    }
    
    /**
     * Get most recent Comment objects across all weblogs,
     * in reverse chrono order by postTime.
     * @param offset   Offset into results (for paging)
     * @param len      Max number of results to return
     */
    public List getComments(int offset, int len) {
        return null;
    }
    
    /**
     * Get most recent User objects, in reverse chrono order by creationDate.
     * @param offset   Offset into results (for paging)
     * @param len      Max number of results to return
     */
    public List getUsers(int offset, int len) {
        return null;
    }
    
    /**
     * Get list of WebsiteDisplay objects, ordered by number of hits.
     * @param sinceDays Only consider weblogs updated in the last sinceDays
     * @param offset   Offset into results (for paging)
     * @param len      Max number of results to return
     */
    public List getHotBlogs(int sinceDays, int offset, int len) {
        return null;
    }
    
    /** Get User object by username */
    //public UserDataWrapper getUser(String username) {}
    
    /** Get Website object by handle */
    //public WebsiteDataWrapper getWeblog(String handle) {}
    
    /**
     * Get map with 26 entries, one for each letter A-Z and
     * containing integers reflecting the number of users whose
     * names start with each letter.
     */
    public Map getUsernameLetterMap() {
        return null;
    }
    
    /** Get collection of users whose names begin with specified letter */
    public List getUsersByLetter(char letter) {
        return null;
    }
    
    /**
     * Get map with 26 entries, one for each letter A-Z and
     * containing integers reflecting the number of weblogs whose
     * names start with each letter.
     */
    public Map getWeblogHandleLetterMap() {
        return null;
    }
    
    /** Get collection of weblogs whose handles begin with specified letter */
    public List getWeblogsByLetter(char letter) {
        return null;
    }
}


