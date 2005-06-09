package org.roller.presentation.velocity;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Date;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.jsp.JspFactory;
import javax.servlet.jsp.PageContext;

import org.apache.commons.collections.comparators.ReverseComparator;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.lucene.document.Document;
import org.apache.lucene.search.Hits;
import org.apache.velocity.Template;
import org.apache.velocity.context.Context;
import org.roller.RollerException;
import org.roller.business.search.FieldConstants;
import org.roller.business.search.operations.SearchOperation;
import org.roller.model.IndexManager;
import org.roller.model.Roller;
import org.roller.model.UserManager;
import org.roller.model.WeblogManager;
import org.roller.pojos.WeblogCategoryData;
import org.roller.pojos.WeblogEntryComparator;
import org.roller.pojos.WeblogEntryData;
import org.roller.pojos.WebsiteData;
import org.roller.presentation.RollerContext;
import org.roller.presentation.RollerRequest;
import org.roller.util.DateUtil;
import org.roller.util.StringUtils;


/**
 * This servlet retrieves (and displays) search results.
 *
 * @web.servlet name="SearchServlet" load-on-startup="5"
 * @web.servlet-init-param name="properties" value="/WEB-INF/velocity.properties"
 * @web.servlet-mapping url-pattern="/search/*"
 */
public class SearchServlet extends BasePageServlet
{
    //~ Static fields/initializers =============================================
    static final long serialVersionUID = -2150090108300585670L;

    private static Log mLogger =
        LogFactory.getFactory().getInstance(SearchServlet.class);
    
    /* How many results to display */
    private static int LIMIT = 10;
    
    /* Where to start fetching results */
    private static int OFFSET = 0;

    //~ Methods ================================================================

    public Template handleRequest(HttpServletRequest request,
                        HttpServletResponse response, Context ctx) throws Exception
    {
         // set request Charcter Encoding here, because the SearchServlet
         // is not preceeded by the RequestFilter
         mLogger.debug("handleRequest()");
		try
		{
			// insure that incoming data is parsed as UTF-8
			request.setCharacterEncoding("UTF-8");
		}
		catch (UnsupportedEncodingException e)
		{
			throw new ServletException("Can't set incoming encoding to UTF-8");
		}
    	        
        ctx.put("term", "");
        ctx.put("hits", new Integer(0));
        ctx.put("searchResults", new TreeMap());          
        // to enable better unit testing
        request.setAttribute("zzz_VelocityContext_zzz", ctx);
        
        mLogger.debug("q = "+request.getParameter("q"));
        
        // do no work if query term is empty
        if (StringUtils.isEmpty(request.getParameter("q")))
        {  
            return generalSearchResults(request, response, ctx);        
        }

        boolean userSpecificSearch = checkForUser(request);
        try
        {
            RollerRequest rreq = getRollerRequest(request, response);
            
            SearchOperation search = 
                new SearchOperation(rreq.getRoller().getIndexManager());
            search.setTerm(request.getParameter("q"));
            ctx.put("term", request.getParameter("q"));

            WebsiteData website = null;
            if (userSpecificSearch)
            {    
                website = rreq.getWebsite();
                search.setUsername(rreq.getUser().getUserName());
                ctx.put("username", rreq.getUser().getUserName());
            }
            
            if (StringUtils.isNotEmpty(request.getParameter("c")))
            {
                search.setCategory(request.getParameter("c"));
            }

            // execute search
            executeSearch(rreq.getRoller(), search);

            Map searchResults = new TreeMap();
            if (search.getResultsCount() == -1)
            {
                // this means there has been a parsing (or IO) error
                //ctx.put("errorMessage", search.getParseError());
                ctx.put("errorMessage", "There was a problem with your search.");
            }
            else
            {    
                // Convert the Hits into WeblogEntryData instances.
                Hits hits = search.getResults();
                searchResults = convertHitsToEntries(rreq, website, hits);
                ctx.put("offset", request.getAttribute("offset"));
                ctx.put("limit", request.getAttribute("limit"));
                
                if (request.getAttribute("categories") != null)
                {
                    Set cats = (Set)request.getAttribute("categories");
                    if (cats.size() > 0)
                    {
                        ctx.put("categories", cats);
                    }
                }
            }
            ctx.put("searchResults", searchResults);
            ctx.put("hits", new Integer(search.getResultsCount()));      
        }
        catch (Exception e)
        {
            mLogger.error("EXCEPTION: in SearchServlet", e);
            request.setAttribute("DisplayException", e);
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }        

        if (userSpecificSearch)   
        {    
            return super.handleRequest(request, response, ctx);
        }

        return generalSearchResults(request, response, ctx);
    }

