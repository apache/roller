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

package org.apache.roller.weblogger.business.plugins.entry;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.roller.weblogger.WebloggerException;
import org.apache.roller.weblogger.config.WebloggerConfig;
import org.apache.roller.weblogger.business.BookmarkManager;
import org.apache.roller.weblogger.business.WebloggerFactory;
import org.apache.roller.weblogger.pojos.WeblogBookmark;
import org.apache.roller.weblogger.pojos.WeblogEntry;
import org.apache.roller.weblogger.pojos.Weblog;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.text.FieldPosition;
import java.text.MessageFormat;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;


/**
 * Provides an easy way to write topic tag links for Technorati (or similar services).
 * <p/>
 * Looks for occurrences of topic tag specifiers of the form
 * <pre>
 * <code>topic:{topicbookmark}[tag]</code>  OR    <code>topic:[tag]</code>
 * </pre>
 * and replaces them with a topic tag link of the form:
 * <pre>
 * <code>&lt;a rel="tag" href="site/tag"&gt;tag&lt;/a&gt;</code>
 * </pre>
 * <p/>
 * More information on topic tag links can be found at <a href="http://www.technorati.com">Technorati</a>.
 * <p/>
 * <p/>
 * In the first form, the <code>topicbookmark</code> is used as the name of a bookmark, and the URL from that bookmark
 * entry is used as the <code>site</code> portion in the <code>href</code> of the link.
 * <p/>
 * All folders are searched to find a bookmark with the name specified by <code>topicbookmark</code>. A name must match
 * exactly, ignoring case.  The first matching bookmark is used, and folders may be searched in any order.  The
 * bookmark's URL value can end in a "/" or not; either will work.
 * <p/>
 * The second form is equivalent to using the string "Default Topic Site" as the value of <code>topicbookmark</code>.
 * <p/>
 * If the bookmark lookup fails, then "http://www.technorati.com/tag" is used as the site name in the topic tag link.
 * <p/>
 * You can specify some Roller site-wide properties in the roller.properties or roller-custom.properties to override
 * some of the defaults of this plugin. All of these are optional.
 * <p/>
 * <dl> <dt><code>org.apache.roller.weblogger.presentation.velocity.plugins.topictag.TopicTagPlugin.defaultTopicBookmarkName</code></dt>
 * <dd>Specify the name of the default topic bookmark instead of "Default Topic Site"</dd>
 * <p/>
 * <dt><code>org.apache.roller.weblogger.presentation.velocity.plugins.topictag.TopicTagPlugin.defaultTopicSite</code></dt> <dd>Specify
 * the default site name to be used instead of "http://www.technorati.com" for the case in which all of the lookups
 * fail.</dd>
 * <p/>
 * <dt><code>org.apache.roller.weblogger.presentation.velocity.plugins.topictag.TopicTagPlugin.tagPatternWithBookmark</code></dt> <dd>Can
 * be used to redefine the regular expression used to find a long-form topic tag specifiers in the input.  This pattern
 * corresponds to the "long form" and must have two matching groups.  Group 1 must correspond to the bookmark name.
 * Group 2 must correspond to the tag.</dd>
 * <p/>
 * <dt><code>org.apache.roller.weblogger.presentation.velocity.plugins.topictag.TopicTagPlugin.tagPatternDefaultBookmark</code></dt>
 * <dd>Can be used to redefine the regular expression used to find short-form topic tag specifiers in the input. This
 * pattern must have one matching group, which corresponds to the tag.</dd>
 * <p/>
 * <dt><code>org.apache.roller.weblogger.presentation.velocity.plugins.topictag.TopicTagPlugin.linkFormatString</code></dt> <dd>Can be
 * used to redefine the format of the generated link.  This string is a message format string with three positional
 * parameters.  Parameter <code>{0}</code> represents the site including a trailing "/",  parameter <code>{1}</code>
 * represents the url-encoded tag and parameter <code>{2}</code> represents the original unencoded tag text.</dd>
 * <p/>
 * </dl>
 *
 * @author <a href="mailto:anil@busybuddha.org">Anil Gangolli</a>
 * @version 0.3
 */
