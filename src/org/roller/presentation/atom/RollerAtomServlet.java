/*
 * Created on Aug 21, 2003
 */
package org.roller.presentation.atom;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.lucene.document.Document;
import org.apache.lucene.search.Hits;
import org.osjava.atom4j.pojo.Entry;
import org.osjava.atom4j.pojo.Feed;
import org.osjava.atom4j.pojo.Template;
import org.osjava.atom4j.servlet.AtomServlet;
import org.roller.RollerException;
import org.roller.model.Roller;
import org.roller.model.UserManager;
import org.roller.model.WeblogManager;
import org.roller.pojos.CommentData;
import org.roller.pojos.PageData;
import org.roller.pojos.UserData;
import org.roller.pojos.WeblogEntryData;
import org.roller.pojos.WebsiteData;
import org.roller.presentation.RollerContext;
import org.roller.presentation.RollerRequest;
import org.roller.presentation.pagecache.PageCache;
import org.roller.presentation.weblog.search.FieldConstants;
import org.roller.presentation.weblog.search.IndexManager;
import org.roller.presentation.weblog.search.operations.SearchOperation;

import java.io.IOException;
import java.net.URLDecoder;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Roller's Atom API support is provided by this Servlet and the Atom4J library.
 *
 * @author Lance Lavandowska
 *
 * @web.servlet name="AtomServlet"
 * @web.servlet-mapping url-pattern="/atom/*"
 * 
 * web.security-role-ref role-name="Atom" role-link="atomuser"
 * web.security-role role-name="atomuser" description="Authenticated AtomServlet User"
 */
public class RollerAtomServlet extends AtomServlet
{
    private static Log mLogger =
        LogFactory.getFactory().getInstance(RollerAtomServlet.class);

    protected static String mBaseUrl = null;

    //------------------------------------------------------------------------
    /**
     * Tries to set the BaseUrl if RollerContext.getAbsolutionContextURL
     * is not null.  Using this method rather than the no-arg to assist
     * in UnitTesting (troubles with MockRunner).
     */
    public void init(ServletConfig config) throws ServletException
    {
        RollerContext rContext = RollerContext.getRollerContext(config.getServletContext());
        if (rContext == null)
        {
            mLogger.error("RollerContext not initialized yet!");
            //mBaseUrl = "/" + getServletContext().getServletContextName();
        }
        else
        {
            setBaseUrl( rContext.getAbsoluteContextUrl() );
        }
        super.init(config);
    }

    private void setBaseUrl(String base)
    {
        if (base != null)
        {
            super.baseURL = mBaseUrl = base;
            AtomAssistant.mBaseUrl = base;
        }
    }

    //------------------------------------------------------------------------
    /*
     * Just in case we weren't able to set the Base URL at init.
     */
    private void startRollerRequest(HttpServletRequest req, HttpServletResponse resp) throws IOException
    {
        RollerRequest rreq = RollerRequest.getRollerRequest();
        if (rreq == null)
        {
            try
            {
                rreq = RollerRequest.getRollerRequest(req, getServletContext());
            }
            catch (RollerException e)
            {
                // An error initializing the request is considered to be a 404
                //mLogger.debug("RollerRequest threw Exception", e);
                resp.sendError(HttpServletResponse.SC_NOT_FOUND);
                return;
            }

        }

        // mBaseUrl was not available at init()
        if (mBaseUrl == null)
        {
            String base = RollerContext.getRollerContext(
                getServletContext() ).getAbsoluteContextUrl(req);
            mLogger.error("mBaseUrl is null, set to " + base);
            setBaseUrl( base );
        }
    }

    private void closeRoller(HttpServletRequest req)
    {
        Roller roller = RollerContext.getRoller( req );
        try
        {
            roller.commit();
        }
        catch (RollerException e)
        {
            mLogger.error("Unable to commit changes");
        }

        roller.release();
    }

    //------------------------------------------------------------------------
    // aren't doGet/Post/Put/Delete all supposed to go through service() first?
    protected void service(HttpServletRequest req, HttpServletResponse resp)
        throws ServletException, IOException
    {
        //mLogger.debug("Entering RollerAtomServlet service() method");
        startRollerRequest(req, resp);
        super.service(req, resp);
        closeRoller(req);
        //mLogger.debug("Exiting RollerAtomServlet service() method");
    }