    /**
     * execute search
     * @param search
     */
    private void executeSearch(Roller roller, SearchOperation search) 
    throws RollerException
    {
        IndexManager indexMgr = roller.getIndexManager();
        indexMgr.executeIndexOperationNow(search);
        if (mLogger.isDebugEnabled())
        {
            mLogger.debug("numresults = " + search.getResultsCount());
        }
    }

    /**
     * Iterate over Hits and build sets of WeblogEntryData
     * objects, placed into Date buckets (in reverse order).
     * 
     * @param rreq
     * @param website
     * @param hits
     * @throws RollerException
     * @throws IOException
     */
    private TreeMap convertHitsToEntries(
        RollerRequest rreq, WebsiteData website, Hits hits) 
        throws RollerException, IOException
    {
        // determine offset (starting point)
        int offset = useOffset(rreq.getRequest());
        if (offset >= hits.length()) offset = OFFSET;
        rreq.getRequest().setAttribute("offset", new Integer(offset));
        
        // determine limit (number of results to display)
        int limit = useLimit(rreq.getRequest());
        rreq.getRequest().setAttribute("limit", new Integer(limit));
        if (offset + limit > hits.length()) limit = hits.length()-offset;
        
        boolean userSpecificSearch = checkForUser(rreq.getRequest());
        TreeMap searchResults = new TreeMap(new ReverseComparator());
        TreeSet categories = new TreeSet();
        UserManager userMgr = 
            RollerContext.getRoller(rreq.getRequest()).getUserManager();
        WeblogManager weblogMgr =
            RollerContext.getRoller(rreq.getRequest()).getWeblogManager();
        WeblogEntryData entry;
        Document doc = null;
        String username = null;
        for (int i = offset; i < offset+limit; i++)
        {
            entry = null; // reset for each iteration
            
            doc = hits.doc(i);
            username =
                doc.getField(FieldConstants.USERNAME).stringValue();
            
            if (userSpecificSearch && website != null) 
            {
                // "wrong user" results have been reported
                if (username.equals(rreq.getUser().getUserName()))
                {    
                    //entry = buildSearchEntry(website, doc);
                    
                    // get real entry for display on user's site
                    entry = weblogMgr.retrieveWeblogEntry(
                        doc.getField(FieldConstants.ID).stringValue() );
                }
            }
            else
            {
                website = userMgr.getWebsite(username);
                // if user is not enabled, website will be null
                if (website != null)
                {
                    entry = buildSearchEntry(website, doc);
                    if (doc.getField(FieldConstants.CATEGORY) != null)
                    {
                        categories.add(
                            doc.getField(FieldConstants.CATEGORY)
                                .stringValue());
                    }
                }
            }
            
            // maybe null if search result returned inactive user
            // or entry's user is not the requested user.
            if (entry != null)
            {    
                addToSearchResults(searchResults, entry);
            }
        }
        rreq.getRequest().setAttribute("categories", categories);
        return searchResults;
    }

    /**
     * @param request
     * @return
     */
    private int useOffset(HttpServletRequest request)
    {
        int offset = OFFSET;
        if (request.getParameter("o") != null)
        {
            try
            {
                offset = Integer.valueOf(request.getParameter("o")).intValue();
            }
            catch (NumberFormatException e)
            {
                // Not a valid Integer
            }
        }
        return offset;
    }