public class TopicTagPlugin implements WeblogEntryPlugin
{
    private static final String version = "0.3";
    private static final Log mLogger = LogFactory.getFactory().getInstance(TopicTagPlugin.class);


    // Default values of properties that can be set from the web.xml configuration.
    private String defaultTopicBookmarkName = "Default Topic Site";
    private String defaultTopicSite = "http://www.technorati.com/tag";
    private String tagRegexWithBookmark = "topic:\\{(.*?)\\}\\[(.*?)\\]";
    private String tagRegexWithoutBookmark = "topic:\\[(.*?)\\]";
    private String linkFormatString = "<a rel=\"tag\" href=\"{0}{1}\">{2}</a>";

    // Compiled form of the regular expressions above.  Compiled during the init()
    private Pattern tagPatternWithBookmark;
    private Pattern tagPatternWithoutBookmark;
    private MessageFormat linkFormat;

    // A map of the user's bookmarks (values of type BookmarkData) keyed by name (String).   If the user has multiple
    // bookmarks with the same name in different folders, only one gets used (the last encountered).
    private Map userBookmarks;


    public TopicTagPlugin()
    {
    }

    /**
     * Initialize the plugin instance.   This sets up the configurable properties and default topic site.
     * 
     * @param rreq Plugins may need to access RollerRequest.
     * @param ctx  Plugins may place objects into the Velocity Context.
     * @see PagWeblogEntryPluginit(org.apache.roller.weblogger.presentation.RollerRequest, org.apache.velocity.context.Context)
     */
    public void init(Weblog website) throws WebloggerException
    {
        if (mLogger.isDebugEnabled())
        {
            mLogger.debug("TopicTagPlugin v. " + version);
        }

        // Initialize property settings
        initializeProperties();

        // Build map of the user's bookmarks
        userBookmarks = buildBookmarkMap(website);

        // Determine default topic site from bookmark if present
        WeblogBookmark defaultTopicBookmark = (WeblogBookmark) userBookmarks.get(defaultTopicBookmarkName);
        if (defaultTopicBookmark != null) defaultTopicSite = defaultTopicBookmark.getUrl();

        // Append / to defaultTopicSite if it doesn't have it
        if (!defaultTopicSite.endsWith("/"))
        {
            defaultTopicSite += "/";
        }

        // Compile patterns and make sure they have the correct number of matching groups in them.
        try
        {
            tagPatternWithBookmark = Pattern.compile(tagRegexWithBookmark);
        }
        catch (PatternSyntaxException e)
        {
            throw new WebloggerException("Invalid regular expression for topic tags with bookmark '" +
                tagRegexWithBookmark + "': " + e.getMessage());
        }
        int groupCount = tagPatternWithBookmark.matcher("").groupCount();
        if (groupCount != 2)
        {
            throw new WebloggerException("Regular expression for topic tags with bookmark '" + tagRegexWithBookmark +
                "' contains wrong number of capture groups.  Must have exactly 2.  Contains " + groupCount);
        }

        try
        {
            tagPatternWithoutBookmark = Pattern.compile(tagRegexWithoutBookmark);
        }
        catch (PatternSyntaxException e)
        {
            throw new WebloggerException("Invalid regular expression for topic tags without bookmark '" +
                tagRegexWithoutBookmark + "': " + e.getMessage());
        }
        groupCount = tagPatternWithoutBookmark.matcher("").groupCount();
        if (groupCount != 1)
        {
            throw new WebloggerException("Regular expression for topic tags without bookmark '" + tagRegexWithoutBookmark +
                "' contains wrong number of capture groups.  Must have exactly 1.  Contains " + groupCount);
        }

        // Create link format from format string
        setLinkFormat(new MessageFormat(linkFormatString));
    }

