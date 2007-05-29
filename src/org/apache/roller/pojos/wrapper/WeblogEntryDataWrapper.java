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

package org.apache.roller.pojos.wrapper;

import org.apache.roller.pojos.WeblogEntry;


/**
 * Generated wrapper for class: org.apache.roller.pojos.WeblogEntryData
 */
public class WeblogEntryDataWrapper {

    // keep a reference to the wrapped pojo
    private WeblogEntry pojo = null;

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

    /**
     * pojo method tagged with @roller.wrapPojoMethod type="simple"
     *
     * Simply returns the same value that the pojo would have returned.
     */
    public java.lang.String getId()
    {   
        return this.pojo.getId();
    }

    /**
     * pojo method tagged with @roller.wrapPojoMethod type="pojo"
     *
     * This method returns another pojo so we need to wrap the returned pojo.
     */
    public org.apache.roller.pojos.wrapper.WeblogCategoryDataWrapper getCategory()
    {
        return org.apache.roller.pojos.wrapper.WeblogCategoryDataWrapper.wrap(this.pojo.getCategory());
    }

    /**
     * pojo method tagged with @roller.wrapPojoMethod type="pojo-collection"
     *
     * This method returns a collection of pojos so we need to wrap
     * each pojo that is part of the collection.
     */
    public java.util.List getCategories()
    {
        java.util.List initialCollection = this.pojo.getCategories();

        // iterate through and wrap
        // we force the use of an ArrayList because it should be good enough to cover
        // for any Collection type we encounter.
        java.util.ArrayList wrappedCollection = new java.util.ArrayList(initialCollection.size());
        java.util.Iterator it = initialCollection.iterator();
        int i = 0;
        while(it.hasNext()) {
            wrappedCollection.add(i, org.apache.roller.pojos.wrapper.WeblogCategoryDataWrapper.wrap((org.apache.roller.pojos.WeblogCategory) it.next()));
            i++;
        }

        return wrappedCollection;
    }

    /**
     * pojo method tagged with @roller.wrapPojoMethod type="pojo"
     *
     * This method returns another pojo so we need to wrap the returned pojo.
     */
    public org.apache.roller.pojos.wrapper.WebsiteDataWrapper getWebsite()
    {
        return org.apache.roller.pojos.wrapper.WebsiteDataWrapper.wrap(this.pojo.getWebsite());
    }

    /**
     * pojo method tagged with @roller.wrapPojoMethod type="simple"
     *
     * Simply returns the same value that the pojo would have returned.
     */
    public org.apache.roller.pojos.User getCreator()
    {   
        return this.pojo.getCreator();
    }

    /**
     * pojo method tagged with @roller.wrapPojoMethod type="simple"
     *
     * Simply returns the same value that the pojo would have returned.
     */
    public java.lang.String getTitle()
    {   
        return this.pojo.getTitle();
    }

    /**
     * pojo method tagged with @roller.wrapPojoMethod type="simple"
     *
     * Simply returns the same value that the pojo would have returned.
     */
    public java.lang.String getSummary()
    {   
        return this.pojo.getSummary();
    }

    /**
     * pojo method tagged with @roller.wrapPojoMethod type="simple"
     *
     * Simply returns the same value that the pojo would have returned.
     */
    public java.lang.String getText()
    {   
        return this.pojo.getText();
    }

    /**
     * pojo method tagged with @roller.wrapPojoMethod type="simple"
     *
     * Simply returns the same value that the pojo would have returned.
     */
    public java.lang.String getContentType()
    {   
        return this.pojo.getContentType();
    }

    /**
     * pojo method tagged with @roller.wrapPojoMethod type="simple"
     *
     * Simply returns the same value that the pojo would have returned.
     */
    public java.lang.String getContentSrc()
    {   
        return this.pojo.getContentSrc();
    }

    /**
     * pojo method tagged with @roller.wrapPojoMethod type="simple"
     *
     * Simply returns the same value that the pojo would have returned.
     */
    public java.lang.String getAnchor()
    {   
        return this.pojo.getAnchor();
    }

