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
import org.apache.roller.weblogger.pojos.CustomTemplateRendition;
import org.apache.roller.weblogger.pojos.StatCount;
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
    void addWeblog(Weblog newWebsite) throws WebloggerException;
    
    
    /**
     * Store a single weblog.
     */
    void saveWeblog(Weblog data) throws WebloggerException;

    /**
     * Remove website object.
     */
    void removeWeblog(Weblog website) throws WebloggerException;
    
    /**
     * Get website object by name.
     */
    Weblog getWeblog(String id) throws WebloggerException;
    
    
    /**
     * Get website specified by handle (or null if enabled website not found).
     * @param handle  Handle of website
     */
    Weblog getWeblogByHandle(String handle) throws WebloggerException;
    
    
    /**
     * Get website specified by handle with option to return only enabled websites.
     * @param handle  Handle of website
     */
    Weblog getWeblogByHandle(String handle, Boolean enabled)
        throws WebloggerException;
    
    
    /**
     * Get websites optionally restricted by user, enabled and active status.
     * @param enabled   Get all with this enabled state (or null or all)
     * @param active    Get all with this active state (or null or all)
     * @param startDate Restrict to those created after (or null for all)
     * @param endDate   Restrict to those created before (or null for all)
     * @param offset    Offset into results (for paging)
     * @param length    Maximum number of results to return (for paging)
     * @return List of Weblog objects.
     */
    List<Weblog> getWeblogs(
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
     * @return List of Weblog objects.
     */
    List<Weblog> getUserWeblogs(User user, boolean enabledOnly) throws WebloggerException;
    
    
    /**
     * Get users of a weblog.
     * @param weblog Weblog to retrieve users for
     * @param enabledOnly Include only enabled users?
     * @return List of WebsiteData objects.
     */
    List<User> getWeblogUsers(Weblog weblog, boolean enabledOnly) throws WebloggerException;
    
    
    /**
     * Get websites ordered by descending number of comments.
     * @param startDate Restrict to those created after (or null for all)
     * @param endDate Restrict to those created before (or null for all)
     * @param offset    Offset into results (for paging)
     * @param length       Maximum number of results to return (for paging)
     * @return List of StatCount objects.
     */
    List<StatCount> getMostCommentedWeblogs(
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
    Map<String, Long> getWeblogHandleLetterMap() throws WebloggerException;
    
    
    /** 
     * Get collection of weblogs whose handles begin with specified letter 
     */
    List<Weblog> getWeblogsByLetter(char letter, int offset, int length)
        throws WebloggerException;
    
        /**
     * Store page.
     */
    void savePage(WeblogTemplate data) throws WebloggerException;
    
    
    /**
     * Remove page.
     */
    void removePage(WeblogTemplate page) throws WebloggerException;
    
    
    /**
     * Get page by id.
     */
    WeblogTemplate getPage(String id) throws WebloggerException;
    
    
    /**
     * Get user's page by action.
     */
    WeblogTemplate getPageByAction(Weblog w, String a) throws WebloggerException;
    
    
    /**
     * Get user's page by name.
     */
    WeblogTemplate getPageByName(Weblog w, String p) throws WebloggerException;
    
    
    /**
     * Get website's page by link.
     */
    WeblogTemplate getPageByLink(Weblog w, String p)
        throws WebloggerException;

    /**
     * Get template code by Type.
     */
    CustomTemplateRendition getTemplateRenditionByType(String templateName, String type)
            throws WebloggerException ;

    /**
     * Save template code
     */
    void saveTemplateRendition(CustomTemplateRendition templateCode) throws WebloggerException;

    /**
     * Get website's pages
     */
    List<WeblogTemplate> getPages(Weblog w) throws WebloggerException;
   
    
    /**
     * Get count of active weblogs
     */    
    long getWeblogCount() throws WebloggerException;
    
    
    /**
     * Release any resources held by manager.
     */
    void release();

}
