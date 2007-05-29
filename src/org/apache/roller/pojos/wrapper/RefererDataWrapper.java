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

import org.apache.roller.pojos.WeblogReferrer;


/**
 * Generated wrapper for class: org.apache.roller.pojos.RefererData
 */
public class RefererDataWrapper {

    // keep a reference to the wrapped pojo
    private WeblogReferrer pojo = null;

    // this is private so that we can force the use of the .wrap(pojo) method
    private RefererDataWrapper(WeblogReferrer toWrap) {
        this.pojo = toWrap;
    }

    // wrap the given pojo if it is not null
    public static RefererDataWrapper wrap(WeblogReferrer toWrap) {
        if(toWrap != null)
            return new RefererDataWrapper(toWrap);

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
    public org.apache.roller.pojos.wrapper.WebsiteDataWrapper getWebsite()
    {
        return org.apache.roller.pojos.wrapper.WebsiteDataWrapper.wrap(this.pojo.getWebsite());
    }

    /**
     * pojo method tagged with @roller.wrapPojoMethod type="pojo"
     *
     * This method returns another pojo so we need to wrap the returned pojo.
     */
    public org.apache.roller.pojos.wrapper.WeblogEntryDataWrapper getWeblogEntry()
    {
        return org.apache.roller.pojos.wrapper.WeblogEntryDataWrapper.wrap(this.pojo.getWeblogEntry());
    }

    /**
     * pojo method tagged with @roller.wrapPojoMethod type="simple"
     *
     * Simply returns the same value that the pojo would have returned.
     */
    public java.lang.String getDateString()
    {   
        return this.pojo.getDateString();
    }

    /**
     * pojo method tagged with @roller.wrapPojoMethod type="simple"
     *
     * Simply returns the same value that the pojo would have returned.
     */
    public java.lang.String getRefererUrl()
    {   
        return this.pojo.getRefererUrl();
    }

    /**
     * pojo method tagged with @roller.wrapPojoMethod type="simple"
     *
     * Simply returns the same value that the pojo would have returned.
     */
    public java.lang.String getRefererPermalink()
    {   
        return this.pojo.getRefererPermalink();
    }

    /**
     * pojo method tagged with @roller.wrapPojoMethod type="simple"
     *
     * Simply returns the same value that the pojo would have returned.
     */
    public java.lang.String getRequestUrl()
    {   
        return this.pojo.getRequestUrl();
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
    public java.lang.String getExcerpt()
    {   
        return this.pojo.getExcerpt();
    }

    /**
     * pojo method tagged with @roller.wrapPojoMethod type="simple"
     *
     * Simply returns the same value that the pojo would have returned.
     */
    public java.lang.Boolean getVisible()
    {   
        return this.pojo.getVisible();
    }

    /**
     * pojo method tagged with @roller.wrapPojoMethod type="simple"
     *
     * Simply returns the same value that the pojo would have returned.
     */
    public java.lang.Boolean getDuplicate()
    {   
        return this.pojo.getDuplicate();
    }

    /**
     * pojo method tagged with @roller.wrapPojoMethod type="simple"
     *
     * Simply returns the same value that the pojo would have returned.
     */
    public java.lang.Integer getDayHits()
    {   
        return this.pojo.getDayHits();
    }

    /**
     * pojo method tagged with @roller.wrapPojoMethod type="simple"
     *
     * Simply returns the same value that the pojo would have returned.
     */
    public java.lang.Integer getTotalHits()
    {   
        return this.pojo.getTotalHits();
    }

    /**
     * pojo method tagged with @roller.wrapPojoMethod type="simple"
     *
     * Simply returns the same value that the pojo would have returned.
     */
    public java.lang.String getDisplayUrl(int maxWidth,boolean includeHits)
    {   
        return this.pojo.getDisplayUrl(maxWidth,includeHits);
    }

    /**
     * pojo method tagged with @roller.wrapPojoMethod type="simple"
     *
     * Simply returns the same value that the pojo would have returned.
     */
    public java.lang.String getUrl()
    {   
        return this.pojo.getUrl();
    }

    /**
     * pojo method tagged with @roller.wrapPojoMethod type="simple"
     *
     * Simply returns the same value that the pojo would have returned.
     */
    public java.lang.String getDisplayUrl()
    {   
        return this.pojo.getDisplayUrl();
    }

    /**
     * this is a special method to access the original pojo
     * we don't really want to do this, but it's necessary
     * because some parts of the rendering process still need the
     * orginal pojo object
     */
    public org.apache.roller.pojos.WeblogReferrer getPojo() {
        return this.pojo;
    }

}
