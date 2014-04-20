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

package org.apache.roller.weblogger.ui.struts2.editor;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import org.apache.commons.lang3.StringUtils;
import org.apache.roller.weblogger.pojos.WeblogEntrySearchCriteria;
import org.apache.roller.weblogger.util.Utilities;


/**
 * A bean for managing entries query data.
 */
public class EntriesBean {
    
    private String endDateString = null;
    private String startDateString = null;
    private String categoryName = null;
    private String tagsAsString = null;
    private String text = null;
    private String status = "ALL";
    private WeblogEntrySearchCriteria.SortBy sortBy = WeblogEntrySearchCriteria.SortBy.UPDATE_TIME;
    private int page = 0;
    
    
    public EntriesBean() {
    }
    
    // convenience method
    public List<String> getTags() {
        if(getTagsAsString() != null) {
            return Utilities.splitStringAsTags(getTagsAsString());
        } else {
            return null;
        }
    }
    
    public Date getStartDate() {
        if(!StringUtils.isEmpty(getStartDateString())) {
            try {
                DateFormat df = new SimpleDateFormat("MM/dd/yy");
                return df.parse(getStartDateString());
            } catch(Exception e) { }
        }
        return null;
    }

    public Date getEndDate() {
        if(!StringUtils.isEmpty(getEndDateString())) {
            try {
                DateFormat df = new SimpleDateFormat("MM/dd/yy");
                return df.parse(getEndDateString());
            } catch(Exception e) { }
        }
        return null;
    }
    
    
    public String getCategoryName() {
        return categoryName;
    }
    
    public void setCategoryName(String categoryId) {
        this.categoryName = categoryId;
    }
    
    public String getTagsAsString() {
        return tagsAsString;
    }
    
    public void setTagsAsString(String tags) {
        this.tagsAsString = tags;
    }
    
    public String getText() {
        return text;
    }
    
    public void setText(String text) {
        this.text = text;
    }
    
    public String getStatus() {
        return status;
    }
    
    public void setStatus(String status) {
        this.status = status;
    }
    
    public WeblogEntrySearchCriteria.SortBy getSortBy() {
        return sortBy;
    }
    
    public int getPage() {
        return page;
    }

    public void setPage(int page) {
        this.page = page;
    }
    
    
    public String toString() {
        StringBuilder buf = new StringBuilder();
        
        buf.append("startDate = ").append(getStartDate()).append("\n");
        buf.append("endDate = ").append(getEndDate()).append("\n");
        buf.append("status = ").append(getStatus()).append("\n");
        buf.append("sortBy = ").append(getSortBy()).append("\n");
        buf.append("catName = ").append(getCategoryName()).append("\n");
        buf.append("tags = ").append(getTagsAsString()).append("\n");
        buf.append("text = ").append(getText()).append("\n");
        buf.append("page = ").append(getPage()).append("\n");
        
        return buf.toString();
    }

    public String getEndDateString() {
        return endDateString;
    }

    public void setEndDateString(String endDateString) {
        this.endDateString = endDateString;
    }

    public String getStartDateString() {
        return startDateString;
    }

    public void setStartDateString(String startDateString) {
        this.startDateString = startDateString;
    }
    
}
