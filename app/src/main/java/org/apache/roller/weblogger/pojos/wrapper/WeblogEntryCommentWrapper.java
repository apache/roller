/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.apache.roller.weblogger.pojos.wrapper;

import java.sql.Timestamp;
import org.apache.commons.text.StringEscapeUtils;
import org.apache.roller.weblogger.business.URLStrategy;
import org.apache.roller.weblogger.business.WebloggerFactory;
import org.apache.roller.weblogger.business.plugins.PluginManager;
import org.apache.roller.weblogger.business.plugins.comment.WeblogEntryCommentPlugin;
import org.apache.roller.weblogger.pojos.WeblogEntryComment;
import org.apache.roller.weblogger.pojos.WeblogEntryComment.ApprovalStatus;
import org.apache.roller.weblogger.util.Utilities;


/**
 * Pojo safety wrapper for WeblogEntryComment object.
 */
public final class WeblogEntryCommentWrapper {
    
    // id of the plugin that restricts HTML comments to a subset of HTML
    private static final String HTML_SUBSET_PLUGIN = "HTMLSubset";
    
    // keep a reference to the wrapped pojo
    private final WeblogEntryComment pojo;
    
    // url strategy to use for any url building
    private final URLStrategy urlStrategy;

    private final String safeUrl;
    
    
    // this is private so that we can force the use of the .wrap(pojo) method
    private WeblogEntryCommentWrapper(WeblogEntryComment toWrap, URLStrategy strat) {
        this.pojo = toWrap;
        this.urlStrategy = strat;
        String normalizedUrl = toWrap.getSafeUrl();
        this.safeUrl = normalizedUrl == null
                ? "" : StringEscapeUtils.escapeHtml4(normalizedUrl);
    }
    
    
    // wrap the given pojo if it is not null
    public static WeblogEntryCommentWrapper wrap(WeblogEntryComment toWrap, URLStrategy strat) {
        if(toWrap != null) {
            return new WeblogEntryCommentWrapper(toWrap, strat);
        }
        
        return null;
    }
    
    
    public String getId() {
        return this.pojo.getId();
    }
    
    
    public WeblogEntryWrapper getWeblogEntry() {
        return WeblogEntryWrapper.wrap(this.pojo.getWeblogEntry(), urlStrategy);
    }
    
    
    /**
     * Get the name of the comment writer.
     *
     * Value is always html escaped.
     */
    public String getName() {
        return StringEscapeUtils.escapeHtml4(this.pojo.getName());
    }
    
    
    /**
     * Get the email address of the comment writer, if specified.
     *
     * Value is always html escaped.
     */
    public String getEmail() {
        return StringEscapeUtils.escapeHtml4(this.pojo.getEmail());
    }
    
    
    /**
     * Get the url of the comment writer, if specified.
     *
     * Value is always html escaped.
     */
    public String getUrl() {
        return safeUrl;
    }
    
    
    /**
     * Get the comment contents.
     *
     * Plain text comments are html escaped. HTML comments are reduced to a
     * subset of HTML by the HTMLSubset plugin, and are html escaped when that
     * plugin isn't applied to them. Any configured comment plugins are then
     * applied, and rel="nofollow" is added to links.
     */
    public String getContent() {
        
        String content = this.pojo.getContent();
        PluginManager pmgr = WebloggerFactory.getWeblogger().getPluginManager();
        
        // escape content if content-type is text/plain, or if it is text/html
        // and the HTMLSubset plugin won't restrict it
        if("text/plain".equals(this.pojo.getContentType())
                || ("text/html".equals(this.pojo.getContentType())
                    && !appliesHtmlSubset(pmgr))) {
            content = StringEscapeUtils.escapeHtml4(content);
        }
        
        // apply plugins
        content = pmgr.applyCommentPlugins(this.pojo, content);
        
        // always add rel=nofollow for links
        content = Utilities.addNofollow(content);
        
        return content;
    }
    
    
    /**
     * True if the HTMLSubset plugin is configured and listed on this comment,
     * so applyCommentPlugins() will run it.
     */
    private boolean appliesHtmlSubset(PluginManager pmgr) {
        String plugins = this.pojo.getPlugins();
        if (plugins == null || !plugins.contains(HTML_SUBSET_PLUGIN)) {
            return false;
        }
        for (WeblogEntryCommentPlugin plugin : pmgr.getCommentPlugins()) {
            if (HTML_SUBSET_PLUGIN.equals(plugin.getId())) {
                return true;
            }
        }
        return false;
    }
    
    
    /**
     * Get the time the comment was posted.
     */
    public Timestamp getPostTime() {
        return this.pojo.getPostTime();
    }
    
    
    public ApprovalStatus getStatus() {
        return this.pojo.getStatus();
    }
    
    
    public Boolean getNotify() {
        return this.pojo.getNotify();
    }
    
    
    public String getRemoteHost() {
        return this.pojo.getRemoteHost();
    }
    
    
    /**
     * Get the HTTP referrer of the comment poster.
     *
     * Value is always html escaped.
     */
    public String getReferrer() {
        return StringEscapeUtils.escapeHtml4(this.pojo.getReferrer());
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
