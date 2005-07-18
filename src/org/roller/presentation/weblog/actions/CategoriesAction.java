/*
 * Created on Oct 21, 2003
 */
package org.roller.presentation.weblog.actions;

import java.util.Comparator;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.TreeSet;

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
import org.roller.model.RollerFactory;
import org.roller.model.WeblogManager;
import org.roller.pojos.WeblogCategoryData;
import org.roller.pojos.WebsiteData;
import org.roller.presentation.RollerRequest;
import org.roller.presentation.RollerSession;
import org.roller.presentation.weblog.formbeans.CategoriesForm;

/**
 * Actions that are initiated from the CategoriesForm.
 * 
 * @struts.action name="categoriesForm" path="/editor/categories" parameter="method"
 * @struts.action-forward name="CategoriesForm" path="/weblog/CategoriesForm.jsp"
 * 
 * @author Dave Johnson
 */
public class CategoriesAction extends DispatchAction
{
    private static Log mLogger = 
        LogFactory.getFactory().getInstance(CategoriesAction.class);

    /**
     * Present the CategoriesForm loaded with category specified by request.
     * @param mapping Action mapping.
     * @param actionForm Form bean.
     * @param request Request.
     * @param response Response.
     * @return Forward to CategoriesForm or access-denied.
     * @throws RollerException
     */
    public ActionForward selectCategory(
        ActionMapping       mapping,
        ActionForm          actionForm,
        HttpServletRequest  request,
        HttpServletResponse response)
        throws RollerException
    {
        RollerRequest rreq = RollerRequest.getRollerRequest(request);
        RollerSession rollerSession = RollerSession.getRollerSession(
                rreq.getRequest());
        if (rollerSession.isUserAuthorizedToEdit())
        {
            addModelObjects(request, (CategoriesForm)actionForm);
            return mapping.findForward("CategoriesForm");
        }
        else
        {
            return mapping.findForward("access-denied");
        }
    }

     /**
     * Move Categories and bookmarks indicated by CategoriesForm bean.
     * @param mapping Action mapping.
     * @param actionForm  Form bean.
     * @param request Request.
     * @param response Response.
     * @return Forward to CategoriesForm or access-denied.
     * @throws RollerException
     */
    public ActionForward moveSelected(
        ActionMapping       mapping,
        ActionForm          actionForm,
        HttpServletRequest  request,
        HttpServletResponse response)
        throws RollerException
    {
        ActionMessages messages = new ActionMessages();
        ActionForward forward = mapping.findForward("CategoriesForm");
        RollerRequest rreq = RollerRequest.getRollerRequest(request);
        RollerSession rollerSession = RollerSession.getRollerSession(
                rreq.getRequest());
        if (rollerSession.isUserAuthorizedToEdit())
        {
            try 
            {
                WeblogManager wmgr = RollerFactory.getRoller().getWeblogManager();
                CategoriesForm form = (CategoriesForm)actionForm; 
    
                mLogger.debug("Moving categories to category, id=" 
                    + form.getMoveToCategoryId());
                    
                // Move subCategories to new category.
                String Categories[] = form.getSelectedCategories();
                WeblogCategoryData parent = 
                    wmgr.retrieveWeblogCategory(form.getMoveToCategoryId());
                if (null != Categories)
                {
                    for (int i = 0; i < Categories.length; i++)
                    {
                        WeblogCategoryData cd = 
                            wmgr.retrieveWeblogCategory(Categories[i]); 
                        
                        // Don't move category into itself.                  
                        if (    !cd.getId().equals(parent.getId()) 
                             && !parent.descendentOf(cd))
                        {
                            cd.setParent(parent);
                            cd.save();
                        }
                        else 
                        {
                            messages.add(null, new ActionMessage(
                                "categoriesForm.warn.notMoving",cd.getName()));
                        }
                    }
                }
    
                RollerFactory.getRoller().commit();
                
                addModelObjects(request, (CategoriesForm)actionForm);
                saveMessages(request, messages);
            }
            catch (RollerException e)
            {
                ActionErrors errors = new ActionErrors();
                errors.add(null, new ActionError("categoriesForm.error.move"));
                saveErrors(request, errors);       
            }
        }
        else
        {
            forward = mapping.findForward("access-denied");
        }
        return forward;
    }

    /**
     * Load model objects for display in CategoriesForm.
     * @param request
     * @throws RollerException
     */
    private void addModelObjects(HttpServletRequest request, CategoriesForm form) 
        throws RollerException
    {
        RollerRequest rreq = RollerRequest.getRollerRequest(request);
        WebsiteData wd = RollerSession.getRollerSession(request).getCurrentWebsite();
        WeblogManager wmgr = RollerFactory.getRoller().getWeblogManager();
        
        TreeSet allCategories = new TreeSet(new CategoryPathComparator());
        
        // Find catid wherever it may be
        String catId = (String)request.getAttribute(RollerRequest.WEBLOGCATEGORYID_KEY);
        if (null == catId) 
        {
            catId = request.getParameter(RollerRequest.WEBLOGCATEGORYID_KEY);
        }  
        if (null == catId)
        {
            catId = form.getId();     
        }
       
        WeblogCategoryData cat = null;
        if (null == catId || catId.equals("null"))
        {
            cat = wmgr.getRootWeblogCategory(wd);
        }
        else 
        {
            cat = wmgr.retrieveWeblogCategory(catId);            
        }
        form.setId(cat.getId());
        
        request.setAttribute("category", cat);
        request.setAttribute("categories", cat.getWeblogCategories());
        
        if (null != cat.getParent())
        {
            LinkedList catPath = new LinkedList();
            catPath.add(0, cat);
            WeblogCategoryData parent = cat.getParent();
            while (parent != null) 
            {
                catPath.add(0, parent);
                parent = parent.getParent();   
            }
            request.setAttribute("categoryPath", catPath);
            
            request.setAttribute(
                RollerRequest.PARENTID_KEY, cat.getParent().getId());
        }
    
        // Build collection of all Categories, except for current one, 
        // sorted by path.
        Iterator iter = wmgr.getWeblogCategories(wd).iterator();
        while (iter.hasNext())
        {
            WeblogCategoryData cd = (WeblogCategoryData) iter.next();
            if (!cd.getId().equals(catId))
            {
                allCategories.add(cd);
            }
        }
        request.setAttribute("allCategories", allCategories);
    }

    private static final class CategoryPathComparator implements Comparator
    {
        public int compare(Object o1, Object o2) {
            WeblogCategoryData f1 = (WeblogCategoryData)o1; 
            WeblogCategoryData f2 = (WeblogCategoryData)o2; 
            return f1.getPath().compareTo(f2.getPath());
        }
    }
}
