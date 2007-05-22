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

package org.apache.roller.ui.struts2.editor;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.roller.business.BookmarkManager;
import org.apache.roller.business.RollerFactory;
import org.apache.roller.pojos.PermissionsData;
import org.apache.roller.ui.struts2.util.UIAction;
import org.apache.roller.util.cache.CacheManager;


/**
 * Import opml file into bookmarks folder.
 */
public final class BookmarksImport extends UIAction {
    
    private static Log log = LogFactory.getLog(BookmarksImport.class);
    
    // uploaded opml file
    private File opmlFile = null;
    
    // content type of uploaded file
    private String opmlFileContentType = null;
    
    // file name of uploaded file
    private String opmlFileFileName = null;
    
    
    public BookmarksImport() {
        this.actionName = "bookmarksImport";
        this.desiredMenu = "editor";
        this.pageTitle = "bookmarksImport.title";
    }
    
    
    // author perms required
    public short requiredWeblogPermissions() {
        return PermissionsData.AUTHOR;
    }
    
    
    /**
     * Request to import bookmarks
     */
    public String execute() {
        return INPUT;
    }
    
    
    /**
     * Save imported bookmarks.
     */
    public String save() {
        
        BookmarkManager bm = RollerFactory.getRoller().getBookmarkManager();
        
        InputStream stream = null;
        if(getOpmlFile() != null && getOpmlFile().exists()) try {
            
            //only write files out that are less than 4MB
            if (getOpmlFile().length() < (4*1024000)) {
                
                stream = new FileInputStream(getOpmlFile());
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                
                byte[] buffer = new byte[8192];
                int bytesRead = 0;
                while ((bytesRead=stream.read(buffer,0,8192)) != -1) {
                    baos.write(buffer, 0, bytesRead);
                }
                String data = new String(baos.toByteArray());
                
                SimpleDateFormat formatter =
                        new SimpleDateFormat("yyyyMMddHHmmss");
                Date now = new Date();
                String folderName = "imported-" + formatter.format(now);
                
                // Use Roller BookmarkManager to import bookmarks
                bm.importBookmarks(getActionWeblog(), folderName, data);
                RollerFactory.getRoller().flush();
                
                // notify caches
                CacheManager.invalidate(getActionWeblog());
                
                // message to user
                addMessage("bookmarksImport.imported", folderName);
                
                // destroy the temporary file created
                getOpmlFile().delete();
                
                return SUCCESS;
                
            } else {
                String data = "The file is greater than 4MB, "
                        +" and has not been written to stream."
                        +" File Size: "+getOpmlFile().length()+" bytes. "
                        +" This is a limitation of this particular "
                        +" web application";
                addError("bookmarksImport.error", data);
            }
            
        } catch (Exception ex) {
            log.error("ERROR: importing bookmarks", ex);
            // TODO: i18n
            addError("bookmarksImport.error", ex.toString());
        } finally {
            if (stream != null) {
                try {
                    stream.close();
                } catch (Exception e) {
                    log.error("Closing stream",e);
                }
            }
        }
        
        return INPUT;
    }
    
    
    public File getOpmlFile() {
        return opmlFile;
    }
    
    public void setOpmlFile(File opmlFile) {
        this.opmlFile = opmlFile;
    }
    
    public String getOpmlFileContentType() {
        return opmlFileContentType;
    }
    
    public void setOpmlFileContentType(String opmlFileContentType) {
        this.opmlFileContentType = opmlFileContentType;
    }
    
    public String getOpmlFileFileName() {
        return opmlFileFileName;
    }
    
    public void setOpmlFileFileName(String opmlFileFileName) {
        this.opmlFileFileName = opmlFileFileName;
    }
    
}
