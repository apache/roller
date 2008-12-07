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

package org.apache.roller.weblogger.business;

import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.math.BigDecimal;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.roller.weblogger.config.WebloggerConfig;
import org.apache.roller.weblogger.config.WebloggerRuntimeConfig;
import org.apache.roller.weblogger.pojos.ThemeResource;
import org.apache.roller.weblogger.pojos.Weblog;
import org.apache.roller.weblogger.util.RollerMessages;


/**
 * Manages files uploaded to Roller weblogs.  
 * 
 * This base implementation writes resources to a filesystem.
 */
public class FileManagerImpl implements FileManager {
    
    private static Log log = LogFactory.getLog(FileManagerImpl.class);
    
    private String upload_dir = null;
    
    
    /**
     * Create file manager.
     */
    public FileManagerImpl() {
        String uploaddir = WebloggerConfig.getProperty("uploads.dir");
        
        // Note: System property expansion is now handled by WebloggerConfig.
        
        if(uploaddir == null || uploaddir.trim().length() < 1)
            uploaddir = System.getProperty("user.home") + File.separator+"roller_data"+File.separator+"uploads";
        
        if( ! uploaddir.endsWith(File.separator))
            uploaddir += File.separator;
        
        this.upload_dir = uploaddir.replace('/',File.separatorChar);
    }
    
    
    /**
     * @see org.apache.roller.weblogger.model.FileManager#getFile(weblog, java.lang.String)
     */
    public ThemeResource getFile(Weblog weblog, String path) 
            throws FileNotFoundException, FilePathException {
        
        // get a reference to the file, checks that file exists & is readable
        File resourceFile = this.getRealFile(weblog, path);
        
        // make sure file is not a directory
        if(resourceFile.isDirectory()) {
            throw new FilePathException("Invalid path ["+path+"], "+
                    "path is a directory.");
        }
        
        // everything looks good, return resource
        return new WeblogResourceFile(weblog, path, resourceFile);
    }
    
    
    /**
     * @see org.apache.roller.weblogger.model.FileManager#getFiles(weblog, java.lang.String)
     */
    public ThemeResource[] getFiles(Weblog weblog, String path) 
            throws FileNotFoundException, FilePathException {
        
        // get a reference to the dir, checks that dir exists & is readable
        File dirFile = this.getRealFile(weblog, path);
        
        // make sure path is a directory
        if(!dirFile.isDirectory()) {
            throw new FilePathException("Invalid path ["+path+"], "+
                    "path is not a directory.");
        }
        
        // everything looks good, list contents
        ThemeResource dir = new WeblogResourceFile(weblog, path, dirFile);
        
        return dir.getChildren();
    }
    
    
    /**
     * @see org.apache.roller.weblogger.model.FileManager#getDirectories(weblog)
     */
    public ThemeResource[] getDirectories(Weblog weblog)
            throws FileNotFoundException, FilePathException {
        
        // get a reference to the root dir, checks that dir exists & is readable
        File dirFile = this.getRealFile(weblog, null);
        
        // we only want a list of directories
        File[] dirFiles = dirFile.listFiles(new FileFilter() {
            public boolean accept(File f) {
                return (f != null) ? f.isDirectory() : false;
            }
        });
        
        // convert 'em to ThemeResource objects
        ThemeResource[] resources = new ThemeResource[dirFiles.length];
        for(int i=0; i < dirFiles.length; i++) {
            String filePath = dirFiles[i].getName();
            resources[i] = new WeblogResourceFile(weblog, filePath, dirFiles[i]);
        }
            
        return resources;
    }
    
    
    /**
     * @see org.apache.roller.weblogger.model.FileManager#saveFile(weblog, java.lang.String, java.lang.String, long, java.io.InputStream)
     */
    public void saveFile(Weblog weblog, 
                         String path, 
                         String contentType, 
                         long size, 
                         InputStream is) 
            throws FileNotFoundException, FilePathException, FileIOException {
        
        saveFile(weblog, path, contentType, size, is, true);
    }
    
