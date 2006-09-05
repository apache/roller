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

import java.util.Date;
import java.util.List;
import java.util.Map;
import org.apache.roller.RollerException;
import org.apache.roller.model.WeblogManager;
import org.apache.roller.pojos.Assoc;
import org.apache.roller.pojos.CommentData;
import org.apache.roller.pojos.WeblogCategoryData;
import org.apache.roller.pojos.WeblogEntryData;
import org.apache.roller.pojos.WebsiteData;

/*
 * DatamapperWeblogManagerImpl.java
 *
 * Created on May 31, 2006, 4:08 PM
 *
 */
public class DatamapperWeblogManagerImpl implements WeblogManager {
    
    /** Creates a new instance of DatamapperWeblogManagerImpl */
    public DatamapperWeblogManagerImpl() {
    }

    public void saveWeblogEntry(WeblogEntryData entry) throws RollerException {
    }

    public void removeWeblogEntry(WeblogEntryData entry) throws RollerException {
    }

    public WeblogEntryData getWeblogEntry(String id) throws RollerException {
    }

    public WeblogEntryData getWeblogEntryByAnchor(WebsiteData website, String anchor) throws RollerException {
    }

    public List getWeblogEntries(WebsiteData website, Date startDate, Date endDate, String catName, String status, String sortBy, int offset, int range) throws RollerException {
    }

    public List getWeblogEntries(WebsiteData website, Date startDate, Date endDate, String catName, String status, String sortBy, Integer maxEntries) throws RollerException {
    }

    public Map getWeblogEntryObjectMap(WebsiteData website, Date startDate, Date endDate, String catName, String status, Integer maxEntries) throws RollerException {
    }

    public Map getWeblogEntryStringMap(WebsiteData website, Date startDate, Date endDate, String catName, String status, Integer maxEntries) throws RollerException {
    }

    public List getWeblogEntries(WeblogCategoryData cat, boolean subcats) throws RollerException {
    }

    public WeblogEntryData getNextEntry(WeblogEntryData current, String catName) throws RollerException {
    }

    public WeblogEntryData getPreviousEntry(WeblogEntryData current, String catName) throws RollerException {
    }

    public List getNextEntries(WeblogEntryData entry, String catName, int maxEntries) throws RollerException {
    }

    public List getPreviousEntries(WeblogEntryData entry, String catName, int maxEntries) throws RollerException {
    }

    public List getWeblogEntriesPinnedToMain(Integer max) throws RollerException {
    }

    public Date getWeblogLastPublishTime(WebsiteData website) throws RollerException {
    }

    public Date getWeblogLastPublishTime(WebsiteData website, String catName) throws RollerException {
    }

    public void saveWeblogCategory(WeblogCategoryData cat) throws RollerException {
    }

    public void moveWeblogCategoryContents(String srcId, String destId) throws RollerException {
    }

    public void removeWeblogCategory(WeblogCategoryData cat) throws RollerException {
    }

    public WeblogCategoryData getWeblogCategory(String id) throws RollerException {
    }

    public WeblogCategoryData getRootWeblogCategory(WebsiteData website) throws RollerException {
    }

    public WeblogCategoryData getWeblogCategoryByPath(WebsiteData website, String categoryPath) throws RollerException {
    }

    public WeblogCategoryData getWeblogCategoryByPath(WebsiteData wd, WeblogCategoryData category, String string) throws RollerException {
    }

    public List getWeblogCategories(WebsiteData website) throws RollerException {
    }

    public List getWeblogCategories(WebsiteData website, boolean includeRoot) throws RollerException {
    }

    public String getPath(WeblogCategoryData category) throws RollerException {
    }

    public Assoc getWeblogCategoryParentAssoc(WeblogCategoryData data) throws RollerException {
    }

    public List getWeblogCategoryChildAssocs(WeblogCategoryData data) throws RollerException {
    }

    public List getAllWeblogCategoryDecscendentAssocs(WeblogCategoryData data) throws RollerException {
    }

    public List getWeblogCategoryAncestorAssocs(WeblogCategoryData data) throws RollerException {
    }

    public void saveComment(CommentData comment) throws RollerException {
    }

    public void removeComment(CommentData comment) throws RollerException {
    }

    public CommentData getComment(String id) throws RollerException {
    }

    public List getComments(WebsiteData website, WeblogEntryData entry, String searchString, Date startDate, Date endDate, Boolean pending, Boolean approved, Boolean spam, boolean reverseChrono, int offset, int length) throws RollerException {
    }

    public String createAnchor(WeblogEntryData data) throws RollerException {
    }

    public boolean isDuplicateWeblogCategoryName(WeblogCategoryData data) throws RollerException {
    }

    public boolean isWeblogCategoryInUse(WeblogCategoryData data) throws RollerException {
    }

    public boolean isDescendentOf(WeblogCategoryData child, WeblogCategoryData ancestor) throws RollerException {
    }

    public String getUrl(WebsiteData website, String contextUrl) throws RollerException {
    }

    public void applyCommentDefaultsToEntries(WebsiteData website) throws RollerException {
    }

    public void release() {
    }
    
}
