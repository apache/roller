package org.roller.util;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.security.MessageDigest;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.NoSuchElementException;
import java.util.StringTokenizer;

/**
 * General purpose utilities. 
 * 
 * <pre>
 * Includes TextToHTML methods dondated by Erik Thauvin - "Donated to the 
 * Roller Weblogger project for publication under the terms of the Roller 
 * Software License. 
 * Copyright (C) 2002-2003 by Erik C. Thauvin (erik@thauvin.net).
 * All rights reserved.
 * </pre>
 * 
 * @author David M Johnson
 * @author Lance Lavandowska
 * @author Matt Raible (added encryption methods)
 */
public class Utilities
{
    /**
     * The <code>Log</code> instance for this class.
     */
    private static Log mLogger = LogFactory.getLog(Utilities.class);
    
    /**
     * Utility methods for calling StringUtils since it cannot be 
     * instantiated and Utilties can.
     */
    public static boolean isNotEmpty(String str)
    {
        return StringUtils.isNotEmpty(str);
    }
    
    //------------------------------------------------------------------------
    /** Strip jsessionid off of a URL */
    public static String stripJsessionId( String url )
    {
        // Strip off jsessionid found in referer URL
        int startPos = url.indexOf(";jsessionid=");
        if ( startPos != -1 )
        {
            int endPos = url.indexOf("?",startPos);
            if ( endPos == -1 )
            {
                url = url.substring(0,startPos);   
            }
            else
            {
                url = url.substring(0,startPos)
                    + url.substring(endPos,url.length());   
            }
        }
        return url;
    }

    //------------------------------------------------------------------------
    /**
     * Escape, but do not replace HTML.
     * The default behaviour is to escape ampersands.
     */
    public static String escapeHTML(String s)
    {
        return escapeHTML(s, true);
    }

    //------------------------------------------------------------------------
    /**
     * Escape, but do not replace HTML.
     * @param escapseAmpersand Optionally escape
     * ampersands (&amp;).
     */
    public static String escapeHTML(String s, boolean escapeAmpersand)
    {
        // got to do amp's first so we don't double escape
        if (escapeAmpersand)
        {
            s = stringReplace(s, "&", "&amp;");
        }
        s = stringReplace(s, "&nbsp;", " ");
        s = stringReplace(s, "\"", "&quot;");
        s = stringReplace(s, "<", "&lt;");
        s = stringReplace(s, ">", "&gt;");
        return s;
    }

    //------------------------------------------------------------------------
    /**
     * Remove occurences of html, defined as any text
     * between the characters "&lt;" and "&gt;".  Replace
     * any HTML tags with a space.
     */
    public static String removeHTML(String str)
    {
        return removeHTML(str, true);
    }
    
    /**
     * Remove occurences of html, defined as any text
     * between the characters "&lt;" and "&gt;". 
     * Optionally replace HTML tags with a space.
     * 
     * @param str
     * @param addSpace
     * @return
     */
    public static String removeHTML(String str, boolean addSpace)
    {
        if (str == null) return "";
        StringBuffer ret = new StringBuffer(str.length());
        int start = 0;
        int beginTag = str.indexOf("<");
        int endTag = 0;
        if (beginTag == -1)
            return str;

        while (beginTag >= start)
        {
            if (beginTag > 0)
            {
                ret.append(str.substring(start, beginTag));
                
                // replace each tag with a space (looks better)
                if (addSpace) ret.append(" ");
            }
            endTag = str.indexOf(">", beginTag);
            
            // if endTag found move "cursor" forward
            if (endTag > -1)
            {
                start = endTag + 1;
                beginTag = str.indexOf("<", start);
            }
            // if no endTag found, get rest of str and break
            else
            {
                ret.append(str.substring(beginTag));
                break;
            }
        }
        // append everything after the last endTag
        if (endTag > -1 && endTag + 1 < str.length())
        {
            ret.append(str.substring(endTag + 1));
        }
        return ret.toString().trim();
    }

    //------------------------------------------------------------------------
    /** Run both removeHTML and escapeHTML on a string.
     * @param s String to be run through removeHTML and escapeHTML.
     * @return String with HTML removed and HTML special characters escaped.
     */
    public static String removeAndEscapeHTML( String s )
    {
        if ( s==null ) return "";
        else return Utilities.escapeHTML( Utilities.removeHTML(s) );
    }

