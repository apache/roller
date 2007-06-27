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

import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.Set;
import java.util.TimeZone;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.roller.weblogger.WebloggerException;
import org.apache.roller.weblogger.business.WebloggerFactory;
import org.apache.roller.weblogger.business.WeblogManager;
import org.apache.roller.weblogger.pojos.WeblogEntryAttribute;
import org.apache.roller.weblogger.pojos.WeblogCategory;
import org.apache.roller.weblogger.pojos.WeblogEntry;


/**
 * Bean for managing entry data.
 */
public class EntryBean {
    
    private static Log log = LogFactory.getLog(EntryBean.class);
    
    private String id = null;
    private String title = null;
    private String locale = null;
    private String categoryId = null;
    private String tagsString = null;
    private String summary = null;
    private String text = null;
    private String status = null;
    
    private String[] plugins = null;
    private String dateString = null;
    private int hours = 0;
    private int minutes = 0;
    private int seconds = 0;
    private boolean allowComments = true;
    private Integer commentDays = new Integer(0);
    private boolean rightToLeft = false;
    private boolean pinnedToMain = false;
    private String enclosureURL = null;
    
    
    public String getId() {
        return this.id;
    }
    
    public void setId( String id ) {
        this.id = id;
    }
    
    public String getTitle() {
        return this.title;
    }
    
    public void setTitle( String title ) {
        this.title = title;
    }
    
    public String getSummary() {
        return this.summary;
    }
    
    public void setSummary( String summary ) {
        this.summary = summary;
    }
    
    public String getText() {
        return this.text;
    }
    
    public void setText( String text ) {
        this.text = text;
    }
    
    public String getStatus() {
        return this.status;
    }
    
    public void setStatus( String status ) {
        this.status = status;
    }
    
    public String getLocale() {
        return this.locale;
    }
    
    public void setLocale( String locale ) {
        this.locale = locale;
    }
    
    public String getTagsAsString() {
        return this.tagsString;
    }
    
    public void setTagsAsString( String tagsAsString ) {
        this.tagsString = tagsAsString;
    }
    
    public String getCategoryId() {
        return categoryId;
    }
    
    public void setCategoryId(String categoryId) {
        this.categoryId = categoryId;
    }
    
    
    public String[] getPlugins() {
        return this.plugins;
    }
    
    public void setPlugins(String[] plugins ) {
        this.plugins = plugins;
    }
    
    public String getDateString() {
        return dateString;
    }
    
    public void setDateString(String date) {
        this.dateString = date;
    }
    
    public int getHours() {
        return hours;
    }
    
    public void setHours(int hours) {
        this.hours = hours;
    }
    
    public int getMinutes() {
        return minutes;
    }
    
    public void setMinutes(int minutes) {
        this.minutes = minutes;
    }
    
    public int getSeconds() {
        return seconds;
    }
    
    public void setSeconds(int seconds) {
        this.seconds = seconds;
    }
    
    public boolean getAllowComments() {
        return this.allowComments;
    }
    
    public void setAllowComments( boolean allowComments ) {
        this.allowComments = allowComments;
    }
    
    public Integer getCommentDays() {
        return this.commentDays;
    }
    
    public void setCommentDays(Integer commentDays) {
        this.commentDays = commentDays;
    }
    
    public boolean getRightToLeft() {
        return this.rightToLeft;
    }
    
    public void setRightToLeft( boolean rightToLeft ) {
        this.rightToLeft = rightToLeft;
    }
    
    public boolean getPinnedToMain() {
        return this.pinnedToMain;
    }
    
    public void setPinnedToMain( boolean pinnedToMain ) {
        this.pinnedToMain = pinnedToMain;
    }
    
    public String getEnclosureURL() {
        return enclosureURL;
    }
    
    public void setEnclosureURL(String trackbackUrl) {
        this.enclosureURL = trackbackUrl;
    }
    
    
    // a convenient way to get the final pubtime of the entry
    public Timestamp getPubTime(Locale locale, TimeZone timezone) {
        
        Timestamp pubtime = null;
        
        if(!StringUtils.isEmpty(getDateString())) try {
            log.debug("pubtime vals are "+getDateString()+", "+getHours()+", "+getMinutes()+", "+getSeconds());
            
            // first convert the specified date string into an actual Date obj
            // TODO: at some point this date conversion should be locale sensitive,
            // however at this point our calendar widget does not take into account
            // locales and only operates in the standard English US locale.
            DateFormat df = DateFormat.getDateInstance(DateFormat.SHORT);
            Date newDate = df.parse(getDateString());
            
            log.debug("dateString yields date - "+newDate);
            
            // Now handle the time from the hour, minute and second combos
            Calendar cal = Calendar.getInstance(locale);
            cal.setTime(newDate);
            cal.setTimeZone(timezone);
            cal.set(Calendar.HOUR_OF_DAY, getHours());
            cal.set(Calendar.MINUTE, getMinutes());
            cal.set(Calendar.SECOND, getSeconds());
            pubtime = new Timestamp(cal.getTimeInMillis());
            
            log.debug("pubtime is "+pubtime);
        } catch(Exception e) {
            log.error("Error calculating pubtime", e);
        }
        
        return pubtime;
    }
    
