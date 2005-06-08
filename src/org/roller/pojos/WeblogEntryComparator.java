/*
 * Created on Apr 16, 2004
 */
package org.roller.pojos;

import java.io.Serializable;
import java.util.Comparator;

/**
 * Sorts WeblogEntryData objects in reverse chronological order
 * (most recently published entries first).  If they happen to
 * have the same pubTime, then sort alphabetically by title.
 * 
 * @author lance.lavandowska
 */
public class WeblogEntryComparator implements Comparator, Serializable
{
    public int compare(Object val1, Object val2)
    throws ClassCastException
    {
        WeblogEntryData entry1 = (WeblogEntryData)val1;
        WeblogEntryData entry2 = (WeblogEntryData)val2;
        long pubTime1 = entry1.getPubTime().getTime();
        long pubTime2 = entry2.getPubTime().getTime();

        if (pubTime1 > pubTime2)
        {
            return -1;
        }
        else if (pubTime1 < pubTime2)
        {
            return 1;
        }

        // if pubTimes are the same, return
        // results of String.compareTo() on Title
        return entry1.getTitle().compareTo(entry2.getTitle());
    }

}
