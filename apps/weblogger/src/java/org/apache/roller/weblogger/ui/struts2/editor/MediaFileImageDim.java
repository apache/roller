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
public class MediaFileImageDim extends MediaFileBase {
    private static Log log = LogFactory.getLog(MediaFileImageDim.class);
    
    private MediaFileBean bean =  new MediaFileBean();
    
    public MediaFileImageDim() {
        this.actionName = "mediaFileImageDim";
    }
    
    /**
     * Show form for adding a new media file.
     * 
     * @return String The result of the action.
     */
    @SkipValidation
    public String execute() {
    	try {
            MediaFileManager mgr = WebloggerFactory.getWeblogger().getMediaFileManager();
            MediaFile mediaFile = mgr.getMediaFile(getMediaFileId());
            bean.copyFrom(mediaFile);
        } catch (WebloggerException ex) {
            log.error("Error looking up media file directory", ex);
        }
		return SUCCESS;
    }

	public MediaFileBean getBean() {
		return bean;
	}

	public void setBean(MediaFileBean bean) {
		this.bean = bean;
	}
    
}