    /**
     * Apply the plugin to the given entry.  Returns the entry text with topic tags expanded.
     *
     * @param entry           WeblogEntry to which plugin should be applied.
     * @param singleEntry     Ignored.
     * @return Results of applying plugin to entry.
     */
    public String render(WeblogEntry entry, String str)
    {
        String entryText = str;
        StringBuffer result = new StringBuffer(entryText.length());
        MessageFormat fmt = getLinkFormat();

        // Replace all of the instances matching the pattern with bookmark specified.
        Matcher m = tagPatternWithBookmark.matcher(entryText);
        while (m.find())
        {
            String bookmark = m.group(1);
            String tag = m.group(2);
            String site = getBookmarkSite(bookmark);
            if (site == null)
            {
                site = getDefaultTopicSite();
            }
            if (!site.endsWith("/"))
            {
                site += "/";
            }
            String link = generateLink(fmt, site, tag);
            m.appendReplacement(result, link);
        }
        m.appendTail(result);

        // Now, in a second phase replace all of the instances matching the pattern without bookmark specified.
        entryText = result.toString();
        result = new StringBuffer(entryText.length());
        m = tagPatternWithoutBookmark.matcher(entryText);
        while (m.find())
        {
            String tag = m.group(1);
            String site = getDefaultTopicSite();
            String link = generateLink(fmt, site, tag);
            m.appendReplacement(result, link);
        }
        m.appendTail(result);

        return result.toString();
    }
    

    /**
     * Returns the human-friendly name of this Plugin. This is what users will see.
     *
     * @return The human-friendly name of this Plugin.
     */
    public String getName()
    {
        // TODO: i18n
        return "Topic Tags";
    }

    /**
     * Briefly describes the function of the Plugin. May contain HTML.
     *
     * @return A brief description of the Plugin.
     */
    public String getDescription()
    {
        // TODO: i18n
        return "Expands topic tags for <a href=\\'http://www.technorati.com\\'>Technorati</a> and similar sites. " +
            "Topic tags are of the form <code>topic:{topicbookmark}[tag]</code>, where <code>topicbookmark</code> " +
            "is the name of a bookmark whose URL will be used for the site name in the topic tag. " +
            "If <code>{topicbookmark}</code> is omitted the plugin will use the URL of the <code>Default Topic Site</code> " +
            "bookmark, if that is defined, otherwise http://www.technorati.com.";
    }

    /**
     * Helper to generate the link from the link format and values of the site and tag.
     *
     * @param fmt  link format.  This should have positional parameters {0} representing site with terminal /,  {1} for
     *             url-encoded-tag, and {2} for visible tag text.
     * @param site base portion of the URL
     * @param tag  tag value
     * @return the generated link as a string
     */
    protected String generateLink(MessageFormat fmt, String site, String tag)
    {
        // Allocate initial capacity of buffer of approximately the right length.
        StringBuffer sb = new StringBuffer(site.length() + tag.length() + getLinkFormatString().length());
        fmt.format(new Object[]{site, urlEncode(tag), tag}, sb, new FieldPosition(0));
        return sb.toString();
    }

    /**
     * Resolve the bookmark name and return the URL from it.  If the bookmark can't be found, return null
     *
     * @param bookmarkName name of the bookmark
     * @return String form of the URL from the bookmark by that name from any of the user's folders, or null if not
     *         found.
     */
    protected String getBookmarkSite(String bookmarkName)
    {
        WeblogBookmark bookmark = (WeblogBookmark) getUserBookmarks().get(bookmarkName);
        return bookmark == null ? null : bookmark.getUrl();
    }


    /**
     * Build the bookmark map.
     * If ignoreBookmarks property is set, an empty map is returned.
     * @return map of the user's bookmarks (type BookmarkData), keyed by name (type String).
     */
    protected Map buildBookmarkMap(Weblog website) throws WebloggerException
    {
        Map bookmarkMap = new HashMap();
        if (WebloggerConfig.getBooleanProperty("plugins.topictag.ignoreBookmarks")) {
            return bookmarkMap;
        }
        if (website == null)
        {
            mLogger.debug("Init called without website.  Skipping bookmark initialization.");
        }
        else
        {
            BookmarkManager bMgr = WebloggerFactory.getWeblogger().getBookmarkManager();
            List bookmarks = bMgr.getBookmarks(bMgr.getRootFolder(website), true);

            for (Iterator i = bookmarks.iterator(); i.hasNext();)
            {
                WeblogBookmark b = (WeblogBookmark) i.next();
                bookmarkMap.put(b.getName(), b);
            }
        }
        return bookmarkMap;
    }


