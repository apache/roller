package org.roller.util;
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

    public boolean equals(Object obj)
    {
        if (obj instanceof TimeZoneComparator)
        {
            if (obj.equals(this)) return true;
        }
        return false;
    }
}