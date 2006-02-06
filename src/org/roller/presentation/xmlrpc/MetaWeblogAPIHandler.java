
package org.roller.presentation.xmlrpc;

import java.io.ByteArrayInputStream;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Vector;
import javax.servlet.http.HttpServletRequest;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.struts.util.RequestUtils;
import org.apache.xmlrpc.XmlRpcException;
import org.roller.RollerException;
import org.roller.model.FileManager;
import org.roller.model.Roller;
import org.roller.model.RollerFactory;
import org.roller.model.WeblogManager;
import org.roller.pojos.UserData;
import org.roller.pojos.WeblogCategoryData;
import org.roller.pojos.WeblogEntryData;
import org.roller.pojos.WebsiteData;
import org.roller.presentation.RollerContext;
import org.roller.presentation.RollerRequest;
import org.roller.util.RollerMessages;
import org.roller.util.Utilities;


/**
 * Roller XML-RPC Handler for the MetaWeblog API.
 *
 * MetaWeblog API spec can be found at http://www.xmlrpc.com/metaWeblogApi
 *
 * @author David M Johnson
 */
public class MetaWeblogAPIHandler extends BloggerAPIHandler {
    
    static final long serialVersionUID = -1364456614935668629L;
    
    private static Log mLogger = LogFactory.getLog(MetaWeblogAPIHandler.class);
    