    // Sets up properties.  For better and worse, doesn't use reflection
    private void initializeProperties()
    {
        setDefaultTopicBookmarkName(getSetting("defaultTopicBookmarkName", getDefaultTopicBookmarkName()));
        setDefaultTopicSite(getSetting("defaultTopicSite", getDefaultTopicSite()));
        setTagRegexWithBookmark(getSetting("tagRegexWithBookmark", getTagRegexWithBookmark()));
        setTagRegexWithoutBookmark(getSetting("tagRegexWithoutBookmark", getTagRegexWithoutBookmark()));
        setLinkFormatString(getSetting("linkFormatString", getLinkFormatString()));
    }

    private String getSetting(String propName, String defaultValue)
    {
        String fullPropName = "plugins.topictag." + propName;
        String val = (String) WebloggerConfig.getProperty(fullPropName);
        return (val != null) ? val : defaultValue;
    }


    // Private helper to URL encode the tag text.
    private String urlEncode(String text)
    {
        // URL encode the searchtext
        try
        {
            return URLEncoder.encode(text, "UTF-8");
        }
        catch (UnsupportedEncodingException uex)
        {
            // Should never actually occur for UTF-8.  If it does, we barf bitterly.
            throw new RuntimeException(uex);
        }
    }


    // Property getters and setters


    public String getDefaultTopicSite()
    {
        return defaultTopicSite;
    }

    public void setDefaultTopicSite(String defaultTopicSite)
    {
        this.defaultTopicSite = defaultTopicSite;
    }

    public String getTagRegexWithBookmark()
    {
        return tagRegexWithBookmark;
    }

    public void setTagRegexWithBookmark(String tagRegexWithBookmark)
    {
        this.tagRegexWithBookmark = tagRegexWithBookmark;
    }

    public String getTagRegexWithoutBookmark()
    {
        return tagRegexWithoutBookmark;
    }

    public void setTagRegexWithoutBookmark(String tagRegexWithoutBookmark)
    {
        this.tagRegexWithoutBookmark = tagRegexWithoutBookmark;
    }

    public String getLinkFormatString()
    {
        return linkFormatString;
    }

    public void setLinkFormatString(String linkFormatString)
    {
        this.linkFormatString = linkFormatString;
    }

    public MessageFormat getLinkFormat()
    {
        return linkFormat;
    }

    public void setLinkFormat(MessageFormat linkFormat)
    {
        this.linkFormat = linkFormat;
    }

    public Pattern getTagPatternWithBookmark()
    {
        return tagPatternWithBookmark;
    }

    public void setTagPatternWithBookmark(Pattern tagPatternWithBookmark)
    {
        this.tagPatternWithBookmark = tagPatternWithBookmark;
    }

    public Pattern getTagPatternWithoutBookmark()
    {
        return tagPatternWithoutBookmark;
    }

    public void setTagPatternWithoutBookmark(Pattern tagPatternWithoutBookmark)
    {
        this.tagPatternWithoutBookmark = tagPatternWithoutBookmark;
    }

    public String getDefaultTopicBookmarkName()
    {
        return defaultTopicBookmarkName;
    }

    public void setDefaultTopicBookmarkName(String defaultTopicBookmarkName)
    {
        this.defaultTopicBookmarkName = defaultTopicBookmarkName;
    }

    public Map getUserBookmarks()
    {
        return userBookmarks;
    }

    public void setUserBookmarks(Map userBookmarks)
    {
        this.userBookmarks = userBookmarks;
    }
    
}
