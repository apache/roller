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
package org.apache.roller.business.jdo;

import java.util.List;

import org.apache.roller.RollerException;
import org.apache.roller.business.RefererManagerImpl;
import org.apache.roller.pojos.WebsiteData;

/**
 * @author Dave Johnson
 */
public class JDORefererManagerImpl extends RefererManagerImpl {

    protected List getReferersWithSameTitle(WebsiteData website,
            String requestUrl, String title, String excerpt)
            throws RollerException {
        // TODO Auto-generated method stub
        return null;
    }

    protected List getExistingReferers(WebsiteData website, String dateString,
            String permalink) throws RollerException {
        // TODO Auto-generated method stub
        return null;
    }

    protected List getReferersToWebsite(WebsiteData website, String refererUrl)
            throws RollerException {
        // TODO Auto-generated method stub
        return null;
    }

    protected List getMatchingReferers(WebsiteData website, String requestUrl,
            String refererUrl) throws RollerException {
        // TODO Auto-generated method stub
        return null;
    }

    protected int getHits(WebsiteData website, String type)
            throws RollerException {
        // TODO Auto-generated method stub
        return 0;
    }

    public List getReferers(WebsiteData website) throws RollerException {
        // TODO Auto-generated method stub
        return null;
    }

    public List getTodaysReferers(WebsiteData website) throws RollerException {
        // TODO Auto-generated method stub
        return null;
    }

    public List getReferersToDate(WebsiteData website, String date)
            throws RollerException {
        // TODO Auto-generated method stub
        return null;
    }

    public List getDaysPopularWebsites(int max) throws RollerException {
        // TODO Auto-generated method stub
        return null;
    }

    public List getReferersToEntry(String entryid) throws RollerException {
        // TODO Auto-generated method stub
        return null;
    }

    public void removeReferersForEntry(String entryid) throws RollerException {
        // TODO Auto-generated method stub

    }

    public void applyRefererFilters() throws RollerException {
        // TODO Auto-generated method stub

    }

    public void applyRefererFilters(WebsiteData website) throws RollerException {
        // TODO Auto-generated method stub

    }

    public void clearReferrers() throws RollerException {
        // TODO Auto-generated method stub

    }

    public void clearReferrers(WebsiteData website) throws RollerException {
        // TODO Auto-generated method stub

    }

}