    //------------------------------------------------------------------------
    /**
     * Autoformat.
     */
    public static String autoformat(String s)
    {
        String ret = StringUtils.replace(s, "\n", "<br />");
        return ret;
    }
    
    //------------------------------------------------------------------------
    /**
     * Format date in ISO-8601 format.
     */
    public static String formatIso8601Date(Date d)
    {
        return DateUtil.formatIso8601(d);
    }

    //------------------------------------------------------------------------
    /**
     * Return a date in RFC-822 format.
     */
    public static String formatRfc822Date(Date date) 
    {
        return DateUtil.formatRfc822(date);
    }

	//------------------------------------------------------------------------
	/**
	 * Replaces occurences of non-alphanumeric characters with an underscore.
	 */
	public static String replaceNonAlphanumeric(String str)
	{
		return replaceNonAlphanumeric(str, '_');
	}

	//------------------------------------------------------------------------
	/**
	 * Replaces occurences of non-alphanumeric characters with a
	 * supplied char.
	 */
	public static String replaceNonAlphanumeric(String str, char subst)
	{
		StringBuffer ret = new StringBuffer(str.length());
		char[] testChars = str.toCharArray();
		for (int i = 0; i < testChars.length; i++)
		{
			if (Character.isLetterOrDigit(testChars[i]))
			{
				ret.append(testChars[i]);
			}
			else
			{
				ret.append( subst );
			}
		}
		return ret.toString();
	}

    //------------------------------------------------------------------------
    /**
     * Remove occurences of non-alphanumeric characters.
     */
    public static String removeNonAlphanumeric(String str)
    {
        StringBuffer ret = new StringBuffer(str.length());
        char[] testChars = str.toCharArray();
        for (int i = 0; i < testChars.length; i++)
        {
            // MR: Allow periods in page links
            if (Character.isLetterOrDigit(testChars[i]) ||
                testChars[i] == '.')
            {
                ret.append(testChars[i]);
            }
        }
        return ret.toString();
    }

    //------------------------------------------------------------------------
    /**
     * @param pathArray
     * @return
     */
    public static String stringArrayToString(String[] stringArray, String delim)
    {
        String ret = new String();
        for (int i = 0; i < stringArray.length; i++)
        {
            if (ret.length() > 0)
                ret = ret + delim + stringArray[i];
            else
                ret = stringArray[i];
        }
        return ret;
    }
    
    //------------------------------------------------------------------------
    /**
     * Replace occurrences of str1 in string str with str2 
     */
    public static String stringReplace(String str, String str1, String str2)
    {
        String ret = StringUtils.replace(str,str1,str2);
        return ret;
    }

    //------------------------------------------------------------------------
    /**
     * Replace occurrences of str1 in string str with str2
     * @param str String to operate on 
     * @param str1 String to be replaced
     * @param str2 String to be used as replacement
     * @param maxCount Number of times to replace, 0 for all
     */
    public static String stringReplace(
        String str,
        String str1,
        String str2,
        int maxCount)
    {
        String ret = StringUtils.replace(str,str1,str2,maxCount);
        return ret;
    }

    //--------------------------------------------------------------------------
    /** Convert string to string array. */
    public static String[] stringToStringArray(String instr, String delim)
        throws NoSuchElementException, NumberFormatException
    {
        StringTokenizer toker = new StringTokenizer(instr, delim);
        String stringArray[] = new String[toker.countTokens()];
        int i = 0;

        while (toker.hasMoreTokens())
        {
            stringArray[i++] = toker.nextToken();
        }
        return stringArray;
    }

    //--------------------------------------------------------------------------
    /** Convert string to integer array. */
    public static int[] stringToIntArray(String instr, String delim)
        throws NoSuchElementException, NumberFormatException
    {
        StringTokenizer toker = new StringTokenizer(instr, delim);
        int intArray[] = new int[toker.countTokens()];
        int i = 0;

        while (toker.hasMoreTokens())
        {
            String sInt = toker.nextToken();
            int nInt = Integer.parseInt(sInt);
            intArray[i++] = new Integer(nInt).intValue();
        }
        return intArray;
    }

