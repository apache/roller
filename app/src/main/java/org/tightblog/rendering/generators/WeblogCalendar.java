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

import org.apache.commons.lang3.StringUtils;
import org.tightblog.business.URLStrategy;
import org.tightblog.business.WeblogEntryManager;
import org.tightblog.pojos.Weblog;
import org.tightblog.pojos.WeblogEntry;
import org.tightblog.pojos.WeblogEntry.PubStatus;
import org.tightblog.pojos.WeblogEntrySearchCriteria;
import org.tightblog.rendering.requests.WeblogPageRequest;
import org.tightblog.util.Utilities;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.TimeZone;

/**
 * HTML generator for a small weblog calendar, commonly used on the weblog sidebars.
 * The small calendar identifies days that have blog entries, but not the entries'
 * titles (see BigWeblogCalendar for that.)
 */
public class WeblogCalendar {

    private WeblogPageRequest pageRequest = null;
    private Map<LocalDate, String> monthMap;
    private LocalDate dayInMonth = null;
    private LocalDate prevMonth = null;
    private LocalDate nextMonth = null;
    private DateTimeFormatter sixCharDateFormat;

    protected WeblogEntryManager weblogEntryManager;
    protected URLStrategy urlStrategy;
    protected Weblog weblog = null;
    protected String cat = null;
    protected String mClassSuffix = "";
    protected DateTimeFormatter eightCharDateFormat;

    public WeblogCalendar(WeblogEntryManager wem, URLStrategy urlStrategy, WeblogPageRequest pRequest) {
        this.weblogEntryManager = wem;
        this.urlStrategy = urlStrategy;
        this.pageRequest = pRequest;

        weblog = pageRequest.getWeblog();
        eightCharDateFormat = DateTimeFormatter.ofPattern(Utilities.FORMAT_8CHARS);
        sixCharDateFormat = DateTimeFormatter.ofPattern(Utilities.FORMAT_6CHARS);

        if (pageRequest.getWeblogCategoryName() != null) {
            cat = pageRequest.getWeblogCategoryName();
        }

        dayInMonth = parseWeblogURLDateString(pageRequest.getWeblogDate());
        initModel();
    }

    private void initModel() {
        LocalDateTime startTime = dayInMonth.withDayOfMonth(1).atStartOfDay();
        LocalDateTime endTime = dayInMonth.withDayOfMonth(dayInMonth.lengthOfMonth()).atStartOfDay().plusDays(1).minusNanos(1);

        // determine if we should have next and prev month links, and if so, the months for them to point to
        WeblogEntry temp = weblogEntryManager.findNearestWeblogEntry(weblog, cat, startTime.minusNanos(1), false);
        prevMonth = firstDayOfMonthOfWeblogEntry(temp);

        temp = weblogEntryManager.findNearestWeblogEntry(weblog, cat, endTime.plusNanos(1), true);
        nextMonth = firstDayOfMonthOfWeblogEntry(temp);

        // retrieve the entries for this month
        WeblogEntrySearchCriteria wesc = new WeblogEntrySearchCriteria();
        wesc.setWeblog(weblog);
        wesc.setStartDate(startTime.atZone(ZoneId.systemDefault()).toInstant());
        wesc.setEndDate(endTime.atZone(ZoneId.systemDefault()).toInstant());
        wesc.setCategoryName(cat);
        wesc.setStatus(PubStatus.PUBLISHED);
        loadWeblogEntries(wesc);
    }

    private LocalDate firstDayOfMonthOfWeblogEntry(WeblogEntry entry) {
        return (entry == null) ? null : entry.getPubTime().atZone(ZoneId.systemDefault()).toLocalDate().withDayOfMonth(1);
    }

    protected String getContent(LocalDate day) {
        return null;
    }

    protected String getDateStringOfEntryOnDay(LocalDate day) {
        // get the 8 char YYYYMMDD datestring for day
        return monthMap.get(day);
    }