    public MetaWeblogAPIHandler() {
        super();
    }
    
    
    /**
     * Authenticates a user and returns the categories available in the website
     *
     * @param blogid Dummy Value for Roller
     * @param userid Login for a MetaWeblog user who has permission to post to the blog
     * @param password Password for said username
     * @throws Exception
     * @return
     */
    public Object getCategories(String blogid, String userid, String password)
            throws Exception {
        
        mLogger.debug("getCategories() Called =====[ SUPPORTED ]=====");
        mLogger.debug("     BlogId: " + blogid);
        mLogger.debug("     UserId: " + userid);
        
        WebsiteData website = validate(blogid, userid,password);
        RollerRequest rreq = RollerRequest.getRollerRequest();
        Roller roller = RollerFactory.getRoller();
        try {
            Hashtable result = new Hashtable();
            WeblogManager weblogMgr = roller.getWeblogManager();
            List cats = weblogMgr.getWeblogCategories(website, false);
            for (Iterator wbcItr = cats.iterator(); wbcItr.hasNext();) {
                WeblogCategoryData category = (WeblogCategoryData) wbcItr.next();
                result.put(category.getPath(),
                        createCategoryStruct(category, userid));
            }
            return result;
        } catch (Exception e) {
            String msg = "ERROR in MetaWeblogAPIHandler.getCategories";
            mLogger.error(msg,e);
            throw new XmlRpcException(UNKNOWN_EXCEPTION, msg);
        }
    }
    
    
    /**
     * Edits a given post. Optionally, will publish the blog after making the edit
     *
     * @param postid Unique identifier of the post to be changed
     * @param userid Login for a MetaWeblog user who has permission to post to the blog
     * @param password Password for said username
     * @param struct Contents of the post
     * @param publish If true, the blog will be published immediately after the post is made
     * @throws org.apache.xmlrpc.XmlRpcException
     * @return
     */
    public boolean editPost(String postid, String userid, String password,
                            Hashtable struct, int publish) throws Exception {
        
        return editPost(postid, userid, password, struct, publish > 0);
    }
    
    
    public boolean editPost(String postid, String userid, String password,
            Hashtable struct, boolean publish) throws Exception {
        
        mLogger.debug("editPost() Called ========[ SUPPORTED ]=====");
        mLogger.debug("     PostId: " + postid);
        mLogger.debug("     UserId: " + userid);
        mLogger.debug("    Publish: " + publish);
        
        Roller roller = RollerFactory.getRoller();
        WeblogManager weblogMgr = roller.getWeblogManager();
        WeblogEntryData entry = weblogMgr.retrieveWeblogEntry(postid);
        
        validate(entry.getWebsite().getHandle(), userid,password);
        
        Hashtable postcontent = struct;
        String description = (String)postcontent.get("description");
        String title = (String)postcontent.get("title");
        if (title == null) title = "";
        
        Date dateCreated = (Date)postcontent.get("dateCreated");
        if (dateCreated == null) dateCreated = (Date)postcontent.get("pubDate");
        
        String cat = null;
        if ( postcontent.get("categories") != null ) {
            Vector cats = (Vector)postcontent.get("categories");
            cat = (String)cats.elementAt(0);
        }
        mLogger.debug("      Title: " + title);
        mLogger.debug("   Category: " + cat);
        
        try {
            
            Timestamp current =
                    new Timestamp(System.currentTimeMillis());
            
            if ( !title.equals("") ) entry.setTitle(title);
            entry.setText(description);
            entry.setUpdateTime(current);
            if (Boolean.valueOf(publish).booleanValue()) {
                entry.setStatus(WeblogEntryData.PUBLISHED);
            } else {
                entry.setStatus(WeblogEntryData.DRAFT);
            }
            if (dateCreated != null) {
                entry.setPubTime(new Timestamp(dateCreated.getTime()));
            }
            
            if ( cat != null ) {
                // Use first category specified by request
                WeblogCategoryData cd =
                        weblogMgr.getWeblogCategoryByPath(entry.getWebsite(), cat);
                entry.setCategory(cd);
            }
            
            entry.save();
            roller.commit();
            flushPageCache(entry.getWebsite());
            
            // TODO: Roller timestamps need better than 1 second accuracy
            // Until then, we can't allow more than one post per second
            Thread.sleep(1000);
            
            return true;
        } catch (Exception e) {
            String msg = "ERROR in MetaWeblogAPIHandler.editPost";
            mLogger.error(msg,e);
            throw new XmlRpcException(UNKNOWN_EXCEPTION, msg);
        }
    }
    
    
    /**
     * Makes a new post to a designated blog. Optionally, will publish the blog after making the post
     *
     * @param blogid Unique identifier of the blog the post will be added to
     * @param userid Login for a MetaWeblog user who has permission to post to the blog
     * @param password Password for said username
     * @param struct Contents of the post
     * @param publish If true, the blog will be published immediately after the post is made
     * @throws org.apache.xmlrpc.XmlRpcException
     * @return
     */
    public String newPost(String blogid, String userid, String password, 
                            Hashtable struct, int publish) throws Exception {
        
        return newPost(blogid, userid, password, struct, publish > 0);
    }
    
    
    public String newPost(String blogid, String userid, String password, 
                            Hashtable struct, boolean publish) throws Exception {
        
        mLogger.debug("newPost() Called ===========[ SUPPORTED ]=====");
        mLogger.debug("     BlogId: " + blogid);
        mLogger.debug("     UserId: " + userid);
        mLogger.debug("    Publish: " + publish);
        
        WebsiteData website = validate(blogid, userid, password);
        
        Hashtable postcontent = struct;
        String description = (String)postcontent.get("description");
        String title = (String)postcontent.get("title");
        if (title == null) title = "";
        
        Date dateCreated = (Date)postcontent.get("dateCreated");
        if (dateCreated == null) dateCreated = (Date)postcontent.get("pubDate");
        if (dateCreated == null) dateCreated = new Date();
        
        String cat = null;
        if ( postcontent.get("categories") != null ) {
            Vector cats = (Vector)postcontent.get("categories");
            if (cats.size() > 0) {
                // only use the first category passed in
                cat = (String)cats.elementAt(0);
            }
        }
        mLogger.debug("      Title: " + title);
        mLogger.debug("   Category: " + cat);
        
        try {
            Roller roller = RollerFactory.getRoller();
            WeblogManager weblogMgr = roller.getWeblogManager();
            UserData user = roller.getUserManager().getUser(userid);
            Timestamp current =
                    new Timestamp(System.currentTimeMillis());
            
            WeblogEntryData entry = new WeblogEntryData();
            entry.setTitle(title);
            entry.setText(description);
            entry.setPubTime(new Timestamp(dateCreated.getTime()));
            entry.setUpdateTime(current);
            entry.setWebsite(website);
            entry.setCreator(user);
            if (Boolean.valueOf(publish).booleanValue()) {
                entry.setStatus(WeblogEntryData.PUBLISHED);
            } else {
                entry.setStatus(WeblogEntryData.DRAFT);
            }
            
            
            if ( cat != null ) {
                // Use first category specified by request
                WeblogCategoryData cd =
                        weblogMgr.getWeblogCategoryByPath(website, cat);
                entry.setCategory(cd);
            } else {
                // Use Blogger API category from user's weblog config
                entry.setCategory(website.getBloggerCategory());
            }
            
            entry.save();
            roller.commit();
            flushPageCache(entry.getWebsite());
            
            // TODO: Roller timestamps need better than 1 second accuracy
            // Until then, we can't allow more than one post per second
            Thread.sleep(1000);
            
            return entry.getId();
        } catch (Exception e) {
            String msg = "ERROR in MetaWeblogAPIHandler.newPost";
            mLogger.error(msg,e);
            throw new XmlRpcException(UNKNOWN_EXCEPTION, msg);
        }
    }
    
    
    /**
     *
     * @param postid
     * @param userid
     * @param password
     * @return
     * @throws Exception
     */
    public Object getPost(String postid, String userid, String password)
            throws Exception {
        
        mLogger.debug("getPost() Called =========[ SUPPORTED ]=====");
        mLogger.debug("     PostId: " + postid);
        mLogger.debug("     UserId: " + userid);
        
        Roller roller = RollerFactory.getRoller();
        WeblogManager weblogMgr = roller.getWeblogManager();
        WeblogEntryData entry = weblogMgr.retrieveWeblogEntry(postid);
        
        validate(entry.getWebsite().getHandle(), userid,password);
        
        try {
            return createPostStruct(entry, userid);
        } catch (Exception e) {
            String msg = "ERROR in MetaWeblogAPIHandler.getPost";
            mLogger.error(msg,e);
            throw new XmlRpcException(UNKNOWN_EXCEPTION, msg);
        }
    }
    
    
    /**
     * Allows user to post a binary object, a file, to Roller. If the file is
     * allowed by the RollerConfig file-upload settings, then the file will be
     * placed in the user's upload diretory.
     */
    public Object newMediaObject(String blogid, String userid, String password, 
                                    Hashtable struct) throws Exception {
        
        mLogger.debug("newMediaObject() Called =[ SUPPORTED ]=====");
        mLogger.debug("     BlogId: " + blogid);
        mLogger.debug("     UserId: " + userid);
        mLogger.debug("   Password: *********");
        
        WebsiteData website = validate(blogid, userid, password);
        try {
            String name = (String) struct.get("name");
            name = name.replaceAll("/","_");
            String type = (String) struct.get("type");
            mLogger.debug("newMediaObject name: " + name);
            mLogger.debug("newMediaObject type: " + type);
            
            byte[] bits = (byte[]) struct.get("bits");
            
            Roller roller = RollerFactory.getRoller();
            FileManager fmgr = roller.getFileManager();
            RollerMessages msgs = new RollerMessages();
            
            // If save is allowed by Roller system-wide policies
            if (fmgr.canSave(website.getHandle(), name, bits.length, msgs)) {
                // Then save the file
                fmgr.saveFile(
                        website.getHandle(), name, bits.length, new ByteArrayInputStream(bits));
                
                RollerRequest rreq = RollerRequest.getRollerRequest();
                HttpServletRequest request = rreq.getRequest();
                
                // TODO: build URL to uploaded file should be done in FileManager
                String uploadPath = RollerFactory.getRoller().getFileManager().getUploadUrl();
                uploadPath += "/" + website.getHandle() + "/" + name;
                String fileLink = RequestUtils.printableURL(
                        RequestUtils.absoluteURL(request, uploadPath));
                
                Hashtable returnStruct = new Hashtable(1);
                returnStruct.put("url", fileLink);
                return returnStruct;
            }
            throw new XmlRpcException(UPLOAD_DENIED_EXCEPTION,
                    "File upload denied because:" + msgs.toString());
        } catch (RollerException e) {
            String msg = "ERROR in BlooggerAPIHander.newMediaObject";
            mLogger.error(msg,e);
            throw new XmlRpcException(UNKNOWN_EXCEPTION, msg);
        }
    }
    
    
    /**
     * Get a list of recent posts for a category
     *
     * @param blogid Unique identifier of the blog the post will be added to
     * @param userid Login for a Blogger user who has permission to post to the blog
     * @param password Password for said username
     * @param numposts Number of Posts to Retrieve
     * @throws XmlRpcException
     * @return
     */
    public Object getRecentPosts(String blogid, String userid, String password, 
                                    int numposts) throws Exception {
        
        mLogger.debug("getRecentPosts() Called ===========[ SUPPORTED ]=====");
        mLogger.debug("     BlogId: " + blogid);
        mLogger.debug("     UserId: " + userid);
        mLogger.debug("     Number: " + numposts);
        
        WebsiteData website = validate(blogid, userid,password);
        
        try {
            Vector results = new Vector();
            
            Roller roller = RollerFactory.getRoller();
            WeblogManager weblogMgr = roller.getWeblogManager();
            if (website != null) {
                List entries = weblogMgr.getWeblogEntries(
                    website,           // website
                    null,              // startDate
                    null,              // endDate
                    null,              // catName
                    null,              // status
                    "updateTime",      // sortby
                    new Integer(numposts));  // maxEntries
                
                Iterator iter = entries.iterator();
                while (iter.hasNext()) {
                     WeblogEntryData entry = (WeblogEntryData)iter.next();
                     results.addElement(createPostStruct(entry, userid));
                }
            }
            return results;
            
        } catch (Exception e) {
            String msg = "ERROR in BlooggerAPIHander.getRecentPosts";
            mLogger.error(msg,e);
            throw new XmlRpcException(UNKNOWN_EXCEPTION, msg);
        }
    }
    
    
    private Hashtable createPostStruct(WeblogEntryData entry, String userid) {
        
        RollerRequest rreq = RollerRequest.getRollerRequest();
        HttpServletRequest request = rreq.getRequest();
        RollerContext rollerCtx = RollerContext.getRollerContext();
        String permalink =
                rollerCtx.getAbsoluteContextUrl(request) + entry.getPermaLink();
        
        Hashtable struct = new Hashtable();
        struct.put("title", entry.getTitle());
        if (entry.getLink() != null) {
            struct.put("link", Utilities.escapeHTML(entry.getLink()));
        }
        
        struct.put("description", entry.getText());
        struct.put("pubDate", entry.getPubTime());
        struct.put("dateCreated", entry.getPubTime());
        struct.put("guid", Utilities.escapeHTML(permalink));
        struct.put("permaLink", Utilities.escapeHTML(permalink));
        struct.put("postid", entry.getId());
        struct.put("userid", userid);
        
        Vector catArray = new Vector();
        catArray.addElement(entry.getCategory().getPath());
        struct.put("categories", catArray);
        
        return struct;
    }
    
    
    private Hashtable createCategoryStruct(WeblogCategoryData category, String userid) {
        
        RollerRequest rreq = RollerRequest.getRollerRequest();
        HttpServletRequest req = rreq.getRequest();
        String contextUrl = RollerContext.getRollerContext().getAbsoluteContextUrl(req);
        
        Hashtable struct = new Hashtable();
        struct.put("description", category.getPath());
        
        String catUrl = contextUrl+"/page/"+userid+"?catname="+category.getPath();
        catUrl = Utilities.stringReplace(catUrl," ","%20");
        struct.put("htmlUrl", catUrl);
        
        String rssUrl = contextUrl+"/rss/"+userid+"?catname="+category.getPath();
        rssUrl = Utilities.stringReplace(catUrl," ","%20");
        struct.put("rssUrl",rssUrl);
        
        return struct;
    }
    
}