    //-------------------------------------------------------------------
    /** Convert integer array to a string. */
    public static String intArrayToString(int[] intArray)
    {
        String ret = new String();
        for (int i = 0; i < intArray.length; i++)
        {
            if (ret.length() > 0)
                ret = ret + "," + Integer.toString(intArray[i]);
            else
                ret = Integer.toString(intArray[i]);
        }
        return ret;
    }

    //------------------------------------------------------------------------
    public static void copyFile(File from, File to) throws IOException
    {
        InputStream in = null;
        OutputStream out = null;

        try
        {
            in = new FileInputStream(from);
        }
        catch (IOException ex)
        {
            throw new IOException(
                "Utilities.copyFile: opening input stream '"
                    + from.getPath()
                    + "', "
                    + ex.getMessage());
        }

        try
        {
            out = new FileOutputStream(to);
        }
        catch (Exception ex)
        {
            try
            {
                in.close();
            }
            catch (IOException ex1)
            {
            }
            throw new IOException(
                "Utilities.copyFile: opening output stream '"
                    + to.getPath()
                    + "', "
                    + ex.getMessage());
        }

        copyInputToOutput(in, out, from.length());
    }

    //------------------------------------------------------------------------
    /**
     * Utility method to copy an input stream to an output stream.
     * Wraps both streams in buffers. Ensures right numbers of bytes copied.
     */
    public static void copyInputToOutput(
        InputStream input,
        OutputStream output,
        long byteCount)
        throws IOException
    {
        int bytes;
        long length;

        BufferedInputStream in = new BufferedInputStream(input);
        BufferedOutputStream out = new BufferedOutputStream(output);

        byte[] buffer;
        buffer = new byte[8192];

        for (length = byteCount; length > 0;)
        {
            bytes = (int) (length > 8192 ? 8192 : length);

            try
            {
                bytes = in.read(buffer, 0, bytes);
            }
            catch (IOException ex)
            {
                try
                {
                    in.close();
                    out.close();
                }
                catch (IOException ex1)
                {
                }
                throw new IOException(
                    "Reading input stream, " + ex.getMessage());
            }

            if (bytes < 0)
                break;

            length -= bytes;

            try
            {
                out.write(buffer, 0, bytes);
            }
            catch (IOException ex)
            {
                try
                {
                    in.close();
                    out.close();
                }
                catch (IOException ex1)
                {
                }
                throw new IOException(
                    "Writing output stream, " + ex.getMessage());
            }
        }

        try
        {
            in.close();
            out.close();
        }
        catch (IOException ex)
        {
            throw new IOException("Closing file streams, " + ex.getMessage());
        }
    }

    //------------------------------------------------------------------------
    public static void copyInputToOutput(
        InputStream input,
        OutputStream output)
        throws IOException
    {
        BufferedInputStream in = new BufferedInputStream(input);
        BufferedOutputStream out = new BufferedOutputStream(output);
        byte buffer[] = new byte[8192];
        for (int count = 0; count != -1;)
        {
            count = in.read(buffer, 0, 8192);
            if (count != -1)
                out.write(buffer, 0, count);
        }

        try
        {
            in.close();
            out.close();
        }
        catch (IOException ex)
        {
            throw new IOException("Closing file streams, " + ex.getMessage());
        }
    }
    
    /**
     * Encode a string using algorithm specified in web.xml and return the
     * resulting encrypted password. If exception, the plain credentials
     * string is returned
     *
     * @param password Password or other credentials to use in authenticating
     *        this username
     * @param algorithm Algorithm used to do the digest
     *
     * @return encypted password based on the algorithm.
     */
    public static String encodePassword(String password, String algorithm) 
    {
        byte[] unencodedPassword = password.getBytes();

        MessageDigest md = null;

        try {
            // first create an instance, given the provider
            md = MessageDigest.getInstance(algorithm);
        } catch (Exception e) {
            mLogger.error("Exception: " + e);

            return password;
        }

        md.reset();

        // call the update method one or more times
        // (useful when you don't know the size of your data, eg. stream)
        md.update(unencodedPassword);

        // now calculate the hash
        byte[] encodedPassword = md.digest();

        StringBuffer buf = new StringBuffer();

        for (int i = 0; i < encodedPassword.length; i++) {
            if ((encodedPassword[i] & 0xff) < 0x10) {
                buf.append("0");
            }

            buf.append(Long.toString(encodedPassword[i] & 0xff, 16));
        }

        return buf.toString();
    }

