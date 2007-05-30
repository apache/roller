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
package org.apache.roller.weblogger.util;

import java.util.Locale;
import java.util.Comparator;
import java.io.Serializable;

public class LocaleComparator implements Comparator, Serializable
{
    public int compare(Object obj1, Object obj2)
    {
        if (obj1 instanceof Locale && obj2 instanceof Locale)
        {
            Locale locale1 = (Locale)obj1;
            Locale locale2 = (Locale)obj2;
            int compName = locale1.getDisplayName().compareTo(locale2.getDisplayName());
            if (compName == 0)
            {
                return locale1.toString().compareTo(locale2.toString());
            }
            return compName;
        }
        return 0;
    }
/* Do Comparators need to implement equals()? -Lance
    public boolean equals(Object obj)
    {
        if (obj instanceof LocaleComparator)
        {
            if (obj.equals(this)) return true;
        }
        return false;
    }
*/
}
