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

package org.apache.roller.ui.authoring.struts.formbeans;

import org.apache.struts.upload.FormFile;
import org.apache.roller.RollerException;
import org.apache.roller.pojos.FolderData;
import org.apache.roller.ui.authoring.struts.forms.FolderForm;


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