    /**
     * Encode a string using Base64 encoding. Used when storing passwords
     * as cookies.
     *
     * This is weak encoding in that anyone can use the decodeString
     * routine to reverse the encoding.
     *
     * @param str
     * @return String
     * @throws IOException
     */
    public static String encodeString(String str) throws IOException 
    {
        sun.misc.BASE64Encoder encoder = new sun.misc.BASE64Encoder();
        String encodedStr = new String(encoder.encodeBuffer(str.getBytes()));

        return (encodedStr.trim());
    }

    /**
     * Decode a string using Base64 encoding.
     *
     * @param str
     * @return String
     * @throws IOException
     */
    public static String decodeString(String str) throws IOException 
    {
        sun.misc.BASE64Decoder dec = new sun.misc.BASE64Decoder();
        String value = new String(dec.decodeBuffer(str));

        return (value);
    }
    
    /**
     * This method based on code from the String taglib at Apache Jakarta:
     * http://cvs.apache.org/viewcvs/jakarta-taglibs/string/src/org/apache/taglibs/string/util/StringW.java?rev=1.16&content-type=text/vnd.viewcvs-markup
     * Copyright (c) 1999 The Apache Software Foundation.
     * Author: Henri Yandell bayard@generationjava.com
     * 
     * @param str
     * @param lower
     * @param upper
     * @param appendToEnd
     * @return
     */
    public static String truncateNicely(String str, int lower, int upper, String appendToEnd)
    {
        // strip markup from the string
        String str2 = removeHTML(str, false);
        boolean diff = (str2.length() < str.length());
        
        // quickly adjust the upper if it is set lower than 'lower'
        if(upper < lower) {
            upper = lower;
        }       
        
        // now determine if the string fits within the upper limit
        // if it does, go straight to return, do not pass 'go' and collect $200
        if(str2.length() > upper) {
            // the magic location int
            int loc;
        
            // first we determine where the next space appears after lower
            loc = str2.lastIndexOf(' ', upper);
            
            // now we'll see if the location is greater than the lower limit
            if(loc >= lower) {
                // yes it was, so we'll cut it off here
                str2 = str2.substring(0, loc);
            } else {
                // no it wasnt, so we'll cut it off at the upper limit
                str2 = str2.substring(0, upper);
                loc = upper;
            }
            
            // HTML was removed from original str
            if (diff)
            {
                
                // location of last space in truncated string
                loc = str2.lastIndexOf(' ', loc);
                
                // get last "word" in truncated string (add 1 to loc to eliminate space
                String str3 = str2.substring(loc+1);
                
                // find this fragment in original str, from 'loc' position
                loc = str.indexOf(str3, loc) + str3.length();
                
                // get truncated string from original str, given new 'loc'
                str2 = str.substring(0, loc);
                
                // get all the HTML from original str after loc
                str3 = extractHTML(str.substring(loc));
                
                // remove any linebreaks (<br> and <p></p>) in the HTML
                str3 = stripLineBreaks(str3);
                
                // append the appendToEnd String and
                // add extracted HTML back onto truncated string
                str = str2 + appendToEnd + str3;
            }
            else
            {
                // the string was truncated, so we append the appendToEnd String                
                str = str2 + appendToEnd;
            }
    
        }
        
        return str;
    }
    
    /**
	 * @param str3
	 * @return
	 */
	private static String stripLineBreaks(String str)
	{
		str = str.replaceAll("<br>", "");
        str = str.replaceAll("<br/>", "");
        str = str.replaceAll("<br />", "");
        str = str.replaceAll("<p></p>", "");
        return str;
	}

