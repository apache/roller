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
import org.roller.presentation.weblog.formbeans.WeblogEntryManagementForm;
import org.roller.util.DateUtil;

/**
 * All data needed to render the edit-weblog page.
 * @author David M Johnson
 */
public class WeblogQueryPageModel extends BasePageModel {
    private List               entries = null;
    private RollerRequest      rollerRequest = null;
    private HttpServletRequest request = null;
    private WebsiteData        website = null;
    private String             category = null;
    private Date               startDate = null;
    private Date               endDate = null;
    private String             status = WeblogEntryData.PUBLISHED;
    private Integer            maxEntries = null;
    private boolean            more = false;
    private WeblogEntryManagementForm    queryForm = null;
    
    public WeblogQueryPageModel(
            HttpServletRequest request,
            HttpServletResponse response,
            ActionMapping mapping,
            WebsiteData website,
            WeblogEntryManagementForm queryForm) throws RollerException {
        super("weblogEntryQuery.title", request, response, mapping);
        rollerRequest = RollerRequest.getRollerRequest(request);
        this.request = request;
        this.queryForm = queryForm;
        this.website = website;
        
        if (null != queryForm.getCategoryId() && !queryForm.getCategoryId().equals("")) {
            WeblogManager wmgr = RollerFactory.getRoller().getWeblogManager();
            WeblogCategoryData cd = wmgr.retrieveWeblogCategory(queryForm.getCategoryId());
            category = cd.getPath();
        }
        
        final DateFormat df =
                DateFormat.getDateInstance(DateFormat.SHORT, request.getLocale());
        String start = queryForm.getStartDateString();
        if (null != start && start.trim().length() > 0) {
            try {
                startDate = DateUtil.getStartOfDay(df.parse(start));
            } catch (ParseException e) {
                throw new RollerException("ERROR parsing start date.");
            }
        }
        
        String end = queryForm.getEndDateString();
        if (null != end && end.trim().length() > 0) {
            try {
                endDate = DateUtil.getEndOfDay(df.parse(end));
            } catch (ParseException e) {
                throw new RollerException("ERROR parsing end date.");
            }
        }
        
        this.status = status;
        this.maxEntries = maxEntries;
        
        entries = RollerFactory.getRoller().getWeblogManager().getWeblogEntries(
                website,
                startDate,
                endDate,
                category,
                status,
                queryForm.getSortby(),
                queryForm.getOffset(),
                queryForm.getCount() + 1);
       if (entries.size() > queryForm.getCount()) {
           more = true;
           entries.remove(entries.size()-1);
       }
    }
    
    public String getBaseURL() {
        return getRequest().getContextPath();
    }
    
    /**
     * Get recent weblog entries using request parameters to determine
     * username, date, and category name parameters.
     * @return List of WeblogEntryData objects.
     * @throws RollerException
     */
    public List getRecentWeblogEntries() throws RollerException {
        return entries;
    }
    
    public List getCategories() throws Exception {
        RollerRequest rreq = RollerRequest.getRollerRequest(request);
        List categories = RollerFactory.getRoller().getWeblogManager()
        .getWeblogCategories(rreq.getWebsite());
        return categories;
    }
    
    public Date getEarliestDate() {
        Date date = null;
        if (entries.size() > 0) {
            WeblogEntryData earliest = (WeblogEntryData)entries.get(entries.size()-1);
            date = earliest.getPubTime();
        }
        return date;
    }
    
    public Date getLatestDate() {
        Date date = null;
        if (entries.size() > 0) {
            WeblogEntryData latest = (WeblogEntryData)entries.get(0);
            date = latest.getPubTime();
        }
        return date;
    }
    
    public String getNextLink() {
        if (more) {
            int offset = queryForm.getOffset() + queryForm.getCount();
            offset = (offset < 0) ? 0 : offset;
            return getQueryLink() + "&offset=" + offset;
        } else {
            return null;
        }
    }
    
    public String getPrevLink() {
        if (queryForm.getOffset() > 0) {
            int offset = queryForm.getOffset() - queryForm.getCount();
            offset = (offset < 0) ? 0 : offset;
            return getQueryLink() + "&offset=" + offset;
        } else {
            return null;
        }
    }
    
    private String getQueryLink() {
        StringBuffer sb = new StringBuffer();
        sb.append(request.getContextPath());
        if (getWebsite() != null) {
            sb.append("/editor/weblogQuery.do"); // TODO: get path from Struts
            sb.append("?method=query");
            sb.append("&weblog=");
            sb.append(getWebsite().getHandle());
        } else {
            sb.append("/admin/weblogQuery.do"); // TODO: get path from Struts
            sb.append("?method=query");
        }
        sb.append("&count=");
        sb.append(queryForm.getCount());
        return sb.toString();
    }
}