    /**
     * pojo method tagged with @roller.wrapPojoMethod type="pojo-collection"
     *
     * This method returns a collection of pojos so we need to wrap
     * each pojo that is part of the collection.
     */
    public java.util.List getEntryAttributes()
    {
        java.util.Set initialCollection = this.pojo.getEntryAttributes();

        // iterate through and wrap
        // we force the use of an ArrayList because it should be good enough to cover
        // for any Collection type we encounter.
        java.util.ArrayList wrappedCollection = new java.util.ArrayList(initialCollection.size());
        java.util.Iterator it = initialCollection.iterator();
        int i = 0;
        while(it.hasNext()) {
            wrappedCollection.add(i,org.apache.roller.pojos.wrapper.EntryAttributeDataWrapper.wrap((org.apache.roller.pojos.WeblogEntryAttribute) it.next()));
            i++;
        }

        return wrappedCollection;
    }

    /**
     * pojo method tagged with @roller.wrapPojoMethod type="simple"
     *
     * Simply returns the same value that the pojo would have returned.
     */
    public java.lang.String findEntryAttribute(java.lang.String name)
    {   
        return this.pojo.findEntryAttribute(name);
    }

    /**
     * pojo method tagged with @roller.wrapPojoMethod type="simple"
     *
     * Simply returns the same value that the pojo would have returned.
     */
    public java.sql.Timestamp getPubTime()
    {   
        return this.pojo.getPubTime();
    }

    /**
     * pojo method tagged with @roller.wrapPojoMethod type="simple"
     *
     * Simply returns the same value that the pojo would have returned.
     */
    public java.sql.Timestamp getUpdateTime()
    {   
        return this.pojo.getUpdateTime();
    }

    /**
     * pojo method tagged with @roller.wrapPojoMethod type="simple"
     *
     * Simply returns the same value that the pojo would have returned.
     */
    public java.lang.String getStatus()
    {   
        return this.pojo.getStatus();
    }

    /**
     * pojo method tagged with @roller.wrapPojoMethod type="simple"
     *
     * Simply returns the same value that the pojo would have returned.
     */
    public java.lang.String getLink()
    {   
        return this.pojo.getLink();
    }

    /**
     * pojo method tagged with @roller.wrapPojoMethod type="simple"
     *
     * Simply returns the same value that the pojo would have returned.
     */
    public java.lang.String getPlugins()
    {   
        return this.pojo.getPlugins();
    }

    /**
     * pojo method tagged with @roller.wrapPojoMethod type="simple"
     *
     * Simply returns the same value that the pojo would have returned.
     */
    public java.lang.Boolean getAllowComments()
    {   
        return this.pojo.getAllowComments();
    }

    /**
     * pojo method tagged with @roller.wrapPojoMethod type="simple"
     *
     * Simply returns the same value that the pojo would have returned.
     */
    public java.lang.Integer getCommentDays()
    {   
        return this.pojo.getCommentDays();
    }

    /**
     * pojo method tagged with @roller.wrapPojoMethod type="simple"
     *
     * Simply returns the same value that the pojo would have returned.
     */
    public java.lang.Boolean getRightToLeft()
    {   
        return this.pojo.getRightToLeft();
    }

    /**
     * pojo method tagged with @roller.wrapPojoMethod type="simple"
     *
     * Simply returns the same value that the pojo would have returned.
     */
    public java.lang.Boolean getPinnedToMain()
    {   
        return this.pojo.getPinnedToMain();
    }

    /**
     * pojo method tagged with @roller.wrapPojoMethod type="simple"
     *
     * Simply returns the same value that the pojo would have returned.
     */
    public java.lang.String getLocale()
    {   
        return this.pojo.getLocale();
    }

