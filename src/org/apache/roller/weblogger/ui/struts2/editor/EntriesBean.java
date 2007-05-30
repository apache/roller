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

import java.util.Date;
import java.util.List;
import org.apache.roller.weblogger.util.Utilities;


/**
 * A bean for managing entries query data.
 */
public class EntriesBean {
    
    private Date endDate = null;
    private Date startDate = null;
    private String categoryPath = null;
    private String tagsAsString = null;
    private String text = null;
    private String status = "ALL";
    private String sortBy = "updateTime";
    
    /** max entries displayed per page */
    private int count = 30;
    
    /** offset into current query results */
    private int offset = 0;
    
    
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
    
    
    public Date getEndDate() {
        return endDate;
    }
    
    public void setEndDate(Date endDate) {
        this.endDate = endDate;
    }
    
    public Date getStartDate() {
        return startDate;
    }
    
    public void setStartDate(Date startDate) {
        this.startDate = startDate;
    }
    
    public String getCategoryPath() {
        return categoryPath;
    }
    
    public void setCategoryPath(String categoryId) {
        this.categoryPath = categoryId;
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
    
    public String getSortBy() {
        return sortBy;
    }
    
    public void setSortBy(String sortBy) {
        this.sortBy = sortBy;
    }
    
    public int getCount() {
        return count;
    }
    
    public void setCount(int count) {
        this.count = count;
    }
    
    public int getOffset() {
        return offset;
    }
    
    public void setOffset(int offset) {
        this.offset = offset;
    }
    
    
    public String toString() {
        StringBuffer buf = new StringBuffer();
        
        buf.append("startDate = ").append(getStartDate()).append("\n");
        buf.append("endDate = ").append(getEndDate()).append("\n");
        buf.append("status = ").append(getStatus()).append("\n");
        buf.append("sortBy = ").append(getSortBy()).append("\n");
        buf.append("catPath = ").append(getCategoryPath()).append("\n");
        buf.append("tags = ").append(getTagsAsString()).append("\n");
        buf.append("text = ").append(getText()).append("\n");
        buf.append("offset = ").append(getOffset()).append("\n");
        buf.append("count = ").append(getCount()).append("\n");
        
        return buf.toString();
    }
    
}
