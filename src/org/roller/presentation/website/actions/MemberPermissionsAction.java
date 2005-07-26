package org.roller.presentation.website.actions;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.actions.DispatchAction;

/**
 * Allows website admin to change website member permissions.
 * 
 * @struts.action path="/editor/memberPermissions" parameter="method" 
 * @struts.action-forward name="memberPermissions.page" path="/website/MemberPermissions.jsp"
 */
public class MemberPermissionsAction extends DispatchAction
{
    private static Log mLogger =
        LogFactory.getFactory().getInstance(MemberPermissionsAction.class);
    
    public ActionForward edit(
        ActionMapping       mapping,
        ActionForm          actionForm,
        HttpServletRequest  request,
        HttpServletResponse response)
        throws IOException, ServletException
    {
        ActionForward forward = mapping.findForward("memberPermissions.page");
        return forward;
    }
}
