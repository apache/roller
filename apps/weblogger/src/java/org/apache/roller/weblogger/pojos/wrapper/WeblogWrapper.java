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

import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.TimeZone;
import org.apache.roller.weblogger.WebloggerException;
import org.apache.roller.weblogger.business.WebloggerFactory;
import org.apache.roller.weblogger.pojos.ThemeTemplate;
import org.apache.roller.weblogger.pojos.Weblog;
import org.apache.roller.weblogger.pojos.WeblogCategory;
import org.apache.roller.weblogger.pojos.WeblogEntry;
import org.apache.roller.weblogger.pojos.WeblogEntryComment;
import org.apache.roller.weblogger.pojos.WeblogReferrer;

/**
 * Pojo safety wrapper for Weblog objects.
 */
public class WeblogWrapper {
    
    // keep a reference to the wrapped pojo
    private final Weblog pojo;
    
    // this is private so that we can force the use of the .wrap(pojo) method
    private WeblogWrapper(Weblog toWrap) {
        this.pojo = toWrap;
    }
    
    
    // wrap the given pojo if it is not null
    public static WeblogWrapper wrap(Weblog toWrap) {
        if(toWrap != null)
            return new WeblogWrapper(toWrap);
        
        return null;
    }
    
    
    public ThemeTemplateWrapper getPageByAction(String action)
            throws WebloggerException {
        return ThemeTemplateWrapper.wrap(this.pojo.getTheme().getTemplateByAction(action));
    }
    
    
    public ThemeTemplateWrapper getPageByName(String name)
            throws WebloggerException {
        return ThemeTemplateWrapper.wrap(this.pojo.getTheme().getTemplateByName(name));
    }
    
    
    public ThemeTemplateWrapper getPageByLink(String link)
            throws WebloggerException {
        return ThemeTemplateWrapper.wrap(this.pojo.getTheme().getTemplateByLink(link));
    }
    
    
    public List getPages() throws WebloggerException {
        
        List initialCollection = this.pojo.getTheme().getTemplates();
        
        // iterate through and wrap
        // we force the use of an ArrayList because it should be good enough to cover
        // for any Collection type we encounter.
        ArrayList wrappedCollection = new ArrayList(initialCollection.size());
        Iterator it = initialCollection.iterator();
        int i = 0;
        while(it.hasNext()) {
            wrappedCollection.add(i,ThemeTemplateWrapper.wrap((ThemeTemplate) it.next()));
            i++;
        }
        
        return wrappedCollection;
    }
    
    
    public String getId() {
        return this.pojo.getId();
    }
    
    
    public String getHandle() {
        return this.pojo.getHandle();
    }
    
    
    public String getName() {
        return this.pojo.getName();
    }
    
    
    public String getDescription() {
        return this.pojo.getDescription();
    }
    
    
    public UserWrapper getCreator() {
        return UserWrapper.wrap(this.pojo.getCreator());
    }
    
    
    public String getDefaultPageId() {
        return this.pojo.getDefaultPageId();
    }
    
    
    public String getWeblogDayPageId() {
        return this.pojo.getWeblogDayPageId();
    }
    
    
    public Boolean getEnableBloggerApi() {
        return this.pojo.getEnableBloggerApi();
    }
    
    
    public WeblogCategoryWrapper getBloggerCategory() {
        return WeblogCategoryWrapper.wrap(this.pojo.getBloggerCategory());
    }
    
    
    public WeblogCategoryWrapper getDefaultCategory() {
        return WeblogCategoryWrapper.wrap(this.pojo.getDefaultCategory());
    }
    
    
    public String getEditorPage() {
        return this.pojo.getEditorPage();
    }
    
    
    public String getBlacklist() {
        return this.pojo.getBlacklist();
    }
    
    
    public Boolean getAllowComments() {
        return this.pojo.getAllowComments();
    }
    
    
    public Boolean getDefaultAllowComments() {
        return this.pojo.getDefaultAllowComments();
    }
    
    
    public int getDefaultCommentDays() {
        return this.pojo.getDefaultCommentDays();
    }
    
    
    public Boolean getModerateComments() {
        return this.pojo.getModerateComments();
    }
    
    
    public Boolean getEmailComments() {
        return this.pojo.getEmailComments();
    }
    
    
    public String getEmailFromAddress() {
        return this.pojo.getEmailFromAddress();
    }
    
    
    public String getEmailAddress() {
        return this.pojo.getEmailAddress();
    }
    
    
    public String getEditorTheme() {
        return this.pojo.getEditorTheme();
    }
    
    
    public String getLocale() {
        return this.pojo.getLocale();
    }
    
    
    public String getTimeZone() {
        return this.pojo.getTimeZone();
    }
    
    
    public Date getDateCreated() {
        return this.pojo.getDateCreated();
    }
    
    
    public String getDefaultPlugins() {
        return this.pojo.getDefaultPlugins();
    }
    
    
    public Locale getLocaleInstance() {
        return this.pojo.getLocaleInstance();
    }
    
    
    public TimeZone getTimeZoneInstance() {
        return this.pojo.getTimeZoneInstance();
    }
    
    
    public int getEntryDisplayCount() {
        return this.pojo.getEntryDisplayCount();
    }
    
    
    public Boolean getEnabled() {
        return this.pojo.getEnabled();
    }
    
    
    public Boolean getActive() {
        return this.pojo.getActive();
    }
    
    
    public Date getLastModified() {
        return this.pojo.getLastModified();
    }
    
    
    public boolean isEnableMultiLang() {
        return this.pojo.isEnableMultiLang();
    }
    
    
    public boolean isShowAllLangs() {
        return this.pojo.isShowAllLangs();
    }
    
    
    public String getStylesheet() throws WebloggerException {
        // custom stylesheet comes from the weblog theme
        if(this.pojo.getTheme().getStylesheet() != null) {
            return WebloggerFactory.getWeblogger().getUrlStrategy().getWeblogPageURL(this.pojo, null, this.pojo.getTheme().getStylesheet().getLink(), null, null, null, null, 0, false);
        }
        return null;
    }
    
    
    /**
     * Get path to weblog icon image if defined.
     *
     * This method is somewhat smart in the sense that it will check the entered
     * icon value and if it is a full url then it will be left alone, but if it
     * is a relative path to a file in the weblog's uploads section then it will
     * build the full url to that resource and return it.
     */
    public String getIcon() {
        
        String iconPath = this.pojo.getIconPath();
        if(iconPath == null) {
            return null;
        }
        
        if(iconPath.startsWith("http") || iconPath.startsWith("/")) {
            // if icon path is a relative path then assume it's a weblog resource
            return iconPath;
        } else {
            // otherwise it's just a plain old url
            return WebloggerFactory.getWeblogger().getUrlStrategy().getWeblogResourceURL(this.pojo, iconPath, false);
        }
        
    }
    
    
    public String getAbout() {
        return this.pojo.getAbout();
    }
    
    
    
