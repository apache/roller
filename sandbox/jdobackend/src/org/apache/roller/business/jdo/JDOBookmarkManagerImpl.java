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
package org.apache.roller.business.jdo;

import java.util.List;

import org.apache.roller.RollerException;
import org.apache.roller.business.BookmarkManagerImpl;
import org.apache.roller.business.PersistenceStrategy;
import org.apache.roller.pojos.Assoc;
import org.apache.roller.pojos.FolderData;
import org.apache.roller.pojos.WebsiteData;

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