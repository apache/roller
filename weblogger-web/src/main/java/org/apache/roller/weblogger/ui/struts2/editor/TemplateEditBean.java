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

import org.apache.roller.weblogger.pojos.WeblogTemplate;
import org.apache.struts2.components.If;


/**
 * Form bean for TemplateEdit action.
 */
public class TemplateEditBean {
    
    private String id = null;
    private String name = null;
    private String action = null;
    private String description = null;
    private String link = null;
    private String contents = null;
    private String templateLanguage = null;
    private boolean navbar= false;
    private boolean hidden = false;
    private Boolean autoContentType = Boolean.TRUE;
    private String manualContentType = null;
    private String type = null;

     // template ID of mobile template version
    private String mobileTemplateId = null;

    // template ID of standard template version
    private String standardTemplateId = null;
    
    
    public String getId() {
        return this.id;
    }
    
    public void setId( String id ) {
        this.id = id;
    }
    
    public String getName() {
        return this.name;
    }
    
    public void setName( String name ) {
        this.name = name;
    }
    
	public void setAction(String action) {
		this.action = action;
	}

	public String getAction() {
		return action;
	}
    
    public String getDescription() {
        return this.description;
    }
    
    public void setDescription( String description ) {
        this.description = description;
    }
    
    public String getLink() {
        return this.link;
    }
    
    public void setLink( String link ) {
        this.link = link;
    }
    
    public String getContents() {
        return this.contents;
    }

    public void setContents( String contents ) {
        this.contents = contents;
    }
    
    public String getTemplateLanguage() {
        return this.templateLanguage;
    }
    
    public void setTemplateLanguage( String templateLanguage ) {
        this.templateLanguage = templateLanguage;
    }
    
    public boolean isNavbar() {
        return this.navbar;
    }
    
    public void setNavbar( boolean navbar ) {
        this.navbar = navbar;
    }
    
    public boolean isHidden() {
        return this.hidden;
    }
    
    public void setHidden( boolean hidden ) {
        this.hidden = hidden;
    }
    
    public Boolean getAutoContentType() {
        return autoContentType;
    }
    
    public void setAutoContentType(Boolean autoContentType) {
        this.autoContentType = autoContentType;
    }
    
    public String getManualContentType() {
        return manualContentType;
    }
    
    public void setManualContentType(String manualContentType) {
        this.manualContentType = manualContentType;
    }
    
    
    public void copyTo(WeblogTemplate dataHolder) {

        // change the default content of template if we are changing standard type code
        if("standard".equals(getType()))
        dataHolder.setContents(getContents());
        
        // the rest of the template properties can only be modified when
        // dealing with a CUSTOM weblog template
        if(dataHolder.isCustom()) {
            dataHolder.setName(getName());
            dataHolder.setAction(getAction());
            dataHolder.setDescription(getDescription());
            dataHolder.setLink(getLink());
            dataHolder.setNavbar(isNavbar());
            dataHolder.setHidden(isHidden());
            dataHolder.setTemplateLanguage(getTemplateLanguage());
        }
    }
    
    
    public void copyFrom(WeblogTemplate dataHolder) {
        
        this.id = dataHolder.getId();
        this.name = dataHolder.getName();
        this.action = dataHolder.getAction();
        this.description = dataHolder.getDescription();
        this.link = dataHolder.getLink();
        this.contents = dataHolder.getContents();
        this.navbar = dataHolder.isNavbar();
        this.hidden = dataHolder.isHidden();
        this.templateLanguage = dataHolder.getTemplateLanguage();
        setManualContentType(dataHolder.getOutputContentType());
        if(getManualContentType() != null) {
            setAutoContentType(Boolean.FALSE);
        }
    }

    public String getMobileTemplateId() {
        return mobileTemplateId;
    }

    public void setMobileTemplateId(String mobileTemplateId) {
        this.mobileTemplateId = mobileTemplateId;
    }

    public String getStandardTemplateId() {
        return standardTemplateId;
    }

    public void setStandardTemplateId(String standardTemplateId) {
        this.standardTemplateId = standardTemplateId;
    }

    public boolean isMobile() {
        return (id.equals(mobileTemplateId));
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }
}
