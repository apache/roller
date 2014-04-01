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

package org.apache.roller.weblogger.ui.rendering.pagers;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.roller.weblogger.business.MediaFileManager;
import org.apache.roller.weblogger.business.URLStrategy;
import org.apache.roller.weblogger.business.WebloggerFactory;
import org.apache.roller.weblogger.pojos.MediaFile;


/**
 * Paging through a collection of media files.
 */
public class MediaFilesPager extends AbstractPager {
    
    private static Log log = LogFactory.getLog(MediaFilesPager.class);
    private int length = 0;
    
    // the collection for the pager
    private List<MediaFile> mediaFiles;
    
    // most recent update time of current set of entries
    private Date lastUpdated = null;        
    
    public MediaFilesPager(
            URLStrategy    strat,
            String         baseUrl,
            int            page,
            int            length) {
        
        super(strat, baseUrl, page);
        this.length = length;
        
        // initialize the collection
        getItems();
    }
    
    
    public List<MediaFile> getItems() {
        
        if (this.mediaFiles == null) {
            // calculate offset
            //int offset = getPage() * length;
            
            List<MediaFile> results = new ArrayList<MediaFile>();
            
            try {
                MediaFileManager mgr = WebloggerFactory.getWeblogger().getMediaFileManager();
                results = mgr.fetchRecentPublicMediaFiles(length);
            } catch (Exception e) {
                log.error("ERROR: fetching comment list", e);
            }
            this.mediaFiles = results;
        }
        
        return this.mediaFiles;
    }
    
    
    public boolean hasMoreItems() {
        return false;
    }
    
    /** Get last updated time from items in pager */
    public Date getLastUpdated() {
        if (lastUpdated == null) {
            // feeds are sorted by pubtime, so first might not be last updated
            List<MediaFile> items = getItems();
            if (items != null && items.size() > 0) {
                Timestamp newest = items.get(0).getLastUpdated();
                for (MediaFile file : items) {
                    if (file.getLastUpdated().after(newest)) {
                        newest = file.getLastUpdated();
                    }
                }
                lastUpdated = new Date(newest.getTime());
            } else {
                // no update so we assume it's brand new
                lastUpdated = new Date();
            }
        }
        return lastUpdated;
    }
}

