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
import org.apache.roller.weblogger.pojos.FileContent;
import org.apache.roller.weblogger.pojos.Weblog;
import org.apache.roller.weblogger.util.RollerMessages;


/**
 * Manages contents of the file uploaded to Roller weblogs.  
 * 
 * This base implementation writes file content to a file system.
 */
public class FileContentManagerImpl implements FileContentManager {
	
    private static Log log = LogFactory.getLog(FileContentManagerImpl.class);
    
    private String upload_dir = null;
    
    
    /**
     * Create file content manager.
     */
    public FileContentManagerImpl() {
        String uploaddir = WebloggerConfig.getProperty("uploads.dir");
        
        // Note: System property expansion is now handled by WebloggerConfig.
        
        if(uploaddir == null || uploaddir.trim().length() < 1)
            uploaddir = System.getProperty("user.home") + File.separator+"roller_data"+File.separator+"uploads";
        
        if( ! uploaddir.endsWith(File.separator))
            uploaddir += File.separator;
        
        this.upload_dir = uploaddir.replace('/',File.separatorChar);
    }
    
    
    /**
     * @see org.apache.roller.weblogger.model.FileContentManager#getFileContent(weblog, java.lang.String)
     */
    public FileContent getFileContent(Weblog weblog, String fileId) 
            throws FileNotFoundException, FilePathException {
        
        // get a reference to the file, checks that file exists & is readable
        File resourceFile = this.getRealFile(weblog, fileId);
        
        // make sure file is not a directory
        if(resourceFile.isDirectory()) {
            throw new FilePathException("Invalid file id ["+fileId+"], "+
                    "path is a directory.");
        }
        
        // everything looks good, return resource
        return new FileContent(weblog, fileId, resourceFile);
    }
    
    /**
     * @see org.apache.roller.weblogger.model.FileContentManager#saveFileContent(weblog, java.lang.String, 
     * java.lang.String, java.io.InputStream, boolean)
     */
    public void saveFileContent(Weblog weblog, 
                         String fileId, 
                         InputStream is)
            throws FileNotFoundException, FilePathException, FileIOException {
        
        // make sure uploads area exists for this weblog
        File dirPath = this.getRealFile(weblog, null);
        
        // create File that we are about to save
        File saveFile = new File(dirPath.getAbsolutePath() + File.separator + fileId);
        
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
     * @see org.apache.roller.weblogger.model.FileContentManager#deleteFile(weblog, java.lang.String)
     */
    public void deleteFile(Weblog weblog, String fileId) 
            throws FileNotFoundException, FilePathException, FileIOException {
        
        // get path to delete file, checks that path exists and is readable
        File delFile = this.getRealFile(weblog, fileId);
        
        if(!delFile.delete()) {
            throw new FileIOException("Delete failed for ["+fileId+"], "+
                    "possibly a non-empty directory?");
        }
    }
    
    /**
     * @inheritDoc
     */
    public void deleteAllFiles(Weblog weblog) throws FileIOException {
        // TODO: Implement
    }
    
    /**
     * @see org.apache.roller.weblogger.model.FileContentManager#overQuota(weblog)
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
     * @see org.apache.roller.weblogger.model.FileContentManager#canSave(
     * weblog, java.lang.String, java.lang.String, long, messages)
     */
    public boolean canSave(Weblog weblog,
    		               String fileName,
                           String contentType,
                           long size, 
                           RollerMessages messages) {
        
        // first check, is uploading enabled?
        if(!WebloggerRuntimeConfig.getBooleanProperty("uploads.enabled")) {
            messages.addError("error.upload.disabled");
            return false;
        }
        
        // second check, does upload exceed max size for file?
        BigDecimal maxFileMB = new BigDecimal(
                WebloggerRuntimeConfig.getProperty("uploads.file.maxsize"));
        int maxFileBytes = (int)(1024000 * maxFileMB.doubleValue());
        log.debug("max allowed file size = "+maxFileBytes);
        log.debug("attempted save file size = "+size);
        if (size > maxFileBytes) {
            messages.addError("error.upload.filemax", maxFileMB.toString());
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
                messages.addError("error.upload.dirmax", maxDirMB.toString());
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
        if (!checkFileType(allowFiles, forbidFiles, fileName, contentType)) {
            messages.addError("error.upload.forbiddenFile", allows);
            return false;
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
        if(allowFiles == null || allowFiles.length < 1) {
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
    private File getRealFile(Weblog weblog, String fileId) 
            throws FileNotFoundException, FilePathException {
        
        // make sure uploads area exists for this weblog
        File weblogDir = new File(this.upload_dir + weblog.getHandle());
        if(!weblogDir.exists()) {
            weblogDir.mkdirs();
        }
        
        // now form the absolute path
        String filePath = weblogDir.getAbsolutePath();
        if (fileId != null) {
            filePath += File.separator + fileId;
        }
        
        // make sure path exists and is readable
        File file = new File(filePath);
        if(!file.exists()) {
            throw new FileNotFoundException("Invalid path ["+filePath+"], "+
                    "directory doesn't exist.");
        } else if(!file.canRead()) {
            throw new FilePathException("Invalid path ["+filePath+"], "+
                    "cannot read from path.");
        }
        
        try {
            // make sure someone isn't trying to sneek outside the uploads dir
            if(!file.getCanonicalPath().startsWith(weblogDir.getCanonicalPath())) {
                throw new FilePathException("Invalid path "+filePath+"], "+
                        "trying to get outside uploads dir.");
            }
        } catch (IOException ex) {
            // rethrow as FilePathException
            throw new FilePathException(ex);
        }
        
        return file;
    }
    
}
