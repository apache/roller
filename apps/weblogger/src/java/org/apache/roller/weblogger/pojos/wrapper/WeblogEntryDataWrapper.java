/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  The ASF licenses this file to You
 * under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.  For additional information regarding
 * copyright in this work, please see the NOTICE file in the top level
 * directory of this distribution.
 */

package org.apache.roller.weblogger.pojos.wrapper;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import org.apache.roller.weblogger.pojos.WeblogCategory;
import org.apache.roller.weblogger.pojos.WeblogEntry;
import org.apache.roller.weblogger.pojos.WeblogEntryAttribute;
import org.apache.roller.weblogger.pojos.WeblogEntryComment;
import org.apache.roller.weblogger.pojos.WeblogEntryTag;
import org.apache.roller.weblogger.pojos.WeblogReferrer;


/**
 * Pojo safety wrapper for WeblogEntry objects.
 */
public class WeblogEntryDataWrapper {
    
    // keep a reference to the wrapped pojo
    private final WeblogEntry pojo;
    
    // this is private so that we can force the use of the .wrap(pojo) method
    private WeblogEntryDataWrapper(WeblogEntry toWrap) {
        this.pojo = toWrap;
    }
    
    
    // wrap the given pojo if it is not null
    public static WeblogEntryDataWrapper wrap(WeblogEntry toWrap) {
        if(toWrap != null)
            return new WeblogEntryDataWrapper(toWrap);
        
        return null;
    }
    
    
    public String getId() {
        return this.pojo.getId();
    }
    
    
    public WeblogCategoryDataWrapper getCategory() {
        return WeblogCategoryDataWrapper.wrap(this.pojo.getCategory());
    }
    
    
    public List getCategories() {
        List initialCollection = this.pojo.getCategories();
        
        // iterate through and wrap
        // we force the use of an ArrayList because it should be good enough to cover
        // for any Collection type we encounter.
        ArrayList wrappedCollection = new ArrayList(initialCollection.size());
        Iterator it = initialCollection.iterator();
        int i = 0;
        while(it.hasNext()) {
            wrappedCollection.add(i, WeblogCategoryDataWrapper.wrap((WeblogCategory) it.next()));
            i++;
        }
        
        return wrappedCollection;
    }
    
    
    public WebsiteDataWrapper getWebsite() {
        return WebsiteDataWrapper.wrap(this.pojo.getWebsite());
    }
    
    
    public UserDataWrapper getCreator() {
        return UserDataWrapper.wrap(this.pojo.getCreator());
    }
    
    
    public String getTitle() {
        return this.pojo.getTitle();
    }
    
    
    public String getSummary() {
        return this.pojo.getSummary();
    }
    
