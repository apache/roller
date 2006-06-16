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
package org.apache.roller.ui.rendering.model;

import java.util.List;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import org.apache.roller.pojos.wrapper.CommentDataWrapper;
import org.apache.roller.pojos.wrapper.WeblogEntryDataWrapper;
import org.apache.roller.pojos.wrapper.WebsiteDataWrapper;
import org.apache.roller.ui.authoring.struts.formbeans.CommentFormEx;

/**
 * New Atlas minimalistic page model provides access to a weblog, possibly
 * a weblog entry and pageable collections of entries and comments.
 *
 * Assumes this data is available from POJOs:
 * <pre>
 * website.getPages()
 * website.getComments()
 * website.getPageByName(String name)
 * webste.getDayHits()
 * </pre>
 */
public class WeblogPageModel implements PageModel {
    
    /** Creates a new instance of AtlasWeblogPageModel */
    public WeblogPageModel() {
    }
    
    /** Template context name to be used for model */
    public String getModelName() {
        return "page";
    }

    /** Init page model based on request */
    public void init(HttpServletRequest request) {
    }
    
    /**
     * Get website being displayed.
     */
    public WebsiteDataWrapper getWebsite() {
        return null;
    }
    
    /** 
     * Get weblog entry to be displayed or null if not on single-entry page.
     */
    public WeblogEntryDataWrapper getWeblogEntry() {
        return null;
    }
        
    /**
     * Get most recent weblog entries for day or month specified by request.
     * @return List of WeblogEntryDataWrapper objects. 
     */
    public List getWeblogEntriesList(String cat, int offset, int length) {
        return null;
    }
    
    /**
     * Get most recent weblog entries for day or month specified by request.
     * @return Map of Lists of weblog entry objects, keyed by 8-char date strings.
     */
    public Map getWeblogEntriesMonthMap(String cat, int offset, int length) {
        return null;
    }
    
    /** 
     * Get weblog entry to be displayed or null if not on single-entry page.
     */
    public WeblogEntryDataWrapper getWeblogEntryNext() {
        return null;
    }
    
    /** 
     * Get weblog entry to be displayed or null if not on single-entry page.
     */
    public WeblogEntryDataWrapper getWeblogEntryPrev() {
        return null;
    }
    
    /**
     * Get most recent approved and non-spam comments in weblog.
     * @return List of CommentDataWrapper objects.
     */
    public List getComments(int offset, int length) {
        return null;
    }
    
    /**
     * Get comment form to be displayed or null if not on single-entry page.
     */
    public CommentFormEx getCommentForm() {
        return null;
    }      
    
    /**
     * Get preview comment or null if none exists.
     */
    public CommentDataWrapper getCommentPreview() {
        return null;
    }   
}