    /**
     * Create previous or next month URLs for the calendar, that normally appear to the left and right of the
     * month name at the top of the calendar.
     *
     * The URL to return is commonly of two types:
     * (1) Usually for the small calendar that is displayed along with the blog entries, a month
     *     paginator that will show the blog entries of a different month, i.e., getWeblogCollectionURL.
     * (2) For the large calendar that might sit on a custom "archive" page, the previous and next month links
     *     should point to that same archive page, albeit giving the calendar for a different month, i.e.,
     *     getCustomPageURL.
     *
     * @param day       Day for URL or null if no entries on that day
     * @param alwaysURL Always return a URL, never return null
     * @return URL for day, or null if no weblog entry on that day
     */
    private String computeUrl(LocalDate day, boolean createMonthURL, boolean alwaysURL) {
        String dateString = getDateStringOfEntryOnDay(day);
        if (dateString == null) {
            if (alwaysURL) {
                dateString = (createMonthURL ? sixCharDateFormat : eightCharDateFormat).format(day);
            } else {
                return null;
            }
        }
        if (pageRequest.getCustomPageName() != null) {
            return urlStrategy.getCustomPageURL(weblog, pageRequest.getCustomPageName(), dateString, false);

        } else {
            return urlStrategy.getWeblogCollectionURL(weblog, cat, dateString, null, -1, false);
        }
    }

    protected void loadWeblogEntries(WeblogEntrySearchCriteria wesc) {
        monthMap = weblogEntryManager.getWeblogEntryStringMap(wesc);
    }

    /**
     * Parse date as either 6-char or 8-char format.  Use current date if date not provided
     * in URL (e.g., a permalink)
     */
    private LocalDate parseWeblogURLDateString(String dateString) {
        LocalDate ret = null;

        if (dateString != null && StringUtils.isNumeric(dateString)) {
            if (dateString.length() == 8) {
                ret = LocalDate.parse(dateString, eightCharDateFormat);
            } else if (dateString.length() == 6) {
                YearMonth tmp = YearMonth.parse(dateString, sixCharDateFormat);
                ret = tmp.atDay(1);
            }
        }

        // make sure the requested date is not in the future
        if (ret == null || ret.isAfter(LocalDate.now())) {
            ret = LocalDate.now();
        }

        return ret;
    }

    private String computeNextMonthUrl() {
        return computeUrl(nextMonth, true, true);
    }

    private String computePrevMonthUrl() {
        return computeUrl(prevMonth, true, true);
    }

    private String computeTodayMonthUrl() {
        return urlStrategy.getWeblogCollectionURL(weblog, cat, null, null, -1, false);
    }

    private Calendar newCalendarInstance() {
        Calendar cal = Calendar.getInstance(TimeZone.getTimeZone(weblog.getZoneId()), weblog.getLocaleInstance());
        cal.setTime(new Timestamp(dayInMonth.atStartOfDay().plusDays(1).toInstant(ZoneOffset.UTC).toEpochMilli()));
        return cal;
    }

    public String generateHTML() {
        String ret;
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw, true);

        Locale locale = pageRequest.getWeblog().getLocaleInstance();

        // build week day names
        String[] dayNames = buildDayNames(locale);

        Calendar monthToDisplay = newCalendarInstance();
        // dayIndex will serve as the iterator, going through not just the days of the
        // desired month but the few days before and/or after it to fill a 5 week grid.
        Calendar dayIndex = newCalendarInstance();
        // for today's date, allows for different formatting.
        Calendar todayCal = new GregorianCalendar();

        // get Resource Bundle
        ResourceBundle bundle = ResourceBundle.getBundle("ApplicationResources", locale);
        // formatter for Month-Year title of calendar
        DateTimeFormatter formatTitle = DateTimeFormatter.ofPattern(bundle.getString("calendar.dateFormat"), locale)
                .withZone(todayCal.getTimeZone().toZoneId());

        // start with the first day of the week containing the first day of the month
        dayIndex.set(Calendar.DAY_OF_MONTH, dayIndex.getMinimum(Calendar.DAY_OF_MONTH));
        while (dayIndex.get(Calendar.DAY_OF_WEEK) != dayIndex.getFirstDayOfWeek()) {
            dayIndex.add(Calendar.DATE, -1);
        }

        // create table of 5 weeks, 7 days per row
        pw.print("<table cellspacing='0' border='0' class='hCalendarTable" + mClassSuffix + "'>");
        pw.print("<tr><td colspan='7' align='center' " +
                "class='hCalendarMonthYearRow" + mClassSuffix + "'>");
        if (prevMonth != null) {
            pw.print("<a href='" + computePrevMonthUrl() + "' title='" + bundle.getString("calendar.prev") +
                    "' class='hCalendarNavBar'>&laquo;</a> ");
        }
        pw.print(formatTitle.format(dayInMonth));
        if (nextMonth != null) {
            pw.print(" <a href='" + computeNextMonthUrl() + "' title='" + bundle.getString("calendar.next") +
                    "' class='hCalendarNavBar'>&raquo;</a>");
        }

