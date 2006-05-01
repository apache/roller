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
package org.roller.presentation.atomadminapi.sdk;

import java.util.Locale;

class LocaleString {
    private Locale locale;
    
    public LocaleString(String localeString) {
        if (localeString == null) {
            locale = null;
            return;
        }
        
        String[] components = localeString.split("_");
        
        if (components == null) {
            locale = null;
            return;
        }
                
        if (components.length == 1) {
            locale = new Locale(components[0]);
        } else if (components.length == 2) {
            locale = new Locale(components[0], components[1]);
        } else if (components.length == 3) {
            locale = new Locale(components[0], components[1], components[2]);
        } else {
            throw new IllegalArgumentException("invalid locale string: " + localeString);
        }
    }
    
    public Locale getLocale() {
        return locale;
    }
    
}