    //------------------------------------------------------------------------
    // The following four methods are primarily for debugging, they could
    // be commented out for deployment
    /*
    public void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException
    {
        mLogger.debug("Entering RollerAtomServlet doGet() method");
        super.doGet(req, resp);
        mLogger.debug("Exiting RollerAtomServlet doGet() method");
    }

    public void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException
    {
        mLogger.debug("Entering RollerAtomServlet doPost() method");
        super.doPost(req, resp);
        mLogger.debug("Exiting RollerAtomServlet doPost() method");
    }

    public void doDelete(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException
    {
        mLogger.debug("Entering RollerAtomServlet doDelete() method");
        super.doDelete(req, resp);
        mLogger.debug("Exiting RollerAtomServlet doDelete() method");
    }

    public void doPut(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException
    {
        mLogger.debug("Entering RollerAtomServlet doPut() method");
        super.doPut(req, resp);
        mLogger.debug("Exiting RollerAtomServlet doPut() method");
    }
    */
    //------------------------------------------------------------------------

    //------------------------------------------------------------------------
    /**
     * Currently just using standard Web authentication.  This will likely
     * need to change (and likely be inherited from Atom4J).
     * @return boolean User is authorized to add/edit content.
     */
    protected boolean authorized()
    {
        try
        {
            return RollerRequest.getRollerRequest().isUserAuthorizedToEdit();
        }
        catch (Exception e)
        {
            mLogger.error("Error checking User Authorization", e);
        }
        return false;
    }

    //------------------------------------------------------------------------
    private WeblogManager getWeblogManager() throws RollerException
    {
        return RollerRequest.getRollerRequest().getRoller().getWeblogManager();
    }

    //------------------------------------------------------------------------
    private UserManager getUserManager() throws RollerException
    {
        return RollerRequest.getRollerRequest().getRoller().getUserManager();
    }

    //------------------------------------------------------------------------
    protected void flushPageCache(RollerRequest rreq)
    {
        PageCache.removeFromCache( rreq.getRequest(), rreq.getUser() );
    }

    //------------------------------------------------------------------------
    /* (non-Javadoc)
     * @see org.osjava.atom4j.servlet.AtomServlet#getEntry(java.lang.String[])
     */
    protected Entry getEntry(String[] pathInfo) throws Exception
    {
        WeblogEntryData entry = getWeblogManager().retrieveWeblogEntry(pathInfo[2]);
        //    getWeblogManager().getWeblogEntryByAnchor(pathInfo[0], pathInfo[2]);

        return AtomAssistant.convertToAtomEntry(entry, authorized());
    }

    //------------------------------------------------------------------------
    /* (non-Javadoc)
     * @see org.osjava.atom4j.servlet.AtomServlet#deleteEntry(java.lang.String[])
     */
    protected void deleteEntry(String[] pathInfo) throws Exception
    {
        // find the WeblogEntryData to be edited
        RollerRequest rreq = RollerRequest.getRollerRequest();
        WeblogManager weblogMgr = getWeblogManager();
        WeblogEntryData entry =
            weblogMgr.getWeblogEntryByAnchor(rreq.getWebsite(), pathInfo[2]);

        if (entry != null)
        {
            weblogMgr.removeWeblogEntry( entry.getId() );
            flushPageCache( rreq );
        }
    }

    //------------------------------------------------------------------------
    /* (non-Javadoc)
     * @see org.osjava.atom4j.servlet.AtomServlet#saveNewEntry(org.osjava.atom4j.pojo.Entry)
     */
    protected void saveNewEntry(Entry atomEntry) throws Exception
    {
        RollerRequest rreq = RollerRequest.getRollerRequest();
        HttpServletRequest req = rreq.getRequest();
        Roller roller = rreq.getRoller();

        WeblogManager weblogMgr = roller.getWeblogManager();

        WebsiteData website = rreq.getWebsite();
        Timestamp current = new Timestamp(System.currentTimeMillis());

        Timestamp pubTime = current;
        Timestamp updateTime = current;
        if (atomEntry.getIssued() != null)
        {
            pubTime = new Timestamp( atomEntry.getIssued().getTime() );
        }
        if (atomEntry.getModified() != null)
        {
            updateTime = new Timestamp( atomEntry.getModified().getTime() );
        }

        WeblogEntryData rollerEntry = new WeblogEntryData();
        rollerEntry.setTitle(atomEntry.getTitle().getText());
        rollerEntry.setText(atomEntry.getContent().getText());
        rollerEntry.setPubTime(pubTime);
        rollerEntry.setUpdateTime(updateTime);
        rollerEntry.setWebsite(website);
        rollerEntry.setPublishEntry( Boolean.TRUE );
        rollerEntry.setCategory(website.getBloggerCategory());

        // store it
        rollerEntry.save();
        flushPageCache(rreq);

        atomEntry.setId(rollerEntry.getId());

        // send ping - no longer automatic
        /*
        String contextUrl =
            RollerContext.getRollerContext(req).getAbsoluteContextUrl(req);
        RollerXmlRpcClient.sendWeblogsPing(
            contextUrl + entry.getPermaLink(),
            entry.getWebsite().getName());
        */
    }