    /**
     * pojo method tagged with @roller.wrapPojoMethod type="pojo-collection"
     *
     * This method returns a collection of pojos so we need to wrap
     * each pojo that is part of the collection.
     */
    public java.util.List getTags()
    {
        java.util.Set initialCollection = this.pojo.getTags();

        // iterate through and wrap
        // we force the use of an ArrayList because it should be good enough to cover
        // for any Collection type we encounter.
        java.util.ArrayList wrappedCollection = new java.util.ArrayList(initialCollection.size());
        java.util.Iterator it = initialCollection.iterator();
        int i = 0;
        while(it.hasNext()) {
            wrappedCollection.add(i, org.apache.roller.pojos.wrapper.WeblogEntryTagDataWrapper.wrap((org.apache.roller.pojos.WeblogEntryTag) it.next()));
            i++;
        }

        return wrappedCollection;
    }

    /**
     * pojo method tagged with @roller.wrapPojoMethod type="simple"
     *
     * Simply returns the same value that the pojo would have returned.
     */
    public java.lang.String getTagsAsString()
    {   
        return this.pojo.getTagsAsString();
    }

    /**
     * pojo method tagged with @roller.wrapPojoMethod type="simple"
     *
     * Simply returns the same value that the pojo would have returned.
     */
    public boolean getCommentsStillAllowed()
    {   
        return this.pojo.getCommentsStillAllowed();
    }

    /**
     * pojo method tagged with @roller.wrapPojoMethod type="simple"
     *
     * Simply returns the same value that the pojo would have returned.
     */
    public java.lang.String formatPubTime(java.lang.String pattern)
    {   
        return this.pojo.formatPubTime(pattern);
    }

    /**
     * pojo method tagged with @roller.wrapPojoMethod type="simple"
     *
     * Simply returns the same value that the pojo would have returned.
     */
    public java.lang.String formatUpdateTime(java.lang.String pattern)
    {   
        return this.pojo.formatUpdateTime(pattern);
    }

    /**
     * pojo method tagged with @roller.wrapPojoMethod type="pojo-collection"
     *
     * This method returns a collection of pojos so we need to wrap
     * each pojo that is part of the collection.
     */
    public java.util.List getComments()
    {
        java.util.List initialCollection = this.pojo.getComments();

        // iterate through and wrap
        // we force the use of an ArrayList because it should be good enough to cover
        // for any Collection type we encounter.
        java.util.ArrayList wrappedCollection = new java.util.ArrayList(initialCollection.size());
        java.util.Iterator it = initialCollection.iterator();
        int i = 0;
        while(it.hasNext()) {
            wrappedCollection.add(i,org.apache.roller.pojos.wrapper.CommentDataWrapper.wrap((org.apache.roller.pojos.WeblogEntryComment) it.next()));
            i++;
        }

        return wrappedCollection;
    }

    /**
     * pojo method tagged with @roller.wrapPojoMethod type="pojo-collection"
     *
     * This method returns a collection of pojos so we need to wrap
     * each pojo that is part of the collection.
     */
    public java.util.List getComments(boolean ignoreSpam,boolean approvedOnly)
    {
        java.util.List initialCollection = this.pojo.getComments(ignoreSpam,approvedOnly);

        // iterate through and wrap
        // we force the use of an ArrayList because it should be good enough to cover
        // for any Collection type we encounter.
        java.util.ArrayList wrappedCollection = new java.util.ArrayList(initialCollection.size());
        java.util.Iterator it = initialCollection.iterator();
        int i = 0;
        while(it.hasNext()) {
            wrappedCollection.add(i,org.apache.roller.pojos.wrapper.CommentDataWrapper.wrap((org.apache.roller.pojos.WeblogEntryComment) it.next()));
            i++;
        }

        return wrappedCollection;
    }

    /**
     * pojo method tagged with @roller.wrapPojoMethod type="simple"
     *
     * Simply returns the same value that the pojo would have returned.
     */
    public int getCommentCount()
    {   
        return this.pojo.getCommentCount();
    }

