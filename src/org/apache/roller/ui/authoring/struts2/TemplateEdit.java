/*
 * TemplateEdit.java
 *
 * Created on April 23, 2007, 2:54 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.apache.roller.ui.authoring.struts2;

import java.util.Arrays;
import java.util.Date;
import java.util.List;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.roller.RollerException;
import org.apache.roller.business.RollerFactory;
import org.apache.roller.business.UserManager;
import org.apache.roller.config.RollerConfig;
import org.apache.roller.pojos.PermissionsData;
import org.apache.roller.pojos.UserData;
import org.apache.roller.pojos.WeblogTemplate;
import org.apache.roller.pojos.WebsiteData;
import org.apache.roller.ui.core.util.menu.Menu;
import org.apache.roller.ui.core.util.menu.MenuHelper;
import org.apache.roller.ui.core.util.struts2.UIAction;
import org.apache.roller.util.Utilities;
import org.apache.roller.util.cache.CacheManager;


/**
 * Action which handles editing for a single WeblogTemplate.
 */
public class TemplateEdit extends UIAction {
    
    private static Log log = LogFactory.getLog(Templates.class);
    
    // form bean for collection all template properties
    private TemplateEditBean bean = new TemplateEditBean();
    
    
    public TemplateEdit() {
        this.actionName = "template";
        this.desiredMenu = "editor";
        this.pageTitle = "pagesForm.title";
    }
    
    
    // must be a weblog admin to use this action
    public short requiredWeblogPermissions() {
        return PermissionsData.ADMIN;
    }
    
    
    public String execute() {
        
        try {
            UserManager mgr = RollerFactory.getRoller().getUserManager();
            WeblogTemplate page = mgr.getPage(getBean().getId());
            if(page == null) {
                throw new RollerException("page not found");
            }
            
            getBean().copyFrom(page);
            
            // empty content-type indicates that page uses auto content-type detection
            if (StringUtils.isEmpty(page.getOutputContentType())) {
                getBean().setAutoContentType(Boolean.TRUE);
            } else {
                getBean().setAutoContentType(Boolean.FALSE);
                getBean().setManualContentType(page.getOutputContentType());
            }
            
        } catch (RollerException ex) {
            log.error("Error loading page to edit - "+getBean().getId(), ex);
            // TODO: i18n
            addError("Couldn't find specified page to edit");
            return "list";
        }
        
        return SUCCESS;
    }
    
    
    public String save() {
        
        // validation
        myValidate();
        
        if(!hasActionErrors()) try {
            
            UserManager mgr = RollerFactory.getRoller().getUserManager();
            WeblogTemplate page = mgr.getPage(getBean().getId());
            if(page == null) {
                throw new RollerException("page not found");
            }
            
            getBean().copyTo(page);
            page.setLastModified( new Date() );
            
            if (getBean().getAutoContentType() == null ||
                    !getBean().getAutoContentType().booleanValue()) {
                page.setOutputContentType(getBean().getManualContentType());
            } else {
                // empty content-type indicates that page uses auto content-type detection
                page.setOutputContentType(null);
            }
            
            // save template and flush
            mgr.savePage( page );
            RollerFactory.getRoller().flush();
            
            CacheManager.invalidate(page);
            
            // success message
            addMessage("pageForm.save.success", page.getName());
            
        } catch (RollerException ex) {
            log.error("Error updating page - "+getBean().getId(), ex);
            // TODO: i18n
            addError("Error saving template");
        }
        
        return SUCCESS;
    }
    
    
    public String cancel() {
        return "cancel";
    }
    
    
    private void myValidate() {
        
        // make sure that we have an appropriate name value
        
        // make sure that we have an appropriate action value
        
        // first off, check if template already exists
//        WeblogTemplate existingPage = mgr.getPageByName(website, getNewTmplName());
//        if(existingPage != null) {
//            addError("pagesForm.error.alreadyExists", getNewTmplName());
//            return INPUT;
//        }
        
    }
    
    
    public List getTemplateLanguages() {
        String langs = RollerConfig.getProperty("rendering.templateLanguages","velocity");
        String[] langsArray = Utilities.stringToStringArray(langs, ",");
        return Arrays.asList(langsArray);
    }
    
    
    public TemplateEditBean getBean() {
        return bean;
    }

    public void setBean(TemplateEditBean bean) {
        this.bean = bean;
    }
    
}
