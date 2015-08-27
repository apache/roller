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
 *
 * Source file modified from the original ASF source; all changes made
 * are also under Apache License.
 */

package org.apache.roller.util;

import org.apache.commons.lang3.time.DateUtils;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

/**
 * General purpose date utilities.
 */
public abstract class DateUtil {
    private static final String FORMAT_6CHARS = "yyyyMM";
    private static final String FORMAT_8CHARS = "yyyyMMdd";

    /**
     * Returns a Date set to the first possible millisecond of the day, just
     * after midnight.
     */
    public static Date getStartOfDay(Date day, Calendar cal) {
        cal.setTime(day);
        return DateUtils.truncate(cal, Calendar.DATE).getTime();
    }

    /**
     * Returns a Date set to the last possible millisecond of the day, just
     * before midnight.
     */
    public static Date getEndOfDay(Date day,Calendar cal) {
        cal.setTime(day);
        return DateUtils.addMilliseconds(DateUtils.ceiling(cal, Calendar.DATE).getTime(), -1);
    }

    /**
     * Returns a Date set to the first possible millisecond of the month.
     */
    public static Date getStartOfMonth(Date day, Calendar cal) {
        cal.setTime(day);
        return DateUtils.truncate(cal, Calendar.MONTH).getTime();
    }

    /**
     * Returns a Date set to the last possible millisecond of the month.
     */
    public static Date getEndOfMonth(Date day,Calendar cal) {
        cal.setTime(day);
        return DateUtils.addMilliseconds(DateUtils.ceiling(cal, Calendar.MONTH).getTime(), -1);
    }

    /**
     * Returns a Date set just to Noon, to the closest possible millisecond of the day.
     */
    public static Date getNoonOfDay(Date day, Calendar cal) {
        cal.setTime(day);
        return DateUtils.addHours(DateUtils.truncate(cal, Calendar.DATE).getTime(), 12);
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
    
    // convenience method returns minimal date format
    public static SimpleDateFormat get8charDateFormat() {
        return new SimpleDateFormat(FORMAT_8CHARS);
    }

    // convenience method returns minimal date format
    public static SimpleDateFormat get6charDateFormat() {
        return new SimpleDateFormat(FORMAT_6CHARS);
    }

    // convenience method returns 8 char day stamp YYYYMMDD using time zone
    public static String format8chars(Date date, TimeZone tz) {
        SimpleDateFormat formatter = get8charDateFormat();
        formatter.setTimeZone(tz);
        return format(date, formatter);
    }

    // convenience method returns 6 char month stamp YYYYMM using time zone
    public static String format6chars(Date date, TimeZone tz) {
        SimpleDateFormat formatter = get6charDateFormat();
        formatter.setTimeZone(tz);
        return format(date, formatter);
    }
}
