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

package org.apache.roller.ui.rendering.plugins;

import java.util.Enumeration;
import java.util.Map;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.roller.RollerException;
import org.apache.roller.config.RollerRuntimeConfig;
import org.apache.roller.business.WeblogEntryPlugin;
import org.apache.roller.pojos.WeblogEntryData;
import org.apache.roller.pojos.WebsiteData;

/**
 * Converts ascii emoticons into HTML image tags.
 */
public class SmileysPlugin implements WeblogEntryPlugin {
    
    private static Log mLogger = LogFactory.getLog(SmileysPlugin.class);
    
    public static Pattern[] smileyPatterns = new Pattern[0]; // public for tests
    static String[] imageTags = new String[0];
    private static Properties smileyDefs = new Properties();
    
    private String name = "Emoticons";
    private String description = "Change ASCII emoticons to graphics.  " +
            ":-) becomes <img src='./images/smileys/smile.gif'>";
    
    
    static {
        try {
            smileyDefs.load(SmileysPlugin.class.getResourceAsStream("smileys.properties"));
        } catch (Exception e) {
            mLogger.error("Unable to load smileys.properties", e);
        }
    }

    
    public SmileysPlugin() {
        mLogger.debug("SmileysPlugin instantiated.");
    }
    
    
    public String getName() {
        return name;
    }
    
    
    public String getDescription() {
        return StringEscapeUtils.escapeJavaScript(description);
    }
    
    
    /*
     * Convert the SmileyDefs into RegEx patterns and img tags for
     * later use.  Need an HttpServletRequest though so that we can
     * get the ServletContext Path.  But only do it once.
     */
    public synchronized void init(WebsiteData website) throws RollerException {
        // don't do this work if Smileys already loaded
        if (SmileysPlugin.smileyPatterns.length < 1) {
            String baseURL = RollerRuntimeConfig.getAbsoluteContextURL();
            
            Pattern[] tempP = new Pattern[SmileysPlugin.smileyDefs.size()];
            String[] tempS = new String[SmileysPlugin.smileyDefs.size()];
            //System.out.println("# smileys: " + smileyDefs.size());
            int count = 0;
            Enumeration enum1 = SmileysPlugin.smileyDefs.propertyNames();
            while(enum1.hasMoreElements()) {
                String smiley = (String)enum1.nextElement();
                String smileyAlt = htmlEscape(smiley);
                tempP[count] = Pattern.compile(regexEscape(smiley));
                tempS[count] = "<img src=\"" +
                        baseURL + "/images/smileys/" +
                        smileyDefs.getProperty(smiley, "smile.gif") +
                        "\" class=\"smiley\"" +
                        " alt=\"" + smileyAlt + "\"" +
                        " title=\"" + smileyAlt +"\" />";
                //System.out.println(smiley + "=" + tempS[count]);
                count++;
            }
            SmileysPlugin.smileyPatterns = tempP;
            SmileysPlugin.imageTags = tempS;
        }
    }
    
    
    /**
     * Find occurences of ascii emoticons and turn them into HTML image pointers.
     */
    public String render(WeblogEntryData entry, String text) {
        Matcher matcher = null;
        for (int i=0; i<smileyPatterns.length; i++) {
            matcher = smileyPatterns[i].matcher(text);
            text = matcher.replaceAll(imageTags[i]);
        }
        return text;
    }
    
    
    /*
     * To display the smiley 'glyph' certain characters
     * must be HTML escaped.
     */
    private String htmlEscape(String smiley) {
        char[] chars = smiley.toCharArray();
        StringBuffer buf = new StringBuffer();
        for (int i=0; i<chars.length; i++) {
            if (chars[i] == '"') {
                buf.append("&quot;");
            } else if (chars[i] == '>') {
                buf.append("&gt;");
            } else if (chars[i] == '<') {
                buf.append("&lt;");
            } else {
                buf.append(chars[i]);
            }
        }
        return buf.toString();
    }
    
    /**
     * Some characters have to escaped with a backslash before
     * being compiled into a Regular Expression.
     *
     * @param smiley
     * @return
     */
    private static char[] escape_regex = new char[]
    {'-', '(', ')', '\\', '|', ':', '^', '$', '*', '+', '?',
     '{', '}', '!', '=', '<', '>', '&', '[', ']' };
    
    private String regexEscape(String smiley) {
        char[] chars = smiley.toCharArray();
        StringBuffer buf = new StringBuffer();
        for (int i=0; i<chars.length; i++) {
            for (int x=0; x<escape_regex.length; x++) {
                if (escape_regex[x] == chars[i]) {
                    buf.append("\\");
                    break;
                }
            }
            buf.append(chars[i]);
        }
        return buf.toString();
    }
    
}