    public String getURL() {
        return this.pojo.getURL();
    }
    
    
    public String getAbsoluteURL() {
        return this.pojo.getAbsoluteURL();
    }
    
    
    public WeblogEntryWrapper getWeblogEntry(String anchor) {
        return WeblogEntryWrapper.wrap(this.pojo.getWeblogEntry(anchor));
    }
    
    
    public List getWeblogCategories() {
        Set initialCollection = this.pojo.getWeblogCategories();
        
        // iterate through and wrap
        // we force the use of an ArrayList because it should be good enough to cover
        // for any Collection type we encounter.
        ArrayList wrappedCollection = new ArrayList(initialCollection.size());
        Iterator it = initialCollection.iterator();
        int i = 0;
        while(it.hasNext()) {
            wrappedCollection.add(i,WeblogCategoryWrapper.wrap((WeblogCategory) it.next()));
            i++;
        }
        
        return wrappedCollection;
    }
    
    
    public List getWeblogCategories(String categoryPath) {
        Set initialCollection = this.pojo.getWeblogCategories(categoryPath);
        
        // iterate through and wrap
        // we force the use of an ArrayList because it should be good enough to cover
        // for any Collection type we encounter.
        ArrayList wrappedCollection = new ArrayList(initialCollection.size());
        Iterator it = initialCollection.iterator();
        int i = 0;
        while(it.hasNext()) {
            wrappedCollection.add(i,WeblogCategoryWrapper.wrap((WeblogCategory) it.next()));
            i++;
        }
        
        return wrappedCollection;
    }
    
    
    public WeblogCategoryWrapper getWeblogCategory(String categoryPath) {
        return WeblogCategoryWrapper.wrap(this.pojo.getWeblogCategory(categoryPath));
    }
    
    
    public List getRecentWeblogEntries(String cat,int length) {
        List initialCollection = this.pojo.getRecentWeblogEntries(cat,length);
        
        // iterate through and wrap
        // we force the use of an ArrayList because it should be good enough to cover
        // for any Collection type we encounter.
        ArrayList wrappedCollection = new ArrayList(initialCollection.size());
        Iterator it = initialCollection.iterator();
        int i = 0;
        while(it.hasNext()) {
            wrappedCollection.add(i,WeblogEntryWrapper.wrap((WeblogEntry) it.next()));
            i++;
        }
        
        return wrappedCollection;
    }
    
    
    public List getRecentWeblogEntriesByTag(String tag,int length) {
        List initialCollection = this.pojo.getRecentWeblogEntriesByTag(tag,length);
        
        // iterate through and wrap
        // we force the use of an ArrayList because it should be good enough to cover
        // for any Collection type we encounter.
        ArrayList wrappedCollection = new ArrayList(initialCollection.size());
        Iterator it = initialCollection.iterator();
        int i = 0;
        while(it.hasNext()) {
            wrappedCollection.add(i,WeblogEntryWrapper.wrap((WeblogEntry) it.next()));
            i++;
        }
        
        return wrappedCollection;
    }
    
    
    public List getRecentComments(int length) {
        List initialCollection = this.pojo.getRecentComments(length);
        
        // iterate through and wrap
        // we force the use of an ArrayList because it should be good enough to cover
        // for any Collection type we encounter.
        ArrayList wrappedCollection = new ArrayList(initialCollection.size());
        Iterator it = initialCollection.iterator();
        int i = 0;
        while(it.hasNext()) {
            wrappedCollection.add(i,WeblogEntryCommentWrapper.wrap((WeblogEntryComment) it.next()));
            i++;
        }
        
        return wrappedCollection;
    }
    
    
    public WeblogBookmarkFolderWrapper getBookmarkFolder(String folderName) {
        return WeblogBookmarkFolderWrapper.wrap(this.pojo.getBookmarkFolder(folderName));
    }
    
    
    public List getTodaysReferrers() {
        List initialCollection = this.pojo.getTodaysReferrers();
        
        // iterate through and wrap
        // we force the use of an ArrayList because it should be good enough to cover
        // for any Collection type we encounter.
        ArrayList wrappedCollection = new ArrayList(initialCollection.size());
        Iterator it = initialCollection.iterator();
        int i = 0;
        while(it.hasNext()) {
            wrappedCollection.add(i,WeblogReferrerWrapper.wrap((WeblogReferrer) it.next()));
            i++;
        }
        
        return wrappedCollection;
    }
    
    
    public int getTodaysHits() {
        return this.pojo.getTodaysHits();
    }
    
    // TODO: needs wrapping
    public List getPopularTags(int sinceDays,int length) {
        return this.pojo.getPopularTags(sinceDays,length);
    }
    
    
    public long getCommentCount() {
        return this.pojo.getCommentCount();
    }
    
    
    public long getEntryCount() {
        return this.pojo.getEntryCount();
    }
    
    
    /**
     * this is a special method to access the original pojo
     * we don't really want to do this, but it's necessary
     * because some parts of the rendering process still need the
     * orginal pojo object
     */
    public Weblog getPojo() {
        return this.pojo;
    }
    
}
