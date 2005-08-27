/*
 * Created on Mar 10, 2004
 */
package org.roller.presentation.weblog.actions;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.roller.RollerException;
import org.roller.model.RollerFactory;
import org.roller.model.WeblogManager;
import org.roller.pojos.UserData;
import org.roller.pojos.WeblogEntryData;
import org.roller.presentation.BasePageModel;
import org.roller.presentation.RollerContext;
import org.roller.presentation.RollerRequest;
import org.roller.presentation.RollerSession;
import org.roller.presentation.tags.calendar.CalendarModel;
import org.roller.presentation.velocity.ContextLoader;
import org.roller.presentation.weblog.formbeans.WeblogEntryFormEx;
import org.roller.presentation.weblog.tags.EditWeblogCalendarModel;
import org.roller.util.StringUtils;

import com.swabunga.spell.event.SpellCheckEvent;

/**
 * All data needed to render the edit-weblog page.
 * @author David M Johnson
 */
public class WeblogEntryPageModel extends BasePageModel
{
    private RollerRequest rollerRequest = null;
    private PageMode mode = null;
    private ArrayList words = null;
    private WeblogEntryFormEx form;
    private List comments = null;
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
        
        if (null != form.getId()) 
        {
            WeblogManager wmgr = RollerFactory.getRoller().getWeblogManager();
            comments = wmgr.getComments(form.getId(), false);
        }
    }
    
    public String getTitle() 
    {
        if (StringUtils.isEmpty(form.getId()))
        {
            return bundle.getString("weblogEdit.title.newEntry");
        }
        return bundle.getString("weblogEdit.title.editEntry");
    }

    public String getBaseURL()
    {
		return getRequest().getContextPath();
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
                null,              // startDate
                null,              // endDate
                null,              // catName
                WeblogEntryData.PUBLISHED, // status
                new Integer(20));  // maxEntries
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
                null,              // startDate
                null,              // endDate
                null,              // catName
                WeblogEntryData.DRAFT, // status
                new Integer(20));  // maxEntries
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
                null,              // startDate
                null,              // endDate
                null,              // catName
                WeblogEntryData.PENDING, // status
                new Integer(20));  // maxEntries
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
        return ContextLoader.hasPlugins();
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

    public CalendarModel getCalendarModel() throws Exception
    {
        // Determine URL to self
        ActionForward selfForward = getMapping().findForward("editWeblog");
        String selfUrl= getRequest().getContextPath()+selfForward.getPath();

        // Setup weblog calendar model
        CalendarModel model = new EditWeblogCalendarModel(
                rollerRequest, getResponse(), selfUrl );
        return model;
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

    public List getComments() throws Exception
    {
        return comments;
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
            .getRollerContext(rollerRequest.getRequest())
            .getAbsoluteContextUrl(rollerRequest.getRequest());
        return context + getWeblogEntry().getPermaLink();
    }
    
    public String getSpellCheckHtml() throws RollerException
    {
        String text = getWeblogEntry().getText();
        String escapeText = StringUtils.replace( text, "<", "{" );
        escapeText = StringUtils.replace( escapeText, ">", "}" );
        StringBuffer newText = new StringBuffer(escapeText);
        ArrayList events = (ArrayList)
            getRequest().getSession().getAttribute("spellCheckEvents");
        SpellCheckEvent event = null;
        String word = null;
        int start = -1;
        int end = -1;
        String select = null;
        for(ListIterator it=events.listIterator(events.size()); it.hasPrevious();)
        {
            event = (SpellCheckEvent)it.previous();
            word = event.getInvalidWord();
            start = event.getWordContextPosition();
            end = start + word.length();
            select = makeSelect(word, event.getSuggestions());
    
            newText.replace( start, end, select );
        }
        escapeText = StringUtils.replace( newText.toString(), "}", "&gt;" );
        escapeText = StringUtils.replace( escapeText, "{", "&lt;" );
        return escapeText;
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
    
    
}
