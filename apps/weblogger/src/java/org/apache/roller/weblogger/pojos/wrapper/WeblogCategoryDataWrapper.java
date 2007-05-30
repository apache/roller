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

import org.apache.roller.weblogger.pojos.WeblogCategory;


/**
 * Generated wrapper for class: org.apache.roller.weblogger.pojos.WeblogCategory
 */
public class WeblogCategoryDataWrapper {

    // keep a reference to the wrapped pojo
    private WeblogCategory pojo = null;

    // this is private so that we can force the use of the .wrap(pojo) method
    private WeblogCategoryDataWrapper(WeblogCategory toWrap) {
        this.pojo = toWrap;
    }

    // wrap the given pojo if it is not null
    public static WeblogCategoryDataWrapper wrap(WeblogCategory toWrap) {
        if(toWrap != null)
            return new WeblogCategoryDataWrapper(toWrap);

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
     * pojo method tagged with @roller.wrapPojoMethod type="simple"
     *
     * Simply returns the same value that the pojo would have returned.
     */
    public java.lang.String getName()
    {   
        return this.pojo.getName();
    }

    /**
     * pojo method tagged with @roller.wrapPojoMethod type="simple"
     *
     * Simply returns the same value that the pojo would have returned.
     */
    public java.lang.String getDescription()
    {   
        return this.pojo.getDescription();
    }

    /**
     * pojo method tagged with @roller.wrapPojoMethod type="simple"
     *
     * Simply returns the same value that the pojo would have returned.
     */
    public java.lang.String getImage()
    {   
        return this.pojo.getImage();
    }

    /**
     * pojo method tagged with @roller.wrapPojoMethod type="simple"
     *
     * Simply returns the same value that the pojo would have returned.
     */
    public java.lang.String getPath()
    {   
        return this.pojo.getPath();
    }

    /**
     * pojo method tagged with @roller.wrapPojoMethod type="pojo"
     *
     * This method returns another pojo so we need to wrap the returned pojo.
     */
    public org.apache.roller.weblogger.pojos.wrapper.WebsiteDataWrapper getWebsite()
    {
        return org.apache.roller.weblogger.pojos.wrapper.WebsiteDataWrapper.wrap(this.pojo.getWebsite());
    }

    /**
     * pojo method tagged with @roller.wrapPojoMethod type="pojo"
     *
     * This method returns another pojo so we need to wrap the returned pojo.
     */
    public org.apache.roller.weblogger.pojos.wrapper.WeblogCategoryDataWrapper getParent()
    {
        return org.apache.roller.weblogger.pojos.wrapper.WeblogCategoryDataWrapper.wrap(this.pojo.getParent());
    }

    /**
     * pojo method tagged with @roller.wrapPojoMethod type="pojo-collection"
     *
     * This method returns a collection of pojos so we need to wrap
     * each pojo that is part of the collection.
     */
    public java.util.List getWeblogCategories()
    {
        java.util.Set initialCollection = this.pojo.getWeblogCategories();

        // iterate through and wrap
        // we force the use of an ArrayList because it should be good enough to cover
        // for any Collection type we encounter.
        java.util.ArrayList wrappedCollection = new java.util.ArrayList(initialCollection.size());
        java.util.Iterator it = initialCollection.iterator();
        int i = 0;
        while(it.hasNext()) {
            wrappedCollection.add(i, org.apache.roller.weblogger.pojos.wrapper.WeblogCategoryDataWrapper.wrap((org.apache.roller.weblogger.pojos.WeblogCategory) it.next()));
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
    public java.util.List retrieveWeblogEntries(boolean subcats)
        throws org.apache.roller.RollerException
    {
        java.util.List initialCollection = this.pojo.retrieveWeblogEntries(subcats);

        // iterate through and wrap
        // we force the use of an ArrayList because it should be good enough to cover
        // for any Collection type we encounter.
        java.util.ArrayList wrappedCollection = new java.util.ArrayList(initialCollection.size());
        java.util.Iterator it = initialCollection.iterator();
        int i = 0;
        while(it.hasNext()) {
            wrappedCollection.add(i,org.apache.roller.weblogger.pojos.wrapper.WeblogEntryDataWrapper.wrap((org.apache.roller.weblogger.pojos.WeblogEntry) it.next()));
            i++;
        }

        return wrappedCollection;
    }

    /**
     * pojo method tagged with @roller.wrapPojoMethod type="simple"
     *
     * Simply returns the same value that the pojo would have returned.
     */
    public boolean descendentOf(org.apache.roller.weblogger.pojos.WeblogCategory ancestor)
    {   
        return this.pojo.descendentOf(ancestor);
    }

    /**
     * pojo method tagged with @roller.wrapPojoMethod type="simple"
     *
     * Simply returns the same value that the pojo would have returned.
     */
    public boolean isInUse()
    {   
        return this.pojo.isInUse();
    }

    /**
     * this is a special method to access the original pojo
     * we don't really want to do this, but it's necessary
     * because some parts of the rendering process still need the
     * orginal pojo object
     */
    public org.apache.roller.weblogger.pojos.WeblogCategory getPojo() {
        return this.pojo;
    }

}
