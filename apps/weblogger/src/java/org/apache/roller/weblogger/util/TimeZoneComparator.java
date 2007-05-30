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
import java.util.TimeZone;
import java.util.Comparator;
import java.io.Serializable;

public class TimeZoneComparator implements Comparator, Serializable
{
    public int compare(Object obj1, Object obj2)
    {
        if (obj1 instanceof TimeZone && obj2 instanceof TimeZone)
        {
            TimeZone zone1 = (TimeZone)obj1;
            TimeZone zone2 = (TimeZone)obj2;
            int compName = zone1.getDisplayName().compareTo(zone2.getDisplayName());
            if (compName == 0)
            {
				return zone1.getID().compareTo(zone2.getID());
			}
            return compName;
        }
        return 0;
    }

    /* Do Comparators need to implement equals()? -Lance
    public boolean equals(Object obj)
    {
        if (obj instanceof TimeZoneComparator)
        {
            if (obj.equals(this)) return true;
        }
        return false;
    }
    */
}