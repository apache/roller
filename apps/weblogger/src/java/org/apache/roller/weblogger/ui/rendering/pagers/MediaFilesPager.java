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
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.roller.weblogger.business.MediaFileManager;
import org.apache.roller.weblogger.business.URLStrategy;
import org.apache.roller.weblogger.business.Weblogger;
import org.apache.roller.weblogger.business.WebloggerFactory;
import org.apache.roller.weblogger.pojos.MediaFile;
import org.apache.roller.weblogger.pojos.MediaFileFilter;
import org.apache.roller.weblogger.pojos.Weblog;
import org.apache.roller.weblogger.pojos.MediaFileFilter.MediaFileOrder;


/**
 * Paging through a collection of media files.
 */
public class MediaFilesPager extends AbstractPager {
    
    private static Log log = LogFactory.getLog(MediaFilesPager.class);
    
    private Weblog weblog = null;
    private int sinceDays = -1;
    private int length = 0;
    
    // the collection for the pager
    private List<MediaFile> mediaFiles;
    
    // are there more items?
    private boolean more = false;
    
    // most recent update time of current set of entries
    private Date lastUpdated = null;        
    
    public MediaFilesPager(
            URLStrategy    strat,
            String         baseUrl,
            Weblog         weblog,
            int            sinceDays,
            int            page,
            int            length) {
        
        super(strat, baseUrl, page);
        
        this.weblog = weblog;
        this.sinceDays = sinceDays;
        this.length = length;
        
        // initialize the collection
        getItems();
    }
    
    
    public List<MediaFile> getItems() {
        
        if (this.mediaFiles == null) {
            // calculate offset
            int offset = getPage() * length;
            
            List<MediaFile> results = new ArrayList<MediaFile>();
            
            Date startDate = null;
            if(sinceDays > 0) {
                Calendar cal = Calendar.getInstance();
                cal.setTime(new Date());
                cal.add(Calendar.DATE, -1 * sinceDays);
                startDate = cal.getTime();
            }
            
            try {
                Weblogger roller = WebloggerFactory.getWeblogger();
                MediaFileManager mgr = roller.getMediaFileManager();
                MediaFileFilter fileFilter = new MediaFileFilter();
                fileFilter.setStartIndex(offset);
                fileFilter.setLength(length + 1);
                fileFilter.setOrder(MediaFileOrder.DATE_UPLOADED);
                results = mgr.searchMediaFiles(weblog, fileFilter);
            } catch (Exception e) {
                log.error("ERROR: fetching comment list", e);
            }
            this.mediaFiles = results;
        }
        
        return this.mediaFiles;
    }
    
    
    public boolean hasMoreItems() {
        return more;
    }
    
    /** Get last updated time from items in pager */
    public Date getLastUpdated() {
        if (lastUpdated == null) {
            // feeds are sorted by pubtime, so first might not be last updated
            List<MediaFile> items = (List<MediaFile>)getItems();
            if (items != null && items.size() > 0) {
                Timestamp newest = ((MediaFile)items.get(0)).getLastUpdated();
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

