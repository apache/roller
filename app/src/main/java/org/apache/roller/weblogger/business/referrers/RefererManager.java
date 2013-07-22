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

package org.apache.roller.weblogger.business.referrers;

import java.util.List;
import org.apache.roller.weblogger.WebloggerException;
import org.apache.roller.weblogger.pojos.WeblogReferrer;
import org.apache.roller.weblogger.pojos.Weblog;

/**
 * Interface to Referer management.
 */
public interface RefererManager {    
    
    /**
     * Store the referer.
     */
    void saveReferer(WeblogReferrer referer) throws WebloggerException;
    
    /**
     * Remove a single referer.
     */
    void removeReferer(WeblogReferrer referer) throws WebloggerException;
    
    /**
     * Clear referrer dayhits and remove referrers without excerpts.
     */
    void clearReferrers() throws WebloggerException;
    
    /**
     * Clear referrer dayhits and remove referrers without excerpts.
     */
    void clearReferrers(Weblog website) throws WebloggerException;
    
    /**
     * Retrieve referer by id.
     */
    WeblogReferrer getReferer(String id) throws WebloggerException;
    
    /**
     * Get all referers for specified weblog.
     * @param weblog
     * @return List of type RefererData
     */
    List getReferers(Weblog weblog) throws WebloggerException;
    
    /**
     * Get all referers for specified user that were made today.
     * @param userName Name of user.
     * @return List of type RefererData
     */
    List getTodaysReferers(Weblog website) throws WebloggerException;
    
    /**
     * Get referers for a specified date.
     * @param userName Name of user.
     * @param date YYYYMMDD format of day's date.
     * @return List of type RefererData.
     * @throws WebloggerException
     */
    List getReferersToDate(Weblog website, String date)
        throws WebloggerException;    
        
    /**
     * Returns hot weblogs as StatCount objects, in descending order by today's hits.
     * @param sinceDays Restrict to last X days (or -1 for all)
     * @param offset    Offset into results (for paging)
     * @param len       Maximum number of results to return (for paging)
     * @return List of StatCount objects.
     */
    List getHotWeblogs(int sinceDays, int offset, int length)
        throws WebloggerException; 

    /**
     * Get referers that refer to a specific weblog entry.
     * @param entryid Weblog entry ID
     * @return List of RefererData objects.
     * @throws WebloggerException
     */
    List getReferersToEntry(String entryid) throws WebloggerException;
    
    /** 
     * Get user's day hits 
     */
    int getDayHits(Weblog website) throws WebloggerException;
    
    /** 
     * Get user's all-time total hits 
     */
    int getTotalHits(Weblog website) throws WebloggerException;
    
    /**
     * Apply ignoreWord/spam filters to all referers in system.
     */
    void applyRefererFilters() throws WebloggerException;
    
    /**
     * Apply ignoreWord/spam filters to all referers in website.
     */
    void applyRefererFilters(Weblog website) throws WebloggerException;
    
    /**
     * Process an incoming referer.
     */
    void processReferrer(
            String requestUrl,
            String referrerUrl,
            String weblogHandle,
            String weblogAnchor,
            String weblogDateString);    
    
    /**
     * Release all resources held by manager.
     */
    void release();
}

