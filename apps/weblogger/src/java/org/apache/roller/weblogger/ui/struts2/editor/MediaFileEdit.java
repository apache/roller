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
import java.util.Collections;
import java.util.List;

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
import org.apache.roller.weblogger.ui.struts2.util.UIAction;
import org.apache.struts2.interceptor.validation.SkipValidation;

/**
 * Adds a new media file.
 */
@SuppressWarnings("serial")
public class MediaFileEdit extends UIAction {
    private static Log log = LogFactory.getLog(MediaFileEdit.class);
    
    private String mediaFileId; 

    private MediaFileBean bean =  new MediaFileBean();
    private MediaFileDirectory directory;
    
    private List<MediaFileDirectory> allDirectories;

    public MediaFileEdit() {
        this.actionName = "mediaFileEdit";
        this.desiredMenu = "editor";
        this.pageTitle = "mediaFile.edit.title";
    }
    
    public void myPrepare() {
        try {
            MediaFileManager mgr = WebloggerFactory.getWeblogger().getMediaFileManager();
            setAllDirectories(mgr.getMediaFileDirectories(getActionWeblog()));
            if(!StringUtils.isEmpty(bean.getDirectoryId())) {
                setDirectory(mgr.getMediaFileDirectory(bean.getDirectoryId()));
            }
        } catch (WebloggerException ex) {
            log.error("Error looking up media file directory", ex);
            // TODO: Ganesh - Handle exception
        }
    }

    /**
     * Show form for adding a new media file.
     * 
     * @return String The result of the action.
     */
    @SkipValidation
    public String execute() {
		MediaFileManager manager = WebloggerFactory.getWeblogger().getMediaFileManager();
		try {
			MediaFile mediaFile = manager.getMediaFile(this.mediaFileId);
			this.bean.copyFrom(mediaFile);
    		return SUCCESS;
        } catch (FileIOException ex) {
            addError("uploadFiles.error.upload", bean.getName());
		} catch (Exception e) {
            log.error("Error saving new entry", e);
            // TODO: i18n
            addError("Error reading uploaded file - " + bean.getName());
		}

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
    			MediaFile mediaFile = manager.getMediaFile(this.mediaFileId);
    			bean.copyTo(mediaFile);
				manager.updateMediaFile(getActionWeblog(), mediaFile);
	            WebloggerFactory.getWeblogger().flush();
	            addMessage("mediaFile.update.success");
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
    	MediaFile fileWithSameName = getDirectory().getMediaFile(getBean().getName());
        if (fileWithSameName != null && !fileWithSameName.getId().equals(this.mediaFileId)) {
            addError("MediaFile.error.duplicateName", getBean().getName());
        }
    }
    
    /**
     * Get the list of all categories for the action weblog, not including root.
     */
    public List<MediaFileDirectory> getDirectories() {
        
    	try {
        	// TODO: Ganesh - do this in prepare method?
    		MediaFileManager mgr = WebloggerFactory.getWeblogger().getMediaFileManager();
            return mgr.getMediaFileDirectories(getActionWeblog());
        } catch (WebloggerException ex) {
            log.error("Error getting media file directory list for weblog - "+getWeblog(), ex);
            return Collections.EMPTY_LIST;
        }
    }

    public MediaFileBean getBean() {
    	return bean;
    }
    
    public void setBean(MediaFileBean b) {
    	this.bean = b;
    }
    
	public List<MediaFileDirectory> getAllDirectories() {
		return allDirectories;
	}
    
	public void setAllDirectories(List<MediaFileDirectory> dirs) {
		this.allDirectories = dirs;
	}

	public String getMediaFileId() {
		return mediaFileId;
	}

	public void setMediaFileId(String mediaFileId) {
		this.mediaFileId = mediaFileId;
	}

	public MediaFileDirectory getDirectory() {
		return directory;
	}

	public void setDirectory(MediaFileDirectory directory) {
		this.directory = directory;
	}

}
