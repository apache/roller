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
import org.roller.presentation.RollerRequest;
import org.roller.presentation.velocity.PagePlugin;

import java.util.Collection;
import java.util.Iterator;

/**
 * @author lance
 *
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
    public void init(RollerRequest rreq, Context ctx) throws RollerException
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

    public String render(WeblogEntryData entry, boolean skipFlag)
    {
        String text = entry.getText();        
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
        BookmarkData bookmark;
        String regEx;
        String link;
        String bkDescription;
        Iterator bookmarks = folder.getBookmarks().iterator();
        while (bookmarks.hasNext())
        {
            bookmark = (BookmarkData)bookmarks.next();
            bkDescription = bookmark.getDescription();
            if (bkDescription == null) bkDescription = "";

            regEx = "\\b" + bookmark.getName() + "\\b";

            link = "<a href=\"" +
                bookmark.getUrl() + "\" title=\"" +
                bkDescription + "\">" +
                bookmark.getName() + "</a>";
            text = text.replaceAll(regEx, link);
        }
        return text;
    }

    public String getName() { return name; }
    public String getDescription() { return StringEscapeUtils.escapeJavaScript(description); }
}
