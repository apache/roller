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
 * Created on Nov 2, 2003
 *
 */
package org.roller.presentation.velocity.plugins.bookmarks;

import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.velocity.context.Context;
import org.roller.RollerException;
import org.roller.model.BookmarkManager;
import org.roller.model.RollerFactory;
import org.roller.pojos.BookmarkData;
import org.roller.pojos.FolderData;
import org.roller.pojos.WeblogEntryData;
import org.roller.model.PagePlugin;
import java.util.regex.PatternSyntaxException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.Collection;
import java.util.Iterator;
import org.roller.pojos.WebsiteData;

/**
 * @author lance
 * @author Anil Gangolli (significant modifications)
 */
public class BookmarkPlugin implements PagePlugin
{
    protected String name = "Bookmark Linker";
    protected String description = "Automatically uses your Bookmarks to " +
        "create links.  Simply use the Name of a Bookmark and it will be " +
        "converted into a hyperlink using the Bookmark's URL.";

    private static Log mLogger =
       LogFactory.getFactory().getInstance(BookmarkPlugin.class);

    public BookmarkPlugin()
    {
        mLogger.debug("BookmarkPlugin instantiated.");
    }

    public String toString() { return name; }

    /* (non-Javadoc)
     * @see org.roller.presentation.velocity.PagePlugin#init(org.roller.presentation.RollerRequest, org.apache.velocity.context.Context)
     */
    public void init(
            WebsiteData website,
            Object config,
            String baseURL,
            Context ctx) throws RollerException
    {
    }

    /*
     * Without Website cannot lookup Folders & Bookmarks
     * (non-Javadoc)
     * @see org.roller.presentation.velocity.PagePlugin#render(java.lang.String)
     */
    public String render(String text)
    {
        return text;
    }

    public String render(WeblogEntryData entry, String str)
    {
        String text = str;        
        try
        {
            BookmarkManager bMgr = RollerFactory.getRoller().getBookmarkManager();
            FolderData rootFolder = bMgr.getRootFolder(entry.getWebsite());
            text = matchBookmarks(text, rootFolder);
            text = lookInFolders(text, rootFolder.getFolders());
        }
        catch (RollerException e)
        {
            // nothing much I can do, go with default "Weblog" value
            // could be RollerException or NullPointerException
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
    private String lookInFolders(String text, Collection folders)
    {
        Iterator it = folders.iterator();
        while (it.hasNext())
        {
            FolderData folder = (FolderData)it.next();
            text = matchBookmarks(text, folder);

            try
            {
                if (!folder.getFolders().isEmpty())
                {
                    lookInFolders(text, folder.getFolders());
                }
            }
            catch (RollerException e)
            {
                mLogger.error("Error getting child Folders");
            }
        }
        return text;
    }

    private String matchBookmarks(String text, FolderData folder)
    {
        Iterator bookmarks = folder.getBookmarks().iterator();
        String workingText = text;
        while (bookmarks.hasNext())
        {
            BookmarkData bookmark = (BookmarkData)bookmarks.next();
            String bkDescription = bookmark.getDescription();
            if (bkDescription == null) bkDescription = "";
            String bookmarkLink = "<a href=\"" +
                bookmark.getUrl() + "\" title=\"" +
                bkDescription + "\">" +
                bookmark.getName() + "</a>";
            try
            {
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
                while (m.find())
                {
                    if (m.group(1) != null)
                    {
                        // self-closed anchor tag <a  ... /> -- ignore
                    }
                    else if (m.group(2) != null)
                    {
                        // matched opening anchor tag <a ...>
                        inLink++;
                    }
                    else if (m.group(3) != null)
                    {
                        // closing anchor tag </a>, but ignore nonmatching ones
                        if (inLink > 0) inLink--;
                    }
                    else if (m.group(4) != null)
                    {
                        // matched the bookmark -- replace, but only if not within a link tag.
                        if (inLink == 0) m.appendReplacement(textBuf, bookmarkLink);
                    }
                    // Any remaining case indicates a bug.  One could add an else with assertion here.  Conservatively don't substitute.
                }
                m.appendTail(textBuf);
                workingText = textBuf.toString();
            }
            catch (PatternSyntaxException e)
            {
                // Can happen since we don't escape pattern the bookmark name to protect pattern characters.
                mLogger.warn("Failed to substitute for bookmark [" + bookmark.getName() + "] due to regular expression characters.");
            }
        }
        return workingText.toString();
    }

    public String getName() { return name; }
    public String getDescription() { return StringEscapeUtils.escapeJavaScript(description); }

    public boolean getSkipOnSingleEntry() {return false;}
}
