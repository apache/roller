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

package org.apache.roller.weblogger.pojos;

import java.io.Serializable;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.roller.weblogger.WebloggerException;
import org.apache.roller.util.UUIDGenerator;


/**
 * POJO that represents a single user defined template page.
 *
 * This template is different from the generic template because it also
 * contains a reference to the website it is part of.
 */
public class WeblogTemplate implements ThemeTemplate, Serializable {
    
    public static final long serialVersionUID = -613737191638263428L;
    public static final String DEFAULT_PAGE = "Weblog";
    
    private static Log log = LogFactory.getLog(WeblogTemplate.class);
    private static Set requiredTemplates = null;
    
    // attributes
    private String id = UUIDGenerator.generateUUID();
    private String  action = null;
    private String  name = null;
    private String  description = null;
    private String  link = null;
    private String  contents = null;
    private Date    lastModified = null;
    private String  templateLanguage = null;
    private boolean hidden = false;
    private boolean navbar = false;
    private String  decoratorName = null;
    private String  outputContentType = null;
    
    // associations
    private Weblog weblog = null;
    
    
    static {
        requiredTemplates = new HashSet();
        requiredTemplates.add("Weblog");
        requiredTemplates.add("_day");
        requiredTemplates.add("_css");
        requiredTemplates.add("_decorator");
    }
    
    
    public WeblogTemplate() {}
    
    
    public ThemeTemplate getDecorator() {
        if(decoratorName != null && !id.equals(decoratorName)) {
            try {
                return weblog.getTheme().getTemplateByName(decoratorName);
            } catch (WebloggerException ex) {
                log.error("Error getting decorator["+decoratorName+"] "+
                        "for template "+id);
            }
        }
        return null;
    }
    
    
    public String getId() {
        return this.id;
    }
    
    public void setId( String id ) {
        this.id = id;
    }
    
    
    public Weblog getWebsite() {
        return this.weblog;
    }
    
    public void setWebsite( Weblog website ) {
        this.weblog = website;
    }
    
    
    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }
    
    
    public String getName() {
        return this.name;
    }
    
    public void setName( String name ) {
        this.name = name;
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
    
    public void setContents( String template ) {
        this.contents = template;
    }
    
    
    public Date getLastModified() {
        return lastModified;
    }
    
    public void setLastModified(final Date newtime ) {
        lastModified = newtime;
    }
    
    
    public String getTemplateLanguage() {
        return templateLanguage;
    }

    public void setTemplateLanguage(String templateLanguage) {
        this.templateLanguage = templateLanguage;
    }
    
    
    public boolean isNavbar() {
        return navbar;
    }

    public void setNavbar(boolean navbar) {
        this.navbar = navbar;
    }
    
    
    public boolean isHidden() {
        return hidden;
    }

    public void setHidden(boolean isHidden) {
        this.hidden = isHidden;
    }
        
    
    public String getDecoratorName() {
        return decoratorName;
    }

    public void setDecoratorName(String decorator) {
        this.decoratorName = decorator;
    }
    
    
    /** 
     * Content-type rendered by template or null for auto-detection by link extension.
     */
    public String getOutputContentType() {
        return outputContentType;
    }
    
    public void setOutputContentType(String outputContentType) {
        this.outputContentType = outputContentType;
    }
    
    
    /**
     * Determine if this WeblogTemplate is required or not.
     */
    public boolean isRequired() {
       /*
        * this is kind of hacky right now, but it's like that so we can be
        * reasonably flexible while we migrate old blogs which may have some
        * pretty strange customizations.
        *
        * my main goal starting now is to prevent further deviations from the
        * standardized templates as we move forward.
        *
        * eventually, the required flag should probably be stored in the db
        * and possibly applicable to any template.
        */
        return (requiredTemplates.contains(this.name) || "Weblog".equals(this.link));
    }
    
    
    /**
     * A convenience method for testing if this template represents a 'custom'
     * template, meaning a template with action = ACTION_CUSTOM.
     */
    public boolean isCustom() {
        return ACTION_CUSTOM.equals(getAction()) && !isRequired();
    }
    
    
    //------------------------------------------------------- Good citizenship

    public String toString() {
        StringBuffer buf = new StringBuffer();
        buf.append("{");
        buf.append(this.id);
        buf.append(", ").append(this.name);
        buf.append(", ").append(this.link);
        buf.append(", ").append(this.decoratorName);
        buf.append(", ").append(this.templateLanguage);
        buf.append("}");
        return buf.toString();
    }

    public boolean equals(Object other) {
        if (other == this) return true;
        if (other instanceof WeblogTemplate != true) return false;
        WeblogTemplate o = (WeblogTemplate)other;
        return new EqualsBuilder()
            .append(name, o.getName()) 
            .append(getWebsite(), o.getWebsite()) 
            .isEquals();
    }
    
    public int hashCode() { 
        return new HashCodeBuilder()
            .append(getName())
            .append(getWebsite())
            .toHashCode();
    }
    
}
