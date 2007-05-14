/*
 * TemplateEditBean.java
 *
 * Created on April 23, 2007, 3:24 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.apache.roller.ui.authoring.struts2;

import java.util.Locale;
import org.apache.roller.RollerException;
import org.apache.roller.pojos.WeblogTemplate;


/**
 * Form bean for TemplateEdit action.
 */
public class TemplateEditBean {
    
    private String id;
    private String action;
    private String name;
    private String description;
    private String link;
    private String contents;
    private java.util.Date lastModified;
    private String templateLanguage;
    private boolean navbar;
    private boolean hidden;
    private String decoratorName;
    private String outputContentType;
    private Boolean autoContentType = Boolean.TRUE;
    private String manualContentType = null;
    private boolean required = false;
    
    
    public String getId() {
        return this.id;
    }
    
    public void setId( String id ) {
        this.id = id;
    }
    
    public String getAction() {
        return this.action;
    }
    
    public void setAction( String action ) {
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
    
    public void setContents( String contents ) {
        this.contents = contents;
    }
    
    public java.util.Date getLastModified() {
        return this.lastModified;
    }
    
    public void setLastModified( java.util.Date lastModified ) {
        this.lastModified = lastModified;
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
    
    public String getDecoratorName() {
        return this.decoratorName;
    }
    
    public void setDecoratorName( String decoratorName ) {
        this.decoratorName = decoratorName;
    }
    
    public String getOutputContentType() {
        return this.outputContentType;
    }
    
    public void setOutputContentType( String outputContentType ) {
        this.outputContentType = outputContentType;
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
    
    public boolean isRequired() {
        return required;
    }

    public void setRequired(boolean required) {
        this.required = required;
    }
    
    
    /**
     * Copy values from this form bean to the specified data object.
     */
    public void copyTo(WeblogTemplate dataHolder) {
        
        // only custom templates get to modify name, description, and link
        if(WeblogTemplate.ACTION_CUSTOM.equals(dataHolder.getAction())) {
            dataHolder.setName(this.name);
            dataHolder.setDescription(this.description);
            dataHolder.setLink(this.link);
        }
        
        dataHolder.setContents(this.contents);
        dataHolder.setLastModified(this.lastModified);
        dataHolder.setTemplateLanguage(this.templateLanguage);
        dataHolder.setNavbar(this.navbar);
        dataHolder.setHidden(this.hidden);
        dataHolder.setDecoratorName(this.decoratorName);
        dataHolder.setOutputContentType(this.outputContentType);
    }
    
    
    /**
     * Copy values from specified data object to this form bean.
     * Includes all types.
     */
    public void copyFrom(WeblogTemplate dataHolder) {
        
        this.id = dataHolder.getId();
        this.action = dataHolder.getAction();
        this.name = dataHolder.getName();
        this.description = dataHolder.getDescription();
        this.link = dataHolder.getLink();
        this.contents = dataHolder.getContents();
        this.lastModified = dataHolder.getLastModified();
        this.templateLanguage = dataHolder.getTemplateLanguage();
        this.navbar = dataHolder.isNavbar();
        this.hidden = dataHolder.isHidden();
        this.decoratorName = dataHolder.getDecoratorName();
        this.outputContentType = dataHolder.getOutputContentType();
        this.required = dataHolder.isRequired();
    }
    
}
