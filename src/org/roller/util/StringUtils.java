/*
 * Created on Jun 3, 2004
 */
package org.roller.util;

/**
 * @author lance.lavandowska
 */
public class StringUtils
{
    public static boolean isEmpty(String str)
    {
        if (str == null) return true;
        return "".equals(str.trim());
    }
    
    public static boolean isNotEmpty(String str)
    {
        return !isEmpty(str);
    }
    
    public static String[] split(String str1, String str2)
    {
       return org.apache.commons.lang.StringUtils.split(str1, str2);
    }
    
    public static String replace(String src, String target, String rWith)
    {
        return org.apache.commons.lang.StringUtils.replace(src, target, rWith);
    }
    
    public static String replace(String src, String target, String rWith, int maxCount)
    {
        return org.apache.commons.lang.StringUtils.replace(src, target, rWith, maxCount);
    }
    
    public static boolean equals(String str1, String str2) 
    {
        return org.apache.commons.lang.StringUtils.equals(str1, str2);
    }
    
    public static boolean isAlphanumeric(String str)
    {
        return org.apache.commons.lang.StringUtils.isAlphanumeric(str);
    }
    
    public static String[] stripAll(String[] strs) 
    {
        return org.apache.commons.lang.StringUtils.stripAll(strs);
    }
    
    public static String left(String str, int length)
    {
        return org.apache.commons.lang.StringUtils.left(str, length);
    }
}
