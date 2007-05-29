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

package org.apache.roller.ui.struts2.admin;

import java.text.DateFormat;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import org.apache.roller.pojos.WeblogEntryComment;
import org.apache.roller.util.DateUtil;
import org.apache.roller.util.Utilities;


/**
 * A bean for managing comments.
 */
public class GlobalCommentManagementBean {
    
    private String searchString = null;
    private Date startDate = null;
    private Date endDate = null;
    private String spamString = "ALL";
    private String approvedString = "ALL";
    private int count = 30;
    private int offset = 0;
    
    private String[] spamComments = new String[0];
    private String[] deleteComments = new String[0];
    
    // Limit updates to just this set of comma-separated IDs
    private String ids = null;
    
    
    public void loadCheckboxes(List comments) {
        
        List<String> allComments = new ArrayList();
        List<String> spamList = new ArrayList();
        
        Iterator it = comments.iterator();
        while (it.hasNext()) {
            WeblogEntryComment comment = (WeblogEntryComment)it.next();
            allComments.add(comment.getId());
            
            if (WeblogEntryComment.SPAM.equals(comment.getStatus())) {
                spamList.add(comment.getId());
            }
        }
        
        String[] idArray = (String[]) allComments.toArray(new String[allComments.size()]);
        this.setIds(Utilities.stringArrayToString(idArray,","));
        
        spamComments = (String[])spamList.toArray(
            new String[spamList.size()]);
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

    public Date getStartDate() {
        return startDate;
    }

    public void setStartDate(Date startDate) {
        this.startDate = startDate;
    }

    public Date getEndDate() {
        return endDate;
    }

    public void setEndDate(Date endDate) {
        this.endDate = endDate;
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
    
}
