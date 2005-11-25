package org.roller.util;

/**
 * This class contains helper methods
 * for Roller pojos (only?)
**/
public abstract class PojoUtil
{
    public static boolean equals(boolean lEquals, Object a, Object b)
    {
        if (a == null)
        {
            lEquals = lEquals && (b == null);
        }
        else
        {
            lEquals = lEquals && a.equals(b);
        }
        return lEquals;
    }

    public static int addHashCode(int result, Object a)
    {
		return (37 * result) + ((a != null) ? a.hashCode() : 0);
	}
}