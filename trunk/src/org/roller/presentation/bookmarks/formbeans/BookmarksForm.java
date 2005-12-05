/*
 * Created on Oct 21, 2003
 */
package org.roller.presentation.bookmarks.formbeans;

import org.apache.struts.action.ActionForm;

/**
 * @struts.form name="bookmarksForm"
 */ 
public class BookmarksForm extends ActionForm
{
    private String folderId = null; 
    private boolean moveContents = false; 
    private String moveToFolderId = null; 
    private String[] selectedBookmarks = null;
    private String[] selectedFolders = null;
    
    public String getFolderId()
    {
        return folderId;
    }

    public void setFolderId(String folderId)
    {
        this.folderId = folderId;
    }

    public boolean isMoveContents()
    {
        return moveContents;
    }

    public void setMoveContents(boolean moveContents)
    {
        this.moveContents = moveContents;
    }

    public String getMoveToFolderId()
    {
        return moveToFolderId;
    }

    public void setMoveToFolderId(String moveToFolderId)
    {
        this.moveToFolderId = moveToFolderId;
    }

    public String[] getSelectedBookmarks()
    {
        return selectedBookmarks;
    }

    public void setSelectedBookmarks(String[] selectedBookmarks)
    {
        this.selectedBookmarks = selectedBookmarks;
    }

    public String[] getSelectedFolders()
    {
        return selectedFolders;
    }

    public void setSelectedFolders(String[] selectedFolders)
    {
        this.selectedFolders = selectedFolders;
    }

}
