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

import org.apache.roller.weblogger.pojos.WeblogBookmark;


/**
 * Generated wrapper for class: org.apache.roller.weblogger.pojos.BookmarkData
 */
public class BookmarkDataWrapper {

    // keep a reference to the wrapped pojo
    private WeblogBookmark pojo = null;

    // this is private so that we can force the use of the .wrap(pojo) method
    private BookmarkDataWrapper(WeblogBookmark toWrap) {
        this.pojo = toWrap;
    }

    // wrap the given pojo if it is not null
    public static BookmarkDataWrapper wrap(WeblogBookmark toWrap) {
        if(toWrap != null)
            return new BookmarkDataWrapper(toWrap);

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
    public java.lang.String getUrl()
    {   
        return this.pojo.getUrl();
    }

    /**
     * pojo method tagged with @roller.wrapPojoMethod type="simple"
     *
     * Simply returns the same value that the pojo would have returned.
     */
    public java.lang.Integer getWeight()
    {   
        return this.pojo.getWeight();
    }

    /**
     * pojo method tagged with @roller.wrapPojoMethod type="simple"
     *
     * Simply returns the same value that the pojo would have returned.
     */
    public java.lang.Integer getPriority()
    {   
        return this.pojo.getPriority();
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
    public java.lang.String getFeedUrl()
    {   
        return this.pojo.getFeedUrl();
    }

    /**
     * pojo method tagged with @roller.wrapPojoMethod type="pojo"
     *
     * This method returns another pojo so we need to wrap the returned pojo.
     */
    public org.apache.roller.weblogger.pojos.wrapper.FolderDataWrapper getFolder()
    {
        return org.apache.roller.weblogger.pojos.wrapper.FolderDataWrapper.wrap(this.pojo.getFolder());
    }

    /**
     * this is a special method to access the original pojo
     * we don't really want to do this, but it's necessary
     * because some parts of the rendering process still need the
     * orginal pojo object
     */
    public org.apache.roller.weblogger.pojos.WeblogBookmark getPojo() {
        return this.pojo;
    }

}
