/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 *  contributor license agreements.  The ASF licenses this file to You
 * under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.  For additional information regarding
 * copyright in this work, please see the NOTICE file in the top level
 * directory of this distribution.
 */

package org.apache.roller.weblogger.ui.rendering.util;

import org.apache.commons.lang.StringEscapeUtils;
import org.apache.roller.weblogger.pojos.WeblogEntryComment;
import org.apache.roller.weblogger.pojos.wrapper.WeblogEntryCommentWrapper;


/**
 * A simple class to represent the comment form displayed on a weblog entry
 * permalink page.  We use this class to manage the interaction with that form.
 */
public class WeblogEntryCommentForm {
    
    private boolean error = false;
    private String message = null;
    
    private String name = "";
    private String email = "";
    private String url = "";
    private String content = "";
    private boolean notify = false;
    
    private WeblogEntryComment previewComment = null;
    
    
    public WeblogEntryCommentForm() {}
    
    
    public void setPreview(WeblogEntryComment preview) {
        this.previewComment = preview;
        setData(preview);
    }
    
    public void setData(WeblogEntryComment comment) {
        this.name = comment.getName();
        this.email = comment.getEmail();
        this.url = comment.getUrl();
        this.content = comment.getContent();
        this.notify = comment.getNotify();
    }
    
    public void setError(String errorMessage) {
        this.error = true;
        this.message = errorMessage;
    }
    
    public WeblogEntryCommentWrapper getPreviewComment() {
        // NOTE: no need for url strategy when wrapping preview comment
        return WeblogEntryCommentWrapper.wrap(previewComment, null);
    }
    
    public boolean isPreview() {
        return (this.previewComment != null);
    }
    
    public String getName() {
        return StringEscapeUtils.escapeHtml(name);
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return StringEscapeUtils.escapeHtml(email);
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getUrl() {
        return StringEscapeUtils.escapeHtml(url);
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getContent() {
        return StringEscapeUtils.escapeHtml(content);
    }

    public void setContent(String content) {
        this.content = content;
    }

    public boolean isNotify() {
        return notify;
    }

    public void setNotify(boolean notify) {
        this.notify = notify;
    }

    public boolean isError() {
        return error;
    }

    public void setError(boolean error) {
        this.error = error;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
    
}
