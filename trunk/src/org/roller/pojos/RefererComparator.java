package org.roller.pojos;


import java.io.Serializable;
import java.util.Comparator;

/** Compares referers based on day hits and then alphabetical order */
public class RefererComparator implements Comparator, Serializable
{
    static final long serialVersionUID = -1658901752434218888L;
    
    public int compare(Object val1, Object val2)
    throws ClassCastException
    {
        RefererData r1 = (RefererData)val1;
        RefererData r2 = (RefererData)val2;
        int hits1 = r1.getDayHits().intValue();
        int hits2 = r2.getDayHits().intValue();

        if (hits1 > hits2)
        {
            return -1;
        }
        else if (hits1 < hits2)
        {
            return 1;
        }

        // if hits are the same, return
        // results of String.compareTo()
        return r1.getRefererUrl().compareTo(r2.getRefererUrl());
    }
}