    //------------------------------------------------------------------------
    /* (non-Javadoc)
     * @see org.osjava.atom4j.servlet.AtomServlet#updateEntry(org.osjava.atom4j.pojo.Entry)
     */
    protected void updateEntry(Entry atomEntry, String[] pathInfo) throws Exception
    {
        RollerRequest rreq = RollerRequest.getRollerRequest();
        UserData user = rreq.getUser();
        if (user != null && pathInfo.length > 2)
        {
            // find the WeblogEntryData to be edited
            WeblogManager weblogMgr = getWeblogManager();
            WeblogEntryData rollerEntry =
                weblogMgr.retrieveWeblogEntry(pathInfo[2]);

            if (rollerEntry != null)
            {
                Timestamp pubTime = rollerEntry.getPubTime();
                Timestamp updateTime = new Timestamp(System.currentTimeMillis());
                if (atomEntry.getIssued() != null)
                {
                    pubTime = new Timestamp( atomEntry.getIssued().getTime() );
                }
                if (atomEntry.getModified() != null)
                {
                    updateTime = new Timestamp( atomEntry.getModified().getTime() );
                }

                rollerEntry.setTitle(atomEntry.getTitle().getText());
                rollerEntry.setText(atomEntry.getContent().getText());
                rollerEntry.setPubTime(pubTime);
                rollerEntry.setUpdateTime(updateTime);

                // store it
                rollerEntry.save();
                flushPageCache(rreq);
            }
        }
    }

    //------------------------------------------------------------------------
    protected List getLatestEntries(String userrName, int maxEntries)
    {
        List rollerEntries = new ArrayList(maxEntries);
        try
        {
            RollerRequest rreq = RollerRequest.getRollerRequest();
            WebsiteData website = rreq.getWebsite();
            rollerEntries = getWeblogManager().getWeblogEntries(
                            website,                  // userName
                            null,                     // startDate
                            new Date(),               // endDate
                            null,                     // catName
                            WeblogManager.PUB_ONLY,   // status
                            new Integer(maxEntries)); // maxEntries
        }
        catch (RollerException e)
        {
            mLogger.error(e);
        }

        return rollerEntries;
    }

    //------------------------------------------------------------------------
    /*
     * This is no longer a part of the Atom spec, but I'm leaving it
     * in anticipation of future spec changes or extension by Roller.
     */
    protected List allEntries(String[] pathInfo) throws Exception
    {
        // I'm not sure what this is supposed to do and it is not part of
        // Atom so I'm commenting it out for now. - Dave

//        List rollerEntries = new ArrayList(50);
//        try
//        {
//            rollerEntries = getWeblogManager().getAllRecentWeblogEntries(new Date(), 0);
//        }
//        catch (RollerException e)
//        {
//            mLogger.error(e);
//        }
//
//        List entries= AtomAssistant.convertEntries(rollerEntries);
//
//        return entries;
        return null;
    }

    //------------------------------------------------------------------------
    /*
     * This is no longer a part of the Atom spec, but I'm leaving it
     * in anticipation of future spec changes or extension by Roller.
     */
    protected List getEntryRange(int startRange, int endRange, String[] pathInfo) throws Exception
    {
        // I'm not sure what this is supposed to do and it is not part of
        // Atom so I'm commenting it out for now. - Dave

//        List rollerEntries = new ArrayList(endRange);
//        try
//        {
//            rollerEntries = getWeblogManager().getAllRecentWeblogEntries(new Date(), endRange);
//        }
//        catch (RollerException e)
//        {
//            mLogger.error(e);
//        }
//
//        if (startRange >= 0)
//        {
//            if (endRange > rollerEntries.size()) endRange = rollerEntries.size();
//            rollerEntries = rollerEntries.subList(startRange, endRange);
//        }
//
//        List entries= AtomAssistant.convertEntries(rollerEntries);
//
//        return entries;
        return null;
    }

