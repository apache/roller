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
package org.tightblog.rendering.generators;

import org.tightblog.business.URLStrategy;
import org.tightblog.business.WeblogEntryManager;
import org.tightblog.pojos.WeblogEntry;
import org.tightblog.pojos.WeblogEntrySearchCriteria;
import org.tightblog.rendering.requests.WeblogPageRequest;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

/**
 * HTML generator for big calendar that displays blog entry titles for each day.
 */
public class BigWeblogCalendar extends WeblogCalendar {

    private Map<LocalDate, List<WeblogEntry>> monthMap;
    private DateTimeFormatter singleDayFormat;

    public BigWeblogCalendar(WeblogPageRequest pRequest, WeblogEntryManager wem, URLStrategy urlStrategy) {
        super(wem, urlStrategy, pRequest);
        singleDayFormat = DateTimeFormatter.ofPattern("dd").withZone(weblog.getZoneId());
        mClassSuffix = "Big";
    }

    @Override
    protected void loadWeblogEntries(WeblogEntrySearchCriteria wesc) {
        monthMap = weblogEntryManager.getWeblogEntryObjectMap(wesc);
    }

    @Override
    protected String getContent(LocalDate day) {

        StringBuilder sb = new StringBuilder();
        // get the 8 char YYYYMMDD datestring for day, returns null if no weblog entry on that day
        String dateString;
        List<WeblogEntry> entries = monthMap.get(day);
        if (entries != null) {
            dateString = entries.get(0).getPubTime().atZone(ZoneId.systemDefault()).toLocalDate().format(eightCharDateFormat);

            // append 8 char date string on end of selfurl
            String dayUrl = urlStrategy.getWeblogCollectionURL(weblog, cat, dateString, null, -1, false);

            sb.append("<div class=\"hCalendarDayTitleBig\"><a href=\"");
            sb.append(dayUrl);
            sb.append("\">");
            sb.append(singleDayFormat.format(day));
            sb.append("</a></div>");

            for (WeblogEntry entry : entries) {
                sb.append("<div class=\"bCalendarDayContentBig\"><a href=\"");
                sb.append(entry.getPermalink());
                sb.append("\">");

                String title = entry.getTitle().trim();
                if (title.length() == 0) {
                    title = entry.getAnchor();
                }
                if (title.length() > 20) {
                    title = title.substring(0, 20) + "...";
                }

                sb.append(title);
                sb.append("</a></div>");
            }

        } else {
            sb.append("<div class=\"hCalendarDayTitleBig\">");
            sb.append(singleDayFormat.format(day));
            sb.append("</div><div class=\"bCalendarDayContentBig\"/>");
        }

        return sb.toString();
    }

    @Override
    protected String getDateStringOfEntryOnDay(LocalDate day) {
        // get the 8 char YYYYMMDD datestring for first entry of day,
        // returns null if no weblog entry on that day
        List<WeblogEntry> entries = monthMap.get(day);
        if (entries != null) {
            WeblogEntry entry = entries.get(0);
            return entry.getPubTime().atZone(ZoneId.systemDefault()).toLocalDate().format(eightCharDateFormat);
        }
        return null;
    }

}
