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

import org.apache.commons.lang.StringUtils;
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

    private String weblog = null;
    private String enclosureUrl = null;
    private String[] selectedImages = null;
    

    public EntryAddWithMediaFile() {
        this.actionName = "entryAdd";
        this.desiredMenu = "editor";
        this.pageTitle = "weblogEdit.title.newEntry";
    }

    /**
     * Prepare bean for creating a new weblog entry with a link to a media file.
     * @return String The result of the action.
     */
    @SkipValidation
    public String execute() {
        MediaFileManager manager =
             WebloggerFactory.getWeblogger().getMediaFileManager();
        try {

            if (selectedImages != null) {
                for (int i=0; i<selectedImages.length; i++) {
                    MediaFile mediaFile = manager.getMediaFile(selectedImages[i]);
                    String link = "";

                    if (mediaFile.isImageFile()) {
                        link = "<p>" + mediaFile.getName() + "</p>";
                        link += "<a href='<url>'><img src='<url>?t=true' alt='<name>' width='<width>' height='<height>'></img></a>";
                        link = link.replace("<url>", getMediaFileURL(mediaFile))
                                   .replace("<name>", mediaFile.getName())
                                   .replace("<width>", ""+mediaFile.getThumbnailWidth())
                                   .replace("<height>", ""+mediaFile.getThumbnailHeight());
                    } else {
                        link = "<a href='<url>'><name></a>";
                        link = link.replace("<url>", getMediaFileURL(mediaFile))
                                   .replace("<name>", mediaFile.getName());
                    }
                    bean.setText(link);
                }
            }

            if (StringUtils.isNotEmpty(enclosureUrl)) {
                bean.setEnclosureURL(enclosureUrl);
            }

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

    /**
     * @return the selectedImages
     */
    public String[] getSelectedImages() {
        return selectedImages;
    }

    /**
     * @param selectedImages the selectedImages to set
     */
    public void setSelectedImages(String[] selectedImages) {
        this.selectedImages = selectedImages;
    }

    /**
     * @return the enclosureUrl
     */
    public String getEnclosureUrl() {
        return enclosureUrl;
    }

    /**
     * @param enclosureUrl the enclosureUrl to set
     */
    public void setEnclosureUrl(String enclosureUrl) {
        this.enclosureUrl = enclosureUrl;
    }

    /**
     * @return the weblog
     */
    public String getWeblog() {
        return weblog;
    }

    /**
     * @param weblog the weblog to set
     */
    public void setWeblog(String weblog) {
        this.weblog = weblog;
    }

}
