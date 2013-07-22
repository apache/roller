/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  The ASF licenses this file to You
 * under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.  For additional information regarding
 * copyright in this work, please see the NOTICE file in the top level
 * directory of this distribution.
 */

package org.apache.roller.weblogger.util;

import java.util.Locale;
import org.apache.commons.lang.StringUtils;


/**
 * A utility class for dealing with i18n.
 */
public final class I18nUtils {
    
    public static Locale toLocale(String locale) {
        if (locale != null) {
            String[] localeStr = StringUtils.split(locale,"_");
            if (localeStr.length == 1) {
                if (localeStr[0] == null) localeStr[0] = "";
                return new Locale(localeStr[0]);
            } else if (localeStr.length == 2) {
                if (localeStr[0] == null) localeStr[0] = "";
                if (localeStr[1] == null) localeStr[1] = "";
                return new Locale(localeStr[0], localeStr[1]);
            } else if (localeStr.length == 3) {
                if (localeStr[0] == null) localeStr[0] = "";
                if (localeStr[1] == null) localeStr[1] = "";
                if (localeStr[2] == null) localeStr[2] = "";
                return new Locale(localeStr[0], localeStr[1], localeStr[2]);
            }
        }
        return Locale.getDefault();
    }
    
}
