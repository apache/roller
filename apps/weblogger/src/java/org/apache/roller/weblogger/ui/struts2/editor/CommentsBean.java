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
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import org.apache.commons.lang.StringUtils;
import org.apache.roller.weblogger.pojos.WeblogEntryComment;
import org.apache.roller.weblogger.util.Utilities;


/**
 * A bean for managing comments.
 */
public class CommentsBean {
    
    private String entryId = null;
    private String searchString = null;
    private String startDateString = null;
    private String endDateString = null;
    private String spamString = "ALL";
    private String approvedString = "ALL";
    private int page = 0;
    
    private String[] approvedComments = new String[0];
    private String[] spamComments = new String[0];
    private String[] deleteComments = new String[0];
    
    // Limit updates to just this set of comma-separated IDs
    private String ids = null;
    
    
    public void loadCheckboxes(List comments) {
        
        List<String> allComments = new ArrayList();
        List<String> approvedList = new ArrayList();
        List<String> spamList = new ArrayList();
        
        Iterator it = comments.iterator();
        while (it.hasNext()) {
            WeblogEntryComment comment = (WeblogEntryComment)it.next();
            allComments.add(comment.getId());
            
            if(WeblogEntryComment.APPROVED.equals(comment.getStatus())) {
                approvedList.add(comment.getId());
            } else if(WeblogEntryComment.SPAM.equals(comment.getStatus())) {
                spamList.add(comment.getId());
            }
        }
        
        // list of ids we are working on
        String[] idArray = (String[]) allComments.toArray(new String[allComments.size()]);
        setIds(Utilities.stringArrayToString(idArray,","));
        
        // approved ids list
        setApprovedComments((String[])approvedList.toArray(new String[approvedList.size()]));
        
        // spam ids list
        setSpamComments((String[])spamList.toArray(new String[spamList.size()]));
    }
    
    
    public String getStatus() {
        if (approvedString.equals("ONLY_APPROVED")) {
            return WeblogEntryComment.APPROVED;
        } else if (approvedString.equals("ONLY_DISAPPROVED")) {
            return WeblogEntryComment.DISAPPROVED;
        } else if (approvedString.equals("ONLY_PENDING")) {
            return WeblogEntryComment.PENDING;
        } else if (spamString.equals("ONLY_SPAM")) {
            return WeblogEntryComment.SPAM;
        } else if (spamString.equals("NO_SPAM")) {
            // all status' except spam
            // special situation, so this doesn't map to a persisted comment status
            return "ALL_IGNORE_SPAM";
        } else {
            // shows *all* comments, regardless of status
            return null;
        }
    }
    
    public Date getStartDate() {
        if(!StringUtils.isEmpty(getStartDateString())) try {
            DateFormat df = new SimpleDateFormat("MM/dd/yy");
            return df.parse(getStartDateString());
        } catch(Exception e) { }
        return null;
    }

    public Date getEndDate() {
        if(!StringUtils.isEmpty(getEndDateString())) try {
            DateFormat df = new SimpleDateFormat("MM/dd/yy");
            return df.parse(getEndDateString());
        } catch(Exception e) { }
        return null;
    }
    
    
    public String getSpamString() {
        return spamString;
    }
    
    public void setSpamString(String spamString) {
        this.spamString = spamString;
    }
    
    public String getPendingString() {
        return approvedString;
    }
    
    public void setPendingString(String pendingString) {
        this.approvedString = pendingString;
    }
    
    public String getIds() {
        return ids;
    }
    
    public void setIds(String ids) {
        this.ids = ids;
    }
    
    public String getSearchString() {
        return searchString;
    }
    
    public void setSearchString(String searchString) {
        this.searchString = searchString;
    }

    public String[] getApprovedComments() {
        return approvedComments;
    }

    public void setApprovedComments(String[] approvedComments) {
        this.approvedComments = approvedComments;
    }
    
    public String[] getSpamComments() {
        return spamComments;
    }

    public void setSpamComments(String[] spamComments) {
        this.spamComments = spamComments;
    }

    public String[] getDeleteComments() {
        return deleteComments;
    }

    public void setDeleteComments(String[] deleteComments) {
        this.deleteComments = deleteComments;
    }

    public String getApprovedString() {
        return approvedString;
    }

    public void setApprovedString(String approvedString) {
        this.approvedString = approvedString;
    }

    public int getPage() {
        return page;
    }

    public void setPage(int page) {
        this.page = page;
    }

    public String getStartDateString() {
        return startDateString;
    }

    public void setStartDateString(String startDateString) {
        this.startDateString = startDateString;
    }

    public String getEndDateString() {
        return endDateString;
    }

    public void setEndDateString(String endDateString) {
        this.endDateString = endDateString;
    }

    public String getEntryId() {
        return entryId;
    }

    public void setEntryId(String entryId) {
        this.entryId = entryId;
    }
    
}