    public boolean isDraft() {
        return status.equals(WeblogEntry.DRAFT);
    }
    
    public boolean isPending() {
        return status.equals(WeblogEntry.PENDING);
    }
    
    public boolean isPublished() {
        return status.equals(WeblogEntry.PUBLISHED);
    }
    
    public boolean isScheduled() {
        return status.equals(WeblogEntry.SCHEDULED);
    }
    
    public void copyTo(WeblogEntry entry) throws WebloggerException {
        
        entry.setTitle(getTitle());
        entry.setStatus(getStatus());
        entry.setLocale(getLocale());
        entry.setSummary(getSummary());
        entry.setText(getText());
        entry.setTagsAsString(getTagsAsString());
        
        // figure out the category selected
        if (getCategoryId() != null) {
            WeblogCategory cat = null;
            try {
                WeblogManager wmgr = WebloggerFactory.getWeblogger().getWeblogManager();
                cat = wmgr.getWeblogCategory(getCategoryId());
            } catch (WebloggerException ex) {
                log.error("Error getting category by id", ex);
            }
            
            if(cat == null) {
                throw new WebloggerException("Category could not be found - "+getCategoryId());
            } else if(!entry.getWebsite().equals(cat.getWebsite())) {
                throw new WebloggerException("Illegal category, not owned by action weblog");
            } else {
                entry.setCategory(cat);
            }
        } else {
            throw new WebloggerException("No category specified");
        }
        
        // join values from all plugins into a single string
        entry.setPlugins(StringUtils.join(getPlugins(),","));
        
        // comment settings & right-to-left option
        entry.setAllowComments(getAllowComments());
        entry.setCommentDays(getCommentDays());
        entry.setRightToLeft(getRightToLeft());
        
        // NOTE: pubtime and pinned to main attributes are set in action
    }
    
    
    /**
     * Copy values from WeblogEntryData to this Form.
     */
    public void copyFrom(WeblogEntry entry, Locale locale) {
        
        setId(entry.getId());
        setTitle(entry.getTitle());
        setLocale(entry.getLocale());
        setStatus(entry.getStatus());
        setSummary(entry.getSummary());
        setText(entry.getText());
        setCategoryId(entry.getCategory().getId());
        setTagsAsString(entry.getTagsAsString());
        
        // init plugins values
        if(entry.getPlugins() != null) {
            setPlugins(StringUtils.split(entry.getPlugins(), ","));
        }
        
        // init pubtime values
        if(entry.getPubTime() != null) {
            log.debug("entry pubtime is "+entry.getPubTime());
            
            //Calendar cal = Calendar.getInstance(locale);
            Calendar cal = Calendar.getInstance();
            cal.setTime(entry.getPubTime());
            cal.setTimeZone(entry.getWebsite().getTimeZoneInstance());
            
            setHours(cal.get(Calendar.HOUR_OF_DAY));
            setMinutes(cal.get(Calendar.MINUTE));
            setSeconds(cal.get(Calendar.SECOND));
            
            // TODO: at some point this date conversion should be locale sensitive,
            // however at this point our calendar widget does not take into account
            // locales and only operates in the standard English US locale.
            DateFormat df = DateFormat.getDateInstance(DateFormat.SHORT);
            df.setTimeZone(entry.getWebsite().getTimeZoneInstance());
            setDateString(df.format(entry.getPubTime()));
            
            log.debug("pubtime vals are "+getDateString()+", "+getHours()+", "+getMinutes()+", "+getSeconds());
        }
        
        setAllowComments(entry.getAllowComments());
        setCommentDays(entry.getCommentDays());
        setRightToLeft(entry.getRightToLeft());
        setPinnedToMain(entry.getPinnedToMain());
        
        // enclosure url, if it exists
        Set<WeblogEntryAttribute> attrs = entry.getEntryAttributes();
        if(attrs != null && attrs.size() > 0) {
            for(WeblogEntryAttribute attr : attrs) {
                if("att_mediacast_url".equals(attr.getName())) {
                    setEnclosureURL(attr.getValue());
                }
            }
        }
    }
    
    
    public String toString() {
        StringBuffer buf = new StringBuffer();
        
        //title,locale,catId,tags,text,summary,dateString,status,comments,plugins
        buf.append("title = ").append(getTitle()).append("\n");
        buf.append("locale = ").append(getLocale()).append("\n");
        buf.append("status = ").append(getStatus()).append("\n");
        buf.append("catId = ").append(getCategoryId()).append("\n");
        buf.append("tags = ").append(getTagsAsString()).append("\n");
        buf.append("date = ").append(getDateString()).append("\n");
        buf.append("hours = ").append(getHours()).append("\n");
        buf.append("minutes = ").append(getMinutes()).append("\n");
        buf.append("seconds = ").append(getSeconds()).append("\n");
        buf.append("txt size = ").append(getText()).append("\n");
        buf.append("summary size = ").append(getSummary()).append("\n");
        buf.append("comments = ").append(getAllowComments()).append("\n");
        buf.append("commentDays = ").append(getCommentDays()).append("\n");
        buf.append("plugins = ").append(getPlugins()).append("\n");
        
        return buf.toString();
    }
    
}
