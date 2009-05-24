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

package org.apache.roller.weblogger.ui.struts2.editor;

import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.roller.weblogger.business.FileIOException;
import org.apache.roller.weblogger.business.FileNotFoundException;
import org.apache.roller.weblogger.business.FilePathException;
import org.apache.roller.weblogger.business.FileManager;
import org.apache.roller.weblogger.business.WebloggerFactory;
import org.apache.roller.weblogger.config.WebloggerRuntimeConfig;
import org.apache.roller.weblogger.pojos.ThemeResource;
import org.apache.roller.weblogger.pojos.WeblogPermission;
import org.apache.roller.weblogger.ui.struts2.util.UIAction;
import org.apache.roller.weblogger.util.RollerMessages;
import org.apache.roller.weblogger.util.RollerMessages.RollerMessage;


/**
 * Manage user uploaded resources.
 */
public final class Resources extends UIAction {
    
    private static Log log = LogFactory.getLog(Resources.class);
    
    // path under resources directory we are viewing
    private String path = null;
    
    // an array of files uploaded by the user, if applicable
    private File[] uploadedFiles = null;
    
    // an array of content types for upload files
    private String[] uploadedFilesContentType = null;
    
    // an array of filenames for uploaded files
    private String[] uploadedFilesFileName = null;
    
    // name of new subdir to create, if applicable
    private String newDir = null;
    
    // ids of files to delete, if applicable
    private String[] deleteIds = null;
    
    // list of files to display
    private List files = Collections.EMPTY_LIST;
    
    // is the weblog over the file quota
    private boolean overQuota = false;
    
