/*
 * Copyright 1999,2004 The Apache Software Foundation.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
// Copied from GenerationJava Core Library.
//package com.generationjava.lang;

package org.apache.roller.weblogger.ui.tags;

import org.apache.commons.lang.StringUtils;

/**
 * A set of String library static methods. While extending String or 
 * StringBuffer would have been the nicest solution, that is not 
 * possible, so a simple set of static methods seems the most workable.
 *
 * Method ideas have so far been taken from the PHP4, Ruby and .NET languages.
 *
 * @author bayard@generationjava.com
 * @version 0.4 20010812
 */
final public class StringW {

    /**
     * Quote a string so that it may be used in a regular expression 
     * without any parts of the string being considered as a 
     * part of the regular expression's control characters.
     */
    static public String quoteRegularExpression(String str) {
        // replace ? + * / . ^ $ as long as they're not in character 
        // class. so must be done by hand
        char[] chrs = str.toCharArray();
        int sz = chrs.length;
        StringBuffer buffer = new StringBuffer(2*sz);
        for(int i=0; i<sz; i++) {
            switch(chrs[i]) {
              case '[' :
              case ']' :
              case '?' :
              case '+' :
              case '*' :
              case '/' :
              case '.' :
              case '^' :
              case '$' :
                buffer.append("\\");
              default : 
                buffer.append(chrs[i]);
            }
        }
        return buffer.toString();
    }

    /**
     * Create a word-wrapped version of a String. Wrap at 80 characters and 
     * use newlines as the delimiter. If a word is over 80 characters long 
     * use a - sign to split it.
     */
    static public String wordWrap(String str) {
        return wordWrap(str, 80, "\n", "-", true);
    }
    /**
     * Create a word-wrapped version of a String. Wrap at a specified width and 
     * use newlines as the delimiter. If a word is over the width in lenght 
     * use a - sign to split it.
     */
    static public String wordWrap(String str, int width) {
        return wordWrap(str, width, "\n", "-", true);
    }
    /**
     * Word-wrap a string.
     *
     * @param str   String to word-wrap
     * @param width int to wrap at
     * @param delim String to use to separate lines
     * @param split String to use to split a word greater than width long
     *
     * @return String that has been word wrapped (with the delim inside width boundaries)
     */
  static public String wordWrap(String str, int width, String delim, String split ) {
    return wordWrap(str, width, delim, split, true);
  }
  
