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

package org.apache.roller.business;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.math.BigDecimal;
import java.util.Map;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.roller.RollerException;
import org.apache.roller.config.RollerConfig;
import org.apache.roller.config.RollerRuntimeConfig;
import org.apache.roller.model.FileManager;
import org.apache.roller.model.Roller;
import org.apache.roller.model.RollerFactory;
import org.apache.roller.pojos.RollerPropertyData;
import org.apache.roller.util.RollerMessages;


/**
 * Responsible for managing website resources.  This base implementation
 * writes resources to a filesystem.
 */
public class FileManagerImpl implements FileManager {
    
    private String upload_dir = null;
    private String upload_url = null;
    
    private static Log mLogger = LogFactory.getLog(FileManagerImpl.class);
    
    
    /**
     * Create file manager.
     */
    public FileManagerImpl() {
        String uploaddir = RollerConfig.getProperty("uploads.dir");
        String uploadurl = RollerConfig.getProperty("uploads.url");
        
        // Note: System property expansion is now handled by RollerConfig.
        
        if(uploaddir == null || uploaddir.trim().length() < 1)
            uploaddir = System.getProperty("user.home") + File.separator+"roller_data"+File.separator+"uploads";
        
        if( ! uploaddir.endsWith(File.separator))
            uploaddir += File.separator;
        
        if(uploadurl == null || uploadurl.trim().length() < 1)
            uploadurl = File.separator+"resources";
        
        this.upload_dir = uploaddir.replace('/',File.separatorChar);
        this.upload_url = uploadurl;
    }
    
    
    /**
     * Get the upload directory being used by this file manager
     **/
    public String getUploadDir() {
        return this.upload_dir;
    }
    
    
    /**
     * Get the upload path being used by this file manager
     **/
    public String getUploadUrl() {
        return this.upload_url;
    }
    
    
    /**
     * Determine if file can be saved given current RollerConfig settings.
     */
    public boolean canSave(String weblogHandle, String name, String contentType,
                           long size, RollerMessages messages)
            throws RollerException {
        
        Roller mRoller = RollerFactory.getRoller();
        Map config = mRoller.getPropertiesManager().getProperties();
        
        if (!((RollerPropertyData)config.get("uploads.enabled")).getValue().equalsIgnoreCase("true")) {
            messages.addError("error.upload.disabled");
            return false;
        }
        
        String allows = ((RollerPropertyData)config.get("uploads.types.allowed")).getValue();
        String forbids = ((RollerPropertyData)config.get("uploads.types.forbid")).getValue();
        String[] allowFiles = StringUtils.split(StringUtils.deleteWhitespace(allows), ",");
        String[] forbidFiles = StringUtils.split(StringUtils.deleteWhitespace(forbids), ",");
        if (!checkFileType(allowFiles, forbidFiles, name, contentType)) {
            messages.addError("error.upload.forbiddenFile", allows);
            return false;
        }
        
        BigDecimal maxDirMB = new BigDecimal(
                ((RollerPropertyData)config.get("uploads.dir.maxsize")).getValue());
        int maxDirBytes = (int)(1024000 * maxDirMB.doubleValue());
        int userDirSize = getWebsiteDirSize(weblogHandle, this.upload_dir);
        if (userDirSize + size > maxDirBytes) {
            messages.addError("error.upload.dirmax", maxDirMB.toString());
            return false;
        }
        
        BigDecimal maxFileMB = new BigDecimal(
                ((RollerPropertyData)config.get("uploads.file.maxsize")).getValue());
        int maxFileBytes = (int)(1024000 * maxFileMB.doubleValue());
        mLogger.debug(""+maxFileBytes);
        mLogger.debug(""+size);
        if (size > maxFileBytes) {
            messages.addError("error.upload.filemax", maxFileMB.toString());
            return false;
        }
        
        return true;
    }
    
    
    public boolean overQuota(String weblogHandle) throws RollerException {
        
        String maxDir = RollerRuntimeConfig.getProperty("uploads.dir.maxsize");
        String maxFile = RollerRuntimeConfig.getProperty("uploads.file.maxsize");
        BigDecimal maxDirSize = new BigDecimal(maxDir); // in megabytes
        BigDecimal maxFileSize = new BigDecimal(maxFile); // in megabytes
        
        // determine the number of bytes in website's directory
        int maxDirBytes = (int)(1024000 * maxDirSize.doubleValue());
        int userDirSize = 0;
        String dir = getUploadDir();
        File d = new File(dir + weblogHandle);
        if (d.mkdirs() || d.exists()) {
            File[] files = d.listFiles();
            long dirSize = 0l;
            for (int i=0; i<files.length; i++) {
                if (!files[i].isDirectory()) {
                    dirSize = dirSize + files[i].length();
                }
            }
            userDirSize = new Long(dirSize).intValue();
        }
        return userDirSize > maxDirBytes;
    }
    
    
    /**
     * Get collection files in website's resource directory.
     * @param site Website
     * @return Collection of files in website's resource directory
     */
    public File[] getFiles(String weblogHandle) throws RollerException {
        String dir = this.upload_dir + weblogHandle;
        File uploadDir = new File(dir);
        return uploadDir.listFiles();
    }
    
    
    /**
     * Delete named file from website's resource area.
     */
    public void deleteFile(String weblogHandle, String name) 
            throws RollerException {
        String dir = this.upload_dir + weblogHandle;
        File f = new File(dir + File.separator + name);
        f.delete();
    }
    
    
    /**
     * Save file to website's resource directory.
     * @param site Website to save to
     * @param name Name of file to save
     * @param size Size of file to be saved
     * @param is Read file from input stream
     */
    public void saveFile(String weblogHandle, String name, String contentType, 
                         long size, InputStream is)
            throws RollerException {
        
        if (!canSave(weblogHandle, name, contentType, size, new RollerMessages())) {
            throw new RollerException("ERROR: upload denied");
        }
        
        byte[] buffer = new byte[8192];
        int bytesRead = 0;
        String dir = this.upload_dir;
        
        File dirPath = new File(dir + File.separator + weblogHandle);
        if (!dirPath.exists()) {
            dirPath.mkdirs();
        }
        OutputStream bos = null;
        try {
            bos = new FileOutputStream(
                    dirPath.getAbsolutePath() + File.separator + name);
            while ((bytesRead = is.read(buffer, 0, 8192)) != -1) {
                bos.write(buffer, 0, bytesRead);
            }
        } catch (Exception e) {
            throw new RollerException("ERROR uploading file", e);
        } finally {
            try {
                bos.flush();
                bos.close();
            } catch (Exception ignored) {}
        }
        if (mLogger.isDebugEnabled()) {
            mLogger.debug("The file has been written to \"" + dir + weblogHandle + "\"");
        }
    }
    
    
    /**
     * Returns current size of file uploads owned by specified weblog handle.
     * @param username User
     * @param dir      Upload directory
     * @return Size of user's uploaded files in bytes.
     */
    private int getWebsiteDirSize(String weblogHandle, String dir) {
        
        int userDirSize = 0;
        File d = new File(dir + File.separator + weblogHandle);
        if (d.mkdirs() || d.exists()) {
            File[] files = d.listFiles();
            long dirSize = 0l;
            for (int i=0; i<files.length; i++) {
                if (!files[i].isDirectory()) {
                    dirSize = dirSize + files[i].length();
                }
            }
            userDirSize = new Long(dirSize).intValue();
        }
        return userDirSize;
    }
    
    
    /**
     * Return true if file is allowed to be uplaoded given specified allowed and
     * forbidden file types.
     * @param allowFiles  File types (i.e. extensions) that are allowed
     * @param forbidFiles File types that are forbidden
     * @param fileName    Name of file to be uploaded
     * @return True if file is allowed to be uploaded
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
    
    
    public void release() {
    }
    
}
