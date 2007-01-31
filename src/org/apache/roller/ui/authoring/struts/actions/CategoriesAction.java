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
/*
 * Created on Oct 21, 2003
 */
package org.apache.roller.ui.authoring.struts.actions;

import java.text.MessageFormat;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.LinkedList;
import java.util.Set;
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
import org.apache.roller.RollerException;
import org.apache.roller.business.RollerFactory;
import org.apache.roller.business.WeblogManager;
import org.apache.roller.pojos.WeblogCategoryData;
import org.apache.roller.ui.core.BasePageModel;
import org.apache.roller.ui.core.RollerRequest;
import org.apache.roller.ui.core.RollerSession;
import org.apache.roller.ui.authoring.struts.formbeans.CategoriesForm;
import org.apache.roller.ui.core.RequestConstants;

/**
 * Actions that are initiated from the CategoriesForm.
 * 
 * @struts.action name="categoriesForm" path="/roller-ui/authoring/categories" parameter="method"
 * @struts.action-forward name="CategoriesForm" path=".CategoriesForm"
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
        CategoriesPageModel pageModel = new CategoriesPageModel(
                request, response, mapping, (CategoriesForm)actionForm);
        RollerSession rses = RollerSession.getRollerSession(request);
        if (rses.isUserAuthorizedToAuthor(pageModel.getCategory().getWebsite()))
        {
            request.setAttribute("model", pageModel);
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

        CategoriesPageModel pageModel = new CategoriesPageModel(
                request, response, mapping, (CategoriesForm)actionForm);
        
        RollerSession rses = RollerSession.getRollerSession(request);
        if (rses.isUserAuthorizedToAuthor(pageModel.getCategory().getWebsite()))
        {
            request.setAttribute("model", pageModel);
            try 
            {
                WeblogManager wmgr = RollerFactory.getRoller().getWeblogManager();
                CategoriesForm form = (CategoriesForm)actionForm; 
    
                mLogger.debug("Moving categories to category, id=" 
                    + form.getMoveToCategoryId());
                    
                // Move subCategories to new category.
                String Categories[] = form.getSelectedCategories();
                WeblogCategoryData parent = 
                    wmgr.getWeblogCategory(form.getMoveToCategoryId());
                if (null != Categories)
                {
                    for (int i = 0; i < Categories.length; i++)
                    {
                        WeblogCategoryData cd = 
                            wmgr.getWeblogCategory(Categories[i]); 
                        
                        // Don't move category into itself.                  
                        if (    !cd.getId().equals(parent.getId()) 
                             && !parent.descendentOf(cd))
                        {
                            wmgr.moveWeblogCategory(cd, parent);
                        }
                        else 
                        {
                            messages.add(null, new ActionMessage(
                                "categoriesForm.warn.notMoving",cd.getName()));
                        }
                    }
                }
                
                RollerFactory.getRoller().flush();
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

    private static final class CategoryPathComparator implements Comparator
    {
        public int compare(Object o1, Object o2) {
            WeblogCategoryData f1 = (WeblogCategoryData)o1; 
            WeblogCategoryData f2 = (WeblogCategoryData)o2; 
            return f1.getPath().compareTo(f2.getPath());
        }
    }
    
    
    public class CategoriesPageModel extends BasePageModel
    {
        private CategoriesForm form = null;
        private WeblogCategoryData cat = null;
        private TreeSet allCategories = null;
        private List catPath = null;
        
        public WeblogCategoryData getCategory() { return cat; }
        public Set getAllCategories() { return allCategories; }
        public List getCategoryPath() { return catPath; }
        
        public CategoriesPageModel(
                HttpServletRequest request,
                HttpServletResponse response,
                ActionMapping mapping,
                CategoriesForm form) throws RollerException
        {
            super("dummy",  request, response, mapping);
            this.form = form;
            
            RollerRequest rreq = RollerRequest.getRollerRequest(request);
            WeblogManager wmgr = RollerFactory.getRoller().getWeblogManager();

            allCategories = new TreeSet(new CategoryPathComparator());

            // Find catid wherever it may be
            String catId = (String)
                request.getAttribute(RequestConstants.WEBLOGCATEGORY_ID);
            if (null == catId) 
            {
                catId = request.getParameter(RequestConstants.WEBLOGCATEGORY_ID);
            }  
            if (null == catId)
            {
                catId = form.getId();     
            }

            cat = null;
            if (null == catId || catId.equals("null"))
            {
                cat = wmgr.getRootWeblogCategory(website);
            }
            else 
            {
                cat = wmgr.getWeblogCategory(catId);  
                website = cat.getWebsite();
            }
            form.setId(cat.getId());

            //request.setAttribute("categories", cat.getWeblogCategories());

            if (null != cat.getParent())
            {
                catPath = new LinkedList();
                catPath.add(0, cat);
                WeblogCategoryData parent = cat.getParent();
                while (parent != null) 
                {
                    catPath.add(0, parent);
                    parent = parent.getParent();   
                }
                //request.setAttribute("categoryPath", catPath);

                request.setAttribute(
                    RequestConstants.PARENT_ID, cat.getParent().getId());
            }

            // Build collection of all Categories, except for current one, 
            // sorted by path.
            Iterator iter = wmgr.getWeblogCategories(website, true).iterator();
            while (iter.hasNext())
            {
                WeblogCategoryData cd = (WeblogCategoryData) iter.next();
                if (!cd.getId().equals(catId))
                {
                    allCategories.add(cd);
                }
            }
            // For Struts tags
            request.setAttribute("allCategories", allCategories);
            request.setAttribute("category", cat);
        }        
        public String getTitle()
        {
            if (catPath == null || catPath.isEmpty()) 
            {
                return bundle.getString("categoriesForm.rootTitle");
            }
            else 
            {
                return MessageFormat.format(
                        bundle.getString("categoriesForm.parent"),
                        new Object[] {cat.getName()});
            }
        }
    }
}
