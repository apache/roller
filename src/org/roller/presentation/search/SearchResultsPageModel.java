/*
 * SearchResultsPageModel.java
 *
 * Created on September 23, 2005, 11:27 AM
 */

package org.roller.presentation.search;

import java.io.IOException;
import java.util.Date;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import javax.servlet.http.HttpServletRequest;
import org.apache.commons.collections.comparators.ReverseComparator;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.lucene.document.Document;
import org.apache.lucene.search.Hits;
import org.roller.RollerException;
import org.roller.business.search.FieldConstants;
import org.roller.business.search.operations.SearchOperation;
import org.roller.model.IndexManager;
import org.roller.model.Roller;
import org.roller.model.RollerFactory;
import org.roller.model.UserManager;
import org.roller.model.WeblogManager;
import org.roller.pojos.WeblogEntryData;
import org.roller.pojos.WeblogEntryWrapperComparator;
import org.roller.pojos.WebsiteData;
import org.roller.pojos.wrapper.WeblogEntryDataWrapper;
import org.roller.presentation.RollerRequest;
import org.roller.util.DateUtil;
import org.roller.util.StringUtils;



/**
 * Encapsulate seach result in page model so it can be used from Velocity or JSP.
 * @author Min (original code)
 * @author Dave Johnson (encapsulation)
 */
public class SearchResultsPageModel {
    
    private String   term = "";
    private Integer  hits = new Integer(0);
    private Integer  offset = new Integer(0);
    private Integer  limit = new Integer(0);
    private TreeMap  results = new TreeMap();
    private Set      categories = new TreeSet();
    private boolean  websiteSpecificSearch = false;
    private String   errorMessage = null;
    
    /* How many results to display */
    private static int LIMIT = 10;
    
    /* Where to start fetching results */
    private static int OFFSET = 0;
    
    private static Log mLogger =
        LogFactory.getFactory().getInstance(SearchResultsPageModel.class);
    private static ResourceBundle bundle = 
        ResourceBundle.getBundle("ApplicationResources");          

    public SearchResultsPageModel(HttpServletRequest request) {        
        try {            
            RollerRequest rreq = RollerRequest.getRollerRequest(request);
            setWebsiteSpecificSearch(checkForWebsite(request));
            
            SearchOperation search =
                new SearchOperation(RollerFactory.getRoller().getIndexManager());
            search.setTerm(request.getParameter("q"));
            setTerm(request.getParameter("q"));

            WebsiteData website = null;
            if (isWebsiteSpecificSearch()) {
                website = rreq.getWebsite();
                search.setWebsiteHandle(rreq.getWebsite().getHandle());
            }

            if (StringUtils.isNotEmpty(request.getParameter("c"))) {
                search.setCategory(request.getParameter("c"));
            }

            // execute search
            executeSearch(RollerFactory.getRoller(), search);

            if (search.getResultsCount() == -1) {
                // this means there has been a parsing (or IO) error
                setErrorMessage(bundle.getString("error.searchProblem"));
            } else {
                // Convert the Hits into WeblogEntryData instances.
                Hits hits = search.getResults();
                setResults(convertHitsToEntries(rreq, website, hits));
                setOffset((Integer)request.getAttribute("offset"));
                setLimit((Integer)request.getAttribute("limit"));
                if (request.getAttribute("categories") != null) {
                    Set cats = (Set)request.getAttribute("categories");
                    if (cats.size() > 0) {
                        setCategories(cats);
                    }
                }
            }
            setHits(new Integer(search.getResultsCount()));
            
        } catch (IOException ex) {
            mLogger.error("ERROR: initializing search page model");
        } catch (RollerException ex) {
            mLogger.error("ERROR: initializing search page model");
        }
    }
    
    private void executeSearch(Roller roller, SearchOperation search)
        throws RollerException {
        IndexManager indexMgr = roller.getIndexManager();
        indexMgr.executeIndexOperationNow(search);
        if (mLogger.isDebugEnabled()) {
            mLogger.debug("numresults = " + search.getResultsCount());
        }
    }
    
    /** Look in PathInfo so req.getRemoteUser() doesn't interfere. */
    private boolean checkForWebsite(HttpServletRequest request) {
        if (StringUtils.isNotEmpty(
                request.getParameter(RollerRequest.WEBLOG_KEY))) {
            return true;
        }        
        String pathInfoStr = request.getPathInfo();
        pathInfoStr = (pathInfoStr!=null) ? pathInfoStr : "";
        
        String[] pathInfo = StringUtils.split(pathInfoStr,"/");
        if ( pathInfo.length > 0 ) {
            return true; // is a user page
        }
        return false;
    }
  