        pw.print("</td></tr>");
        // emit the HTML calendar
        for (int w = -1; w < 6; w++) {
            pw.print("<tr>");
            for (int d = 0; d < 7; d++) {
                if (w == -1) {
                    pw.print("<th class=\"hCalendarDayNameRow" + mClassSuffix + "\" align=\"center\">");
                    pw.print(dayNames[d]);
                    pw.print("</th>");
                    continue;
                }

                // determine URL for this calendar day
                LocalDate tddate = LocalDate.from(new Timestamp(dayIndex.getTimeInMillis()).toLocalDateTime());
                String url = computeUrl(tddate, false, false);
                String content = getContent(tddate);

                // day is in calendar month
                if ((dayIndex.get(Calendar.MONTH) == monthToDisplay.get(Calendar.MONTH)) &&
                        (dayIndex.get(Calendar.YEAR) == monthToDisplay.get(Calendar.YEAR))) {
                    if ((dayIndex.get(Calendar.DAY_OF_MONTH) == todayCal.get(Calendar.DAY_OF_MONTH)) &&
                            (dayIndex.get(Calendar.MONTH) == todayCal.get(Calendar.MONTH)) &&
                            (dayIndex.get(Calendar.YEAR) == todayCal.get(Calendar.YEAR))) {
                        printDayInThisMonth(pw, dayIndex, url, content, true);
                    } else {
                        printDayInThisMonth(pw, dayIndex, url, content, false);
                    }
                } else {
                    // apply day-not-in-month style ;-)
                    printDayNotInMonth(pw);
                }

                // increment calendar by one day
                dayIndex.add(Calendar.DATE, 1);
            }
            pw.print("</tr>");
        }

        pw.print("<tr class=\"hCalendarNextPrev" + mClassSuffix + "\">");
        pw.print("<td colspan=\"7\" align=\"center\">");

        pw.print("<a href=\"" + computeTodayMonthUrl() + "\" class=\"hCalendarNavBar\">" +
                bundle.getString("calendar.today") + "</a>");

        pw.print("</td></tr></table>");

        ret = sw.toString();
        return ret;
    }

    private void printDayNotInMonth(PrintWriter pw) {
        pw.print("<td class=\"hCalendarDayNotInMonth" + mClassSuffix + "\">&nbsp;</td>");
    }

    private void printDayInThisMonth(PrintWriter pw, Calendar cal, String url, String content, boolean today) {
        String mainClass = (today ? "hCalendarDayCurrent" : "hCalendarDay") + mClassSuffix;

        if (content != null) {
            pw.print("<td class=\"" + mainClass + "\">");
            pw.print(content);
            pw.print("</td>");
        } else if (url != null) {
            pw.print("<td class=\"" + mainClass + "\">");
            pw.print("<a href=\"" + url + "\" class=\"hCalendarDayTitle" + mClassSuffix + "\">");
            pw.print(cal.get(Calendar.DAY_OF_MONTH));
            pw.print("</a></td>");
        } else {
            pw.print("<td class=\"" + mainClass + "\">");
            pw.print("<div class=\"hCalendarDayTitle" + mClassSuffix + "\">");
            pw.print(cal.get(Calendar.DAY_OF_MONTH));
            pw.print("</div></td>");
        }
    }

    /**
     * Helper method to build the names of the weekdays (Sun, Mon, Tue, etc.).
     */
    private String[] buildDayNames(Locale locale) {
        // build array of names of days of week
        String[] dayNames = new String[7];
        Calendar dayNameCal = Calendar.getInstance(locale);
        SimpleDateFormat dayFormatter = new SimpleDateFormat("EEE", locale);
        dayNameCal.set(Calendar.DAY_OF_WEEK, dayNameCal.getFirstDayOfWeek());
        for (int dnum = 0; dnum < 7; dnum++) {
            dayNames[dnum] = dayFormatter.format(dayNameCal.getTime());
            dayNameCal.add(Calendar.DATE, 1);
        }
        return dayNames;
    }

}
