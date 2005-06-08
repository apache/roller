/*
 * Created on Nov 8, 2003
 *
 */
package org.roller.util;
import org.apache.commons.codec.binary.Hex;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
/** * @author lance */public class RegexUtil{    public static Pattern mailtoPattern = Pattern.compile("mailto:([a-zA-Z0-9\\.]+@[a-zA-Z0-9\\.]+\\.[a-zA-Z0-9]+)");    public static Pattern emailPattern = Pattern.compile("\\b[a-zA-Z0-9\\.]+(@)([a-zA-Z0-9\\.]+)(\\.)([a-zA-Z0-9]+)\\b");        public static String encodeEmail(String str)    {        // obfuscate mailto's: turns them into hex encoded,        // so that browsers can still understand the mailto link        Matcher mailtoMatch = mailtoPattern.matcher(str);        while (mailtoMatch.find())        {            String email = mailtoMatch.group(1);            //System.out.println("email=" + email);            String hexed = encode(email);            str = str.replaceFirst("mailto:"+email, hexed);        }                return obfuscateEmail(str);    }    /**     * obfuscate plaintext emails: makes them     * "human-readable" - still too easy for     * machines to parse however.     *      * @param str     * @return     */    public static String obfuscateEmail(String str)    {        Matcher emailMatch = emailPattern.matcher(str);        while (emailMatch.find())        {            String at = emailMatch.group(1);            //System.out.println("at=" + at);            str = str.replaceFirst(at, "-AT-");                        String dot = emailMatch.group(2) + emailMatch.group(3) + emailMatch.group(4);            String newDot = emailMatch.group(2) + "-DOT-" + emailMatch.group(4);            //System.out.println("dot=" + dot);            str = str.replaceFirst(dot, newDot);        }        return str;    }
    
    /**
     * Return the specified match "groups" from the pattern.
     * For each group matched a String will be entered in the ArrayList.
     * 
     * @param pattern The Pattern to use.
     * @param match The String to match against.
     * @param group The group number to return in case of a match.
     * @return
     */
    public static ArrayList getMatches(Pattern pattern, String match, int group)
    {
        ArrayList matches = new ArrayList();
        Matcher matcher = pattern.matcher(match);
        while (matcher.find()) 
        {
            matches.add( matcher.group(group) );
        }
        return matches;
    }	/**     * Thanks to the folks at Blojsom (http://sf.net/projects/blojsom)     * for showing me what I was doing wrong with the Hex class.     * 	 * @param email	 * @return	 */	public static String encode(String email)	{        StringBuffer result = new StringBuffer();        try {            char[] hexString = Hex.encodeHex(email.getBytes("UTF8"));            for (int i = 0; i < hexString.length; i++) {                if (i % 2 == 0) {                    result.append("%");                }                result.append(hexString[i]);            }        } catch (UnsupportedEncodingException e) {            return email;        }        return result.toString();	}}