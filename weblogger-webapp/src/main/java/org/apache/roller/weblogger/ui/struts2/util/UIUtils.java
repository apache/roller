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

package org.apache.roller.weblogger.ui.struts2.util;

import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;


/**
 * A utilities class used by the Weblogger UI.
 */
public final class UIUtils {
    
    private static final List locales;
    private static final List timeZones;
    
    
    // load up the locales and time zones lists
    static {
        // build locales list
        locales = Arrays.asList(Locale.getAvailableLocales());
        Collections.sort(locales, new LocaleComparator());
        
        // build time zones list
        timeZones = Arrays.asList(TimeZone.getAvailableIDs());
        Collections.sort(timeZones);
    }
    
    
    public static String getLocale(String localeName) {
        Locale locale = new Locale(localeName,localeName);        
        return locale.toString();        
    }
    
    public static List getLocales() {
        return locales;
    }
    
    public static String getTimeZone(String timeZoneName) {
        return TimeZone.getTimeZone(timeZoneName).getID();                
    }
    
    public static List getTimeZones() {
        return timeZones;
    }
    
    
    // special comparator for sorting locales
    private static final class LocaleComparator implements Comparator {
        public int compare(Object obj1, Object obj2) {
            if (obj1 instanceof Locale && obj2 instanceof Locale) {
                Locale locale1 = (Locale)obj1;
                Locale locale2 = (Locale)obj2;
                int compName = locale1.getDisplayName().compareTo(locale2.getDisplayName());
                if (compName == 0) {
                    return locale1.toString().compareTo(locale2.toString());
                }
                return compName;
            }
            return 0;
        }
    }
    
}
