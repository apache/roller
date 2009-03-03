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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.roller.weblogger.WebloggerException;
import org.apache.roller.weblogger.business.MediaFileManager;
import org.apache.roller.weblogger.business.WebloggerFactory;
import org.apache.roller.weblogger.pojos.MediaFile;
import org.apache.roller.weblogger.pojos.MediaFileFilter;
import org.apache.roller.weblogger.ui.struts2.pagers.MediaFilePager;
import org.apache.roller.weblogger.ui.struts2.util.KeyValueObject;
import org.apache.struts2.interceptor.validation.SkipValidation;

/**
 * Handles actions associated with media file search page.
 */
@SuppressWarnings("serial")
public class MediaFileSearch extends MediaFileBase {
    private static Log log = LogFactory.getLog(MediaFileSearch.class);

    private static final List<String> fileTypes;
    private static final List<KeyValueObject> sizeFilterTypes;
    private static final List<KeyValueObject> sizeUnits;
    private static final List<KeyValueObject> sortOptions;
    
    private MediaFilePager pager;

    private String newDirectoryPath;
    
    static {
        fileTypes = Arrays.asList("", "Audio", "Video", "Image");
        
        sizeFilterTypes = Arrays.asList(
        		new KeyValueObject(0, "greater than"),
        		new KeyValueObject(1, "greater than or equal to"),
        		new KeyValueObject(2, "equal to"),
        		new KeyValueObject(3, "less than or equal to"),
        		new KeyValueObject(4, "less than")
        		);

        sizeUnits = Arrays.asList(
        		new KeyValueObject(0, "bytes"),
        		new KeyValueObject(1, "kilobytes"),
        		new KeyValueObject(2, "megabytes")
        		);
        
        sortOptions = Arrays.asList(
        		new KeyValueObject(0, "Name"),
        		new KeyValueObject(1, "Date Uploaded"),
        		new KeyValueObject(2, "Type")
        		); 
    }
    
    private MediaFileSearchBean bean =  new MediaFileSearchBean();

    public MediaFileSearch() {
        this.actionName = "mediaFileSearch";
        this.desiredMenu = "editor";
        this.pageTitle = "mediaFile.search.title";
    }
    
    public void myPrepare() {
    	refreshAllDirectories();
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
    public String search() {
    	boolean valSuccess = myValidate();
    	if (valSuccess) {
    		MediaFileFilter filter = new MediaFileFilter();
    		bean.copyTo(filter);
    		MediaFileManager manager = WebloggerFactory.getWeblogger().getMediaFileManager();
    		try {
    			List<MediaFile> rawResults = manager.searchMediaFiles(getActionWeblog(), filter);
    			boolean hasMore = false;
    			List<MediaFile> results = new ArrayList<MediaFile>();
    			results.addAll(rawResults);
    			if (results.size() > MediaFileSearchBean.PAGE_SIZE) {
    				results.remove(results.size() - 1);
    				hasMore = true;
    			}
    			
    			this.pager = new MediaFilePager(bean.getPageNum(), results, hasMore);
    			
    			if (results.isEmpty()) {
                    addError("MediaFile.error.search.noResults");
    			}
	    		return SUCCESS;
			} catch (Exception e) {
	            log.error("Error applying search criteria", e);
                // TODO: i18n
                addError("Error applying search criteria");
			}
    		
    	}
		return INPUT;
    }
    
    public String delete() {
    	doDeleteMediaFile();
    	return search();
    }
    
    public String includeInGallery() {
    	doIncludeMediaFileInGallery();
    	return search();
    }

    public String deleteSelected() {
    	doDeleteSelected();
    	return search();
    }
    
    public String moveSelected() {
        doMoveSelected();
    	return search();
    }
    
    public String createDirByPath() {
    	boolean dirCreated = false;
		if (StringUtils.isEmpty(this.newDirectoryPath)) {
			addError("mediaFile.error.search.dirPathEmpty");
		}
		else if (!this.newDirectoryPath.startsWith("/")) {
			addError("mediaFile.error.search.dirPathInvalid");
		}
		else {
	    	try {
				log.debug("Creating directory by path - " + this.newDirectoryPath);
				MediaFileManager manager = WebloggerFactory.getWeblogger().getMediaFileManager();
				manager.createMediaFileDirectoryByPath(getActionWeblog(), this.newDirectoryPath);
				// flush changes
				WebloggerFactory.getWeblogger().flush();
				addMessage("mediaFile.directoryCreate.success");
				dirCreated = true;
			} catch (WebloggerException e) {
	            log.error("Error creating new directory by path", e);
	            // TODO: i18n
	            addError("Error creating new directory by path");
			}
		}
        
		if (dirCreated) {
			// Refresh list of directories so the newly created directory is included.
			refreshAllDirectories();
		}
    	return search();
    }
    
    public boolean myValidate() {
        if (StringUtils.isEmpty(bean.getName())
        	&& StringUtils.isEmpty(bean.getTags())
        	&& StringUtils.isEmpty(bean.getType())
        	&& bean.getSize() == 0) {
        	addError("MediaFile.error.search.empty");
        	return false;
        }
        return true;
    }
    
    public MediaFileSearchBean getBean() {
    	return bean;
    }
    
    public void setBean(MediaFileSearchBean b) {
    	this.bean = b;
    }
    
    public List<String> getFileTypes() {
    	return fileTypes;
    }
    
    public List<KeyValueObject> getSizeFilterTypes() {
    	return sizeFilterTypes;
    }
    
    public List<KeyValueObject> getSizeUnits() {
    	return sizeUnits;
    }

    public List<KeyValueObject> getSortOptions() {
    	return sortOptions;
    }

    public MediaFilePager getPager() {
		return pager;
	}

	public void setPager(MediaFilePager pager) {
		this.pager = pager;
	}

	public String getNewDirectoryPath() {
		return newDirectoryPath;
	}

	public void setNewDirectoryPath(String newDirectoryPath) {
		this.newDirectoryPath = newDirectoryPath;
	}
}
