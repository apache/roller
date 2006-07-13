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
/* Created on Mar 10, 2004 */
package org.apache.roller.ui.authoring.struts.actions;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.roller.RollerException;
import org.apache.roller.model.RollerFactory;
import org.apache.roller.pojos.UserData;
import org.apache.roller.pojos.WeblogEntryData;
import org.apache.roller.ui.core.BasePageModel;
import org.apache.roller.ui.core.RollerContext;
import org.apache.roller.ui.core.RollerRequest;
import org.apache.roller.ui.core.RollerSession;
import org.apache.roller.ui.core.tags.calendar.CalendarModel;
import org.apache.roller.ui.authoring.struts.actions.WeblogEntryPageModel.PageMode;
import org.apache.roller.ui.authoring.struts.formbeans.WeblogEntryFormEx;
import org.apache.commons.lang.StringUtils;

//import com.swabunga.spell.event.SpellCheckEvent;
import java.util.Map;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.roller.model.PluginManager;
import org.apache.roller.model.Roller;
import org.apache.roller.ui.core.tags.calendar.WeblogCalendarModel;

/**
 * All data needed to render the edit-weblog page.
 * @author David M Johnson
 */
public class WeblogEntryPageModel extends BasePageModel
{
    private static Log logger = 
       LogFactory.getFactory().getInstance(WeblogEntryPageModel.class);
        
    private RollerRequest rollerRequest = null;
    private PageMode mode = null;
    private ArrayList words = null;
    private WeblogEntryFormEx form;
    private WeblogEntryData weblogEntry;
        
    public static class PageMode {
        private String name;
        public PageMode(String name) {
            this.name = name;
        }
        public boolean equals(Object obj) {
            return ((PageMode)obj).name.equals(name);
        }
        public int hashCode() {
            return HashCodeBuilder.reflectionHashCode(this);            
        }
    }
    
    public static final PageMode EDIT_MODE = new PageMode("EDIT_MODE");
    public static final PageMode SPELL_MODE = new PageMode("SPELL_MODE");
    public static final PageMode PREVIEW_MODE = new PageMode("PREVIEW_MODE");
    

    public WeblogEntryPageModel(
            HttpServletRequest request,
            HttpServletResponse response,
            ActionMapping mapping,
            WeblogEntryFormEx form,
            PageMode mode,
            ArrayList words) throws RollerException
    {
        this(request, response, mapping, form, mode);
        this.words = words;
    }

    public WeblogEntryPageModel(
            HttpServletRequest request,
            HttpServletResponse response,
            ActionMapping mapping,
            WeblogEntryFormEx form,
            PageMode mode) throws RollerException
    {
        super("dummy", request, response, mapping);
        this.rollerRequest = RollerRequest.getRollerRequest(request);  
        this.form = form;
        this.mode = mode;       
        getRequest().setAttribute("leftPage","/weblog/WeblogEditSidebar.jsp");
    }
    
    public String getTitle() 
    {
        if (StringUtils.isEmpty(form.getId()))
        {
            return bundle.getString("weblogEdit.title.newEntry");
        }
        return bundle.getString("weblogEdit.title.editEntry");
    }

    /**
     * Get recent weblog entries using request parameters to determine
     * username, date, and category name parameters.
     * @return List of WeblogEntryData objects.
     * @throws RollerException
     */
    public List getRecentPublishedEntries() throws RollerException
    {
        RollerSession rollerSession = RollerSession.getRollerSession(getRequest());
        return RollerFactory.getRoller().getWeblogManager()
            .getWeblogEntries(
                getWeblogEntry().getWebsite(), // userName
                null,
                null,              // startDate
                null,              // endDate
                null,              // catName
                WeblogEntryData.PUBLISHED, // status
                null,              // sortby (null for pubTime)
                0, 20, null);   
    }

    /**
     * Get recent weblog entries using request parameters to determine
     * username, date, and category name parameters.
     * @return List of WeblogEntryData objects.
     * @throws RollerException
     */
    public List getRecentDraftEntries() throws RollerException
    {
        RollerSession rollerSession = RollerSession.getRollerSession(getRequest());
        return RollerFactory.getRoller().getWeblogManager()
            .getWeblogEntries(
                getWeblogEntry().getWebsite(), 
                null,
                null,              // startDate
                null,              // endDate
                null,              // catName
                WeblogEntryData.DRAFT, // status
                "updateTime",      // sortby 
                0, 20, null);  // maxEntries
    }
    
    /**
     * Get recent weblog entries using request parameters to determine
     * username, date, and category name parameters.
     * @return List of WeblogEntryData objects.
     * @throws RollerException
     */
    public List getRecentPendingEntries() throws RollerException
    {
        RollerSession rollerSession = RollerSession.getRollerSession(getRequest());
        return RollerFactory.getRoller().getWeblogManager()
            .getWeblogEntries(
                getWeblogEntry().getWebsite(), 
                null,
                null,              // startDate
                null,              // endDate
                null,              // catName
                WeblogEntryData.PENDING, // status
                "updateTime",      // sortby
                0, 20, null);  
    }
 
