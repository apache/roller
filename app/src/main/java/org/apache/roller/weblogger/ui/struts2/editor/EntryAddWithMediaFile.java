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

import org.apache.commons.lang3.StringUtils;
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

    private String   weblog = null;
    // used by MediaFileView.jsp: multiple images and optional enclosure URL to create a blog post
    private String[] selectedImages = null;
    // used by MediaFileView.jsp: single image to create a blog post
    private String   selectedImage = null;
    

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

            if (StringUtils.isNotEmpty(selectedImage) && selectedImages == null) {
                selectedImages = new String[1];
                selectedImages[0] = selectedImage;
            }

            StringBuilder sb = new StringBuilder();

            if (selectedImages != null) {
                for (String image : selectedImages) {
                    MediaFile mediaFile = manager.getMediaFile(image);
                    String link;

                    if (mediaFile.isImageFile()) {
                        link = "<p>" + mediaFile.getName() + "</p>";
                        link += "<a href='<url>'><img src='<urlt>' alt='<name>' width='<width>' height='<height>'></img></a>";
                        link = link.replace("<url>", mediaFile.getPermalink())
                                   .replace("<urlt>", mediaFile.getThumbnailURL())
                                   .replace("<name>", mediaFile.getName())
                                   .replace("<width>", ""+mediaFile.getThumbnailWidth())
                                   .replace("<height>", ""+mediaFile.getThumbnailHeight());
                    } else {
                        link = "<a href='<url>'><name></a> (<size> bytes, <type>)";
                        link = link.replace("<url>", mediaFile.getPermalink())
                                   .replace("<name>", mediaFile.getName())
                                   .replace("<size>",""+mediaFile.getLength())
                                   .replace("<type>",mediaFile.getContentType());
                    }
                    sb.append(link);
                }
            }

            if (bean.getEnclosureURL() != null) {
                sb.append("<p>")
                  .append(getText("mediaFileEdit.includesEnclosure"))
                  .append("<br />")
                  .append("<a href=''>")
                  .append(bean.getEnclosureURL())
                  .append("</a></p>");
            }

            bean.setText(sb.toString());

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

    /**
     * @return the selectedImage
     */
    public String getSelectedImage() {
        return selectedImage;
    }

    /**
     * @param selectedImage the selectedImage to set
     */
    public void setSelectedImage(String selectedImage) {
        this.selectedImage = selectedImage;
    }

}
