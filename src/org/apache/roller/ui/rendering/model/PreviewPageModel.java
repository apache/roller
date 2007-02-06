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

import java.util.Map;
import org.apache.roller.RollerException;
import org.apache.roller.pojos.wrapper.WeblogEntryDataWrapper;
import org.apache.roller.ui.rendering.pagers.WeblogEntriesLatestPager;
import org.apache.roller.ui.rendering.pagers.WeblogEntriesPager;
import org.apache.roller.ui.rendering.pagers.WeblogEntriesPreviewPager;
import org.apache.roller.ui.rendering.util.WeblogPreviewRequest;
import org.apache.roller.ui.rendering.util.WeblogRequest;


/**
 * An extension of the PageModel to make some adjustments for previewing.
 */
public class PreviewPageModel extends PageModel {
    
    private WeblogPreviewRequest previewRequest = null;
    
    
    /** 
     * Init model.
     */
    public void init(Map initData) throws RollerException {
        
        // we expect the init data to contain a weblogRequest object
        WeblogRequest weblogRequest = (WeblogRequest) initData.get("weblogRequest");
        if(weblogRequest == null) {
            throw new RollerException("expected weblogRequest from init data");
        }
        
        // PreviewPageModel only works on preview requests, so cast weblogRequest
        // into a WeblogPreviewRequest and if it fails then throw exception
        if(weblogRequest instanceof WeblogPreviewRequest) {
            this.previewRequest = (WeblogPreviewRequest) weblogRequest;
        } else {
            throw new RollerException("weblogRequest is not a WeblogPreviewRequest."+
                    "  PreviewPageModel only supports preview requests.");
        }
        
        super.init(initData);
    }    
    
    
    public boolean isPermalink() {
        return (previewRequest.getPreviewEntry() != null ||
                previewRequest.getWeblogAnchor() != null);
    }
    
    
    public WeblogEntryDataWrapper getWeblogEntry() {
        
        if(previewRequest.getPreviewEntry() != null ||
                previewRequest.getWeblogAnchor() != null) {
            return WeblogEntryDataWrapper.wrap(previewRequest.getWeblogEntry());
        }
        return null;
    }
    
    
    /**
     * Override method that returns pager so that we can introduce a custom
     * pager for preview pages which can display things that we don't want
     * available on the "live" weblog, like DRAFT entries.
     */
    public WeblogEntriesPager getWeblogEntriesPager(String catArgument) {
        
        String anchor = previewRequest.getPreviewEntry();
        if(anchor == null) {
            anchor = previewRequest.getWeblogAnchor();
        }
        
        if (anchor != null) {
            return new WeblogEntriesPreviewPager(
                    previewRequest.getWeblog(),
                    previewRequest.getLocale(),
                    previewRequest.getWeblogPageName(),
                    anchor,
                    previewRequest.getWeblogDate(),
                    null,
                    previewRequest.getTags(),
                    previewRequest.getPageNum());
        } else {
            return new WeblogEntriesLatestPager(
                    previewRequest.getWeblog(),
                    previewRequest.getLocale(),
                    previewRequest.getWeblogPageName(),
                    previewRequest.getWeblogAnchor(),
                    previewRequest.getWeblogDate(),
                    null,
                    previewRequest.getTags(),
                    previewRequest.getPageNum());
        }
        
    }

}