    /**
     * pojo method tagged with @roller.wrapPojoMethod type="simple"
     *
     * Simply returns the same value that the pojo would have returned.
     */
    public String getText() {
        return this.pojo.getText();
    }
    
    
    public String getContentType() {
        return this.pojo.getContentType();
    }
    
    
    public String getContentSrc() {
        return this.pojo.getContentSrc();
    }
    
    
    public String getAnchor() {
        return this.pojo.getAnchor();
    }
    
    
    public List getEntryAttributes() {
        Set initialCollection = this.pojo.getEntryAttributes();
        
        // iterate through and wrap
        // we force the use of an ArrayList because it should be good enough to cover
        // for any Collection type we encounter.
        ArrayList wrappedCollection = new ArrayList(initialCollection.size());
        Iterator it = initialCollection.iterator();
        int i = 0;
        while(it.hasNext()) {
            wrappedCollection.add(i,EntryAttributeDataWrapper.wrap((WeblogEntryAttribute) it.next()));
            i++;
        }
        
        return wrappedCollection;
    }
    
    
    public String findEntryAttribute(String name) {
        return this.pojo.findEntryAttribute(name);
    }
    
    
    public Timestamp getPubTime() {
        return this.pojo.getPubTime();
    }
    
    
    public Timestamp getUpdateTime() {
        return this.pojo.getUpdateTime();
    }
    
    
    public String getStatus() {
        return this.pojo.getStatus();
    }
    
    
    public String getLink() {
        return this.pojo.getLink();
    }
    
    
    public String getPlugins() {
        return this.pojo.getPlugins();
    }
    
    
    public Boolean getAllowComments() {
        return this.pojo.getAllowComments();
    }
    
    
    public Integer getCommentDays() {
        return this.pojo.getCommentDays();
    }
    
    
    public Boolean getRightToLeft() {
        return this.pojo.getRightToLeft();
    }
    
    
    public Boolean getPinnedToMain() {
        return this.pojo.getPinnedToMain();
    }
    
    
    public String getLocale() {
        return this.pojo.getLocale();
    }
    
    
    public List getTags() {
        Set initialCollection = this.pojo.getTags();
        
        // iterate through and wrap
        // we force the use of an ArrayList because it should be good enough to cover
        // for any Collection type we encounter.
        ArrayList wrappedCollection = new ArrayList(initialCollection.size());
        Iterator it = initialCollection.iterator();
        int i = 0;
        while(it.hasNext()) {
            wrappedCollection.add(i, WeblogEntryTagDataWrapper.wrap((WeblogEntryTag) it.next()));
            i++;
        }
        
        return wrappedCollection;
    }
    
    
    public String getTagsAsString() {
        return this.pojo.getTagsAsString();
    }
    
    
    public boolean getCommentsStillAllowed() {
        return this.pojo.getCommentsStillAllowed();
    }
    
    
    public String formatPubTime(String pattern) {
        return this.pojo.formatPubTime(pattern);
    }
    
    
    public String formatUpdateTime(String pattern) {
        return this.pojo.formatUpdateTime(pattern);
    }
    
    
    public List getComments() {
        List initialCollection = this.pojo.getComments();
        
        // iterate through and wrap
        // we force the use of an ArrayList because it should be good enough to cover
        // for any Collection type we encounter.
        ArrayList wrappedCollection = new ArrayList(initialCollection.size());
        Iterator it = initialCollection.iterator();
        int i = 0;
        while(it.hasNext()) {
            wrappedCollection.add(i,CommentDataWrapper.wrap((WeblogEntryComment) it.next()));
            i++;
        }
        
        return wrappedCollection;
    }
    
    
    public List getComments(boolean ignoreSpam,boolean approvedOnly) {
        List initialCollection = this.pojo.getComments(ignoreSpam,approvedOnly);
        
        // iterate through and wrap
        // we force the use of an ArrayList because it should be good enough to cover
        // for any Collection type we encounter.
        ArrayList wrappedCollection = new ArrayList(initialCollection.size());
        Iterator it = initialCollection.iterator();
        int i = 0;
        while(it.hasNext()) {
            wrappedCollection.add(i,CommentDataWrapper.wrap((WeblogEntryComment) it.next()));
            i++;
        }
        
        return wrappedCollection;
    }
    
    
    public int getCommentCount() {
        return this.pojo.getCommentCount();
    }
    
    
    public List getReferers() {
        List initialCollection = this.pojo.getReferers();
        
        // iterate through and wrap
        // we force the use of an ArrayList because it should be good enough to cover
        // for any Collection type we encounter.
        ArrayList wrappedCollection = new ArrayList(initialCollection.size());
        Iterator it = initialCollection.iterator();
        int i = 0;
        while(it.hasNext()) {
            wrappedCollection.add(i,RefererDataWrapper.wrap((WeblogReferrer) it.next()));
            i++;
        }
        
        return wrappedCollection;
    }
    
    
    public String getPermalink() {
        return this.pojo.getPermalink();
    }
    
    
    public String getPermaLink() {
        return this.pojo.getPermaLink();
    }
    
    
    public String getCommentsLink() {
        return this.pojo.getCommentsLink();
    }
    
    
    public String getDisplayTitle() {
        return this.pojo.getDisplayTitle();
    }
    
    
    public String getRss09xDescription() {
        return this.pojo.getRss09xDescription();
    }
    
    
    public String getRss09xDescription(int maxLength) {
        return this.pojo.getRss09xDescription(maxLength);
    }
    
    
    // TODO: check this method for safety
    public List getPluginsList() {
        return this.pojo.getPluginsList();
    }
    
    
    public String getTransformedText() {
        return this.pojo.getTransformedText();
    }
    
    
    public String getTransformedSummary() {
        return this.pojo.getTransformedSummary();
    }
    
    
    public String displayContent(String readMoreLink) {
        return this.pojo.displayContent(readMoreLink);
    }
    
    
    public String getDisplayContent() {
        return this.pojo.getDisplayContent();
    }
    
    
    /**
     * this is a special method to access the original pojo.
     * we don't really want to do this, but it's necessary
     * because some parts of the rendering process still need the
     * orginal pojo object.
     */
    public WeblogEntry getPojo() {
        return this.pojo;
    }
    
}
