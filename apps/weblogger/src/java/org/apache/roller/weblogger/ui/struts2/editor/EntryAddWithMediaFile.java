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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.roller.weblogger.business.MediaFileManager;
import org.apache.roller.weblogger.business.WebloggerFactory;
import org.apache.roller.weblogger.pojos.MediaFile;
import org.apache.struts2.interceptor.validation.SkipValidation;

/**
 * Prepares creation of a new weblog entry with an embedded pointer to a media file.
 */
@SuppressWarnings("serial")
public class EntryAddWithMediaFile extends MediaFileBase {
    private static Log log = LogFactory.getLog(EntryAddWithMediaFile.class);
    
    // bean for managing form data
    private EntryBean bean = new EntryBean();

    public EntryAddWithMediaFile() {
    }
    
    /**
     * Prepare bean for creating a new weblog entry with a link to a media file.
     * 
     * @return String The result of the action.
     */
    @SkipValidation
    public String execute() {
    	MediaFileManager manager = WebloggerFactory.getWeblogger().getMediaFileManager();
		try {
			MediaFile mediaFile = manager.getMediaFile(getMediaFileId());
			String link;
			if (mediaFile.isImageFile()) {
				link = "<img src='<url>' alt='<name>' width='<width>' height='<height>' />";
				link = link
				.replace("<url>", getMediaFileURL(mediaFile))
				.replace("<name>", mediaFile.getName())
				.replace("<width>", "")
				.replace("<height>", "");
			}
			else {
				link = "<a href='<url>'><name></a>";
				link = link
				.replace("<url>", getMediaFileURL(mediaFile))
				.replace("<name>", mediaFile.getName());
			}
			bean.setText(link);
		} catch (Exception e) {
            log.error("Error while constructing media file link for new entry", e);
		}
		return SUCCESS;
    }

	public EntryBean getBean() {
		return bean;
	}

	public void setBean(EntryBean bean) {
		this.bean = bean;
	}
    
}
