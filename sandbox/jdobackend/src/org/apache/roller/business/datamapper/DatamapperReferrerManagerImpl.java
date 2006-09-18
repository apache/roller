/*
 * Copyright 2006 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.roller.business.datamapper;

import java.util.List;
import org.apache.roller.RollerException;
import org.apache.roller.model.RefererManager;
import org.apache.roller.pojos.RefererData;
import org.apache.roller.pojos.WebsiteData;

/*
 * DatamapperReferrerManagerImpl.java
 *
 * Created on May 31, 2006, 4:06 PM
 *
 */
public class DatamapperReferrerManagerImpl implements RefererManager {

    /** The strategy for this manager. */
    private DatamapperPersistenceStrategy strategy;

    /** Creates a new instance of DatamapperReferrerManagerImpl */
    public DatamapperReferrerManagerImpl
            (DatamapperPersistenceStrategy strategy) {
        this.strategy = strategy;
    }

    /**
     * Store the referer.
     */
    public void saveReferer(RefererData referer) throws RollerException {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    /**
     * Remove a single referer.
     */
    public void removeReferer(RefererData referer) throws RollerException {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    /**
     * Clear referrer dayhits and remove referrers without excerpts.
     */
    public void clearReferrers() throws RollerException {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    /**
     * Clear referrer dayhits and remove referrers without excerpts.
     */
    public void clearReferrers(WebsiteData website) throws RollerException {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    /**
     * Retrieve referer by id.
     */
    public RefererData getReferer(String id) throws RollerException {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    /**
     * Get all referers for specified weblog.
     * @param weblog
     * @return List of type RefererData
     */
    public List getReferers(WebsiteData weblog) throws RollerException {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    /**
     * Get all referers for specified user that were made today.
     * @param userName Name of user.
     * @return List of type RefererData
     */
    public List getTodaysReferers(WebsiteData website) throws RollerException {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    /**
     * Get referers for a specified date.
     * @param userName Name of user.
     * @param date YYYYMMDD format of day's date.
     * @return List of type RefererData.
     * @throws org.apache.roller.RollerException
     */
    public List getReferersToDate(WebsiteData website, String date)
            throws RollerException {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    /**
     * Get most popular websites based on referer day hits.
     * @param offset Offset into results (for paging)
     * @param len Maximum number of results to return (for paging)
     * @return List of WebsiteDisplayData objects.
     */
    public List getDaysPopularWebsites(int offset, int length)
            throws RollerException {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    /**
     * Returns hot weblogs as StatCount objects, in descending order by today's
     * hits.
     * @param sinceDays Restrict to last X days (or -1 for all)
     * @param offset Offset into results (for paging)
     * @param len Maximum number of results to return (for paging)
     * @return List of StatCount objects.
     */
    public List getHotWeblogs(int sinceDays, int offset, int length)
            throws RollerException {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    /**
     * Get referers that refer to a specific weblog entry.
     * @param entryid Weblog entry ID
     * @return List of RefererData objects.
     * @throws org.apache.roller.RollerException
     */
    public List getReferersToEntry(String entryid) throws RollerException {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    /**
     * Get user's day hits
     */
    public int getDayHits(WebsiteData website) throws RollerException {
        return 0;  //To change body of implemented methods use File | Settings | File Templates.
    }

    /**
     * Get user's all-time total hits
     */
    public int getTotalHits(WebsiteData website) throws RollerException {
        return 0;  //To change body of implemented methods use File | Settings | File Templates.
    }

    /**
     * Apply ignoreWord/spam filters to all referers in system.
     */
    public void applyRefererFilters() throws RollerException {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    /**
     * Apply ignoreWord/spam filters to all referers in website.
     */
    public void applyRefererFilters(WebsiteData website)
            throws RollerException {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    /**
     * Process an incoming referer.
     */
    public void processReferrer(String requestUrl, String referrerUrl,
            String weblogHandle, String weblogAnchor, String weblogDateString) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    /**
     * Release all resources held by manager.
     */
    public void release() {
        //To change body of implemented methods use File | Settings | File Templates.
    }
}
