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

package org.apache.roller.weblogger.business;

import java.util.Date;
import java.util.List;
import java.util.Map;
import org.apache.roller.weblogger.WebloggerException;
import org.apache.roller.weblogger.pojos.User;
import org.apache.roller.weblogger.pojos.Weblog;
import org.apache.roller.weblogger.pojos.WeblogTemplate;


/**
 * Interface to weblog and page template management.
 */
public interface WeblogManager {
    
    /**
     * Add new website, give creator admin permission, creates blogroll,
     * creates categories and other objects required for new website.
     * @param newWebsite New website to be created, must have creator.
     */
    public void addWeblog(Weblog newWebsite) throws WebloggerException;
    
    
    /**
     * Store a single weblog.
     */
    public void saveWeblog(Weblog data) throws WebloggerException;
    
    
    /**
     * Remove website object.
     */
    public void removeWeblog(Weblog website) throws WebloggerException;
    
    
    /**
     * Get website object by name.
     */
    public Weblog getWeblog(String id) throws WebloggerException;
    
    
    /**
     * Get website specified by handle (or null if enabled website not found).
     * @param handle  Handle of website
     */
    public Weblog getWeblogByHandle(String handle) throws WebloggerException;
    
    
    /**
     * Get website specified by handle with option to return only enabled websites.
     * @param handle  Handle of website
     */
    public Weblog getWeblogByHandle(String handle, Boolean enabled)
        throws WebloggerException;
    
    
    /**
     * Get websites optionally restricted by user, enabled and active status.
     * @param enabled   Get all with this enabled state (or null or all)
     * @param active    Get all with this active state (or null or all)
     * @param startDate Restrict to those created after (or null for all)
     * @param endDate   Restrict to those created before (or null for all)
     * @param offset    Offset into results (for paging)
     * @param length    Maximum number of results to return (for paging)
     * @returns List of WebsiteData objects.
     */
    public List getWeblogs(
            Boolean  enabled,
            Boolean  active,
            Date     startDate,
            Date     endDate,
            int      offset,
            int      length)
            throws WebloggerException;
    
    
    /**
     * Get websites of a user.
     * @param user        Get all weblogs for this user
     * @param enabledOnly Include only enabled weblogs?
     * @returns List of WebsiteData objects.
     */
    public List getUserWeblogs(User user, boolean enabledOnly) throws WebloggerException;
    
    
    /**
     * Get users of a weblog.
     * @param user        Get all users for this weblog
     * @param enabledOnly Include only enabled users?
     * @returns List of WebsiteData objects.
     */
    public List getWeblogUsers(Weblog weblog, boolean enabledOnly) throws WebloggerException;
    
    
    /**
     * Get websites ordered by descending number of comments.
     * @param startDate Restrict to those created after (or null for all)
     * @param endDate Restrict to those created before (or null for all)
     * @param offset    Offset into results (for paging)
     * @param len       Maximum number of results to return (for paging)
     * @returns List of WebsiteData objects.
     */
    public List getMostCommentedWeblogs(
            Date startDate,
            Date endDate,
            int  offset,
            int  length)
            throws WebloggerException;
    
    
    /**
     * Get map with 26 entries, one for each letter A-Z and
     * containing integers reflecting the number of weblogs whose
     * names start with each letter.
     */
    public Map getWeblogHandleLetterMap() throws WebloggerException;
    
    
    /** 
     * Get collection of weblogs whose handles begin with specified letter 
     */
    public List getWeblogsByLetter(char letter, int offset, int length) 
        throws WebloggerException;
    
        /**
     * Store page.
     */
    public void savePage(WeblogTemplate data) throws WebloggerException;
    
    
    /**
     * Remove page.
     */
    public void removePage(WeblogTemplate page) throws WebloggerException;
    
    
    /**
     * Get page by id.
     */
    public WeblogTemplate getPage(String id) throws WebloggerException;
    
    
    /**
     * Get user's page by action.
     */
    public WeblogTemplate getPageByAction(Weblog w, String a) throws WebloggerException;
    
    
    /**
     * Get user's page by name.
     */
    public WeblogTemplate getPageByName(Weblog w, String p) throws WebloggerException;
    
    
    /**
     * Get website's page by link.
     */
    public WeblogTemplate getPageByLink(Weblog w, String p)
        throws WebloggerException;
    
    
    /**
     * Get website's pages
     */
    public List getPages(Weblog w) throws WebloggerException;
   
    
    /**
     * Get count of active weblogs
     */    
    public long getWeblogCount() throws WebloggerException;
    
    
    /**
     * Release any resources held by manager.
     */
    public void release();
}
