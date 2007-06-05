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

import org.apache.roller.weblogger.pojos.WeblogReferrer;


/**
 * Pojo safety wrapper for WeblogReferrer object.
 */
public class WeblogReferrerWrapper {
    
    // keep a reference to the wrapped pojo
    private final WeblogReferrer pojo;
    
    // this is private so that we can force the use of the .wrap(pojo) method
    private WeblogReferrerWrapper(WeblogReferrer toWrap) {
        this.pojo = toWrap;
    }
    
    
    // wrap the given pojo if it is not null
    public static WeblogReferrerWrapper wrap(WeblogReferrer toWrap) {
        if(toWrap != null)
            return new WeblogReferrerWrapper(toWrap);
        
        return null;
    }
    
    
    public String getId() {
        return this.pojo.getId();
    }
    
    
    public WeblogWrapper getWebsite() {
        return WeblogWrapper.wrap(this.pojo.getWebsite());
    }
    
    
    public WeblogEntryWrapper getWeblogEntry() {
        return WeblogEntryWrapper.wrap(this.pojo.getWeblogEntry());
    }
    
    
    public String getDateString() {
        return this.pojo.getDateString();
    }
    
    
    public String getRefererUrl() {
        return this.pojo.getRefererUrl();
    }
    
    
    public String getRefererPermalink() {
        return this.pojo.getRefererPermalink();
    }
    
    
    public String getRequestUrl() {
        return this.pojo.getRequestUrl();
    }
    
    
    public String getTitle() {
        return this.pojo.getTitle();
    }
    
    
    public String getExcerpt() {
        return this.pojo.getExcerpt();
    }
    
    
    public Boolean getVisible() {
        return this.pojo.getVisible();
    }
    
    
    public Boolean getDuplicate() {
        return this.pojo.getDuplicate();
    }
    
    
    public Integer getDayHits() {
        return this.pojo.getDayHits();
    }
    
    
    public Integer getTotalHits() {
        return this.pojo.getTotalHits();
    }
    
    
    public String getDisplayUrl(int maxWidth,boolean includeHits) {
        return this.pojo.getDisplayUrl(maxWidth,includeHits);
    }
    
    
    public String getUrl() {
        return this.pojo.getUrl();
    }
    
    
    public String getDisplayUrl() {
        return this.pojo.getDisplayUrl();
    }
    
}
