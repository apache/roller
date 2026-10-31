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
 * A pager implementation for a collection of media files. Media file search
 * is a POSTed form, so instead of links this exposes page numbers that the
 * page resubmits the search form with.
 */
public class MediaFilePager {
    
    // the collection for the pager
    private final List<MediaFile> items;
    
    // What page we are on
    private final int pageNum;
    
    // media files matching the search across all pages
    private final long totalCount;

    private final int pageSize;
    
    public MediaFilePager(int page, List<MediaFile> mediaFiles, long totalCount, int pageSize) {
        this.pageNum = page;
        this.items = mediaFiles;
        this.totalCount = totalCount;
        this.pageSize = pageSize;
    }
    
    public List<MediaFile> getItems() {
        return items;
    }

    public boolean isMoreItems() {
        return (long) (pageNum + 1) * pageSize < totalCount;
    }

    public long getTotalCount() {
        return totalCount;
    }

    /** Zero-based page number, as the search form submits it. */
    public int getPageNum() {
        return pageNum;
    }

    /** One-based page number, for display. */
    public int getPageNumber() {
        return pageNum + 1;
    }

    /** Number of pages; at least 1, so an empty result reads "Page 1 of 1". */
    public int getPageCount() {
        return (int) Math.max(1, (totalCount + pageSize - 1) / pageSize);
    }

}
