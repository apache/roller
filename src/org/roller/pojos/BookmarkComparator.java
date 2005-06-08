package org.roller.pojos;


import java.io.Serializable;
import java.util.Comparator;

public class BookmarkComparator implements Comparator, Serializable
{
    public int compare(Object val1, Object val2)
    throws ClassCastException
    {
        BookmarkData bd1 = (BookmarkData)val1;
        BookmarkData bd2 = (BookmarkData)val2;
        int priority1 = bd1.getPriority().intValue();
        int priority2 = bd2.getPriority().intValue();

        if (priority1 > priority2)
        {
            return 1;
        }
        else if (priority1 < priority2)
        {
            return -1;
        }

        // if priorities are the same, return
        // results of String.compareTo()
        return bd1.getName().compareTo(bd2.getName());

    }

}
