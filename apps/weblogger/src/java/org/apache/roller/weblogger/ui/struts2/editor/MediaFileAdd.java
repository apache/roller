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
import java.util.Arrays;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.roller.weblogger.WebloggerException;
import org.apache.roller.weblogger.business.FileIOException;
import org.apache.roller.weblogger.business.MediaFileManager;
import org.apache.roller.weblogger.business.WebloggerFactory;
import org.apache.roller.weblogger.config.WebloggerRuntimeConfig;
import org.apache.roller.weblogger.pojos.MediaFile;
import org.apache.roller.weblogger.pojos.MediaFileDirectory;
import org.apache.roller.weblogger.pojos.MediaFileType;
import org.apache.struts2.interceptor.validation.SkipValidation;

/**
 * Adds a new media file.
 */
@SuppressWarnings("serial")
public class MediaFileAdd extends MediaFileBase {
    private static Log log = LogFactory.getLog(MediaFileAdd.class);
    
    private MediaFileBean bean =  new MediaFileBean();
    
    private MediaFileDirectory directory;
    // file uploaded by the user
    private File uploadedFile = null;
    
    // content type for upload file
    private String uploadedFileContentType = null;
    
    // filename for uploaded file
    private String uploadedFileFileName = null;

    public MediaFileAdd() {
        this.actionName = "mediaFileAdd";
        this.desiredMenu = "editor";
        this.pageTitle = "mediaFile.add.title";
    }
    
    public void myPrepare() {
        System.out.println("Into myprepare");
    	refreshAllDirectories();
    	try {
            MediaFileManager mgr = WebloggerFactory.getWeblogger().getMediaFileManager();
            if(!StringUtils.isEmpty(bean.getDirectoryId())) {
                setDirectory(mgr.getMediaFileDirectory(bean.getDirectoryId()));
            }
            else {
                setDirectory(mgr.createRootMediaFileDirectory(getActionWeblog()));
            }
        } catch (WebloggerException ex) {
            log.error("Error looking up media file directory", ex);
        }
    }

    /**
     * Show form for adding a new media file.
     * 
     * @return String The result of the action.
     */
    @SkipValidation
    public String execute() {
		return INPUT;
    }
    
    /**
     * Save a media file.
     * 
     * @return String The result of the action.
     */
    public String save() {
    	myValidate();
    	if (!hasActionErrors()) {
    		MediaFileManager manager = WebloggerFactory.getWeblogger().getMediaFileManager();
    		try {
    			MediaFile mediaFile = new MediaFile();
    			bean.copyTo(mediaFile);
    			mediaFile.setDirectory(getDirectory());
    			mediaFile.setLength(this.uploadedFile.length());
    			mediaFile.setInputStream(new FileInputStream(this.uploadedFile));
    			mediaFile.setContentType(this.uploadedFileContentType);
				manager.createMediaFile(getActionWeblog(), mediaFile);
	            WebloggerFactory.getWeblogger().flush();
	            bean.setId(mediaFile.getId());
	    		return SUCCESS;
            } catch (FileIOException ex) {
                addError("uploadFiles.error.upload", bean.getName());
			} catch (Exception e) {
	            log.error("Error saving new entry", e);
                // TODO: i18n
                addError("Error reading uploaded file - " + bean.getName());
			}
			
    	}
		return INPUT;
    }
    
    public void myValidate() {
        if (getDirectory().hasMediaFile(getBean().getName())) {
            addError("MediaFile.error.duplicateName", getBean().getName());
        }
        // make sure uploads are enabled
        if(!WebloggerRuntimeConfig.getBooleanProperty("uploads.enabled")) {
            addError("error.upload.disabled");
        }
        
        if (StringUtils.isEmpty(this.uploadedFileFileName)) {
        	addError("error.upload.file");
        }
    }
    
    public MediaFileBean getBean() {
    	return bean;
    }
    
    public void setBean(MediaFileBean b) {
    	this.bean = b;
    }
    
	public MediaFileDirectory getDirectory() {
		return directory;
	}

	public void setDirectory(MediaFileDirectory directory) {
		this.directory = directory;
	}
	
    public File getUploadedFile() {
        return uploadedFile;
    }

    public void setUploadedFile(File uploadedFile) {
        this.uploadedFile = uploadedFile;
    }

    public String getUploadedFileContentType() {
        return uploadedFileContentType;
    }

    public void setUploadedFileContentType(String uploadedFileContentType) {
        this.uploadedFileContentType = uploadedFileContentType;
    }

    public String getUploadedFileFileName() {
        return uploadedFileFileName;
    }

    public void setUploadedFileFileName(String uploadedFileFileName) {
        this.uploadedFileFileName = uploadedFileFileName;
    }
    
    public boolean isContentTypeImage() {
    	String[] allowedImageContentTypes = MediaFileType.IMAGE.getContentTypes(); 
    	for (String imageContentType: allowedImageContentTypes) {
    		if (imageContentType.equals(this.uploadedFileContentType)) {
    			return true;
    		}
    	}
    	return false;
    }
}