    /**
     * @see org.apache.roller.weblogger.model.FileManager#saveFile(weblog, java.lang.String, java.lang.String, long, java.io.InputStream)
     */
    public void saveFile(Weblog weblog, 
                         String path, 
                         String contentType, 
                         long size, 
                         InputStream is,
                         boolean checkCanSave)
            throws FileNotFoundException, FilePathException, FileIOException {
        
        String savePath = path;
        if(path.startsWith("/")) {
            savePath = path.substring(1);
        }
        
        // make sure we are allowed to save this file
        RollerMessages msgs = new RollerMessages();
        if (checkCanSave && !canSave(weblog, savePath, contentType, size, msgs)) {
            throw new FileIOException(msgs.toString());
        }
        
        // make sure uploads area exists for this weblog
        File dirPath = this.getRealFile(weblog, null);
        
        // if we are saving into a subfolder, make sure it exists
        if(path.indexOf("/") != -1) {
            String subDir = path.substring(0, path.indexOf("/"));
            File subDirFile = new File(dirPath.getAbsolutePath() + File.separator + subDir);
            if(!subDirFile.exists()) {
                // directory doesn't exist yet, create it
                log.debug("Creating directory ["+subDir+"] automatically");
                subDirFile.mkdir();
            }
        }
        
        // create File that we are about to save
        File saveFile = new File(dirPath.getAbsolutePath() + File.separator + savePath);
        
        byte[] buffer = new byte[8192];
        int bytesRead = 0;
        OutputStream bos = null;
        try {
            bos = new FileOutputStream(saveFile);
            while ((bytesRead = is.read(buffer, 0, 8192)) != -1) {
                bos.write(buffer, 0, bytesRead);
            }
            
            log.debug("The file has been written to ["+saveFile.getAbsolutePath()+"]");
        } catch (Exception e) {
            throw new FileIOException("ERROR uploading file", e);
        } finally {
            try {
                bos.flush();
                bos.close();
            } catch (Exception ignored) {}
        }
        
        
    }
    
    
    /**
     * @see org.apache.roller.weblogger.model.FileManager#createDirectory(weblog, java.lang.String)
     */
    public void createDirectory(Weblog weblog, String path)
            throws FileNotFoundException, FilePathException, FileIOException {
        
        // get path to weblog's uploads area
        File weblogDir = this.getRealFile(weblog, null);
        
        String savePath = path;
        if(path.startsWith("/")) {
            savePath = path.substring(1);
        }
        
        if(savePath != null && savePath.indexOf('/') != -1) {
            throw new FilePathException("Invalid path ["+path+"], "+
                        "trying to use nested directories.");
        }
        
        // now construct path to new directory
        File dir = new File(weblogDir.getAbsolutePath() + File.separator + savePath);
        
        // check if it already exists
        if(dir.exists() && dir.isDirectory() && dir.canRead()) {
            // already exists, we don't need to do anything
            return;
        }
        
        try {
            // make sure someone isn't trying to sneek outside the uploads dir
            if(!dir.getCanonicalPath().startsWith(weblogDir.getCanonicalPath())) {
                throw new FilePathException("Invalid path ["+path+"], "+
                        "trying to get outside uploads dir.");
            }
        } catch (IOException ex) {
            // rethrow as FilePathException
            throw new FilePathException(ex);
        }
        
        // create it
        if(!dir.mkdir()) {
            // failed for some reason
            throw new FileIOException("Failed to create directory ["+path+"], "+
                    "probably doesn't have needed parent directories.");
        }
    }
    
    
    /**
     * @see org.apache.roller.weblogger.model.FileManager#deleteFile(weblog, java.lang.String)
     */
    public void deleteFile(Weblog weblog, String path) 
            throws FileNotFoundException, FilePathException, FileIOException {
        
        // get path to delete file, checks that path exists and is readable
        File delFile = this.getRealFile(weblog, path);
        
        if(!delFile.delete()) {
            throw new FileIOException("Delete failed for ["+path+"], "+
                    "possibly a non-empty directory?");
        }
    }
    
    
    /**
     * @inheritDoc
     */
    public void deleteAllFiles(Weblog weblog) throws FileIOException {
        
        try {
            // get path to root folder
            File delFile = this.getRealFile(weblog, "/");
            
            // delete folder and it's contents
            deleteAllFiles(delFile);
            
        } catch (FileNotFoundException ex) {
            // if it doesn't exist then we already have no files, so we're done
            return;
        } catch (FilePathException ex) {
            // should never happen when trying to get root folder
        }
    }
    
    
    // convenience method to delete a folder and all of it's contents.  called recursively
    private void deleteAllFiles(File dir) {
        
        // delete directory contents
        File[] dirFiles = dir.listFiles();
        if(dirFiles != null && dirFiles.length > 0) {
            for( File file : dirFiles ) {
                if(file.isDirectory()) {
                    // recursive call
                    deleteAllFiles(file);
                } else {
                    file.delete();
                }
            }
        }
        
        // delete directory itself
        dir.delete();
    }
    
    
    /**
     * @see org.apache.roller.weblogger.model.FileManager#overQuota(weblog)
     */
    public boolean overQuota(Weblog weblog) {
        
        String maxDir = WebloggerRuntimeConfig.getProperty("uploads.dir.maxsize");
        String maxFile = WebloggerRuntimeConfig.getProperty("uploads.file.maxsize");
        BigDecimal maxDirSize = new BigDecimal(maxDir); // in megabytes
        BigDecimal maxFileSize = new BigDecimal(maxFile); // in megabytes
        
        long maxDirBytes = (long)(1024000 * maxDirSize.doubleValue());
        
        try {
            File uploadsDir = this.getRealFile(weblog, null);
            long weblogDirSize = this.getDirSize(uploadsDir, true);
            
            return weblogDirSize > maxDirBytes;
        } catch (Exception ex) {
            // shouldn't ever happen, this means user's uploads dir is bad
            // rethrow as a runtime exception
            throw new RuntimeException(ex);
        }
    }
    
    
    public void release() {
    }
    
    
    /**
     * Determine if file can be saved given current WebloggerConfig settings.
     */
    public boolean canSave(Weblog weblog,
                           String path, 
                           String contentType,
                           long size, 
                           RollerMessages errors) {
        
        // first check, is uploading enabled?
        if(!WebloggerRuntimeConfig.getBooleanProperty("uploads.enabled")) {
            errors.addError("error.upload.disabled");
            return false;
        }
        
        // second check, does upload exceed max size for file?
        BigDecimal maxFileMB = new BigDecimal(
                WebloggerRuntimeConfig.getProperty("uploads.file.maxsize"));
        int maxFileBytes = (int)(1024000 * maxFileMB.doubleValue());
        log.debug("max allowed file size = "+maxFileBytes);
        log.debug("attempted save file size = "+size);
        if (size > maxFileBytes) {
            errors.addError("error.upload.filemax", maxFileMB.toString());
            return false;
        }
        
        // third check, does file cause weblog to exceed quota?
        BigDecimal maxDirMB = new BigDecimal(
                WebloggerRuntimeConfig.getProperty("uploads.dir.maxsize"));
        long maxDirBytes = (long)(1024000 * maxDirMB.doubleValue());
        try {
            File uploadsDir = this.getRealFile(weblog, null);
            long userDirSize = getDirSize(uploadsDir, true);
            if (userDirSize + size > maxDirBytes) {
                errors.addError("error.upload.dirmax", maxDirMB.toString());
                return false;
            }
        } catch (Exception ex) {
            // shouldn't ever happen, means the weblogs uploads dir is bad somehow
            // rethrow as a runtime exception
            throw new RuntimeException(ex);
        }
        
        // fourth check, is upload type allowed?
        String allows = WebloggerRuntimeConfig.getProperty("uploads.types.allowed");
        String forbids = WebloggerRuntimeConfig.getProperty("uploads.types.forbid");
        String[] allowFiles = StringUtils.split(StringUtils.deleteWhitespace(allows), ",");
        String[] forbidFiles = StringUtils.split(StringUtils.deleteWhitespace(forbids), ",");
        if (!checkFileType(allowFiles, forbidFiles, path, contentType)) {
            errors.addError("error.upload.forbiddenFile", allows);
            return false;
        }
        
        // fifth check, is save path viable?
        if(path.indexOf("/") != -1) {
            // just make sure there is only 1 directory, we don't allow multi
            // level directory hierarchies right now
            if(path.lastIndexOf("/") != path.indexOf("/")) {
                errors.addError("error.upload.badPath");
                return false;
            }
        }
        
        return true;
    }
    
    
    /**
     * Get the size in bytes of given directory.
     *
     * Optionally works recursively counting subdirectories if they exist.
     */
    private long getDirSize(File dir, boolean recurse) {
        
        long size = 0;
        if(dir.exists() && dir.isDirectory() && dir.canRead()) {
            File[] files = dir.listFiles();
            long dirSize = 0l;
            for (int i=0; i < files.length; i++) {
                if (!files[i].isDirectory()) {
                    dirSize += files[i].length();
                } else if(recurse) {
                    // count a subdirectory
                    dirSize += getDirSize(files[i], recurse);
                }
            }
            size += dirSize;
        }
        
        return size;
    }
    
    
    /**
     * Return true if file is allowed to be uplaoded given specified allowed and
     * forbidden file types.
     */
    private boolean checkFileType(String[] allowFiles, String[] forbidFiles,
                                  String fileName, String contentType) {
        
        // TODO: Atom Publushing Protocol figure out how to handle file
        // allow/forbid using contentType.
        // TEMPORARY SOLUTION: In the allow/forbid lists we will continue to
        // allow user to specify file extensions (e.g. gif, png, jpeg) but will
        // now also allow them to specify content-type rules (e.g. */*, image/*,
        // text/xml, etc.).
        
        // if content type is invalid, reject file
        if (contentType == null || contentType.indexOf("/") == -1)  {
            return false;
        }
        
        // default to false
        boolean allowFile = false;
        
        // if this person hasn't listed any allows, then assume they want
        // to allow *all* filetypes, except those listed under forbid
        if (allowFiles == null || allowFiles.length < 1) {
            allowFile = true;
        }
        
        // First check against what is ALLOWED
        
        // check file against allowed file extensions
        if (allowFiles != null && allowFiles.length > 0) {
            for (int y=0; y<allowFiles.length; y++) {
                // oops, this allowed rule is a content-type, skip it
                if (allowFiles[y].indexOf("/") != -1) continue;
                if (fileName.toLowerCase().endsWith(
                        allowFiles[y].toLowerCase())) {
                    allowFile = true;
                    break;
                }
            }
        }
        
        // check file against allowed contentTypes
        if (allowFiles != null && allowFiles.length > 0) {
            for (int y=0; y<allowFiles.length; y++) {
                // oops, this allowed rule is NOT a content-type, skip it
                if (allowFiles[y].indexOf("/") == -1) continue;
                if (matchContentType(allowFiles[y], contentType)) {
                    allowFile = true;
                    break;
                }
            }
        }
        
        // First check against what is FORBIDDEN
        
        // check file against forbidden file extensions, overrides any allows
        if (forbidFiles != null && forbidFiles.length > 0) {
            for (int x=0; x<forbidFiles.length; x++) {
                // oops, this forbid rule is a content-type, skip it
                if (forbidFiles[x].indexOf("/") != -1) continue;
                if (fileName.toLowerCase().endsWith(
                        forbidFiles[x].toLowerCase())) {
                    allowFile = false;
                    break;
                }
            }
        }
        
        
        // check file against forbidden contentTypes, overrides any allows
        if (forbidFiles != null && forbidFiles.length > 0) {
            for (int x=0; x<forbidFiles.length; x++) {
                // oops, this forbid rule is NOT a content-type, skip it
                if (forbidFiles[x].indexOf("/") == -1) continue;
                if (matchContentType(forbidFiles[x], contentType)) {
                    allowFile = false;
                    break;
                }
            }
        }
        
        return allowFile;
    }
    
    
    /**
     * Super simple contentType range rule matching
     */
    private boolean matchContentType(String rangeRule, String contentType) {
        if (rangeRule.equals("*/*")) return true;
        if (rangeRule.equals(contentType)) return true;
        String ruleParts[] = rangeRule.split("/");
        String typeParts[] = contentType.split("/");
        if (ruleParts[0].equals(typeParts[0]) && ruleParts[1].equals("*")) 
            return true;
        
        return false;
    }
    
    
    /**
     * Construct the full real path to a resource in a weblog's uploads area.
     */
    private File getRealFile(Weblog weblog, String path) 
            throws FileNotFoundException, FilePathException {
        
        // make sure uploads area exists for this weblog
        File weblogDir = new File(this.upload_dir + weblog.getHandle());
        if(!weblogDir.exists()) {
            weblogDir.mkdirs();
        }
        
        // crop leading slash if it exists
        String relPath = path;
        if(path != null && path.startsWith("/")) {
            relPath = path.substring(1);
        }
        
        // we only allow a single level of directories right now, so make
        // sure that the path doesn't try to go multiple levels
        if(relPath != null && (relPath.lastIndexOf('/') > relPath.indexOf('/'))) {
            throw new FilePathException("Invalid path ["+path+"], "+
                    "trying to use nested directories.");
        }
        
        // convert "/" to filesystem specific file separator
        if(relPath != null) {
            relPath = relPath.replace('/', File.separatorChar);
        }
        
        // now form the absolute path
        String filePath = weblogDir.getAbsolutePath();
        if(relPath != null) {
            filePath += File.separator + relPath;
        }
        
        // make sure path exists and is readable
        File file = new File(filePath);
        if(!file.exists()) {
            throw new FileNotFoundException("Invalid path ["+path+"], "+
                    "directory doesn't exist.");
        } else if(!file.canRead()) {
            throw new FilePathException("Invalid path ["+path+"], "+
                    "cannot read from path.");
        }
        
        try {
            // make sure someone isn't trying to sneek outside the uploads dir
            if(!file.getCanonicalPath().startsWith(weblogDir.getCanonicalPath())) {
                throw new FilePathException("Invalid path ["+path+"], "+
                        "trying to get outside uploads dir.");
            }
        } catch (IOException ex) {
            // rethrow as FilePathException
            throw new FilePathException(ex);
        }
        
        return file;
    }
    
    
    /**
     * A FileManagerImpl specific implementation of a ThemeResource.
     *
     * ThemeResources from the FileManagerImpl are backed by a java.io.File
     * object which represents the resource on a filesystem.
     *
     * This class is internal to the FileManagerImpl class because there should 
     * not be any external classes which need to construct their own instances
     * of this class.
     */
    class WeblogResourceFile implements ThemeResource {
        
