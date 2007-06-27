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

package org.apache.roller.weblogger.business.plugins.entry;

import java.util.Iterator;
import java.util.Map;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.roller.weblogger.WebloggerException;
import org.apache.roller.weblogger.business.WebloggerFactory;
import org.apache.roller.weblogger.business.UserManager;
import org.apache.roller.weblogger.business.plugins.entry.WeblogEntryPlugin;
import org.apache.roller.weblogger.pojos.WeblogEntry;
import org.apache.roller.weblogger.pojos.WeblogTemplate;
import org.apache.roller.weblogger.pojos.Weblog;


/**
 * Adds full text to pre-defined acronyms.
 *
 * Example: HTML would become &lt;acronym title="Hyper Text Markup Language"&gt;HTML&lt;/acronym&gt;
 *
 * @author <a href="mailto:molen@mail.com">Jaap van der Molen</a>
 * @version $Revision: 1.3 $
 */
public class AcronymsPlugin implements WeblogEntryPlugin {
    
    private static final Log mLogger = LogFactory.getLog(AcronymsPlugin.class);
    
    protected String name = "Acronyms";
    protected String description = "Expands acronyms defined in _acronym page. " +
            "Example: definition 'HTML=Hyper Text Markup Language' " +
            "becomes &lt;acronym title='Hyper Text Markup Language'&gt;HTML&lt;/acronym&gt;. " +
            "You must create an " +
            "<a href='page.do?method=editPages&rmik=tabbedmenu.website.pages'>" +
            "_acronym page</a> to use Acronyms.";
    
    
    public AcronymsPlugin() {
        super();
        mLogger.debug("AcronymsPlugin instantiated.");
    }
    
    
    public String getName() {
        return name;
    }
    
    
    public String getDescription() {
        return StringEscapeUtils.escapeJavaScript(description);
    }
    
    
    public void init(Weblog website) throws WebloggerException {}
    
    
    public String render(WeblogEntry entry, String str) {
        String text = str;
        
        if (mLogger.isDebugEnabled()) {
            mLogger.debug("render(entry = "+entry.getId()+")");
        }
        
        /*
         * Get acronyms Properties.
         */
        Properties acronyms = loadAcronyms(entry.getWebsite());
        mLogger.debug("acronyms.size()=" + acronyms.size());
        if (acronyms.size() == 0) {
            return text;
        }
        
        /*
         * Compile the user's acronyms into RegEx patterns.
         */
        Pattern[] acronymPatterns = new Pattern[acronyms.size()];
        String[] acronymTags = new String[acronyms.size()];
        int count = 0;
        for (Iterator iter = acronyms.keySet().iterator(); iter.hasNext();) {
            String acronym = (String) iter.next();
            acronymPatterns[count] = Pattern.compile("\\b" + acronym + "\\b");
            mLogger.debug("match '" + acronym + "'");
            acronymTags[count] =
                    "<acronym title=\""
                    + acronyms.getProperty(acronym)
                    + "\">"
                    + acronym
                    + "</acronym>";
            count++;
        }
        
        // if there are none, no work to do
        if (acronymPatterns == null || acronymPatterns.length == 0) {
            return text;
        }
        
        return matchAcronyms(text, acronymPatterns, acronymTags);
    }
    
    
    /**
     * Look for any _acronyms Page and parse it into Properties.
     * @param website
     * @return
     * @throws WebloggerException
     */
    private Properties loadAcronyms(Weblog website) {
        Properties acronyms = new Properties();
        try {
            UserManager userMgr = WebloggerFactory.getRoller().getUserManager();
            WeblogTemplate acronymsPage = userMgr.getPageByName(
                    website, "_acronyms");
            if (acronymsPage != null) {
                acronyms = parseAcronymPage(acronymsPage, acronyms);
            }
        } catch (WebloggerException e) {
            // not much we can do about it
            mLogger.warn(e);
        }
        return acronyms;
    }
    
    
    /**
     * Iterates through the acronym properties and replaces matching
     * acronyms in the entry text with acronym html-tags.
     *
     * @param text entry text
     * @param acronyms user provided set of acronyms
     * @return entry text with acronym explanations
     */
    private String matchAcronyms(String text, Pattern[] acronymPatterns, String[] acronymTags) {
        if (mLogger.isDebugEnabled()) {
            mLogger.debug("matchAcronyms("+text+")");
        }
        
        Matcher matcher = null;
        for (int i=0; i<acronymPatterns.length; i++) {
            matcher = acronymPatterns[i].matcher(text);
            text = matcher.replaceAll(acronymTags[i]);
        }
        return text;
    }
    
    /**
     * Parse the Template of the provided WeblogTemplate and turns it
     * into a <code>Properties</code> collection.
     *
     * @param acronymPage
     * @return acronym properties (key = acronym, value= full text), empty if Template is empty
     */
    private Properties parseAcronymPage(WeblogTemplate acronymPage, Properties acronyms) {
        String rawAcronyms = acronymPage.getContents();
        
        if (mLogger.isDebugEnabled()) {
            mLogger.debug("parsing _acronyms template: \n'"+rawAcronyms+"'");
        }
        
        String regex = "\n"; // end of line
        String[] lines = rawAcronyms.split(regex);
        
        if (lines != null) {
            for (int i = 0; i < lines.length; i++) {
                int index = lines[i].indexOf('=');
                if (index > 0) {
                    String key = lines[i].substring(0, index).trim();
                    String value =
                            lines[i].substring(index + 1, lines[i].length()).trim();
                    acronyms.setProperty(key, value);
                }
            }
        }
        
        return acronyms;
    }
    
}