    /**
     * pojo method tagged with @roller.wrapPojoMethod type="pojo-collection"
     *
     * This method returns a collection of pojos so we need to wrap
     * each pojo that is part of the collection.
     */
    public java.util.List getReferers()
    {
        java.util.List initialCollection = this.pojo.getReferers();

        // iterate through and wrap
        // we force the use of an ArrayList because it should be good enough to cover
        // for any Collection type we encounter.
        java.util.ArrayList wrappedCollection = new java.util.ArrayList(initialCollection.size());
        java.util.Iterator it = initialCollection.iterator();
        int i = 0;
        while(it.hasNext()) {
            wrappedCollection.add(i,org.apache.roller.pojos.wrapper.RefererDataWrapper.wrap((org.apache.roller.pojos.WeblogReferrer) it.next()));
            i++;
        }

        return wrappedCollection;
    }

    /**
     * pojo method tagged with @roller.wrapPojoMethod type="simple"
     *
     * Simply returns the same value that the pojo would have returned.
     */
    public java.lang.String getPermalink()
    {   
        return this.pojo.getPermalink();
    }

    /**
     * pojo method tagged with @roller.wrapPojoMethod type="simple"
     *
     * Simply returns the same value that the pojo would have returned.
     */
    public java.lang.String getPermaLink()
    {   
        return this.pojo.getPermaLink();
    }

    /**
     * pojo method tagged with @roller.wrapPojoMethod type="simple"
     *
     * Simply returns the same value that the pojo would have returned.
     */
    public java.lang.String getCommentsLink()
    {   
        return this.pojo.getCommentsLink();
    }

    /**
     * pojo method tagged with @roller.wrapPojoMethod type="simple"
     *
     * Simply returns the same value that the pojo would have returned.
     */
    public java.lang.String getDisplayTitle()
    {   
        return this.pojo.getDisplayTitle();
    }

    /**
     * pojo method tagged with @roller.wrapPojoMethod type="simple"
     *
     * Simply returns the same value that the pojo would have returned.
     */
    public java.lang.String getRss09xDescription()
    {   
        return this.pojo.getRss09xDescription();
    }

    /**
     * pojo method tagged with @roller.wrapPojoMethod type="simple"
     *
     * Simply returns the same value that the pojo would have returned.
     */
    public java.lang.String getRss09xDescription(int maxLength)
    {   
        return this.pojo.getRss09xDescription(maxLength);
    }

    /**
     * pojo method tagged with @roller.wrapPojoMethod type="simple"
     *
     * Simply returns the same value that the pojo would have returned.
     */
    public java.util.List getPluginsList()
    {   
        return this.pojo.getPluginsList();
    }

    /**
     * pojo method tagged with @roller.wrapPojoMethod type="simple"
     *
     * Simply returns the same value that the pojo would have returned.
     */
    public java.lang.String getTransformedText()
    {   
        return this.pojo.getTransformedText();
    }

    /**
     * pojo method tagged with @roller.wrapPojoMethod type="simple"
     *
     * Simply returns the same value that the pojo would have returned.
     */
    public java.lang.String getTransformedSummary()
    {   
        return this.pojo.getTransformedSummary();
    }

    /**
     * pojo method tagged with @roller.wrapPojoMethod type="simple"
     *
     * Simply returns the same value that the pojo would have returned.
     */
    public java.lang.String displayContent(java.lang.String readMoreLink)
    {   
        return this.pojo.displayContent(readMoreLink);
    }

    /**
     * pojo method tagged with @roller.wrapPojoMethod type="simple"
     *
     * Simply returns the same value that the pojo would have returned.
     */
    public java.lang.String getDisplayContent()
    {   
        return this.pojo.getDisplayContent();
    }

    /**
     * this is a special method to access the original pojo
     * we don't really want to do this, but it's necessary
     * because some parts of the rendering process still need the
     * orginal pojo object
     */
    public org.apache.roller.pojos.WeblogEntry getPojo() {
        return this.pojo;
    }

}