    public List getHoursList()
    {
        List ret = new LinkedList();
        for (int i=0; i<24; i++)
        {
            ret.add(new Integer(i));
        }
        return ret;
    }

    public List getMinutesList()
    {
        List ret = new LinkedList();
        for (int i=0; i<60; i++)
        {
            ret.add(new Integer(i));
        }
        return ret;
    }

    public List getSecondsList()
    {
        return getMinutesList();
    }

    public boolean getHasPagePlugins()
    {
        boolean ret = false;
        try {
            Roller roller = RollerFactory.getRoller();
            PluginManager ppmgr = roller.getPagePluginManager();
            ret = ppmgr.hasPagePlugins();
        } catch (RollerException e) {
            logger.error(e);
        }
        return ret;
    }
    
    public List getPagePlugins() 
    {
        List list = new ArrayList();
        try {
            if (getHasPagePlugins()) 
            {
                Roller roller = RollerFactory.getRoller();
                PluginManager ppmgr = roller.getPagePluginManager();
                Map plugins = ppmgr.getWeblogEntryPlugins(
                    getWebsite(),
                    new HashMap());
                Iterator it = plugins.values().iterator();
                while (it.hasNext()) list.add(it.next());
            }
        } catch (Exception e) {
            logger.error(e);
        }
        return list;
    }

    public String getEditorPage()
    {
        // Select editor page selected by user (simple text editor,
        // DHTML editor, Ekit Java applet, etc.
        RollerSession rollerSession = RollerSession.getRollerSession(getRequest());
        String editorPage = weblogEntry.getWebsite().getEditorPage();
        if (StringUtils.isEmpty( editorPage ))
        {
            editorPage = "editor-text.jsp";
        }
        return editorPage;
    }

    public UserData getUser()
    {
        RollerSession rollerSession = RollerSession.getRollerSession(getRequest());
        return rollerSession.getAuthenticatedUser();
    }

    public List getCategories() throws Exception
    {
        RollerSession rollerSession = RollerSession.getRollerSession(getRequest());
        return RollerFactory.getRoller().getWeblogManager()
            .getWeblogCategories(weblogEntry.getWebsite(), false);
    }
    
    public WeblogEntryFormEx getWeblogEntryForm() throws RollerException
    {
        return this.form;
    }

    /** returns a dummied-up weblog entry object */
    public WeblogEntryData getWeblogEntry() throws RollerException
    {
        if (weblogEntry == null) 
        {
            weblogEntry = new WeblogEntryData();
            weblogEntry.setWebsite(getWebsite());
            form.copyTo(weblogEntry, 
                    getRequest().getLocale(), getRequest().getParameterMap());
            weblogEntry.setWebsite(weblogEntry.getWebsite());
        }
        return weblogEntry;
    }
    
    public String getPermaLink() throws RollerException
    {
        String context = RollerContext
            .getRollerContext()
            .getAbsoluteContextUrl(rollerRequest.getRequest());
        return context + getWeblogEntry().getPermaLink();
    }
    
    public static String makeSelect(String word, List words)
    {
        StringBuffer buf = new StringBuffer("<select name=\"");
        buf.append("replacementWords\" style=\"font-size: 10px;\">");
        buf.append("<option selected=\"selected\" value=\"").append(word);
        buf.append("\">").append(word).append("</option>");
        if (words == null || words.size() < 1)
        {
            buf.append("<option value=\"").append(word);
            buf.append("\">No Suggestions</option>");
        }
        else
        {
            for (Iterator it2=words.iterator(); it2.hasNext();)
            {
                word = it2.next().toString();
                buf.append("<option value=\"").append(word);
                buf.append("\">").append(word).append("</option>");
            }
        }    
        buf.append("</select>");
        return buf.toString();
    }

    /**
     * @return Returns the mode.
     */
    public PageMode getMode() {
        return mode;
    }
    
    /**
     * @param mode The mode to set.
     */
    public void setMode(PageMode mode) {
        this.mode = mode;
    }
    
    public boolean getEditMode()
    {
        return mode.equals(EDIT_MODE);
    }
    
    public boolean getSpellMode()
    {
        return mode.equals(SPELL_MODE);
    }
    
    public boolean getPreviewMode()
    {
        return mode.equals(PREVIEW_MODE);
    }
    
    /**
     * @return Returns the words.
     */
    public ArrayList getWords() {
        return words;
    }
    /**
     * @param words The words to set.
     */
    public void setWords(ArrayList words) {
        this.words = words;
    }

    public boolean getUserAuthorized() throws RollerException
    {
        return getRollerSession().isUserAuthorized(getWeblogEntry().getWebsite());
    }
    
    public boolean getUserAuthorizedToAuthor() throws RollerException
    {
        return getRollerSession().isUserAuthorizedToAuthor(getWeblogEntry().getWebsite());
    }

    public PageMode getEDIT_MODE() {
        return EDIT_MODE;
    }
    
    public int getCommentCount() {
        // Don't check for comments on unsaved entry (fixed ROL-970)
        if (weblogEntry.getId() == null) return 0;
        List comments = comments = weblogEntry.getComments(false, false);
        return comments != null ? comments.size() : 0;
    }
}

