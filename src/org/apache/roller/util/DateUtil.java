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
 */
public abstract class DateUtil extends Object {
    
    public static final long millisInDay = 86400000;
    
    // some static date formats
    private static SimpleDateFormat[] mDateFormats = loadDateFormats();
    
    private static final SimpleDateFormat mFormat8chars =
            new SimpleDateFormat("yyyyMMdd");
    
    private static final SimpleDateFormat mFormat6chars =
            new SimpleDateFormat("yyyyMM");
    
    private static final SimpleDateFormat mFormatIso8601Day =
            new SimpleDateFormat("yyyy-MM-dd");
    
    private static final SimpleDateFormat mFormatIso8601 =
            new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ");
    
    // http://www.w3.org/Protocols/rfc822/Overview.html#z28
    // Using Locale.US to fix ROL-725 and ROL-628
    private static final SimpleDateFormat mFormatRfc822 =
            new SimpleDateFormat("EEE, d MMM yyyy HH:mm:ss Z", Locale.US);
    
    
    private static SimpleDateFormat[] loadDateFormats() {
        SimpleDateFormat[] temp = {
            //new SimpleDateFormat("MM/dd/yyyy hh:mm:ss.SSS a"),
            new SimpleDateFormat("EEE MMM d HH:mm:ss z yyyy"), // standard Date.toString() results
            new SimpleDateFormat("M/d/yy hh:mm:ss"),
            new SimpleDateFormat("M/d/yyyy hh:mm:ss"),
            new SimpleDateFormat("M/d/yy hh:mm a"),
            new SimpleDateFormat("M/d/yyyy hh:mm a"),
            new SimpleDateFormat("M/d/yy HH:mm"),
            new SimpleDateFormat("M/d/yyyy HH:mm"),
            new SimpleDateFormat("dd.MM.yyyy HH:mm:ss"),
            new SimpleDateFormat("yy-MM-dd HH:mm:ss.SSS"),
            new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS"), // standard Timestamp.toString() results
            new SimpleDateFormat("M-d-yy HH:mm"),
            new SimpleDateFormat("M-d-yyyy HH:mm"),
            new SimpleDateFormat("MM/dd/yyyy HH:mm:ss.SSS"),
            new SimpleDateFormat("M/d/yy"),
            new SimpleDateFormat("M/d/yyyy"),
            new SimpleDateFormat("M-d-yy"),
            new SimpleDateFormat("M-d-yyyy"),
            new SimpleDateFormat("MMMM d, yyyyy"),
            new SimpleDateFormat("MMM d, yyyyy")
        };
        
        return temp;
    }
    
    
    /**
     * Gets the array of SimpleDateFormats that DateUtil knows about.
     **/
    private static SimpleDateFormat[] getFormats() {
        return mDateFormats;
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
        if (day == null) day = new Date();
        cal.setTime(day);
        cal.set(Calendar.HOUR_OF_DAY, cal.getMaximum(Calendar.HOUR_OF_DAY));
        cal.set(Calendar.MINUTE,      cal.getMaximum(Calendar.MINUTE));
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
        if (day == null) day = new Date();
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
        if (day == null) day = new Date();
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
        if (day == null) day = new Date();
        cal.setTime(day);
        cal.set(Calendar.HOUR_OF_DAY, cal.getMinimum(Calendar.HOUR_OF_DAY));
        cal.set(Calendar.MINUTE,      cal.getMinimum(Calendar.MINUTE));
        cal.set(Calendar.SECOND,      cal.getMinimum(Calendar.SECOND));
        cal.set(Calendar.MILLISECOND, cal.getMinimum(Calendar.MILLISECOND));
        return cal.getTime();
    }
    
    
    /**
     * Returns a Date set just to Noon, to the closest possible millisecond
     * of the day. If a null day is passed in, a new Date is created.
     * nnoon (00m 12h 00s)
     */
    public static Date getNoonOfDay(Date day, Calendar cal) {
        if (day == null) day = new Date();
        cal.setTime(day);
        cal.set(Calendar.HOUR_OF_DAY, 12);
        cal.set(Calendar.MINUTE,      cal.getMinimum(Calendar.MINUTE));
        cal.set(Calendar.SECOND,      cal.getMinimum(Calendar.SECOND));
        cal.set(Calendar.MILLISECOND, cal.getMinimum(Calendar.MILLISECOND));
        return cal.getTime();
    }
    
    
    public static Date parseFromFormats(String aValue) {
        if (StringUtils.isEmpty(aValue)) return null;
        
        // get DateUtil's formats
        SimpleDateFormat formats[] = DateUtil.getFormats();
        if (formats == null) return null;
        
        // iterate over the array and parse
        Date myDate = null;
        for (int i = 0; i <formats.length; i++) {
            try {
                myDate = DateUtil.parse(aValue, formats[i]);
                //if (myDate instanceof Date)
                return myDate;
            } catch (Exception e) {
                // do nothing because we want to try the next
                // format if current one fails
            }
        }
        // haven't returned so couldn't parse
        return null;
    }
    
    
    public static java.sql.Timestamp parseTimestampFromFormats(String aValue) {
        if (StringUtils.isEmpty(aValue)) return null;
        
        // call the regular Date formatter
        Date myDate = DateUtil.parseFromFormats(aValue);
        if (myDate != null) return new java.sql.Timestamp(myDate.getTime());
        return null;
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
     * Tries to take the passed-in String and format it as a date string in the
     * the passed-in format.
     **/
    public static String formatDateString(String aString, SimpleDateFormat aFormat) {
        if (StringUtils.isEmpty(aString) || aFormat == null)  return "";
        try {
            java.sql.Timestamp aDate = parseTimestampFromFormats(aString);
            if (aDate != null) {
                return DateUtil.format(aDate, aFormat);
            }
        } catch (Exception e) {
            // Could not parse aString.
        }
        return "";
    }
    
    
    /**
     * Returns a Date using the passed-in string and format.  Returns null if the string
     * is null or empty or if the format is null.  The string must match the format.
     **/
    public static Date parse(String aValue, SimpleDateFormat aFormat) throws ParseException {
        if (StringUtils.isEmpty(aValue) || aFormat == null) {
            return null;
        }
        
        return aFormat.parse(aValue);
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
        
        if (equalOK) {
            // true if they are equal
            if (startDate.equals(endDate)) { return true; }
        }
        
        // true if endDate after startDate
        if (endDate.after(startDate)) { return true; }
        
        return false;
    }
    
    
    // returns full timestamp format
    public static java.text.SimpleDateFormat defaultTimestampFormat() {
        return new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
    }
    
    
    // convenience method returns minimal date format
    public static java.text.SimpleDateFormat get8charDateFormat() {
        return DateUtil.mFormat8chars;
    }
    
    
    // convenience method returns minimal date format
    public static java.text.SimpleDateFormat get6charDateFormat() {
        return DateUtil.mFormat6chars;
    }
    
    
    // convenience method returns minimal date format
    public static java.text.SimpleDateFormat defaultDateFormat() {
        return DateUtil.friendlyDateFormat(true);
    }
    
    
    // convenience method
    public static String defaultTimestamp(Date date) {
        return DateUtil.format(date, DateUtil.defaultTimestampFormat());
    }
    
    
    // convenience method
    public static String defaultDate(Date date) {
        return DateUtil.format(date, DateUtil.defaultDateFormat());
    }
    
    
    // convenience method returns long friendly timestamp format
    public static java.text.SimpleDateFormat friendlyTimestampFormat() {
        return new java.text.SimpleDateFormat("dd.MM.yyyy HH:mm:ss");
    }
    
    
    // convenience method returns long friendly formatted timestamp
    public static String friendlyTimestamp(Date date) {
        return DateUtil.format(date, DateUtil.friendlyTimestampFormat());
    }
    
    
    // convenience method returns 8 char day stamp YYYYMMDD
    public static String format8chars(Date date) {
        return DateUtil.format(date, mFormat8chars);
    }
    
    
    // convenience method returns 6 char month stamp YYYYMM
    public static String format6chars(Date date) {
        return DateUtil.format(date, mFormat6chars);
    }
    
    
    // convenience method returns long friendly formatted timestamp
    public static String formatIso8601Day(Date date) {
        return DateUtil.format(date, mFormatIso8601Day);
    }
    
    
    public static String formatRfc822(Date date) {
        return DateUtil.format(date,mFormatRfc822);
    }
    
    
    // This is a hack, but it seems to work
    public static String formatIso8601(Date date) {
        if (date == null) return "";
        
        // Add a colon 2 chars before the end of the string
        // to make it a valid ISO-8601 date.
        
        String str = DateUtil.format(date,mFormatIso8601);
        StringBuffer sb = new StringBuffer();
        sb.append( str.substring(0,str.length()-2) );
        sb.append( ":" );
        sb.append( str.substring(str.length()-2) );
        return sb.toString();
    }
    
    
    // convenience method returns minimal date format
    public static java.text.SimpleDateFormat minimalDateFormat() {
        return DateUtil.friendlyDateFormat(true);
    }
    
    
    // convenience method using minimal date format
    public static String minimalDate(Date date) {
        return DateUtil.format(date, DateUtil.minimalDateFormat());
    }
    
    
    // convenience method that returns friendly data format
    // using full month, day, year digits.
    public static java.text.SimpleDateFormat fullDateFormat() {
        return DateUtil.friendlyDateFormat(false);
    }
    
    
    public static String fullDate(Date date) {
        return DateUtil.format(date, DateUtil.fullDateFormat());
    }
    
    
    /** Returns a "friendly" date format.
     *  @param mimimalFormat Should the date format allow single digits.
     **/
    public static java.text.SimpleDateFormat friendlyDateFormat(boolean minimalFormat) {
        if (minimalFormat) {
            return new java.text.SimpleDateFormat("d.M.yy");
        }
        
        return new java.text.SimpleDateFormat("dd.MM.yyyy");
    }
    
    
    /**
     * Format the date using the "friendly" date format.
     */
    public static String friendlyDate(Date date, boolean minimalFormat) {
        return DateUtil.format(date, DateUtil.friendlyDateFormat(minimalFormat));
    }
    
    
    // convenience method
    public static String friendlyDate(Date date) {
        return DateUtil.format(date, DateUtil.friendlyDateFormat(true));
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
