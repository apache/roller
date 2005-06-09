/*
 * Created on Mar 25, 2004
 */
package org.roller.presentation.weblog.actions;

import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.ActionMessage;
import org.apache.struts.action.ActionMessages;
import org.apache.struts.actions.DispatchAction;
import org.apache.struts.util.RequestUtils;
import org.roller.RollerException;
import org.roller.model.WeblogManager;
import org.roller.pojos.WeblogEntryData;
import org.roller.presentation.BasePageModel;
import org.roller.presentation.RollerRequest;
import org.roller.presentation.velocity.ExportRss;
import org.roller.presentation.weblog.formbeans.WeblogQueryForm;
import org.roller.util.DateUtil;

import java.io.IOException;
import java.net.MalformedURLException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.jsp.JspFactory;
import javax.servlet.jsp.PageContext;

/**
 * @struts.action path="/editor/exportEntries" name="weblogQueryForm" 
 *    scope="request" parameter="method"
 * 
 * @struts.action-forward name="exportEntries.page" path="/weblog/export-entries.jsp"
 * 
 * @author lance.lavandowska
 */
public class ExportEntriesAction extends DispatchAction
{
    private SimpleDateFormat dayFormat = new SimpleDateFormat("yyyyMMdd");
    private SimpleDateFormat monthFormat = new SimpleDateFormat("yyyyMM");
    private SimpleDateFormat yearFormat = new SimpleDateFormat("yyyy");

    public ActionForward unspecified(
                              ActionMapping       mapping,
                              ActionForm          actionForm,
                              HttpServletRequest  request,
                              HttpServletResponse response)
    throws IOException, ServletException
    {
        return edit(mapping, actionForm, request, response);
    }
    
    /**
     * Prepare the form for selecting date-range to export.
     * 
     * @param mapping
     * @param actionForm
     * @param request
     * @param response
     * @return
     * @throws IOException
     * @throws ServletException
     */
    public ActionForward edit(
                              ActionMapping       mapping,
                              ActionForm          actionForm,
                              HttpServletRequest  request,
                              HttpServletResponse response)
    throws IOException, ServletException
    {
        ActionForward forward = mapping.findForward("exportEntries.page");
        try
        {
            RollerRequest rreq = RollerRequest.getRollerRequest(request);
            if ( !rreq.isUserAuthorizedToEdit() )
            {
                forward = mapping.findForward("access-denied");
            }
            else
            {
                request.setAttribute("model",
                    new BasePageModel(request, response, mapping));
            }
        }
        catch (Exception e)
        {
            request.getSession().getServletContext().log("ERROR",e);
            throw new ServletException(e);
        }
        return forward;
    }

