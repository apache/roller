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

package org.apache.roller.weblogger.ui.struts2.pagers;

import java.util.List;

import org.apache.roller.weblogger.pojos.MediaFile;

/**
 * A pager implementation for a collection of media files.
 *
 */
public class MediaFilePager {
    
    // the collection for the pager
    private final List<MediaFile> items;
    
    // What page we are on
    private final int pageNum;
    
    // Are there more items?
    private final boolean moreItems;
    
    public MediaFilePager(int page, List<MediaFile> mediaFiles, boolean hasMore) {
        this.pageNum = page;
        this.items = mediaFiles;
        this.moreItems = hasMore;
    }
    
    public List<MediaFile> getItems() {
        return items;
    }

    public boolean isMoreItems() {
        return moreItems;
    }

    /**
     * Indicates whether there are more than one page of item.
     * 
     */
    public boolean isJustOnePage() {
    	return (pageNum == 0 && !moreItems);
    }
    
    /**
     * Has previous page?
     * 
     */
    public boolean hasPrevious() {
    	return (pageNum > 0);
    }
    
    /**
     * Has next page?
     * 
     */
    public boolean hasNext() {
    	return this.moreItems;
    }
    
    

}
