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
package org.roller.presentation.weblog.formbeans;

import java.text.DateFormat;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

import javax.servlet.ServletRequest;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.apache.struts.action.ActionMapping;
import org.roller.pojos.CommentData;
import org.roller.util.DateUtil;
import org.roller.util.Utilities;

/**
 * @struts.form name="commentManagementForm"
 * @author Dave Johnson
 */
public class CommentManagementForm
    extends    org.apache.struts.action.ActionForm
    implements java.io.Serializable {
    
    private static Log logger =
        LogFactory.getFactory().getInstance(CommentManagementForm.class);
    
    private String entryid = null;
    private String handle = null;
    
    private String searchString = null;
    private String startDateString;
    private String endDateString;
    
    /** ALL, NO_SPAM or ONLY_SPAM */
    private String  spamString = "ALL";   
    
    /** ALL, NO_APPROVED or ONLY_APPROVED */
    private String  approvedString = "ALL"; 
   
    /** max comments displayed per page */
    private int count = 30; 
    
     /** offset into current query results */
    private int offset = 0;

    /** IDs of comments to mark as spam */
    private String[] spamComments = new String[0];
    
    /** IDs of comments to mark as approved */
    private String[] approvedComments = new String[0];
    
    /** IDs of comments to delete */
    private String[] deleteComments = new String[0];
    
    /** Limit updates to just this set of comma-separated IDs */
    private String ids = null;
    
    
    public void reset(ActionMapping mapping, ServletRequest request) {
        // reset time fields to current time
        Calendar cal = Calendar.getInstance(request.getLocale());
        Date now = new Date();
        cal.setTime(now);        
    
        DateFormat df = DateFormat.getDateInstance(DateFormat.SHORT, request.getLocale());
        setEndDateString(df.format(now));
        
        cal.set(Calendar.DAY_OF_MONTH, 1);
        setStartDateString(df.format(cal.getTime()));
    }    

    public void loadCheckboxes(List comments) {
        ArrayList all = new ArrayList();
        ArrayList approvedList = new ArrayList();
        ArrayList spamList = new ArrayList();
        Iterator it = comments.iterator();
        while (it.hasNext()) {
            CommentData comment = (CommentData)it.next();
            all.add(comment.getId());
            if (comment.getApproved().booleanValue()) {
                approvedList.add(comment.getId());
            }            
            if (comment.getSpam().booleanValue()) {
                spamList.add(comment.getId());
            }
        }
        String[] idArray = (String[])all.toArray(
            new String[all.size()]);
        ids = Utilities.stringArrayToString(idArray,",");
        
        approvedComments = (String[])approvedList.toArray(
            new String[approvedList.size()]);
        spamComments = (String[])spamList.toArray(
            new String[spamList.size()]);
    }
        
    public Date getStartDate(Locale locale) {
        Date startDate = null;
        final DateFormat df =
            DateFormat.getDateInstance(DateFormat.SHORT, locale);
        if (null != getStartDateString() && getStartDateString().trim().length() > 0) {
            try {
                startDate = DateUtil.getStartOfDay(df.parse(getStartDateString()));
            } catch (ParseException e) {
                // what!?! calendar widget handed us a bad date?
                logger.debug("Parsing startDate", e);
            }
        }
        return startDate;
    }

    public Date getEndDate(Locale locale) {
        Date endDate = null;
        final DateFormat df =
            DateFormat.getDateInstance(DateFormat.SHORT, locale);
        if (null != getEndDateString() && getEndDateString().trim().length() > 0) {
            try {
                endDate = DateUtil.getEndOfDay(df.parse(getEndDateString()));
            } catch (ParseException e) {
                // what!?! calendar widget handed us a bad date?
                logger.debug("Parsing endDate", e);
            }
        }
        return endDate;
    }

    public Boolean getSpam() {
        if (spamString.equals("ONLY_SPAM")) {
            return Boolean.TRUE;
        } else if (spamString.equals("NO_SPAM")) {
            return Boolean.FALSE;
        }
        return null;
    }

    public Boolean getPending() {
        if (approvedString.equals("ONLY_PENDING")) {
            return Boolean.TRUE;
        }
        return null;
    }
    
    public Boolean getApproved() {
        if (approvedString.equals("ONLY_APPROVED")) {
            return Boolean.TRUE;
        } else if (approvedString.equals("ONLY_DISAPPROVED")) {
            return Boolean.FALSE;
        }
        return null;
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

    public String getEntryid() {
        return entryid;
    }

    public void setEntryid(String entryid) {
        this.entryid = entryid;
    }

    public String getWeblog() {
        return handle;
    }

    public void setWeblog(String handle) {
        this.handle = handle;
    }
    
    public String[] getDeleteComments() {
        return deleteComments;
    }
    
    public void setDeleteComments(String[] deleteComments) {
        this.deleteComments = deleteComments;
    }
    
    public String[] getSpamComments() {
        return spamComments;
    }
    
    public void setSpamComments(String[] commentsToMarkAsSpam) {
        this.spamComments = commentsToMarkAsSpam;
    }
    
    public String[] getApprovedComments() {
        return approvedComments;
    }
    
    public void setApprovedComments(String[] approvedComments) {
        this.approvedComments = approvedComments;
    }

}