    /**
     * Word-wrap a string.
     *
     * @param str   String to word-wrap
     * @param width int to wrap at
     * @param delim String to use to separate lines
     * @param split String to use to split a word greater than width long
     * @param delimInside wheter or not delim should be included in chunk before length reaches width.
     *
     * @return String that has been word wrapped
     */
    static public String wordWrap(String str, int width, String delim,
                                  String split, boolean delimInside) {
        int sz = str.length();

        //        System.err.println( ">>>> inside: " + delimInside  + " sz : " + sz );

        /// shift width up one. mainly as it makes the logic easier
        width++;

        // our best guess as to an initial size
        StringBuffer buffer = new StringBuffer(sz/width*delim.length()+sz);

        // every line might include a delim on the end
        //        System.err.println( "width before: "+ width );
        if ( delimInside ) {
          width = width - delim.length();
        } else {
          width --;
        }
        //        System.err.println( "width after: "+ width );

        int idx = -1;
        String substr = null;

        // beware: i is rolled-back inside the loop
        for(int i=0; i<sz; i+=width) {

            // on the last line
            if(i > sz - width) {
                buffer.append(str.substring(i));
//                System.err.print("LAST-LINE: "+str.substring(i));
                break;
            }

//            System.err.println("loop[i] is: "+i);
            // the current line
            substr = str.substring(i, i+width);
            //            System.err.println( "substr: " + substr );

            // is the delim already on the line
            idx = substr.indexOf(delim);
            //            System.err.println( "i: " + i + " idx : " + idx );
            if(idx != -1) {
                buffer.append(substr.substring(0,idx));
//                System.err.println("Substr: '"substr.substring(0,idx)+"'");
                buffer.append(delim);
                i -= width-idx-delim.length();
                
//                System.err.println("loop[i] is now: "+i);
//                System.err.println("ounfd-whitespace: '"+substr.charAt(idx+1)+"'.");
                // Erase a space after a delim. Is this too obscure?
                if(substr.length() > idx+1) {
                    if(substr.charAt(idx+1) != '\n') {
                        if(Character.isWhitespace(substr.charAt(idx+1))) {
                            i++;
                        }
                    }
                }
//                System.err.println("i -= "+width+"-"+idx);
                continue;
            }

            idx = -1;

            // figure out where the last space is
            char[] chrs = substr.toCharArray();
            for(int j=width; j>0; j--) {
                if(Character.isWhitespace(chrs[j-1])) {
                    idx = j;
//                    System.err.println("Found whitespace: "+idx);
                    break;
                }
            }

            // idx is the last whitespace on the line.
//            System.err.println("idx is "+idx);
            if(idx == -1) {
                for(int j=width; j>0; j--) {
                    if(chrs[j-1] == '-') {
                        idx = j;
//                        System.err.println("Found Dash: "+idx);
                        break;
                    }
                }
                if(idx == -1) {
                    buffer.append(substr);
                    buffer.append(delim);
//                    System.err.print(substr);
//                    System.err.print(delim);
                } else {
                    if(idx != width) {
                        idx++;
                    }
                    buffer.append(substr.substring(0,idx));
                    buffer.append(delim);
//                    System.err.print(substr.substring(0,idx));
//                    System.err.print(delim);
                    i -= width-idx;
                }
            } else {
                /*
                if(force) {
                    if(idx == width-1) {
                        buffer.append(substr);
                        buffer.append(delim);
                    } else {
                        // stick a split in.
                        int splitsz = split.length();
                        buffer.append(substr.substring(0,width-splitsz));
                        buffer.append(split);
                        buffer.append(delim);
                        i -= splitsz;
                    }
                } else {
                */
                    // insert spaces
                    buffer.append(substr.substring(0,idx));
                    buffer.append(StringUtils.repeat(" ",width-idx));
//                    System.err.print(substr.substring(0,idx));
//                    System.err.print(StringUtils.repeat(" ",width-idx));
                    buffer.append(delim);
//                    System.err.print(delim);
//                    System.err.println("i -= "+width+"-"+idx);
                    i -= width-idx;
//                }
            }
        }
//        System.err.println("\n*************");
        return buffer.toString();
    }

    /**
     * Truncates a string nicely. It will search for the first space 
     * after the lower limit and truncate the string there.  It will 
     * also append any string passed as a parameter to the end of the 
     * string.  The hard limit can be specified to forcibily truncate a 
     * string (in the case of an extremely long word or such).  All 
     * HTML/XML markup will be stripped from the string prior to 
     * processing for truncation.
     *
     * @param str         String the string to be truncated.
     * @param lower       int value of the lower limit.
     * @param upper       int value of the upper limit, -1 if no limit is 
     *                    desired. If the uppper limit is lower than the 
     *                    lower limit, it will be adjusted to be same as 
     *                    the lower limit.
     * @param appendToEnd String to be appended to the end of the 
     *                    truncated string. 
     *                    This is appended ONLY if the string was indeed 
     *                    truncated. The append is does not count towards 
     *                    any lower/upper limits.
     *
     * @author timster@mac.com
     */
	public static String truncateNicely(String str, int lower, int upper, String appendToEnd)
	{
		// strip markup from the string
		str = XmlW.removeXml(str);
		
		// quickly adjust the upper if it is set lower than 'lower'
		if(upper < lower) {
			upper = lower;
		}		
		
		// now determine if the string fits within the upper limit
		// if it does, go straight to return, do not pass 'go' and collect $200
		if(str.length() > upper) {
			// the magic location int
			int loc;
		
			// first we determine where the next space appears after lower
			loc = str.lastIndexOf(' ', upper);
			
			// now we'll see if the location is greater than the lower limit
			if(loc >= lower) {
				// yes it was, so we'll cut it off here
				str = str.substring(0, loc);
			} else {
				// no it wasnt, so we'll cut it off at the upper limit
				str = str.substring(0, upper);
			}
	
			// the string was truncated, so we append the appendToEnd String				
			str = str + appendToEnd;
		}
		
		return str;
	}
}
