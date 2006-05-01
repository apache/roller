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
package org.roller.business.jdo;

import java.util.Date;
import java.util.List;

import org.roller.RollerException;
import org.roller.business.PersistenceStrategy;
import org.roller.business.WeblogManagerImpl;
import org.roller.pojos.Assoc;
import org.roller.pojos.WeblogCategoryData;
import org.roller.pojos.WeblogEntryData;
import org.roller.pojos.WebsiteData;

/**
 * @author Dave Johnson
 */
public class JDOWeblogManagerImpl extends WeblogManagerImpl {

    public JDOWeblogManagerImpl(PersistenceStrategy strategy) {
        super(strategy);
        // TODO Auto-generated constructor stub
    }

    public List getWeblogEntries(WebsiteData website, Date startDate,
            Date endDate, String catName, String status, Integer maxEntries,
            Boolean pinned) throws RollerException {

        return null;
    }

    public List getNextPrevEntries(WeblogEntryData current, String catName,
            int maxEntries, boolean next) throws RollerException {

        return null;
    }

    public void moveWeblogCategoryContents(String srcId, String destId)
            throws RollerException {

    }

    public List getWeblogCategories(WebsiteData website) throws RollerException {

        return null;
    }

    public List getWeblogCategories(WebsiteData website, boolean includeRoot)
            throws RollerException {

        return null;
    }

    public WeblogCategoryData getRootWeblogCategory(WebsiteData website)
            throws RollerException {

        return null;
    }

    public List getComments(WebsiteData website, WeblogEntryData entry,
            String searchString, Date startDate, Date endDate, Boolean pending,
            Boolean approved, Boolean spam, boolean reverseChrono, int offset,
            int length) throws RollerException {

        return null;
    }

    public List retrieveWeblogEntries(WeblogCategoryData cat, boolean subcats)
            throws RollerException {

        return null;
    }

    public WeblogEntryData getWeblogEntryByAnchor(WebsiteData website,
            String anchor) throws RollerException {

        return null;
    }

    public Date getWeblogLastPublishTime(WebsiteData website, String catName)
            throws RollerException {

        return null;
    }

    public void removeWeblogEntryContents(WeblogEntryData data)
            throws RollerException {

    }

    public String createAnchor(WeblogEntryData data) throws RollerException {

        return null;
    }

    public boolean isDuplicateWeblogCategoryName(WeblogCategoryData data)
            throws RollerException {

        return false;
    }

    public boolean isWeblogCategoryInUse(WeblogCategoryData data)
            throws RollerException {

        return false;
    }

    public boolean isDescendentOf(WeblogCategoryData child,
            WeblogCategoryData ancestor) throws RollerException {

        return false;
    }

    public Assoc getWeblogCategoryParentAssoc(WeblogCategoryData data)
            throws RollerException {

        return null;
    }

    public List getWeblogCategoryChildAssocs(WeblogCategoryData data)
            throws RollerException {

        return null;
    }

    public List getAllWeblogCategoryDecscendentAssocs(WeblogCategoryData data)
            throws RollerException {

        return null;
    }

    public List getWeblogCategoryAncestorAssocs(WeblogCategoryData data)
            throws RollerException {

        return null;
    }

}