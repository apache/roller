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

    public void saveReferer(RefererData referer) throws RollerException {
        strategy.store(referer);
    }

    public void removeReferer(RefererData referer) throws RollerException {
        strategy.remove(referer);
    }

    public void clearReferrers() throws RollerException {
    }

    public void clearReferrers(WebsiteData website) throws RollerException {
    }

    public RefererData getReferer(String id) throws RollerException {
    }

    public List getReferers(WebsiteData weblog) throws RollerException {
    }

    public List getTodaysReferers(WebsiteData website) throws RollerException {
    }

    public List getReferersToDate(WebsiteData website, String date) throws RollerException {
    }

    public List getDaysPopularWebsites(int max) throws RollerException {
    }

    public List getReferersToEntry(String entryid) throws RollerException {
    }

    public int getDayHits(WebsiteData website) throws RollerException {
    }

    public int getTotalHits(WebsiteData website) throws RollerException {
    }

    public void applyRefererFilters() throws RollerException {
    }

    public void applyRefererFilters(WebsiteData website) throws RollerException {
    }

    public void processReferrer(String requestUrl, String referrerUrl, String weblogHandle, String weblogAnchor, String weblogDateString) {
    }

    public void release() {
    }
    
}
