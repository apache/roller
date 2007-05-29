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

package org.apache.roller.ui.rendering.util;

import org.apache.roller.pojos.WeblogEntryComment;
import org.apache.roller.pojos.WeblogEntryData;
import org.apache.roller.pojos.wrapper.CommentDataWrapper;
import org.apache.roller.pojos.wrapper.WeblogEntryDataWrapper;


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
        this.notify = comment.getNotify().booleanValue();
    }
    
    public void setError(String errorMessage) {
        this.error = true;
        this.message = errorMessage;
    }
    
    public CommentDataWrapper getPreviewComment() {
        return CommentDataWrapper.wrap(previewComment);
    }
    
    public boolean isPreview() {
        return (this.previewComment != null);
    }
    
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getContent() {
        return content;
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
