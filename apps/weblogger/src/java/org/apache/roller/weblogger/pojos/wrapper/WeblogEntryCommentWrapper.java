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

import org.apache.commons.lang.StringEscapeUtils;
import org.apache.roller.weblogger.business.URLStrategy;
import org.apache.roller.weblogger.pojos.WeblogEntryComment;


/**
 * Pojo safety wrapper for WeblogEntryComment object.
 */
public class WeblogEntryCommentWrapper {
    
    // keep a reference to the wrapped pojo
    private final WeblogEntryComment pojo;
    
    // url strategy to use for any url building
    private final URLStrategy urlStrategy;
    
    
    // this is private so that we can force the use of the .wrap(pojo) method
    private WeblogEntryCommentWrapper(WeblogEntryComment toWrap, URLStrategy strat) {
        this.pojo = toWrap;
        this.urlStrategy = strat;
    }
    
    
    // wrap the given pojo if it is not null
    public static WeblogEntryCommentWrapper wrap(WeblogEntryComment toWrap, URLStrategy strat) {
        if(toWrap != null)
            return new WeblogEntryCommentWrapper(toWrap, strat);
        
        return null;
    }
    
    
    public String getId() {
        return this.pojo.getId();
    }
    
    
    public WeblogEntryWrapper getWeblogEntry() {
        return WeblogEntryWrapper.wrap(this.pojo.getWeblogEntry(), urlStrategy);
    }
    
    
    public String getName() {
        return StringEscapeUtils.escapeHtml(this.pojo.getName());
    }
    
    
    public String getEmail() {
        return StringEscapeUtils.escapeHtml(this.pojo.getEmail());
    }
    
    
    public String getUrl() {
        return StringEscapeUtils.escapeHtml(this.pojo.getUrl());
    }
    
    
    public String getContent() {
        return StringEscapeUtils.escapeHtml(this.pojo.getContent());
    }
    
    
    public java.sql.Timestamp getPostTime() {
        return this.pojo.getPostTime();
    }
    
    
    public String getStatus() {
        return this.pojo.getStatus();
    }
    
    
    public Boolean getNotify() {
        return this.pojo.getNotify();
    }
    
    
    public String getRemoteHost() {
        return this.pojo.getRemoteHost();
    }
    
    
    public String getReferrer() {
        return StringEscapeUtils.escapeHtml(this.pojo.getReferrer());
    }
    
    
    public String getUserAgent() {
        return this.pojo.getUserAgent();
    }
    
    
    public Boolean getSpam() {
        return this.pojo.getSpam();
    }
    
    
    public Boolean getPending() {
        return this.pojo.getPending();
    }
    
    
    public Boolean getApproved() {
        return this.pojo.getApproved();
    }
    
    
    public String getTimestamp() {
        return this.pojo.getTimestamp();
    }
    
}
