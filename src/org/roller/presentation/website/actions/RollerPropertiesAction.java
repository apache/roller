/*
 * RollerPropertiesAction.java
 *
 * Created on April 21, 2005, 2:48 PM
 */

package org.roller.presentation.website.actions;

import java.io.IOException;
import java.util.Iterator;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.struts.action.ActionError;
import org.apache.struts.action.ActionErrors;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.ActionMessage;
import org.apache.struts.action.ActionMessages;
import org.apache.struts.actions.DispatchAction;
import org.roller.RollerException;
import org.roller.RollerPermissionsException;
import org.roller.model.PropertiesManager;
import org.roller.model.Roller;
import org.roller.model.RollerFactory;
import org.roller.pojos.RollerPropertyData;
import org.roller.presentation.RollerRequest;
import org.roller.presentation.RollerSession;



/**
 * Struts Action class which handles requests to the Admin Properties page.
 *
 * @author Allen Gilliland
 *
 * @struts.action path="/admin/rollerConfig"
 *  scope="request" parameter="method"
 *
 * @struts.action-forward name="rollerProperties.page"
 *  path="/website/rollerProperties.jsp"
 */
public class RollerPropertiesAction extends DispatchAction {
    
    private static Log mLogger =
            LogFactory.getFactory().getInstance(RollerPropertiesAction.class);
    
    
    public ActionForward unspecified(
            ActionMapping       mapping,
            ActionForm          actionForm,
            HttpServletRequest  request,
            HttpServletResponse response)
            throws IOException, ServletException {
        
        // make "edit" our default action
        return this.edit(mapping, actionForm, request, response);
    }
    
    
    public ActionForward edit(
            ActionMapping       mapping,
            ActionForm          actionForm,
            HttpServletRequest  request,
            HttpServletResponse response)
            throws IOException, ServletException {
        
        mLogger.debug("Handling edit request");
        
        ActionForward forward = mapping.findForward("rollerProperties.page");
        try {
            RollerRequest rreq = RollerRequest.getRollerRequest(request);
            RollerSession rollerSession = RollerSession.getRollerSession(request);
            if (rollerSession.isAdminUser() ) {
                
                // just grab our properties map and put it in the request
                Roller mRoller = RollerFactory.getRoller();
                PropertiesManager propsManager = mRoller.getPropertiesManager();
                Map props = propsManager.getProperties();
                request.setAttribute("RollerProps", props);
                
            } else {
                forward = mapping.findForward("access-denied");
            }
        } catch (Exception e) {
            mLogger.error("ERROR in action",e);
            throw new ServletException(e);
        }
        return forward;
    }
    
    
    public ActionForward update(
            ActionMapping       mapping,
            ActionForm          actionForm,
            HttpServletRequest  request,
            HttpServletResponse response)
            throws IOException, ServletException {
        
        mLogger.debug("Handling update request");
        
        ActionForward forward = mapping.findForward("rollerProperties.page");
        ActionErrors errors = new ActionErrors();
        try {
            RollerRequest rreq = RollerRequest.getRollerRequest(request);
            RollerSession rollerSession = RollerSession.getRollerSession(request);
            if ( rollerSession.isUserAuthorizedToAdmin() && rollerSession.isAdminUser() ) {
            
                // just grab our properties map and put it in the request
                Roller mRoller = RollerFactory.getRoller();
                PropertiesManager propsManager = mRoller.getPropertiesManager();
                Map props = propsManager.getProperties();
                request.setAttribute("RollerProps", props);
                
                // only set values for properties that are already defined
                String propName = null;
                RollerPropertyData updProp = null;
                String incomingProp = null;
                Iterator propsIT = props.keySet().iterator();
                while(propsIT.hasNext()) {
                    propName = (String) propsIT.next();
                    updProp = (RollerPropertyData) props.get(propName);
                    incomingProp = request.getParameter(updProp.getName());
                    
                    mLogger.debug("Checking property ["+propName+"]");
                    
                    // some special treatment for booleans
                    // this is a bit hacky since we are assuming that any prop
                    // with a value of "true" or "false" is meant to be a boolean
                    // it may not always be the case, but we should be okay for now
                    if(updProp.getValue().equals("true") ||
                            updProp.getValue().equals("false")) {
                        
                        if(incomingProp == null || !incomingProp.equals("on"))
                            incomingProp = "false";
                        else
                            incomingProp = "true";
                    }
                    
                    // only work on props that were submitted with the request
                    if(incomingProp != null) {
                        mLogger.debug("Setting new value for ["+propName+"]");
                        
                        // NOTE: the old way had some locale sensitive way to do this??
                        updProp.setValue(incomingProp.trim());
                    }
                }
                
                // save it
                propsManager.store(props);
                mRoller.getRefererManager().applyRefererFilters();
                mRoller.commit();
                
                ActionMessages uiMessages = new ActionMessages();
                uiMessages.add(null, new ActionMessage("weblogEdit.changesSaved"));
                saveMessages(request, uiMessages);
                
            } else {
                forward = mapping.findForward("access-denied");
            }
            
        } catch (RollerPermissionsException e) {
            errors.add(null, new ActionError("error.permissions.deniedSave"));
            saveErrors(request, errors);
            forward = mapping.findForward("access-denied");
            
        } catch (RollerException e) {
            mLogger.error(e);
            errors.add(ActionErrors.GLOBAL_ERROR, new ActionError(
                    "error.update.rollerConfig",e.getClass().getName()));
            saveErrors(request,errors);
        }
        
        return forward;
    }
    
}
