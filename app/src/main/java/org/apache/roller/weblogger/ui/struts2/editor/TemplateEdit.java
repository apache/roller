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
import org.apache.roller.weblogger.WebloggerException;
import org.apache.roller.weblogger.business.WebloggerFactory;
import org.apache.roller.weblogger.pojos.TemplateRendition.TemplateLanguage;
import org.apache.roller.weblogger.pojos.WeblogTemplate;
import org.apache.roller.weblogger.ui.struts2.util.UIAction;
import org.apache.roller.weblogger.util.cache.CacheManager;
import org.apache.struts2.interceptor.validation.SkipValidation;

import java.util.Date;
import java.util.EnumMap;
import java.util.Map;


/**
 * Action which handles editing for a single WeblogTemplate.
 */
public class TemplateEdit extends UIAction {

    private static Log log = LogFactory.getLog(TemplateEdit.class);

    // form bean for collection all template properties
    private TemplateEditBean bean = new TemplateEditBean();

    // the template we are working on
    private WeblogTemplate template = null;

    public TemplateEdit() {
        this.actionName = "templateEdit";
        this.desiredMenu = "editor";
        this.pageTitle = "pagesForm.title";
    }

    public void myPrepare() {
        try {
            setTemplate(WebloggerFactory.getWeblogger().getWeblogManager().getTemplate(getBean().getId()));
        } catch (WebloggerException ex) {
            log.error("Error looking up template - " + getBean().getId(), ex);
        }
    }


    /**
     * Show template edit page.
     */
    @SkipValidation
    public String execute() {
        try {
            if (getTemplate() == null) {
                addError("Unable to locate specified template");
                return LIST;
            }
            WeblogTemplate page = getTemplate();
            getBean().copyFrom(template);

            // empty content-type indicates that page uses auto content-type detection
            if (StringUtils.isEmpty(page.getOutputContentType())) {
                getBean().setAutoContentType(Boolean.TRUE);
            } else {
                getBean().setAutoContentType(Boolean.FALSE);
                getBean().setManualContentType(page.getOutputContentType());
            }

        } catch (WebloggerException ex) {
           log.error("Error updating page - " + getBean().getId(), ex);
           addError("Error saving template - check Roller logs");
        }

        return INPUT;
    }

    /**
     * Save an existing template.
     */
    public String save() {
        log.debug("Entering save()");

        if (getTemplate() == null) {
            addError("Unable to locate specified template");
            return LIST;
        }

        // validation
        myValidate();

        if (!hasActionErrors()) {
            try {

                WeblogTemplate templateToSave = getTemplate();
                getBean().copyTo(templateToSave);
                templateToSave.setLastModified(new Date());

                if (getBean().getAutoContentType() == null ||
                        !getBean().getAutoContentType()) {
                    templateToSave.setOutputContentType(getBean().getManualContentType());
                } else {
                    // empty content-type indicates that template uses auto content-type detection
                    templateToSave.setOutputContentType(null);
                }

                // save template
                WebloggerFactory.getWeblogger().getWeblogManager().saveTemplate(templateToSave);
                log.debug("Saved template: " + templateToSave.getId());

                //flush
                WebloggerFactory.getWeblogger().flush();

                // notify caches
                CacheManager.invalidate(templateToSave);

                // success message
                addMessage("pageForm.save.success", templateToSave.getName());

            } catch (Exception ex) {
                log.error("Error updating page - " + getBean().getId(), ex);
                addError("Error updating template - check Roller logs");
            }
        }

        log.debug("Leaving save()");
        return INPUT;
    }

    private void myValidate() {

        // if name changed make sure there isn't a conflict
        if (!getTemplate().getName().equals(getBean().getName())) {
            try {
                if (WebloggerFactory.getWeblogger().getWeblogManager()
                    .getTemplateByName(getActionWeblog(), getBean().getName()) != null) {
                    addError("pagesForm.error.alreadyExists", getBean().getName());
                }
            } catch (WebloggerException ex) {
                log.error("Error checking page name uniqueness", ex);
            }
        }

        // if link changed make sure there isn't a conflict
        if (!StringUtils.isEmpty(getBean().getLink()) &&
                !getBean().getLink().equals(getTemplate().getLink())) {
            try {
                if (WebloggerFactory.getWeblogger().getWeblogManager()
                        .getTemplateByLink(getActionWeblog(), getBean().getLink()) != null) {
                    addError("pagesForm.error.alreadyExists", getBean().getLink());
                }
            } catch (WebloggerException ex) {
                log.error("Error checking page link uniqueness", ex);
            }
        }
    }

    public Map<TemplateLanguage, String> getTemplateLanguages() {
        Map<TemplateLanguage, String> langMap = new EnumMap<TemplateLanguage, String>(TemplateLanguage.class);
        for (TemplateLanguage lang : TemplateLanguage.values()) {
            langMap.put(lang, lang.getReadableName());
        }
        return langMap;
    }


    public TemplateEditBean getBean() {
        return bean;
    }

    public void setBean(TemplateEditBean bean) {
        this.bean = bean;
    }

    public WeblogTemplate getTemplate() {
        return template;
    }

    public void setTemplate(WeblogTemplate template) {
        this.template = template;
    }
}