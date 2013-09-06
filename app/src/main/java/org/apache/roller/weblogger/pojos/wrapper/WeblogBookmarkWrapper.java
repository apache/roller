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
 * Pojo safety wrapper for WeblogBookmark object.
 */
public final class WeblogBookmarkWrapper {
    
    // keep a reference to the wrapped pojo
    private final WeblogBookmark pojo;
    
    // this is private so that we can force the use of the .wrap(pojo) method
    private WeblogBookmarkWrapper(WeblogBookmark toWrap) {
        this.pojo = toWrap;
    }
    
    
    // wrap the given pojo if it is not null
    public static WeblogBookmarkWrapper wrap(WeblogBookmark toWrap) {
        if(toWrap != null) {
            return new WeblogBookmarkWrapper(toWrap);
        }
        
        return null;
    }
    
    
    public String getId() {
        return this.pojo.getId();
    }
    
    
    public String getName() {
        return this.pojo.getName();
    }
    
    
    public String getDescription() {
        return this.pojo.getDescription();
    }
    
    
    public String getUrl() {
        return this.pojo.getUrl();
    }
    
    
    public Integer getWeight() {
        return this.pojo.getWeight();
    }
    
    
    public Integer getPriority() {
        return this.pojo.getPriority();
    }
    
    
    public String getImage() {
        return this.pojo.getImage();
    }
    
    
    public String getFeedUrl() {
        return this.pojo.getFeedUrl();
    }
    
    
    public WeblogBookmarkFolderWrapper getFolder() {
        return WeblogBookmarkFolderWrapper.wrap(this.pojo.getFolder());
    }
    
}
