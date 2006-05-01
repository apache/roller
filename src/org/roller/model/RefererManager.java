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

package org.roller.model;

import java.util.List;
import org.roller.RollerException;
import org.roller.pojos.RefererData;
import org.roller.pojos.WebsiteData;


/**
 * Interface to Referer management.
 */
public interface RefererManager {
    
    
    /**
     * Store the referer.
     */
    public void saveReferer(RefererData referer) throws RollerException;
    
    
    /**
     * Remove a single referer.
     */
    public void removeReferer(RefererData referer) throws RollerException;
    
    
    /**
     * Clear referrer dayhits and remove referrers without excerpts.
     */
    public void clearReferrers() throws RollerException;
    
    
    /**
     * Clear referrer dayhits and remove referrers without excerpts.
     */
    public void clearReferrers(WebsiteData website) throws RollerException;
    
    
    /**
     * Retrieve referer by ID.
     */
    public RefererData getReferer(String id) throws RollerException;
    
    
    /**
     * Get all referers for specified weblog.
     * @param weblog
     * @return List of type RefererData
     * @throws RollerException
     */
    public List getReferers(WebsiteData weblog) throws RollerException;
    
    
    /**
     * Get all referers for specified user that were made today.
     * @param userName Name of user.
     * @return List of type RefererData
     * @throws RollerException
     */
    public List getTodaysReferers(WebsiteData website) throws RollerException;
    
    
    /**
     * Get referers for a specified date.
     * @param userName Name of user.
     * @param date YYYYMMDD format of day's date.
     * @return List of type RefererData.
     * @throws RollerException
     */
    public List getReferersToDate(WebsiteData website, String date) throws RollerException;
    
    
    /**
     * Get most popular websites based on referer day hits.
     * @return List of WebsiteDisplayData objects.
     */
    public List getDaysPopularWebsites(int max) throws RollerException;
    
    
    /**
     * Get referers that refer to a specific weblog entry.
     * @param entryid Weblog entry ID
     * @return List of RefererData objects.
     * @throws RollerException
     */
    public List getReferersToEntry(String entryid) throws RollerException;
    
    
    /** 
     * Get user's day hits 
     */
    public int getDayHits(WebsiteData website) throws RollerException;
    
    
    /** 
     * Get user's all-time total hits 
     */
    public int getTotalHits(WebsiteData website) throws RollerException;
    
    
    /**
     * Apply ignoreWord/spam filters to all referers in system.
     */
    public void applyRefererFilters() throws RollerException;
    
    
    /**
     * Apply ignoreWord/spam filters to all referers in website.
     */
    public void applyRefererFilters(WebsiteData website) throws RollerException;
    
    
    /**
     * Process an incoming referer.
     */
    public void processReferrer(
            String requestUrl,
            String referrerUrl,
            String weblogHandle,
            String weblogAnchor,
            String weblogDateString);
    
    
    /**
     * Release all resources associated with Roller session.
     */
    public void release();
    
}

