/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  The ASF licenses this file to You
 * under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
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

import org.apache.commons.logging.Log;
import org.apache.roller.RollerException;
import org.apache.roller.pojos.WeblogEntry;
import org.apache.roller.pojos.Weblog;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.text.FieldPosition;
import java.text.MessageFormat;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Implements the common portion of search link plugins.
 *
 * @author <a href="mailto:anil@busybuddha.org">Anil Gangolli</a>
 * @version 2.1
 */
public abstract class SearchPluginBase {
    private String baseVersion = "2.1";
    private Log mLogger;

    /**
     * Instantiation is per request.
     */
    public SearchPluginBase() {
        mLogger = getLogger();
    }

    /**
     * Initialize.  Called once for each request.
     *
     * @see org.apache.roller.model.PagePlugin#init(WebsiteData, Object, String baseUrl, org.apache.velocity.context.Context)
     */
    public void init(Weblog website) throws RollerException {
        if (mLogger.isDebugEnabled()) {
            mLogger.debug(getClass().getName() + "; version:  " + getVersion() + "; base version " + baseVersion);
        }
    }
    

    /**
     * Apply plugin to content of specified String.
     *
     * @param str String to which plugin should be applied.
     * @return Results of applying plugin to string.
     * @see org.apache.roller.model.PagePlugin#render(String)
     */
    public String render(WeblogEntry entry, String str) {
        Pattern pattern = getPattern();
        Matcher m = pattern.matcher(str);
        StringBuffer result = new StringBuffer(str.length() + 128);   // rough guess at a reasonable length
        Object[] args = new Object[]{"", "", null, null};
        while (m.find()) {
            // parse out the parts of the match
            String type = m.group(1);
            boolean feelinLucky = type.equals("!");   // are ya feelin lucky? are ya punk?
            String linkText = m.group(2);
            String searchText = m.group(3);
            if (searchText == null || searchText.length() == 0) {
                searchText = linkText;
            }

            // URL-encode the search text
            String encodedSearchText = encodeSearchText(searchText);

            // form the replacement string
            MessageFormat linkFormat = feelinLucky ? getLuckyLinkFormat() : getLinkFormat();
            StringBuffer replacement = new StringBuffer(128);
            args[2] = linkText;
            args[3] = encodedSearchText;
            linkFormat.format(args, replacement, new FieldPosition(0));

            // append replacement
            m.appendReplacement(result, replacement.toString());
        }
        m.appendTail(result);

        return result.toString();
    }

    /**
     * Returns the human-friendly name of this Plugin. This is what users will see.
     *
     * @return The human-friendly name of this Plugin.
     * @see org.apache.roller.model.PagePlugin#getName()
     */
    public abstract String getName();

    /**
     * Briefly describes the function of the Plugin. May contain HTML.
     *
     * @return A brief description of the Plugin.
     * @see org.apache.roller.model.PagePlugin#getDescription()
     */
    public abstract String getDescription();

    /**
     * Return the logger for this class.
     *
     * @return the logger for this class.
     */
    protected abstract Log getLogger();

    /**
     * Return the implementation version.
     *
     * @return the implementation version.
     */
    protected abstract String getVersion();

    /**
     * Get the regexp pattern for finding search links in the input text.   Three matching groups are expected: (1) The
     * lucky or not indicator (either <code>!</code> or <code>:</code>) (2) the link text (3) the search text (optional,
     * defaults to the link text).
     *
     * @return the regexp pattern for finding search links in the input text
     */
    protected abstract Pattern getPattern();

    /**
     * The MessageFormat for the replacement string (actual HTML link) that will form the replacement in the regular
     * (non-"lucky") case.  This must have two positional parameters "{2} and {3}" which are the link text and
     * (URL-encoded) search text from the regexp pattern.  Note that the parameters "{0}" and "{1}" are not used. They
     * will be empty strings.
     *
     * @return the message format for non-"lucky" search links.
     */
    protected abstract MessageFormat getLinkFormat();

    /**
     * The MessageFormat for the replacement string (actual HTML link) that will form the replacement in the "lucky"
     * case.  This must have two positional parameters "{2} and {3}" which are the link text and (URL-encoded) search
     * text from the regexp pattern.  Note that the parameters "{0}" and "{1}" are not used. They will be empty
     * strings.
     *
     * @return the message format for "lucky" search links.
     */
    protected abstract MessageFormat getLuckyLinkFormat();


    // Private helper to URL encode the search text.
    private String encodeSearchText(String searchText) {
        // URL encode the searchtext
        try {
            return URLEncoder.encode(searchText, "UTF-8");
        } catch (UnsupportedEncodingException uex) {
            // By Java spec, this should never actually occur for UTF-8.  If it does, we barf bitterly.
            throw new RuntimeException(uex);
        }
    }
    
}
