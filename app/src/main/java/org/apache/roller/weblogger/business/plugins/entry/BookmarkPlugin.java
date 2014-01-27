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

import java.util.Collection;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;
import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.roller.weblogger.WebloggerException;
import org.apache.roller.weblogger.business.BookmarkManager;
import org.apache.roller.weblogger.business.WebloggerFactory;
import org.apache.roller.weblogger.pojos.WeblogBookmark;
import org.apache.roller.weblogger.pojos.WeblogBookmarkFolder;
import org.apache.roller.weblogger.pojos.WeblogEntry;
import org.apache.roller.weblogger.pojos.Weblog;


/**
 * Automatically insert links into entry text based on users bookmarks.
 */
public class BookmarkPlugin implements WeblogEntryPlugin {
    
    private static Log mLogger = LogFactory.getLog(BookmarkPlugin.class);
    
    protected String name = "Bookmark Linker";
    protected String description = "Automatically uses your Bookmarks to " +
            "create links.  Simply use the Name of a Bookmark and it will be " +
            "converted into a hyperlink using the Bookmark's URL.";
    
    
    public BookmarkPlugin() {
        mLogger.debug("BookmarkPlugin instantiated.");
    }
    
    
    public String getName() {
        return name;
    }
    
    
    public String getDescription() {
        return StringEscapeUtils.escapeEcmaScript(description);
    }
    
    
    public void init(Weblog website) throws WebloggerException {}
    
    
    public String render(WeblogEntry entry, String str) {
        String text = str;
        try {
            BookmarkManager bMgr = WebloggerFactory.getWeblogger().getBookmarkManager();
            WeblogBookmarkFolder rootFolder = bMgr.getRootFolder(entry.getWebsite());
            text = matchBookmarks(text, rootFolder);
            text = lookInFolders(text, rootFolder.getFolders());
        } catch (WebloggerException e) {
            // nothing much I can do, go with default "Weblog" value
            // could be WebloggerException or NullPointerException
            mLogger.warn(e);
        }
        return text;
    }
    
    
    /**
     * Recursively travel down Folder tree, attempting
     * to match up Bookmarks in each Folder.
     *
     * @param text
     * @param folders
     * @return
     */
    private String lookInFolders(String text, Collection<WeblogBookmarkFolder> folders) {
        
        for (WeblogBookmarkFolder folder: folders) {
            text = matchBookmarks(text, folder);

            if (!folder.getFolders().isEmpty()) {
                lookInFolders(text, folder.getFolders());
            }
        }

        return text;
    }
    
    
    private String matchBookmarks(String text, WeblogBookmarkFolder folder) {
        String workingText = text;
        for (WeblogBookmark bookmark : folder.getBookmarks()) {
            String bkDescription = bookmark.getDescription();
            if (bkDescription == null) {
                bkDescription = "";
            }
            String bookmarkLink = "<a href=\"" +
                    bookmark.getUrl() + "\" title=\"" +
                    bkDescription + "\">" +
                    bookmark.getName() + "</a>";
            try {
                // Replace all occurrences of bookmark name that don't occur within the bounds of an anchor tag
                // Notes:
                // - use reluctant quantifiers on the tags to avoid gobbling more than desired
                // - use non-capturing groups for boundaries to avoid replacing the boundary as well as the bookmark name.
                // - we depend on the numbering of the specific groups in this expression in the replacement code below.
                // TODO: should escape the bookmark name
                String regEx = "(<a(?:\\s.*?)??/>)|(<a(?:\\s.*?)??>)|(</a(?:\\s.*?)??>)|(?:\\b)(" + bookmark.getName() + ")(?:\\b)";
                Matcher m = Pattern.compile(regEx).matcher(workingText);
                StringBuffer textBuf = new StringBuffer(workingText.length());
                int inLink = 0;
                while (m.find()) {
                    if (m.group(1) != null) {
                        // self-closed anchor tag <a  ... /> -- ignore
                    } else if (m.group(2) != null) {
                        // matched opening anchor tag <a ...>
                        inLink++;
                    } else if (m.group(3) != null) {
                        // closing anchor tag </a>, but ignore nonmatching ones
                        if (inLink > 0) {
                            inLink--;
                        }
                    } else if (m.group(4) != null) {
                        // matched the bookmark -- replace, but only if not within a link tag.
                        if (inLink == 0) {
                            m.appendReplacement(textBuf, bookmarkLink);
                        }
                    }
                    // Any remaining case indicates a bug.  One could add an else with assertion here.  Conservatively don't substitute.
                }
                m.appendTail(textBuf);
                workingText = textBuf.toString();
            } catch (PatternSyntaxException e) {
                // Can happen since we don't escape pattern the bookmark name to protect pattern characters.
                mLogger.warn("Failed to substitute for bookmark [" + bookmark.getName() + "] due to regular expression characters.");
            }
        }
        return workingText;
    }
    
}
