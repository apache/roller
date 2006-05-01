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
/*
 * Created on Jun 3, 2004
 */
package org.apache.roller.util;

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
