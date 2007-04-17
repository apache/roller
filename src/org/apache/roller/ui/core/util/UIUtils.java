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

package org.apache.roller.ui.core.util;

import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;
import java.util.TreeMap;
import org.apache.roller.util.LocaleComparator;


/**
 * A utilities class used by the Weblogger UI.
 */
public class UIUtils {
    
    public static Map allLocales = null;
    public static Map allTimeZones = null;
    
    
    // load up the locales and time zones maps
    static {
        // build locales Map
        allLocales = new HashMap(); 
        Locale[] localeArray = Locale.getAvailableLocales();
        Arrays.sort(localeArray, new LocaleComparator());
        for (int i=0; i < localeArray.length; i++) {
            allLocales.put(localeArray[i].toString(),
                           localeArray[i].getDisplayName());
        }
        
        // build time zones Map
        allTimeZones = new TreeMap();
        String[] zoneArray = TimeZone.getAvailableIDs();
        
        Date today = new Date();
        TimeZone zone = null;
        for (int i=0; i < zoneArray.length; i++) {
            zone = TimeZone.getTimeZone(zoneArray[i]);
            
            // build a display key
            StringBuffer sb = new StringBuffer();
            sb.append(zone.getDisplayName(zone.inDaylightTime(today), TimeZone.SHORT));
            sb.append(" - ");
            sb.append(zone.getID());
            
            allTimeZones.put(zone.getID(), sb.toString());
        }
    }
    
    
    /**
     * A Map of locales supported by Roller.
     */
    public static Map getLocalesMap() {
        return allLocales;
    }
    
    
    /**
     * A Map of time zones supported by Roller.
     */
    public static Map getTimeZonesMap() {
        return allTimeZones;
    }
    
}
