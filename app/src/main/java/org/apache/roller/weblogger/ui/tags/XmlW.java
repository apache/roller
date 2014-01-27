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
//package com.generationjava.web;

package org.apache.roller.weblogger.ui.tags;

import org.apache.commons.lang3.StringUtils;

/**
 * XML helping static methods.
 *
 * @author bayard@generationjava.com
 * @version 0.4 20010812
 */
public final class XmlW {

    public static String escapeXml(String str) {
        str = StringUtils.replace(str,"&","&amp;");
        str = StringUtils.replace(str,"<","&lt;");
        str = StringUtils.replace(str,">","&gt;");
        str = StringUtils.replace(str,"\"","&quot;");
        str = StringUtils.replace(str,"'","&apos;");
        return str;
    }

    public static String unescapeXml(String str) {
        str = StringUtils.replace(str,"&amp;","&");
        str = StringUtils.replace(str,"&lt;","<");
        str = StringUtils.replace(str,"&gt;",">");
        str = StringUtils.replace(str,"&quot;","\"");
        str = StringUtils.replace(str,"&apos;","'");
        return str;
    }

    /**
     * Remove any xml tags from a String.
     * Same as HtmlW's method.
     */
    public static String removeXml(String str) {
        int sz = str.length();
        StringBuilder buffer = new StringBuilder(sz);
        boolean inString = false;
        boolean inTag = false;
        for(int i=0; i<sz; i++) {
            char ch = str.charAt(i);
            if(ch == '<') {
                inTag = true;
            } else
            if(ch == '>') {
                inTag = false;
                continue;
            }
            if(!inTag) {
                buffer.append(ch);
            }
        }
        return buffer.toString();
    }

    public static String getContent(String tag, String text) {
        int idx = XmlW.getIndexOpeningTag(tag, text);
        if(idx == -1) {
            return "";
        }
        text = text.substring(idx);
        int end = XmlW.getIndexClosingTag(tag, text);
        idx = text.indexOf('>');
        if(idx == -1) {
            return "";
        }
        return text.substring(idx+1, end);
    }

    public static int getIndexOpeningTag(String tag, String text) {
        return getIndexOpeningTag(tag, text, 0);
    }

    private static int getIndexOpeningTag(String tag, String text, int start) {
        // consider whitespace?
        int idx = text.indexOf("<"+tag, start);
        if(idx == -1) {
            return -1;
        }
        char next = text.charAt(idx+1+tag.length());
        if( (next == '>') || Character.isWhitespace(next) ) {
            return idx;
        } else {
            return getIndexOpeningTag(tag, text, idx+1);
        }
    }

    // Pass in "para" and a string that starts with 
    // <para> and it will return the index of the matching </para>
    // It assumes well-formed xml. Or well enough.
    public static int getIndexClosingTag(String tag, String text) {
        return getIndexClosingTag(tag, text, 0);
    }

    public static int getIndexClosingTag(String tag, String text, int start) {
        String open = "<"+tag;
        String close = "</"+tag+">";
//        System.err.println("OPEN: "+open);
//        System.err.println("CLOSE: "+close);
        int closeSz = close.length();
        int nextCloseIdx = text.indexOf(close, start);
//        System.err.println("first close: "+nextCloseIdx);
        if(nextCloseIdx == -1) {
            return -1;
        }
        int count = StringUtils.countMatches(text.substring(start, nextCloseIdx), open);
//        System.err.println("count: "+count);
        if(count == 0) {
            return -1;  // tag is never opened
        }
        int expected = 1;
        while(count != expected) {
            nextCloseIdx = text.indexOf(close, nextCloseIdx+closeSz);
            if(nextCloseIdx == -1) {
                return -1;
            }
            count = StringUtils.countMatches(text.substring(start, nextCloseIdx), open);
            expected++;
        }
        return nextCloseIdx;
    }

    public static String getAttribute(String attribute, String text) {
        return getAttribute(attribute, text, 0);
    }

    public static String getAttribute(String attribute, String text, int idx) {
         int close = text.indexOf('>', idx);
         int attrIdx = text.indexOf(attribute+"=\"", idx);
         if(attrIdx == -1) {
             return null;
         }
         if(attrIdx > close) {
             return null;
         }
         int attrStartIdx = attrIdx + attribute.length() + 2;
         int attrCloseIdx = text.indexOf("\"", attrStartIdx);
         if(attrCloseIdx > close) {
             return null;
         }
         return unescapeXml(text.substring(attrStartIdx, attrCloseIdx));
    }

}
