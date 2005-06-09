
package org.roller.model;

import org.roller.RollerException;
import org.roller.pojos.Assoc;
import org.roller.pojos.BookmarkData;
import org.roller.pojos.FolderData;
import org.roller.pojos.WebsiteData;

import java.io.Serializable;
import java.util.List;


/** 
 * Interface to Bookmark Management. Provides methods for retrieving, storing,
 * moving, removing and querying for folders and bookmarks.
 * 
 * @author David M Johnson
 */
public interface BookmarkManager extends Serializable
{
	//---------------------------------------------------------- Bookmark CRUD

    /** Create new bookmark, NOT a persistent instance. */
    public BookmarkData createBookmark();
    
    /** Create new bookmark, NOT a persistent instance. */
    public BookmarkData createBookmark(
        FolderData parent, 
        String name, 
        String desc, 
        String url, 
        String feedUrl,
        Integer weight, 
        Integer priority, 
        String image) throws RollerException;

    /** Retrieve bookmark by ID, a persistent instance. */
    public BookmarkData retrieveBookmark(String id) throws RollerException;

	/** Update or store bookmark object, becomes a persistent instance. */
	//public void storeBookmark(BookmarkData data) throws RollerException;

	/** Delete bookmark by ID. */
	public void removeBookmark(String id) throws RollerException;

	//------------------------------------------------------------ Folder CRUD 

    /** Create new folder, NOT a persistent instance. */
    public FolderData createFolder();
    
    /** Create new folder, NOT a persistent instance. */
    public FolderData createFolder(
        FolderData parent,
        String name, 
        String desc, 
        WebsiteData website) throws RollerException;

    /** Retrieve folder by ID, a persistent instance. */
    public FolderData retrieveFolder(String id) throws RollerException;

    /**
     * Determine if folder is in use.  A folder is <em>in use</em> if it contains any bookmarks
     * or has any children.
     * @param folder
     * @return true if the folder contains bookmarks or has children, false otherwise.
     * @throws RollerException
     */
    public boolean isFolderInUse(FolderData folder) throws RollerException;
    
	/** Store folder object, becomes a persistent instance. */
	//public void storeFolder(FolderData data) throws RollerException;

	/** 
     * Remove folder by ID, does not cascade to subfolders and bookmarks.
     * You should instead use FolderData.remove() to remove a folder. 
     */
	//public void removeFolder(String id) throws RollerException;
	
    //------------------------------------------------------------- Operations

    /** Import bookmarks from OPML string into specified folder. 
      * @param site Website.
      * @param folder Name of folder to hold bookmarks.
      * @param opml OPML data to be imported. 
      */
    public void importBookmarks(WebsiteData site, String folder, String opml) 
        throws RollerException;
    
	/** Move contents of folder to another folder.
	  * @param src Source folder.
	  * @param dest Destination folder.
	  */
	public void moveFolderContents(FolderData src, FolderData dest) 
		throws RollerException;

    public void deleteFolderContents(FolderData src) throws RollerException;
    
    //---------------------------------------------------------------- Queries 

    /** Get all folders for a website.
      * @param website Website.
      */
    public List getAllFolders(WebsiteData wd) throws RollerException;

    /** Get top level folders for a website.
      * @param website Website.
      */
    public FolderData getRootFolder(WebsiteData website) 
        throws RollerException;

	/** Get folder specified by website and folderPath. 
	  * @param website Website of folder.
	  * @param folderPath Path of folder, relative to folder root.
	  */
	public FolderData getFolder(WebsiteData website, String folderPath) 
		throws RollerException;

    /**
     * Get absolute path to folder, appropriate for use by getFolderByPath().
     * @param folder Folder.
     * @return Forward slash separated path string.
     */
    public String getPath(FolderData folder) throws RollerException;

    /**
     * Get subfolder by path relative to specified folder.
     * @param folder  Root of path or null to start at top of folder tree.
     * @param path    Path of folder to be located.
     * @param website Website of folders.
     * @return FolderData specified by path or null if not found.
     */
    public FolderData getFolderByPath(
        WebsiteData wd, FolderData folder, String string) 
        throws RollerException;

    /**
     * @param folder
     * @param ancestor
     * @param relation
     * @return
     */
    public Assoc createFolderAssoc(
        FolderData folder, FolderData ancestor, String relation) 
        throws RollerException;
        
    /**
     * @param data
     * @param subfolders
     * @return
     */
    public List retrieveBookmarks(FolderData data, boolean subfolders)
        throws RollerException;

    /**
     * Check duplicate folder name. 
     */
    public boolean isDuplicateFolderName(FolderData data) throws RollerException;

    /**
     */
    public Assoc getFolderParentAssoc(FolderData data) throws RollerException;

    /**
     */
    public List getFolderChildAssocs(FolderData data) throws RollerException;

    /**
     */
    public List getAllFolderDecscendentAssocs(FolderData data) throws RollerException;

    /**
     */
    public List getFolderAncestorAssocs(FolderData data) throws RollerException;

    public void release();

    public boolean isDescendentOf(FolderData data, FolderData ancestor) throws RollerException;
}

