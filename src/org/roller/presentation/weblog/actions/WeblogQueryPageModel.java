/*
 * Created on Mar 10, 2004
 */
package org.roller.presentation.weblog.actions;

import java.text.DateFormat;
import java.text.ParseException;
import java.util.Date;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.struts.action.ActionMapping;
import org.roller.RollerException;
import org.roller.model.RollerFactory;
import org.roller.model.WeblogManager;
import org.roller.pojos.WeblogCategoryData;
import org.roller.pojos.WeblogEntryData;
import org.roller.pojos.WebsiteData;
import org.roller.presentation.BasePageModel;
import org.roller.presentation.RollerRequest;
import org.roller.presentation.RollerSession;
import org.roller.util.DateUtil;

/**
 * All data needed to render the edit-weblog page.
 * @author David M Johnson
 */
public class WeblogQueryPageModel extends BasePageModel
{
    private RollerRequest rollerRequest = null;
    private HttpServletRequest request = null;
    private WebsiteData website = null;
    private String category = null;
    private Date startDate = null;
    private Date endDate = null;
    private String status = WeblogEntryData.PUBLISHED;
    private Integer maxEntries = null;

    public WeblogQueryPageModel(
            HttpServletRequest request,
            HttpServletResponse response,
            ActionMapping mapping,
            WebsiteData website, 
            String categoryId, 
            String start, 
            String end,
            String status,
            Integer maxEntries) throws RollerException
    {
        super(request, response, mapping);
        rollerRequest = RollerRequest.getRollerRequest(request);
        this.request = request;
        
        this.website = website;
        
        if (null != categoryId && !categoryId.equals("")) 
        {
            WeblogManager wmgr = RollerFactory.getRoller().getWeblogManager();
            WeblogCategoryData cd = wmgr.retrieveWeblogCategory(categoryId);
            category = cd.getPath();          
        }
        
        final DateFormat df = 
            DateFormat.getDateInstance(DateFormat.SHORT, request.getLocale());
        if (null != start && start.trim().length() > 0) 
        {
            try
            {
                startDate = DateUtil.getStartOfDay(df.parse(start));
            }
            catch (ParseException e)
            {
                throw new RollerException("ERROR parsing start date.");
            }
        }

        if (null != end && end.trim().length() > 0) 
        {
            try
            {
                endDate = DateUtil.getEndOfDay(df.parse(end));
            }
            catch (ParseException e)
            {
                throw new RollerException("ERROR parsing end date.");
            }
        }
        
        this.status = status;
        this.maxEntries = maxEntries;
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
    public List getRecentWeblogEntries() throws RollerException
    {
        return RollerFactory.getRoller().getWeblogManager().getWeblogEntries(
                    website,
                    startDate,
                    endDate,
                    category,
                    status,
                    maxEntries);   
    }

    public List getCategories() throws Exception
    {
        RollerSession rollerSession = RollerSession.getRollerSession(request);
        List categories = RollerFactory.getRoller().getWeblogManager()
            .getWeblogCategories(rollerSession.getCurrentWebsite());
        return categories;
    }

//    public List getHoursList()
//    {
//        List ret = new LinkedList();
//        for (int i=0; i<24; i++)
//        {
//            ret.add(new Integer(i));
//        }
//        return ret;
//    }
//
//    public List getMinutesList()
//    {
//        List ret = new LinkedList();
//        for (int i=0; i<60; i++)
//        {
//            ret.add(new Integer(i));
//        }
//        return ret;
//    }
//
//    public List getSecondsList()
//    {
//        return getMinutesList();
//    }
//
//    public boolean getHasPagePlugins()
//    {
//        return ContextLoader.hasPlugins();
//    }
//
//    public String getEditorPage()
//    {
//        // Select editor page selected by user (simple text editor,
//        // DHTML editor, Ekit Java applet, etc.
//        String editorPage = rollerRequest.getWebsite().getEditorPage();
//        if (StringUtils.isEmpty( editorPage ))
//        {
//            editorPage = "editor-text.jsp";
//        }
//        return editorPage;
//    }
//
//    public CalendarModel getCalendarModel() throws Exception
//    {
//        // Determine URL to self
//        ActionForward selfForward = getMapping().findForward("editWeblog");
//        String selfUrl= getRequest().getContextPath()+selfForward.getPath();
//
//        // Setup weblog calendar model
//        CalendarModel model = new EditWeblogCalendarModel(
//                rollerRequest.getRequest(), getResponse(), selfUrl );
//        model.setDay( rollerRequest.getDate(true) );
//        return model;
//    }
//
//    public UserData getUser()
//    {
//        return rollerRequest.getUser();
//    }
//
//    public int getCommentCount(WeblogEntryData entry) throws Exception
//    {
//		// include Spam in comments count
//		List comments = rollerRequest.getRoller().getWeblogManager().getComments(entry.getId(), false);
//		if (comments == null) return 0;
//		return comments.size();
//	}
}
