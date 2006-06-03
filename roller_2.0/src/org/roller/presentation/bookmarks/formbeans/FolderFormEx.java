
package org.roller.presentation.bookmarks.formbeans;

import org.apache.struts.upload.FormFile;
import org.roller.RollerException;
import org.roller.pojos.FolderData;
import org.roller.presentation.forms.FolderForm;


/**
 * Extends the FolderForm so that additional properties may be added.
 * These properties are not persistent and are only needed for the UI.
 *  
 * @struts.form name="folderFormEx"
 */ 
public class FolderFormEx extends FolderForm
{
	private boolean mMoveContents = false; 
	private String mMoveToFolderId = null; 
    private String[] mSelectedBookmarks = null;
    private String[] mSelectedFolders = null;
    private transient FormFile mBookmarksFile = null;

	public FolderFormEx()
	{
		super();
	}

	public FolderFormEx(FolderData folderData, java.util.Locale locale) throws RollerException
	{
		super(folderData, locale);
	}

    public String getShortenedDesc() 
    {
        if ( getDescription().length() > 20 )
        {
            return getDescription().substring(0,19)+"...";
        }
        return getDescription();
    }

    public void setShortenedDesc( String desc )
    {
        // readonly
    }

    //------------------------------------------------- Property bookmarksFile 

    /** Bookmark file to be imported */
    public void setBookmarksFile(FormFile file) { mBookmarksFile = file; }

    /** Bookmark file to be imported */
    public FormFile getBookmarksFile() { return mBookmarksFile; }

    //-------------------------------------------------- Property moveContents

	/** If true then contents should be moved when this folder is removed */
	public boolean getMoveContents() { return mMoveContents; }

	/** If true then contents should be moved when this folder is removed */
	public void setMoveContents( boolean flag ) { mMoveContents = flag;}

    //------------------------------------------------ Property moveToFolderId

	/** Folder where contents should be moved if this folder is removed */ 
	public String getMoveToFolderId() { return mMoveToFolderId; }

	/** Folder where contents should be moved if this folder is removed */ 
	public void setMoveToFolderId( String id ) { mMoveToFolderId = id;}

    //--------------------------------------------- Property selectedBookmarks 

    /** Get selected bookmarks */
    public String[] getSelectedBookmarks() { return mSelectedBookmarks; }

    /** Set selected bookmarks */
    public void setSelectedBookmarks( String[] b ) { mSelectedBookmarks = b; }

    //--------------------------------------------- Property selectedBookmarks 

    /** Get selected folders */
    public String[] getSelectedFolders() { return mSelectedFolders; }

    /** Set selected bookmarks */
    public void setSelectedFolders( String[] f ) { mSelectedFolders = f; }
}

