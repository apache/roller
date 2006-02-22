
package org.roller.presentation.weblog.actions;

import java.io.IOException;
import java.text.DateFormat;
import java.text.ParseException;
import java.util.Date;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.actions.DispatchAction;
import org.roller.RollerException;
import org.roller.model.RollerFactory;
import org.roller.model.WeblogManager;
import org.roller.pojos.WeblogCategoryData;
import org.roller.pojos.WeblogEntryData;
import org.roller.pojos.WebsiteData;
import org.roller.presentation.BasePageModel;
import org.roller.presentation.RollerRequest;
import org.roller.presentation.RollerSession;
import org.roller.presentation.weblog.formbeans.WeblogEntryManagementForm;
import org.roller.util.DateUtil;


/////////////////////////////////////////////////////////////////////////////
/**
 * Query weblog entries and display the results in tabular form.
 *
 * @struts.action path="/editor/weblogEntryManagement" name="weblogEntryManagementForm" 
 *     scope="request" parameter="method"
 * 
 * @struts.action-forward name="weblogEntryManagement.page" path=".WeblogEntryManagement"
 * 
 * @author Dave Johnson
 */
public final class WeblogEntryManagementAction extends DispatchAction
{
    //-----------------------------------------------------------------------
    /**
     * Respond to request to add a new or edit an existing weblog entry.
     * Loads the appropriate model objects and forwards the request to
     * the edit weblog page.
     */
    public ActionForward query(
        ActionMapping       mapping,
        ActionForm          actionForm,
        HttpServletRequest  request,
        HttpServletResponse response)
        throws IOException, ServletException, RollerException
    {
        WeblogEntryManagementForm form = (WeblogEntryManagementForm)actionForm;
        RollerRequest   rreq = RollerRequest.getRollerRequest(request);
        WeblogManager   wmgr = RollerFactory.getRoller().getWeblogManager();           
        RollerSession   rses = RollerSession.getRollerSession(request);
        
        // ensure that weblog is specfied and user has permission to work there
        if (rreq.getWebsite() != null && rses.isUserAuthorized(rreq.getWebsite())) {
            String status= form.getStatus().equals("ALL") ? null : form.getStatus();        
            request.setAttribute("model", new PageModel(
               request, 
               response, 
               mapping,
               rreq.getWebsite(),
               form)); 
        } 
        else 
        {
            return mapping.findForward("access-denied");
        }
        return mapping.findForward("weblogEntryManagement.page");
    }
    
    public class PageModel extends BasePageModel {
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

        public PageModel( 
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

            this.status = "ALL".equals(queryForm.getStatus()) ? null: queryForm.getStatus();    
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

        public int getWeblogEntryCount() {
            return entries.size();
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
            sb.append("/editor/weblogEntryManagement.do"); // TODO: get path from Struts
            sb.append("?method=query");
            sb.append("&weblog=");
            sb.append(getWebsite().getHandle());
            sb.append("&count=");
            sb.append(queryForm.getCount());
            return sb.toString();
        }
    }
}
