/*
 * CustomPingTargetAdd.java
 *
 * Created on April 30, 2007, 9:52 AM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.apache.roller.ui.struts2.editor;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.roller.pojos.PermissionsData;
import org.apache.roller.pojos.PingTargetData;
import org.apache.roller.ui.struts2.common.PingTargetAddBase;


/**
 * Action for adding a custom weblog ping target.
 */
public class CustomPingTargetAdd extends PingTargetAddBase {
    
    private static Log log = LogFactory.getLog(CustomPingTargetAdd.class);
    
    
    public CustomPingTargetAdd() {
        this.actionName = "customPingTargetAdd";
        this.desiredMenu = "editor";
        this.pageTitle = "pingTarget.pingTarget";
    }
    
    
    // admin perms required
    public short requiredWeblogPermission() {
        return PermissionsData.ADMIN;
    }
    
    
    protected Log getLogger() {
        return log;
    }
    
    
    protected PingTargetData createPingTarget() {
        
        return new PingTargetData(
                null, 
                getBean().getName(), 
                getBean().getPingUrl(), 
                getActionWeblog(), 
                false);
    }
    
}