    /**
     * @param request
     * @return
     */
    private int useLimit(HttpServletRequest request)
    {
        int limit = LIMIT;
        if (request.getParameter("n") != null)
        {
            try
            {
                limit = Integer.valueOf(request.getParameter("n")).intValue();
            }
            catch (NumberFormatException e)
            {
                // Not a valid Integer
            }
        }
        return limit;
    }

    /**
     * @param request
     * @param response
     * @return
     */
    private RollerRequest getRollerRequest(HttpServletRequest request, HttpServletResponse response)
    {
        PageContext pageContext =
            JspFactory.getDefaultFactory()
                .getPageContext(this, request, response, "", true, 8192, true);
        
        // Needed to init request attributes, etc.
        return RollerRequest.getRollerRequest(pageContext);
    }

    /**
     * @param searchResults
     * @param entry
     */
    private void addToSearchResults(TreeMap searchResults, WeblogEntryData entry)
    {
        // convert entry's each date to midnight (00m 00h 00s)
        Date midnight = DateUtil.getStartOfDay( entry.getPubTime() );
        
        // ensure we do not get duplicates from Lucene by
        // using a Set Collection.  Entries sorted by pubTime.
        TreeSet set = (TreeSet) searchResults.get(midnight);
        if (set == null)
        {
            // date is not mapped yet, so we need a new Set
            set = new TreeSet( new WeblogEntryComparator() );
            searchResults.put(midnight, set);
        }
        set.add(entry);
    }

    /**
     * @param website
     * @param doc
     * @return
     */
    private WeblogEntryData buildSearchEntry(WebsiteData website, Document doc)
    {
        String pubTimeStr = doc.getField(FieldConstants.PUBLISHED).stringValue();
        WeblogEntryData entry = new WeblogEntryData();
        entry.setWebsite(website);
        entry.setTitle(doc.getField(FieldConstants.TITLE).stringValue());
        entry.setAnchor(doc.getField(FieldConstants.ANCHOR).stringValue());
        entry.setPubTime( DateUtil.parseTimestampFromFormats(pubTimeStr) );
        entry.setText( doc.getField(FieldConstants.CONTENT_STORED).stringValue() );
        if (doc.getField(FieldConstants.CATEGORY) != null)
        {
            WeblogCategoryData category = new WeblogCategoryData();
            category.setWebsite(website);
            category.setName(doc.getField(FieldConstants.CATEGORY).stringValue());
            entry.setCategory( category );
        }
        return entry;
    }

    /**
     * If this is not a user-specific search, we need to display the 
     * "generic" search results list.
     * 
     * @param request
     * @param response
     * @param ctx
     * @return
     */
    private Template generalSearchResults(HttpServletRequest request, HttpServletResponse response, Context ctx)
    {
        Template outty = null;
        Exception pageException = null;
        try
        {
            ContextLoader.setupContext( 
                ctx, RollerRequest.getRollerRequest(request), response );
            outty = getTemplate( "searchresults.vm", "UTF-8" );
        }
        catch (Exception e)
        {
            pageException = e;
            response.setStatus( HttpServletResponse.SC_INTERNAL_SERVER_ERROR );
        }

        if (pageException != null)
        {
            mLogger.error("EXCEPTION: in RollerServlet", pageException);
            request.setAttribute("DisplayException", pageException);
        }
        return outty;
    }

    /**
     * Look in PathInfo so req.getRemoteUser() doesn't interfere.
     * 
     * @param request
     * @return
     */
    private boolean checkForUser(HttpServletRequest request)
    {
        if (StringUtils.isNotEmpty(
                request.getParameter(RollerRequest.USERNAME_KEY))) 
        {
            return true;
        }
        
        String pathInfoStr = request.getPathInfo();
        pathInfoStr = (pathInfoStr!=null) ? pathInfoStr : "";            
        
        String[] pathInfo = StringUtils.split(pathInfoStr,"/");            
        if ( pathInfo.length > 0 )
        {
            return true; // is a user page
        }
        return false;
    }

}
