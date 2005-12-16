package org.roller.business.jdo;

import java.util.List;

import org.roller.RollerException;
import org.roller.business.BookmarkManagerImpl;
import org.roller.business.PersistenceStrategy;
import org.roller.pojos.Assoc;
import org.roller.pojos.FolderData;
import org.roller.pojos.WebsiteData;

/**
 * @author Dave Johnson
 */
public class JDOBookmarkManagerImpl extends BookmarkManagerImpl {

    public JDOBookmarkManagerImpl(PersistenceStrategy pstrategy) {
        super(pstrategy);
        // TODO Auto-generated constructor stub
    }

    public boolean isFolderInUse(FolderData folder) throws RollerException {
        // TODO Auto-generated method stub
        return false;
    }

    public List getAllFolders(WebsiteData wd) throws RollerException {
        // TODO Auto-generated method stub
        return null;
    }

    public FolderData getRootFolder(WebsiteData website) throws RollerException {
        // TODO Auto-generated method stub
        return null;
    }

    public List retrieveBookmarks(FolderData data, boolean subfolders)
            throws RollerException {
        // TODO Auto-generated method stub
        return null;
    }

    public boolean isDuplicateFolderName(FolderData data)
            throws RollerException {
        // TODO Auto-generated method stub
        return false;
    }

    public Assoc getFolderParentAssoc(FolderData data) throws RollerException {
        // TODO Auto-generated method stub
        return null;
    }

    public List getFolderChildAssocs(FolderData data) throws RollerException {
        // TODO Auto-generated method stub
        return null;
    }

    public List getAllFolderDecscendentAssocs(FolderData data)
            throws RollerException {
        // TODO Auto-generated method stub
        return null;
    }

    public List getFolderAncestorAssocs(FolderData data) throws RollerException {
        // TODO Auto-generated method stub
        return null;
    }

    public boolean isDescendentOf(FolderData data, FolderData ancestor)
            throws RollerException {
        // TODO Auto-generated method stub
        return false;
    }

}