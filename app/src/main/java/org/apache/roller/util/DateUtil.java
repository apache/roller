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

package org.apache.roller.util;

import java.text.ParseException;
import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;
import org.apache.commons.lang.StringUtils;


/**
 * General purpose date utilities.
 *
 * TODO: all date handling functions need to be aware of locale and timezone.
 */
public abstract class DateUtil {
    
    public static final long MILLIS_IN_DAY = 86400000;
    
    // a bunch of date formats
    private static final String FORMAT_DEFAULT_DATE = "dd.MM.yyyy";
    private static final String FORMAT_DEFAULT_DATE_MINIMAL = "d.M.yy";
    private static final String FORMAT_DEFAULT_TIMESTAMP = "yyyy-MM-dd HH:mm:ss.SSS";
    
    private static final String FORMAT_FRIENDLY_TIMESTAMP = "dd.MM.yyyy HH:mm:ss";
    
    private static final String FORMAT_6CHARS = "yyyyMM";
    private static final String FORMAT_8CHARS = "yyyyMMdd";
    
    private static final String FORMAT_ISO_8601 = "yyyy-MM-dd'T'HH:mm:ssZ";
    private static final String FORMAT_ISO_8601_DAY = "yyyy-MM-dd";
    
    private static final String FORMAT_RFC_822 = "EEE, d MMM yyyy HH:mm:ss Z";
    
    
    /**
     * Returns a Date set to the first possible millisecond of the day, just
     * after midnight. If a null day is passed in, a new Date is created.
     * midnight (00m 00h 00s)
     */
    public static Date getStartOfDay(Date day) {
        return getStartOfDay(day, Calendar.getInstance());
    }
    
    
    /**
     * Returns a Date set to the first possible millisecond of the day, just
     * after midnight. If a null day is passed in, a new Date is created.
     * midnight (00m 00h 00s)
     */
    public static Date getStartOfDay(Date day, Calendar cal) {
        if (day == null) {
            day = new Date();
        }
        cal.setTime(day);
        cal.set(Calendar.HOUR_OF_DAY, cal.getMinimum(Calendar.HOUR_OF_DAY));
        cal.set(Calendar.MINUTE,      cal.getMinimum(Calendar.MINUTE));
        cal.set(Calendar.SECOND,      cal.getMinimum(Calendar.SECOND));
        cal.set(Calendar.MILLISECOND, cal.getMinimum(Calendar.MILLISECOND));
        return cal.getTime();
    }
    
    
    /**
     * Returns a Date set to the last possible millisecond of the day, just
     * before midnight. If a null day is passed in, a new Date is created.
     * midnight (00m 00h 00s)
     */
    public static Date getEndOfDay(Date day) {
        return getEndOfDay(day,Calendar.getInstance());
    }
    
    
    public static Date getEndOfDay(Date day,Calendar cal) {
        if (day == null) {
            day = new Date();
        }
        cal.setTime(day);
        cal.set(Calendar.HOUR_OF_DAY, cal.getMaximum(Calendar.HOUR_OF_DAY));
        cal.set(Calendar.MINUTE,      cal.getMaximum(Calendar.MINUTE));
        cal.set(Calendar.SECOND,      cal.getMaximum(Calendar.SECOND));
        cal.set(Calendar.MILLISECOND, cal.getMaximum(Calendar.MILLISECOND));
        return cal.getTime();
    }
    
    
    /**
     * Returns a Date set to the first possible millisecond of the hour.
     * If a null day is passed in, a new Date is created.
     */
    public static Date getStartOfHour(Date day) {
        return getStartOfHour(day, Calendar.getInstance());
    }
    
    
    /**
     * Returns a Date set to the first possible millisecond of the hour.
     * If a null day is passed in, a new Date is created.
     */
    public static Date getStartOfHour(Date day, Calendar cal) {
        if (day == null) {
            day = new Date();
        }
        cal.setTime(day);
        cal.set(Calendar.MINUTE,      cal.getMinimum(Calendar.MINUTE));
        cal.set(Calendar.SECOND,      cal.getMinimum(Calendar.SECOND));
        cal.set(Calendar.MILLISECOND, cal.getMinimum(Calendar.MILLISECOND));
        return cal.getTime();
    }
    
    
    /**
     * Returns a Date set to the last possible millisecond of the day, just
     * before midnight. If a null day is passed in, a new Date is created.
     * midnight (00m 00h 00s)
     */
    public static Date getEndOfHour(Date day) {
        return getEndOfHour(day, Calendar.getInstance());
    }
    
    
    public static Date getEndOfHour(Date day, Calendar cal) {
        if (day == null || cal == null) {
            return day;
        }
        
        cal.setTime(day);
        cal.set(Calendar.MINUTE,      cal.getMaximum(Calendar.MINUTE));
        cal.set(Calendar.SECOND,      cal.getMaximum(Calendar.SECOND));
        cal.set(Calendar.MILLISECOND, cal.getMaximum(Calendar.MILLISECOND));
        return cal.getTime();
    }
    
    
    /**
     * Returns a Date set to the first possible millisecond of the minute.
     * If a null day is passed in, a new Date is created.
     */
    public static Date getStartOfMinute(Date day) {
        return getStartOfMinute(day, Calendar.getInstance());
    }
    
    
    /**
     * Returns a Date set to the first possible millisecond of the minute.
     * If a null day is passed in, a new Date is created.
     */
    public static Date getStartOfMinute(Date day, Calendar cal) {
        if (day == null) {
            day = new Date();
        }
        cal.setTime(day);
        cal.set(Calendar.SECOND,      cal.getMinimum(Calendar.SECOND));
        cal.set(Calendar.MILLISECOND, cal.getMinimum(Calendar.MILLISECOND));
        return cal.getTime();
    }
    
    
    /**
     * Returns a Date set to the last possible millisecond of the minute.
     * If a null day is passed in, a new Date is created.
     */
    public static Date getEndOfMinute(Date day) {
        return getEndOfMinute(day, Calendar.getInstance());
    }
    
    
    public static Date getEndOfMinute(Date day, Calendar cal) {
        if (day == null || cal == null) {
            return day;
        }
        
        cal.setTime(day);
        cal.set(Calendar.SECOND,      cal.getMaximum(Calendar.SECOND));
        cal.set(Calendar.MILLISECOND, cal.getMaximum(Calendar.MILLISECOND));
        return cal.getTime();
    }
    
    
    /**
     * Returns a Date set to the first possible millisecond of the month, just
     * after midnight. If a null day is passed in, a new Date is created.
     * midnight (00m 00h 00s)
     */
    public static Date getStartOfMonth(Date day) {
        return getStartOfMonth(day, Calendar.getInstance());
    }
    
    
    public static Date getStartOfMonth(Date day, Calendar cal) {
        if (day == null) {
            day = new Date();
        }
        cal.setTime(day);
        
        // set time to start of day
        cal.set(Calendar.HOUR_OF_DAY, cal.getMinimum(Calendar.HOUR_OF_DAY));
        cal.set(Calendar.MINUTE,      cal.getMinimum(Calendar.MINUTE));
        cal.set(Calendar.SECOND,      cal.getMinimum(Calendar.SECOND));
        cal.set(Calendar.MILLISECOND, cal.getMinimum(Calendar.MILLISECOND));
        
        // set time to first day of month
        cal.set(Calendar.DAY_OF_MONTH, 1);
        
        return cal.getTime();
    }
    
    
    /**
     * Returns a Date set to the last possible millisecond of the month, just
     * before midnight. If a null day is passed in, a new Date is created.
     * midnight (00m 00h 00s)
     */
    public static Date getEndOfMonth(Date day) {
        return getEndOfMonth(day, Calendar.getInstance());
    }
    
    
    public static Date getEndOfMonth(Date day,Calendar cal) {
        if (day == null) {
            day = new Date();
        }
        cal.setTime(day);
        
        // set time to end of day
        cal.set(Calendar.HOUR_OF_DAY, cal.getMaximum(Calendar.HOUR_OF_DAY));
        cal.set(Calendar.MINUTE,      cal.getMaximum(Calendar.MINUTE));
        cal.set(Calendar.SECOND,      cal.getMaximum(Calendar.SECOND));
        cal.set(Calendar.MILLISECOND, cal.getMaximum(Calendar.MILLISECOND));
        
        // set time to first day of month
        cal.set(Calendar.DAY_OF_MONTH, 1);
        
        // add one month
        cal.add(Calendar.MONTH, 1);
        
        // back up one day
        cal.add(Calendar.DAY_OF_MONTH, -1);
        
        return cal.getTime();
    }
    
    
    /**
     * Returns a Date set just to Noon, to the closest possible millisecond
     * of the day. If a null day is passed in, a new Date is created.
     * noon (00m 12h 00s)
     */
    public static Date getNoonOfDay(Date day, Calendar cal) {
        if (day == null) {
            day = new Date();
        }
        cal.setTime(day);
        cal.set(Calendar.HOUR_OF_DAY, 12);
        cal.set(Calendar.MINUTE,      cal.getMinimum(Calendar.MINUTE));
        cal.set(Calendar.SECOND,      cal.getMinimum(Calendar.SECOND));
        cal.set(Calendar.MILLISECOND, cal.getMinimum(Calendar.MILLISECOND));
        return cal.getTime();
    }
    
    
    /**
     * Returns a java.sql.Timestamp equal to the current time
     **/
    public static java.sql.Timestamp now() {
        return new java.sql.Timestamp(new java.util.Date().getTime());
    }
    
    
    /**
     * Returns a string the represents the passed-in date parsed
     * according to the passed-in format.  Returns an empty string
     * if the date or the format is null.
     **/
    public static String format(Date aDate, SimpleDateFormat aFormat) {
        if (aDate == null || aFormat == null ) { return ""; }
        synchronized (aFormat) {
            return aFormat.format(aDate);
        }
    }
    
    
    /**
     * Returns a Date using the passed-in string and format.  Returns null if the string
     * is null or empty or if the format is null.  The string must match the format.
     **/
    public static Date parse(String aValue, SimpleDateFormat aFormat) throws ParseException {
        if (StringUtils.isEmpty(aValue) || aFormat == null) {
            return null;
        }
        synchronized(aFormat) {
            return aFormat.parse(aValue);
        }
    }
    
    
    /**
     * Returns true if endDate is after startDate or if startDate equals endDate
     * or if they are the same date.  Returns false if either value is null.
     **/
    public static boolean isValidDateRange(Date startDate, Date endDate) {
        return isValidDateRange(startDate, endDate, true);
    }
    
    
    /**
     * Returns true if endDate is after startDate or if startDate equals endDate.
     * Returns false if either value is null.  If equalOK, returns true if the
     * dates are equal.
     **/
    public static boolean isValidDateRange(Date startDate, Date endDate, boolean equalOK) {
        // false if either value is null
        if (startDate == null || endDate == null) { return false; }
        
        if (equalOK && startDate.equals(endDate)) {
            // true if they are equal
            return true;
        }
        
        // true if endDate after startDate
        if (endDate.after(startDate)) {
            return true;
        }
        
        return false;
    }
    
    
    // convenience method returns minimal date format
    public static SimpleDateFormat defaultDateFormat() {
        return DateUtil.friendlyDateFormat(true);
    }
    
    
    // convenience method returns minimal date format
    public static java.text.SimpleDateFormat minimalDateFormat() {
        return friendlyDateFormat(true);
    }
    
    
    // convenience method that returns friendly data format
    // using full month, day, year digits.
    public static SimpleDateFormat fullDateFormat() {
        return friendlyDateFormat(false);
    }
    
    
    /** 
     * Returns a "friendly" date format.
     * @param minimalFormat Should the date format allow single digits.
     **/
    public static SimpleDateFormat friendlyDateFormat(boolean minimalFormat) {
        if (minimalFormat) {
            return new SimpleDateFormat(FORMAT_DEFAULT_DATE_MINIMAL);
        }
        
        return new SimpleDateFormat(FORMAT_DEFAULT_DATE);
    }
    
    
    // returns full timestamp format
    public static SimpleDateFormat defaultTimestampFormat() {
        return new SimpleDateFormat(FORMAT_DEFAULT_TIMESTAMP);
    }
    
    
    // convenience method returns long friendly timestamp format
    public static SimpleDateFormat friendlyTimestampFormat() {
        return new SimpleDateFormat(FORMAT_FRIENDLY_TIMESTAMP);
    }
    
    
    // convenience method returns minimal date format
    public static SimpleDateFormat get8charDateFormat() {
        return new SimpleDateFormat(FORMAT_8CHARS);
    }
    
    
    // convenience method returns minimal date format
    public static SimpleDateFormat get6charDateFormat() {
        return new SimpleDateFormat(FORMAT_6CHARS);
    }
    
    
    // convenience method returns minimal date format
    public static SimpleDateFormat getIso8601DateFormat() {
        return new SimpleDateFormat(FORMAT_ISO_8601);
    }
    
    
    // convenience method returns minimal date format
    public static SimpleDateFormat getIso8601DayDateFormat() {
        return new SimpleDateFormat(FORMAT_ISO_8601_DAY);
    }
    
    
    // convenience method returns minimal date format
    public static SimpleDateFormat getRfc822DateFormat() {
        // http://www.w3.org/Protocols/rfc822/Overview.html#z28
        // Using Locale.US to fix ROL-725 and ROL-628
        return new SimpleDateFormat(FORMAT_RFC_822, Locale.US);
    }
    
    
    // convenience method
    public static String defaultDate(Date date) {
        return format(date, defaultDateFormat());
    }
    
    
    // convenience method using minimal date format
    public static String minimalDate(Date date) {
        return format(date, DateUtil.minimalDateFormat());
    }
    
    
    public static String fullDate(Date date) {
        return format(date, DateUtil.fullDateFormat());
    }
    
    
    /**
     * Format the date using the "friendly" date format.
     */
    public static String friendlyDate(Date date, boolean minimalFormat) {
        return format(date, friendlyDateFormat(minimalFormat));
    }
    
    
    // convenience method
    public static String friendlyDate(Date date) {
        return format(date, friendlyDateFormat(true));
    }
    
    
    // convenience method
    public static String defaultTimestamp(Date date) {
        return format(date, defaultTimestampFormat());
    }
    
    
    // convenience method returns long friendly formatted timestamp
    public static String friendlyTimestamp(Date date) {
        return format(date, friendlyTimestampFormat());
    }
    
    
    // convenience method returns 8 char day stamp YYYYMMDD
    public static String format8chars(Date date) {
        return format(date, get8charDateFormat());
    }
    
    
    // convenience method returns 6 char month stamp YYYYMM
    public static String format6chars(Date date) {
        return format(date, get6charDateFormat());
    }
    
    
    // convenience method returns long friendly formatted timestamp
    public static String formatIso8601Day(Date date) {
        return format(date, getIso8601DayDateFormat());
    }
    
    
    public static String formatRfc822(Date date) {
        return format(date, getRfc822DateFormat());
    }
    
    
    // This is a hack, but it seems to work
    public static String formatIso8601(Date date) {
        if (date == null) {
            return "";
        }
        
        // Add a colon 2 chars before the end of the string
        // to make it a valid ISO-8601 date.
        
        String str = format(date, getIso8601DateFormat());
        StringBuffer sb = new StringBuffer();
        sb.append( str.substring(0,str.length()-2) );
        sb.append( ":" );
        sb.append( str.substring(str.length()-2) );
        return sb.toString();
    }
    
    
    public static Date parseIso8601(String value) throws Exception {
        return ISO8601DateParser.parse(value);
    }
    
    
    /**
     * Parse data as either 6-char or 8-char format.
     */
    public static Date parseWeblogURLDateString(String dateString, TimeZone tz, Locale locale) {
        
        Date ret = new Date();
        SimpleDateFormat char8DateFormat = DateUtil.get8charDateFormat();
        SimpleDateFormat char6DateFormat = DateUtil.get6charDateFormat();
        
        if (dateString != null
                && dateString.length()==8
                && StringUtils.isNumeric(dateString) ) {
            ParsePosition pos = new ParsePosition(0);
            ret = char8DateFormat.parse(dateString, pos);
            
            // make sure the requested date is not in the future
            Date today = null;
            Calendar todayCal = Calendar.getInstance();
            todayCal = Calendar.getInstance(tz, locale);
            todayCal.setTime(new Date());
            today = todayCal.getTime();
            if(ret.after(today)) {
                ret = today;
            }
            
        } else if(dateString != null
                && dateString.length()==6
                && StringUtils.isNumeric(dateString)) {
            ParsePosition pos = new ParsePosition(0);
            ret = char6DateFormat.parse(dateString, pos);
            
            // make sure the requested date is not in the future
            Calendar todayCal = Calendar.getInstance();
            todayCal = Calendar.getInstance(tz, locale);
            todayCal.setTime(new Date());
            Date today = todayCal.getTime();
            if(ret.after(today)) {
                ret = today;
            }
        }
        
        return ret;
    }
    
}