        // the physical java.io.File backing this resource
        private File resourceFile = null;
        
        // the relative path of the resource within the weblog's uploads area
        private String relativePath = null;
        
        // the weblog the resource is attached to
        private Weblog weblog = null;
        
        
        public WeblogResourceFile(Weblog weblog, String path, File file) {
            this.weblog = weblog;
            relativePath = path;
            resourceFile = file;
        }
        
        public Weblog getWeblog() {
            return weblog;
        }
        
        public ThemeResource[] getChildren() {
            
            if(!resourceFile.isDirectory()) {
                return null;
            }
            
            // we only want files, no directories
            File[] dirFiles = resourceFile.listFiles(new FileFilter() {
                public boolean accept(File f) {
                    return (f != null) ? f.isFile() : false;
                }
            });
            
            // convert Files into ThemeResources
            ThemeResource[] resources = new ThemeResource[dirFiles.length];
            for(int i=0; i < dirFiles.length; i++) {
                String filePath = dirFiles[i].getName();
                if(relativePath != null && !relativePath.trim().equals("")) {
                    filePath = relativePath + "/" + filePath;
                }
                
                resources[i] = new WeblogResourceFile(weblog, filePath, dirFiles[i]);
            }
            
            return resources;
        }
        
        public String getName() {
            return resourceFile.getName();
        }
        
        public String getPath() {
            return relativePath;
        }
        
        public long getLastModified() {
            return resourceFile.lastModified();
        }
        
        public long getLength() {
            return resourceFile.length();
        }
        
        public boolean isDirectory() {
            return resourceFile.isDirectory();
        }
        
        public boolean isFile() {
            return resourceFile.isFile();
        }
        
        public InputStream getInputStream() {
            try {
                return new FileInputStream(resourceFile);
            } catch (java.io.FileNotFoundException ex) {
                // should never happen, rethrow as runtime exception
                throw new RuntimeException("Error constructing input stream", ex);
            }
        }
    }
    
}
