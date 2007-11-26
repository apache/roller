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
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.roller.weblogger.business.URLStrategy;
import org.apache.roller.weblogger.business.Weblogger;
import org.apache.roller.weblogger.business.WebloggerFactory;
import org.apache.roller.weblogger.business.WeblogEntryManager;
import org.apache.roller.weblogger.pojos.WeblogEntry;
import org.apache.roller.weblogger.pojos.Weblog;
import org.apache.roller.weblogger.pojos.wrapper.WeblogEntryWrapper;


/**
 * A special pager for showing entries in preview mode.
 *
 * This pager is different from the other pagers because it allows access to
 * data we don't normally provide to templates working on the "live" weblog,
 * like entries in DRAFT mode.
 */
public class WeblogEntriesPreviewPager extends WeblogEntriesPermalinkPager {
    
    private static Log log = LogFactory.getLog(WeblogEntriesPreviewPager.class);
    
    
    public WeblogEntriesPreviewPager(
            URLStrategy        strat,
            Weblog             weblog,
            String             locale,
            String             pageLink,
            String             entryAnchor,
            String             dateString,
            String             catPath,
            List               tags,
            int                page) {
        
        super(strat, weblog, locale, pageLink, entryAnchor, dateString, catPath, tags, page);
    }
    
    
    public Map getEntries() {
        if (entries == null) try {
            Weblogger roller = WebloggerFactory.getWeblogger();
            WeblogEntryManager wmgr = roller.getWeblogEntryManager();
            currEntry = wmgr.getWeblogEntryByAnchor(weblog, entryAnchor);
            if (currEntry != null) {
                
                // clone the entry since we don't want to work with the real pojo
                WeblogEntry tmpEntry = new WeblogEntry();
                tmpEntry.setData(currEntry);
                
                // set the pubtime to the current time if it is unset
                if(tmpEntry.getPubTime() == null) {
                    tmpEntry.setPubTime(new Timestamp(System.currentTimeMillis()));
                }
                
                // store the entry in the collection
                entries = new TreeMap();
                entries.put(tmpEntry.getPubTime(),Collections.singletonList(WeblogEntryWrapper.wrap(tmpEntry, urlStrategy)));
            }
        } catch (Exception e) {
            log.error("ERROR: fetching entry", e);
        }


        
        return entries;
    }
    
}
