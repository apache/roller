/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  The ASF licenses this file to You
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
 *
 * Source file modified from the original ASF source; all changes made
 * are also under Apache License.
 */
package org.apache.roller.weblogger.ui.rendering.generators;


import org.apache.commons.lang3.time.FastDateFormat;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.roller.weblogger.WebloggerException;
import org.apache.roller.weblogger.business.URLStrategy;
import org.apache.roller.weblogger.business.WeblogEntryManager;
import org.apache.roller.weblogger.pojos.WeblogEntry;
import org.apache.roller.weblogger.pojos.WeblogEntrySearchCriteria;
import org.apache.roller.weblogger.ui.rendering.requests.WeblogPageRequest;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;


/**
 * HTML generator for big calendar that displays blog entry titles for each day.
 */
public class BigWeblogCalendar extends WeblogCalendar {
    
    private static Log log = LogFactory.getLog(BigWeblogCalendar.class);

    private Map<Date, List<WeblogEntry>> monthMap;
    protected FastDateFormat singleDayFormat;

    public BigWeblogCalendar(WeblogPageRequest pRequest, WeblogEntryManager wem, URLStrategy urlStrategy) {
        super(wem, urlStrategy, pRequest);
        TimeZone tz = weblog.getTimeZoneInstance();
        singleDayFormat = FastDateFormat.getInstance("dd", tz);
        mClassSuffix = "Big";
    }

    @Override
    protected void loadWeblogEntries(WeblogEntrySearchCriteria wesc) {
        try {
            monthMap = weblogEntryManager.getWeblogEntryObjectMap(wesc);
        } catch (WebloggerException e) {
            log.error(e);
            monthMap = new HashMap<>();
        }
    }

    @Override
    protected String getContent(Date day) {
        String content = null;
        try {
            StringBuilder sb = new StringBuilder();

            // get the 8 char YYYYMMDD datestring for day, returns null if no weblog entry on that day
            String dateString;
            List<WeblogEntry> entries = monthMap.get(day);
            if ( entries != null ) {
                dateString = eightCharDateFormat.format(entries.get(0).getPubTime());

                // append 8 char date string on end of selfurl
                String dayUrl = urlStrategy.getWeblogCollectionURL(weblog, cat, dateString, null, -1, false);

                sb.append("<div class=\"hCalendarDayTitleBig\"><a href=\"");
                sb.append( dayUrl );
                sb.append("\">");
                sb.append(singleDayFormat.format(day));
                sb.append("</a></div>");

                for (WeblogEntry entry : entries) {
                    sb.append("<div class=\"bCalendarDayContentBig\"><a href=\"");
                    sb.append(entry.getPermalink());
                    sb.append("\">");

                    String title = entry.getTitle().trim();
                    if ( title.length()==0 ) {
                        title = entry.getAnchor();
                    }
                    if ( title.length() > 20 ) {
                        title = title.substring(0,20)+"...";
                    }

                    sb.append( title );
                    sb.append("</a></div>");
                }

            } else {
                sb.append("<div class=\"hCalendarDayTitleBig\">");
                sb.append(singleDayFormat.format(day));
                sb.append("</div><div class=\"bCalendarDayContentBig\"/>");
            }
            content = sb.toString();
        } catch (Exception e) {
            log.error("ERROR: creating URL", e);
        }
        return content;
    }

    @Override
    protected String getDateStringOfEntryOnDay(Date day) {
        // get the 8 char YYYYMMDD datestring for first entry of day,
        // returns null if no weblog entry on that day
        List<WeblogEntry> entries = monthMap.get(day);
        if (entries != null) {
            WeblogEntry entry = entries.get(0);
            return eightCharDateFormat.format(entry.getPubTime());
        }
        return null;
    }

}