    /**
     * Iterate over Hits and build sets of WeblogEntryData
     * objects, placed into Date buckets (in reverse order).
     * @param rreq
     * @param website
     * @param hits
     * @throws RollerException
     * @throws IOException
     */
    private TreeMap convertHitsToEntries(
            RollerRequest rreq, WebsiteData website, Hits hits)
            throws RollerException, IOException {
        // determine offset (starting point)
        int ioffset = useOffset(rreq.getRequest());
        if (ioffset >= hits.length()) ioffset = OFFSET;
        rreq.getRequest().setAttribute("offset", new Integer(ioffset));
        
        // determine limit (number of results to display)
        int ilimit = useLimit(rreq.getRequest());
        rreq.getRequest().setAttribute("limit", new Integer(ilimit));
        if (ioffset + ilimit > hits.length()) ilimit = hits.length()-ioffset;
        
        boolean websiteSpecificSearch = checkForWebsite(rreq.getRequest());
        TreeMap searchResults = new TreeMap(new ReverseComparator());
        TreeSet categories = new TreeSet();
        Roller roller = RollerFactory.getRoller();
        UserManager userMgr = roller.getUserManager();
        WeblogManager weblogMgr =roller.getWeblogManager();
        WeblogEntryData entry;
        Document doc = null;
        String handle = null;
        for (int i = ioffset; i < ioffset+ilimit; i++) {
            entry = null; // reset for each iteration
            
            doc = hits.doc(i);
            handle = doc.getField(FieldConstants.WEBSITE_HANDLE).stringValue();
            
            if (websiteSpecificSearch && website != null) {
                // "wrong user" results have been reported
                if (handle.equals(rreq.getWebsite().getHandle())) {
                    // get real entry for display on user's site
                    entry = weblogMgr.retrieveWeblogEntry(
                            doc.getField(FieldConstants.ID).stringValue() );
                }
            } else {
                // if user is not enabled, website will be null
                //entry = buildSearchEntry(website, doc);
                entry = weblogMgr.retrieveWeblogEntry(
                        doc.getField(FieldConstants.ID).stringValue() );
                if (doc.getField(FieldConstants.CATEGORY) != null) {
                    categories.add(
                            doc.getField(FieldConstants.CATEGORY).stringValue());
                }
            }
            
            // maybe null if search result returned inactive user
            // or entry's user is not the requested user.
            if (entry != null) {
                addToSearchResults(searchResults, WeblogEntryDataWrapper.wrap(entry));
            }
        }
        rreq.getRequest().setAttribute("categories", categories);
        return searchResults;
    }
    
    private void addToSearchResults(
            TreeMap searchResults, WeblogEntryDataWrapper entry) {
        // convert entry's each date to midnight (00m 00h 00s)
        Date midnight = DateUtil.getStartOfDay( entry.getPubTime() );
        
        // ensure we do not get duplicates from Lucene by
        // using a Set Collection.  Entries sorted by pubTime.
        TreeSet set = (TreeSet) searchResults.get(midnight);
        if (set == null) {
            // date is not mapped yet, so we need a new Set
            set = new TreeSet( new WeblogEntryWrapperComparator() );
            searchResults.put(midnight, set);
        }
        set.add(entry);
    }
    
    private int useOffset(HttpServletRequest request) {
        int offset = OFFSET;
        if (request.getParameter("o") != null) {
            try {
                offset = Integer.valueOf(request.getParameter("o")).intValue();
            } catch (NumberFormatException e) {
                // Not a valid Integer
            }
        }
        return offset;
    }
    
    private int useLimit(HttpServletRequest request) {
        int limit = LIMIT;
        if (request.getParameter("n") != null) {
            try {
                limit = Integer.valueOf(request.getParameter("n")).intValue();
            } catch (NumberFormatException e) {
                // Not a valid Integer
            }
        }
        return limit;
    }

    public String getTerm() {
        return term;
    }

    public void setTerm(String term) {
        this.term = term;
    }

    public Integer getHits() {
        return hits;
    }

    public void setHits(Integer hits) {
        this.hits = hits;
    }

    public Integer getOffset() {
        return offset;
    }

    public void setOffset(Integer offset) {
        this.offset = offset;
    }

    public Integer getLimit() {
        return limit;
    }

    public void setLimit(Integer limit) {
        this.limit = limit;
    }

    public TreeMap getResults() {
        return results;
    }

    public void setResults(TreeMap results) {
        this.results = results;
    }

    public Set getCategories() {
        return categories;
    }

    public void setCategories(Set categories) {
        this.categories = categories;
    }

    public boolean isWebsiteSpecificSearch() {
        return websiteSpecificSearch;
    }

    public void setWebsiteSpecificSearch(boolean websiteSpecificSearch) {
        this.websiteSpecificSearch = websiteSpecificSearch;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }
}