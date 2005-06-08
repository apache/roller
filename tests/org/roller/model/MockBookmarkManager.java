/*
 * Created on Mar 4, 2004
 */
package org.roller.model;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.roller.RollerException;
import org.roller.business.PersistenceStrategy;
import org.roller.pojos.Assoc;
import org.roller.pojos.BookmarkData;
import org.roller.pojos.FolderData;
import org.roller.pojos.WebsiteData;

import java.util.List;

/**
 * @author lance.lavandowska
 */
public class MockBookmarkManager implements BookmarkManager
{
    private static Log mLogger =
        LogFactory.getFactory().getInstance(MockUserManager.class);
    
    private PersistenceStrategy mStrategy = null;

    /**
     * @param strategy
     * @param roller
     */
    public MockBookmarkManager(PersistenceStrategy strategy, MockRoller roller)
    {
        mStrategy = strategy;
    }

    /* 
     * @see org.roller.model.BookmarkManager#createBookmark()
     */
    public BookmarkData createBookmark()
    {
        BookmarkData book = new BookmarkData();
        try
        {
            mStrategy.store(book);
        }
        catch (RollerException e)
        {
        }
        return book;
    }

    /* 
     * @see org.roller.model.BookmarkManager#createBookmark(org.roller.pojos.FolderData, java.lang.String, java.lang.String, java.lang.String, java.lang.String, java.lang.Integer, java.lang.Integer, java.lang.String)
     */
    public BookmarkData createBookmark(FolderData parent, String name,
                                       String desc, String url, String feedUrl,
                                       Integer weight, Integer priority,
                                       String image) throws RollerException
    {
        BookmarkData book =  new BookmarkData(parent, name, desc, url, feedUrl,
                                             weight, priority, image);
        try
        {
            mStrategy.store(book);
        }
        catch (RollerException e)
        {
        }
        return book;
    }

    /* 
     * @see org.roller.model.BookmarkManager#retrieveBookmark(java.lang.String)
     */
    public BookmarkData retrieveBookmark(String id) throws RollerException
    {
        return (BookmarkData)mStrategy.load(id, BookmarkData.class);
    }

    /* 
     * @see org.roller.model.BookmarkManager#removeBookmark(java.lang.String)
     */
    public void removeBookmark(String id) throws RollerException
    {
        mStrategy.remove(id, BookmarkData.class);
    }

    /* 
     * @see org.roller.model.BookmarkManager#createFolder()
     */
    public FolderData createFolder()
    {
        FolderData folder = new FolderData();
        try
        {
            mStrategy.store(folder);
        }
        catch (RollerException e)
        {
        }
        return folder;
    }

    /* 
     * @see org.roller.model.BookmarkManager#createFolder(org.roller.pojos.FolderData, java.lang.String, java.lang.String, org.roller.pojos.WebsiteData)
     */
    public FolderData createFolder(FolderData parent, String name, String desc,
                                   WebsiteData website) throws RollerException
    {
        FolderData folder = new FolderData(parent, name, desc, website);
        try
        {
            mStrategy.store(folder);
        }
        catch (RollerException e)
        {
        }
        return folder;
    }

    /* 
     * @see org.roller.model.BookmarkManager#retrieveFolder(java.lang.String)
     */
    public FolderData retrieveFolder(String id) throws RollerException
    {
        return (FolderData)mStrategy.load(id, FolderData.class);
    }

    /* 
     * @see org.roller.model.BookmarkManager#importBookmarks(org.roller.pojos.WebsiteData, java.lang.String, java.lang.String)
     */
    public void importBookmarks(WebsiteData site, String folder, String opml)
            throws RollerException
    {
        // TODO Auto-generated method stub

    }

    /* 
     * @see org.roller.model.BookmarkManager#moveFolderContents(org.roller.pojos.FolderData, org.roller.pojos.FolderData)
     */
    public void moveFolderContents(FolderData src, FolderData dest)
            throws RollerException
    {
        // TODO Auto-generated method stub

    }

    /* 
     * @see org.roller.model.BookmarkManager#getAllFolders(org.roller.pojos.WebsiteData)
     */
    public List getAllFolders(WebsiteData wd) throws RollerException
    {
        // TODO Auto-generated method stub
        return null;
    }

    /* 
     * @see org.roller.model.BookmarkManager#getRootFolder(org.roller.pojos.WebsiteData)
     */
    public FolderData getRootFolder(WebsiteData website) throws RollerException
    {
        // TODO Auto-generated method stub
        return null;
    }

    /* 
     * @see org.roller.model.BookmarkManager#getFolder(org.roller.pojos.WebsiteData, java.lang.String)
     */
    public FolderData getFolder(WebsiteData website, String folderPath)
            throws RollerException
    {
        // TODO Auto-generated method stub
        return null;
    }

    /* 
     * @see org.roller.model.BookmarkManager#getPath(org.roller.pojos.FolderData)
     */
    public String getPath(FolderData folder) throws RollerException
    {
        // TODO Auto-generated method stub
        return null;
    }

    /* 
     * @see org.roller.model.BookmarkManager#getFolderByPath(org.roller.pojos.WebsiteData, org.roller.pojos.FolderData, java.lang.String)
     */
    public FolderData getFolderByPath(WebsiteData wd, FolderData folder,
                                      String string) throws RollerException
    {
        // TODO Auto-generated method stub
        return null;
    }

    /* 
     * @see org.roller.model.BookmarkManager#createFolderAssoc(org.roller.pojos.FolderData, org.roller.pojos.FolderData, java.lang.String)
     */
    public Assoc createFolderAssoc(FolderData folder, FolderData ancestor,
                                   String relation) throws RollerException
    {
        // TODO Auto-generated method stub
        return null;
    }

    /* 
     * @see org.roller.model.BookmarkManager#retrieveBookmarks(org.roller.pojos.FolderData, boolean)
     */
    public List retrieveBookmarks(FolderData data, boolean subfolders)
            throws RollerException
    {
        // TODO Auto-generated method stub
        return null;
    }

    /** 
     * @see org.roller.model.BookmarkManager#isDuplicateFolderName(org.roller.pojos.FolderData)
     */
    public boolean isDuplicateFolderName(FolderData data) throws RollerException
    {
        // TODO Auto-generated method stub
        return false;
    }

    /** 
     * @see org.roller.model.BookmarkManager#getFolderParentAssoc(org.roller.pojos.FolderData)
     */
    public Assoc getFolderParentAssoc(FolderData data) throws RollerException
    {
        // TODO Auto-generated method stub
        return null;
    }

    /** 
     * @see org.roller.model.BookmarkManager#getFolderChildAssocs(org.roller.pojos.FolderData)
     */
    public List getFolderChildAssocs(FolderData data) throws RollerException
    {
        // TODO Auto-generated method stub
        return null;
    }

    /** 
     * @see org.roller.model.BookmarkManager#getAllFolderDecscendentAssocs(org.roller.pojos.FolderData)
     */
    public List getAllFolderDecscendentAssocs(FolderData data) throws RollerException
    {
        // TODO Auto-generated method stub
        return null;
    }

    /** 
     * @see org.roller.model.BookmarkManager#getFolderAncestorAssocs(org.roller.pojos.FolderData)
     */
    public List getFolderAncestorAssocs(FolderData data) throws RollerException
    {
        // TODO Auto-generated method stub
        return null;
    }

    /* 
     * @see org.roller.model.BookmarkManager#deleteFolderContents(org.roller.pojos.FolderData)
     */
    public void deleteFolderContents(FolderData src) throws RollerException
    {
        // TODO Auto-generated method stub
        
    }

}