    // total size of all files for weblog
    private long totalSize = 0;
    
    
    public Resources() {
        this.actionName = "resources";
        this.desiredMenu = "editor";
        this.pageTitle = "uploadFiles.title";
    }
    
    
    // requires author role
    public List<String> requiredWeblogPermissionActions() {
        return Collections.singletonList(WeblogPermission.POST);
    }
    
    
    /**
     * Display weblog resources.
     */
    public String execute()  {
        
        FileManager fmgr = WebloggerFactory.getWeblogger().getFileManager();
        
        setOverQuota(fmgr.overQuota(getActionWeblog()));
        
        try {
            // get files, add them to the list
            long totalSize = 0;
            ThemeResource[] resources = fmgr.getFiles(getActionWeblog(), getPath());
            log.debug(resources.length+" files found");
            for (int i=0; i<resources.length; i++) {
                totalSize += resources[i].getLength();
            }
            List filesList = new ArrayList(Arrays.asList(resources));
            
            if(getPath() == null) {
                ThemeResource[] dirs = fmgr.getDirectories(getActionWeblog());
                filesList.addAll(Arrays.asList(dirs));
            }
            
            // sort 'em
            Collections.sort(filesList, new WeblogResourceComparator());
            
            setFiles(filesList);
            setTotalSize(totalSize);
            
        } catch (Exception ex) {
            log.error("Error getting files list for weblog - "+getActionWeblog().getHandle(), ex);
            // TODO: i18n
            addError("Error getting files list");
        }
        
        return LIST;
    }
    
    
    /**
     * Create a subdirectory.
     */
    public String createSubdir() {
        
        String path = getPath();
        String newDir = getNewDir();
        if(newDir != null &&
                newDir.trim().length() > 0 &&
                newDir.indexOf("/") == -1 &&
                newDir.indexOf("\\") == -1 &&
                newDir.indexOf("..") == -1) {
            
            // figure the new directory path
            String newDirPath = newDir;
            if(path != null && path.trim().length() > 0) {
                newDirPath = path + "/" + newDir;
            }
            
            try {
                FileManager fmgr = WebloggerFactory.getWeblogger().getFileManager();
                
                // add the new subdirectory
                fmgr.createDirectory(getActionWeblog(), newDirPath);
                
                addMessage("uploadFiles.createdDir", newDirPath);
                
                // reset newDir prop so it doesn't autopopulate on the form
                setNewDir(null);
                
            } catch (FilePathException ex) {
                addError("uploadFiles.error.badPath", newDirPath);
            } catch (FileNotFoundException ex) {
                addError("uploadFiles.error.badPath", newDirPath);
            } catch (FileIOException ex) {
                addError("uploadFiles.error.createDir", newDirPath);
            }
        } else {
            addError("uploadFiles.error.badPath", newDir);
        }
        
        return execute();
    }
    
    
    /**
     * Upload selected file(s).
     */
    public String upload() {
        
        // make sure uploads are enabled
        if(!WebloggerRuntimeConfig.getBooleanProperty("uploads.enabled")) {
            addError("error.upload.disabled");
            return execute();
        }
            
        FileManager fmgr = WebloggerFactory.getWeblogger().getFileManager();

        RollerMessages errors = new RollerMessages();
        List<String> uploaded = new ArrayList();
        File[] uploads = getUploadedFiles();
        if (uploads != null && uploads.length > 0) {
            
            // loop over uploaded files and try saving them
            for (int i=0; i < uploads.length; i++) {
                
                // skip null files
                if (uploads[i] == null || !uploads[i].exists())
                    continue;
                
                // figure file name and path
                String fileName = getUploadedFilesFileName()[i];
                int terminated = fileName.indexOf("\000");
                if (terminated != -1) {
                    // disallow sneaky null terminated strings
                    fileName = fileName.substring(0, terminated).trim();
                }
                
                // make sure fileName is valid
                if (fileName.indexOf("/") != -1 ||
                        fileName.indexOf("\\") != -1 ||
                        fileName.indexOf("..") != -1) {
                    addError("uploadFiles.error.badPath", fileName);
                    continue;
                }
                
                // add on the path element if needed
                if(getPath() != null && getPath().trim().length() > 0) {
                    fileName = getPath() + "/" + fileName;
                }

                if (!fmgr.canSave(getActionWeblog(), fileName, getUploadedFilesContentType()[i], uploads[i].length(), errors)) {
                    continue;
                }
                
                try {
                    fmgr.saveFile(getActionWeblog(), 
                            fileName,
                            getUploadedFilesContentType()[i],
                            uploads[i].length(),
                            new FileInputStream(uploads[i]), true);
                    
                    uploaded.add(fileName);
                    
                    //destroy the temporary file created
                    uploads[i].delete();
                    
                } catch (FilePathException ex) {
                    addError("uploadFiles.error.badPath", fileName);
                } catch (FileNotFoundException ex) {
                    addError("uploadFiles.error.badPath", fileName);
                } catch (FileIOException ex) {
                    addError("uploadFiles.error.upload", fileName);
                } catch (Exception ex) {
                    log.error("Error reading from uploaded file - "+uploads[i].getAbsolutePath(), ex);
                    // TODO: i18n
                    addError("Error reading uploaded file - "+getUploadedFilesFileName()[i]);
                }
            }
        }
        
        for (Iterator it = errors.getErrors(); it.hasNext();) {
            RollerMessage msg = (RollerMessage)it.next();
            if (msg.getArgs() != null) {
               addError(msg.getKey(), Arrays.asList(msg.getArgs()));
            } else {
               addError(msg.getKey());
            }
        }

        if(uploaded.size() > 0) {
            addMessage("uploadFiles.uploadedFiles");

            for (String upload : uploaded) {
                addMessage("uploadFiles.uploadedFile",
                        WebloggerFactory.getWeblogger().getUrlStrategy().getWeblogResourceURL(getActionWeblog(), upload, true));
            }
        }
        
        return execute();
    }
    
    
    /**
     * Delete selected file(s).
     */
    public String remove() {
        
        int numDeleted = 0;
        String[] deleteFiles = getDeleteIds();
        if(deleteFiles != null) {
            log.debug("Attempting to delete "+deleteFiles.length+" files");
            
            FileManager fmgr = WebloggerFactory.getWeblogger().getFileManager();
            for (int i=0; i < deleteFiles.length; i++) {
                if (deleteFiles[i].trim().startsWith("/") ||
                        deleteFiles[i].trim().startsWith("\\") ||
                        deleteFiles[i].indexOf("..") != -1) {
                    // ignore absolute paths, or paths that contiain '..'
                    log.debug("Ignoring delete path - "+deleteFiles[i]);
                } else {
                    try {
                        log.debug("Deleting file at path - "+deleteFiles[i]);
                        fmgr.deleteFile(getActionWeblog(), deleteFiles[i]);
                        numDeleted++;
                    } catch (FileNotFoundException ex) {
                        addError("uploadFiles.error.badPath");
                    } catch (FilePathException ex) {
                        addError("uploadFiles.error.badPath");
                    } catch (FileIOException ex) {
                        addError("uploadFiles.error.delete", deleteFiles[i]);
                    }
                    
                }
            }
            
            if(numDeleted > 0) {
                addMessage("uploadFiles.deletedFiles", ""+numDeleted);
            }
        }
        
        return execute();
    }
    
    
    public String getResourceURL(String path) {
        return WebloggerFactory.getWeblogger().getUrlStrategy().getWeblogResourceURL(getActionWeblog(), path, false);
    }
    
    
    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        if(path != null && 
                path.trim().length() != 0 && 
                !path.trim().equals("/")) {
            this.path = path;
        }
    }

    public String getNewDir() {
        return newDir;
    }

    public void setNewDir(String newDir) {
        this.newDir = newDir;
    }

    public String[] getDeleteIds() {
        return deleteIds;
    }

    public void setDeleteIds(String[] deleteIds) {
        this.deleteIds = deleteIds;
    }

    public List getFiles() {
        return files;
    }

    public void setFiles(List files) {
        this.files = files;
    }

    public boolean isOverQuota() {
        return overQuota;
    }

    public void setOverQuota(boolean overQuota) {
        this.overQuota = overQuota;
    }

    public long getTotalSize() {
        return totalSize;
    }

    public void setTotalSize(long totalSize) {
        this.totalSize = totalSize;
    }

    public File[] getUploadedFiles() {
        return uploadedFiles;
    }

    public void setUploadedFiles(File[] uploadedFiles) {
        this.uploadedFiles = uploadedFiles;
    }

    public String[] getUploadedFilesContentType() {
        return uploadedFilesContentType;
    }

    public void setUploadedFilesContentType(String[] uploadedFilesContentType) {
        this.uploadedFilesContentType = uploadedFilesContentType;
    }

    public String[] getUploadedFilesFileName() {
        return uploadedFilesFileName;
    }

    public void setUploadedFilesFileName(String[] uploadedFilesFileName) {
        this.uploadedFilesFileName = uploadedFilesFileName;
    }
    
    
    public class WeblogResourceComparator implements Comparator {
        public int compare(Object o1, Object o2) {
            ThemeResource r1 = (ThemeResource)o1;
            ThemeResource r2 = (ThemeResource)o2;
            
            // consider directories so they go to the top of the list
            if(r1.isDirectory() && r2.isDirectory()) {
                // if we have 2 directories then just go by name
                return r1.getPath().compareTo(r2.getPath());
            } else if(r1.isDirectory()) {
                // directories go before files
                return -1;
            } else if(r2.isDirectory()) {
                // directories go before files
                return 1;
            } else {
                // if we have 2 files then just go by name
                return r1.getPath().compareTo(r2.getPath());
            }
        }
        public boolean equals(Object o1, Object o2) {
            ThemeResource r1 = (ThemeResource)o1;
            ThemeResource r2 = (ThemeResource)o2;
            
            // need to be same type to be equals, i.e both files or directories
            if((r1.isDirectory() && !r2.isDirectory()) ||
                    (r1.isFile() && !r2.isFile())) {
                return false;
            }
            
            // after that it's just a matter of comparing paths
            return r1.getPath().equals(r2.getPath());
        }
    }
    
}