	/**
     * Extract (keep) JUST the HTML from the String.
     * @param str
     * @return
     */
    public static String extractHTML(String str)
    {
        if (str == null) return "";
        StringBuffer ret = new StringBuffer(str.length());
        int start = 0;
        int beginTag = str.indexOf("<");
        int endTag = 0;
        if (beginTag == -1)
            return str;

        while (beginTag >= start)
        {
            endTag = str.indexOf(">", beginTag);
            
            // if endTag found, keep tag
            if (endTag > -1)
            {
                ret.append( str.substring(beginTag, endTag+1) );
                
                // move start forward and find another tag
                start = endTag + 1;
                beginTag = str.indexOf("<", start);
            }
            // if no endTag found, break
            else
            {
                break;
            }
        }
        return ret.toString();
    }

    
    public static String hexEncode(String str)
    {
        if (StringUtils.isEmpty(str)) return str;
        
        return RegexUtil.encode(str);
    }
    
    public static String encodeEmail(String str)
    {
        return RegexUtil.encodeEmail(str);
    }

    /**
     * Converts a character to HTML or XML entity.
     *
     * @param ch The character to convert.
     * @param xml Convert the character to XML if set to true.
     * @author Erik C. Thauvin
     *
     * @return The converted string.
     */
    public static final String charToHTML(char ch, boolean xml)
    {
        int c;

        // Convert left bracket
        if (ch == '<')
        {
            return ("&lt;");
        }

        // Convert left bracket
        else if (ch == '>')
        {
            return ("&gt;");
        }

        // Convert ampersand
        else if (ch == '&')
        {
            return ("&amp;");
        }

        // High-ASCII character
        else if (ch >= 128)
        {
            c = ch;

            return ("&#" + c + ';');
        }

        // Convert double quote
        else if (xml && (ch == '"'))
        {
            return ("&quot;");
        }

        // Convert single quote
        else if (xml && (ch == '\''))
        {
            return ("&#39;");
        }

        // No conversion
        else
        {
            // Return character as string
            return (String.valueOf(ch));
        }
    }

    /**
     * Converts a text string to HTML or XML entities.
     *
     * @author Erik C. Thauvin
     * @param text The string to convert.
     * @param xml Convert the string to XML if set to true.
     *
     * @return The converted string.
     */
    public static final String textToHTML(String text, boolean xml)
    {
        final StringBuffer html = new StringBuffer();

        // Loop thru each characters of the text
        for (int i = 0; i < text.length(); i++)
        {
            // Convert character to HTML/XML
            html.append(charToHTML(text.charAt(i), xml));
        }

        // Return HTML/XML string
        return html.toString();
    }

    /**
     * Converts a text string to HTML or XML entities.
     *
     * @param text The string to convert.
     * @author Erik C. Thauvin
     * @return The converted string.
     */
    public static final String textToHTML(String text)
    {
        return textToHTML(text, false);
    }

    /**
     * Converts a text string to XML entities.
     *
     * @param text The string to convert.
     * @author Erik C. Thauvin
     * @return The converted string.
     */
    public static final String textToXML(String text)
    {
        return textToHTML(text, true);
    }

    /**
     * Converts a text string to HTML or XML entities.
     * @param text The string to convert.
     * @return The converted string.
     */
    public static final String textToCDATA(String text)
    {
        final StringBuffer html = new StringBuffer();

        // Loop thru each characters of the text
        for (int i = 0; i < text.length(); i++)
        {
            // Convert character to HTML/XML
            html.append(charToCDATA(text.charAt(i)));
        }

        // Return HTML/XML string
        return html.toString();
    }

    /**
     * Converts a character to CDATA character.
     * @param ch The character to convert.
     * @return The converted string.
     */
    public static final String charToCDATA(char ch)
    {
        int c;

        if (ch >= 128)
        {
            c = ch;

            return ("&#" + c + ';');
        }

        // No conversion
        else
        {
            // Return character as string
            return (String.valueOf(ch));
        }
    }
    
    public static final String encode(String s) {
    	try {
			return URLEncoder.encode(s, "utf-8");
    	} catch(UnsupportedEncodingException e) {
    		return s;
    	}
    }

    /**
     * @param string
     * @return
     */
    public static int stringToInt(String string)
    {
        try
        {
            return Integer.valueOf(string).intValue();
        }
        catch (NumberFormatException e)
        {
            mLogger.debug("Invalid Integer:" + string);
        }
        return 0;
    }
}