    //------------------------------------------------------------------------
    /*
     * This is no longer a part of the Atom spec, but I'm leaving it
     * in anticipation of future spec changes or extension by Roller.
     */
    protected List entryKeywordSearch(String[] pathInfo) throws Exception
    {
        String searchTerm = URLDecoder.decode( pathInfo[2], "UTF-8" );
        IndexManager manager =
            RollerContext.getRollerContext(
                RollerContext.getServletContext() ).getIndexManager();
        SearchOperation search = new SearchOperation();
        search.setTerm( searchTerm );
        search.setUsername( pathInfo[0] );
        manager.executeIndexOperationNow( search );

        Hits hits = search.getResults();
        WeblogManager wmanager = getWeblogManager();

        if (mLogger.isDebugEnabled())
        {
            mLogger.debug("numresults = " + hits.length());
        }

        List searchResults = new ArrayList();
        Document doc = null;
        String username = null;
        String anchor = null;
        WeblogEntryData entry = null;
        for (int i = 0; i < hits.length(); i++)
        {
             doc = hits.doc(i);
             username =
                 doc.getField(FieldConstants.USERNAME).stringValue();
             anchor =
                 doc.getField(FieldConstants.ANCHOR).stringValue();
             entry =
                 wmanager.getWeblogEntryByAnchor(entry.getWebsite(), anchor);
             searchResults.add( AtomAssistant.convertToAtomEntry(entry, authorized()) );
        }

        return searchResults;
    }

    //------------------------------------------------------------------------
    /*
     * This is no longer a part of the Atom spec, but I'll continue
     * the implementation for Roller.  This will make a good exercise
     * in extending Atom.
     */
    protected List findTemplates(String[] pathInfo) throws Exception
    {
        WebsiteData website = getUserManager().getWebsite(pathInfo[0]);
        List pages = getUserManager().getPages(website);
        ArrayList templates = new ArrayList(pages.size());
        Template template = null;
        for (int i=0; i<pages.size(); i++)
        {
            PageData page = (PageData)pages.get(i);
            template = new Template();
            template.setBaseURL(mBaseUrl);
            template.setId(page.getId());
            template.setTitle(page.getName());
            templates.add(template);
        }

        return templates;
    }

    //------------------------------------------------------------------------
    /*
     * This is no longer a part of the Atom spec, but I'll continue
     * the implementation for Roller.  This will make a good exercise
     * in extending Atom.
     */
    protected byte[] getTemplate(String[] pathInfo) throws Exception
    {
        PageData page = getUserManager().retrievePage(pathInfo[2]); // page id

        return page.getTemplate().getBytes();
    }

    //------------------------------------------------------------------------
    /*
     * This is no longer a part of the Atom spec, but I'll continue
     * the implementation for Roller.  This will make a good exercise
     * in extending Atom.
     */
    protected void updateTemplate(String template, String[] pathInfo) throws Exception
    {
        PageData page = getUserManager().retrievePage(pathInfo[2]); // page id
        page.setTemplate(template);
        getUserManager().storePage(page);
        flushPageCache( RollerRequest.getRollerRequest() );
    }

    //------------------------------------------------------------------------
    /*
     * This is no longer a part of the Atom spec, but I'll continue
     * the implementation for Roller.  This will make a good exercise
     * in extending Atom.
     */
    protected byte[] deleteTemplate(String[] pathInfo) throws Exception
    {
        PageData page = getUserManager().retrievePage(pathInfo[2]); // page id
        byte[] bytes = page.getTemplate().getBytes();
        getUserManager().removePage(pathInfo[2]); // page id
        flushPageCache( RollerRequest.getRollerRequest() );

        return bytes;
    }

    //------------------------------------------------------------------------
    /* (non-Javadoc)
     * @see org.osjava.atom4j.servlet.AtomServlet#postComment(org.osjava.atom4j.pojo.Entry, org.osjava.atom4j.pojo.Entry, java.lang.String[])
     */
    protected void postComment(Entry entry, Entry comment, String[] pathInfo) throws Exception
    {
        WeblogEntryData rollerEntry = getWeblogManager().retrieveWeblogEntry( entry.getId() );
        if (rollerEntry != null)
        {
            CommentData rollerComment = new CommentData(
                null, rollerEntry,
                comment.getAuthor().getName(),
                comment.getAuthor().getEmail(),
                comment.getAuthor().getUrl(),
                comment.getContent().getText(),
                new java.sql.Timestamp( new Date().getTime() ),
                Boolean.FALSE
            );
            rollerComment.save();
            flushPageCache( RollerRequest.getRollerRequest() );
        }
    }

    /*
     * @see org.osjava.atom4j.servlet.AtomServlet#getFeed(java.lang.String[])
     */
    protected Feed getFeed(String[] pathInfo) throws Exception
    {
        WebsiteData website = getUserManager().getWebsite(pathInfo[0]);
        if (website == null) return null;

        // returns list of Roller Entries
        List entries = getLatestEntries(pathInfo[0],
            RollerRequest.getRollerRequest().getWeblogEntryCount());

        return AtomAssistant.convertToAtomFeed(website, entries, authorized());
    }

    /* (non-Javadoc)
     * @see org.osjava.atom4j.servlet.AtomServlet#getPassword(java.lang.String)
     */
    public String getPassword(String userName) throws Exception
    {
        UserData user = getUserManager().getUser(userName);
        return user.getPassword();
    }
}