    /**
     * Export entries from the requested date-range to XML.
     * 
     * @param mapping
     * @param actionForm
     * @param request
     * @param response
     * @return
     * @throws IOException
     * @throws ServletException
     */
    public ActionForward export(
                              ActionMapping       mapping,
                              ActionForm          actionForm,
                              HttpServletRequest  request,
                              HttpServletResponse response)
    throws IOException, ServletException
    {
        ActionForward forward = mapping.findForward("exportEntries.page");
        try
        {
            RollerRequest rreq = RollerRequest.getRollerRequest(request);
            WeblogQueryForm form = (WeblogQueryForm)actionForm;
            if ( rreq.isUserAuthorizedToEdit() )
            {               
                request.setAttribute("model",
                                     new BasePageModel(request, response, mapping));
                
                Locale locale = Locale.getDefault();//rreq.getWebsite().getLocaleInstance();
                final DateFormat df = DateFormat.getDateInstance(DateFormat.SHORT, locale);
                Date startDate;
                Date endDate;
                try
                {
                    startDate = DateUtil.getStartOfDay(df.parse(form.getStartDateString()));
                    endDate = DateUtil.getEndOfDay(df.parse(form.getEndDateString()));
                }
                catch (ParseException e)
                {
                    throw new RollerException("ERROR parsing date:" + e.getMessage());
                }
                
                if (startDate != null && endDate != null) 
                {
                    // this work should go into a Thread!
                    WeblogManager weblogMgr =
                        rreq.getRoller().getWeblogManager();
                    
                    //List entries = weblogMgr.getWeblogEntriesInDateRange(
                        //rreq.getUser().getUserName(), null, startDate, endDate, false);
                    //System.out.println("Export: got " + entries.size() + " entries.");
                    
                    List entries = weblogMgr.getWeblogEntries(
                                    rreq.getWebsite(), // userName
                                    startDate,         // startDate
                                    endDate,           // endDate
                                    null,              // catName
                                    WeblogManager.ALL,  // status
                                    null);              // maxEntries

                    ActionMessages messages = writeSuccessMessage(request, response, rreq, form);

                    // seperate the entries as specified: day, month, year
                    Map entryMap = seperateByPeriod(entries, form.getFileBy());

                    // now export each List in the entryMap
                    ExportRss exporter = new ExportRss(rreq.getWebsite());
                    String exportTo = form.getExportFormat().toLowerCase();
                    if ("atom".equals(exportTo))
                    {
                        exporter.setExportAtom(true);
                    }
                    ArrayList fileNames = new ArrayList();
                    Iterator it = entryMap.keySet().iterator();
                    while(it.hasNext())
                    {
                        String key = (String)it.next();
                        ArrayList list = (ArrayList)entryMap.get(key);
                        exporter.exportEntries(list, key+"_"+exportTo+".xml");
                        fileNames.add("Exported " + list.size() + " entry(s) to " + key+"_"+exportTo+".xml<br />");
                        //System.out.println("Exported: " + list.size() + " entries for " + key);
                    }
                    
                    StringBuffer fileMessage = new StringBuffer();
                    it = fileNames.iterator();
                    while (it.hasNext())
                    {
                        fileMessage.append((String)it.next());
                    }
                    if (fileMessage.length() > 0) 
                    {
                        messages.add(ActionMessages.GLOBAL_MESSAGE, 
                                     new ActionMessage("weblogEntryExport.exportFiles", 
                                                       fileMessage.toString()));
                    }
                    saveMessages(request, messages);
                }
                else
                {
                    form.reset(mapping, (ServletRequest)request);
                    return edit(mapping, actionForm, request, response);
                }

                //forward = mapping.findForward("exportEntries.done");
            }
            else
            {    
                forward = mapping.findForward("access-denied");
            }
        }
        catch (Exception e)
        {
            request.getSession().getServletContext().log("ERROR",e);
            throw new ServletException(e);
        }
        return forward;
    }

    /**
     * Place entries into Lists, placed into date-related
     * buckets.  The individual buckets may represent a
     * day, month, or year, depending on which the user specified.
     * 
     * @param entries
     * @return
     */
    private Map seperateByPeriod(List entries, String period)
    {
        HashMap map = new HashMap();
        WeblogEntryData entry = null;
        String key = null;
        ArrayList list = null;
        SimpleDateFormat formatter = monthFormat;
        if ("year".equals(period))
        {
            formatter = yearFormat;
        }
        else if ("day".equals(period))
        {
            formatter = dayFormat;
        }
        
        Iterator it = entries.iterator();
        while (it.hasNext()) 
        {
            entry = (WeblogEntryData)it.next();
            key = formatter.format(entry.getPubTime());
            list = (ArrayList)map.get(key);
            if (list == null)
            {
                list = new ArrayList();
                map.put(key, list);
            }
            list.add(entry);
        }
        return map;
    }

    private ActionMessages writeSuccessMessage(
                    HttpServletRequest request, 
                    HttpServletResponse response, 
                    RollerRequest rreq, 
                    WeblogQueryForm form) throws MalformedURLException
    {
        PageContext pageContext =
            JspFactory.getDefaultFactory().getPageContext( 
                this.getServlet(), request, response, "", true, 8192, true);
        Map params = new HashMap();
        params.put( RollerRequest.USERNAME_KEY,  rreq.getUser().getUserName());
        params.put("rmik", "Files");
        String filesLink = RequestUtils.computeURL(
             pageContext, (String)null, (String)null, (String)null,
             "uploadFiles", params, (String)null, false);
        
        String message = 
            "Exporting Entries from " + 
            form.getStartDateString() + " to " +
            form.getEndDateString() + ".<br />" +
            "Check your <a href=\"" + filesLink + "\">Files</a>.";

        ActionMessages messages = new ActionMessages();
        messages.add(ActionMessages.GLOBAL_MESSAGE, 
                     new ActionMessage("weblogEntryExport.exportSuccess", message));
                                                                      
        return messages;
    }
}
