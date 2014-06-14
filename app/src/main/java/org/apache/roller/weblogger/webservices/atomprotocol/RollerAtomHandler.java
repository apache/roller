/*
* Licensed to the Apache Software Foundation (ASF) under one or more
* contributor license agreements.  The ASF licenses this file to You
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
package org.apache.roller.weblogger.webservices.atomprotocol;
import com.rometools.propono.atom.common.Categories;
import com.rometools.propono.atom.server.AtomRequest;
import java.util.StringTokenizer;
import javax.servlet.http.HttpServletRequest;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.roller.util.RollerConstants;
import org.apache.roller.weblogger.business.Weblogger;
import org.apache.roller.weblogger.business.WebloggerFactory;
import org.apache.roller.weblogger.pojos.User;
import org.apache.roller.weblogger.pojos.WeblogEntry;
import org.apache.roller.weblogger.pojos.Weblog;
import org.apache.roller.weblogger.util.Utilities;
import org.apache.roller.weblogger.util.WSSEUtilities;
import com.rometools.propono.atom.common.AtomService;
import com.rometools.propono.atom.server.AtomException;
import com.rometools.propono.atom.server.AtomHandler;
import com.rometools.propono.atom.server.AtomMediaResource;
import com.rometools.propono.atom.server.AtomNotFoundException;
import com.rometools.rome.feed.atom.Entry;
import com.rometools.rome.feed.atom.Feed;
import javax.servlet.http.HttpServletResponse;
import net.oauth.OAuthAccessor;
import net.oauth.OAuthMessage;
import net.oauth.server.OAuthServlet;
import org.apache.commons.lang3.StringUtils;
import org.apache.roller.weblogger.WebloggerException;
import org.apache.roller.weblogger.business.OAuthManager;
import org.apache.roller.weblogger.config.WebloggerConfig;
import org.apache.roller.weblogger.config.WebloggerRuntimeConfig;
import org.apache.roller.weblogger.pojos.WeblogPermission;


/**
 * Weblogger's ROME Propono-based Atom Protocol implementation.
 * 
 * Each Weblogger workspace has two collections, one that accepts entries and 
 * that accepts everything. The entries collection represents the weblog 
 * entries in a single weblog and the everything collection represents that 
 * weblog's uploaded-files. 
 * 
 * Here are the APP URIs suppored by Weblogger:
 * 
 * <pre>
 *    /roller-services/app
 *    Introspection doc
 * 
 * 
 *    /roller-services/app/[weblog-handle>/entries
 *    Entry collection for a weblog (GET, POST)
 * 
 *    /roller-services/app/[weblog-handle]/entries/[offset]
 *    Entry collection for a blog, with offset (GET)
 * 
 *    /roller-services/app/[weblog-handle]/entry/[id]
 *    Individual entry (GET, PUT, DELETE)
 *
 * 
 *    /roller-services/app/[weblog-handle]/resources
 *    Resource (i.e. file-uploads) collection for a weblog (GET, POST)
 * 
 *    /roller-services/app/[weblog-handle]/resources/[offset]
 *    Resource collection for a blog, with offset (GET)
 * 
 *    /roller-services/app/[weblog-handle]/resource/*.media-link[name]
 *    Individual resource metadata (GET, PUT, DELETE)
 * 
 *    /roller-services/app/[weblog-handle]/resource/[name]
 *    Individual resource data (GET)
 * </pre>
 *
 * @author David M Johnson
 */
public class RollerAtomHandler implements AtomHandler {
    protected Weblogger roller = null;
    protected User user = null;
    protected int maxEntries = 20;
    protected String atomURL = null;
    
    protected static final boolean THROTTLE;
    
    protected static Log log =
            LogFactory.getFactory().getInstance(RollerAtomHandler.class);
    
    static {
        THROTTLE = WebloggerConfig
            .getBooleanProperty("webservices.atomprotocol.oneSecondThrottle", true);
    }
    
    //------------------------------------------------------------ construction

    /**
     * Create Atom handler for a request and attempt to authenticate user.
     * If user is authenticated, then getAuthenticatedUsername() will return
     * then user's name, otherwise it will return null.
     */
    public RollerAtomHandler(HttpServletRequest request, HttpServletResponse response) {
        roller = WebloggerFactory.getWeblogger();

        String userName;
        if ("oauth".equals(WebloggerRuntimeConfig.getProperty("webservices.atomPubAuth"))) {
            userName = authenticationOAUTH(request, response);

        } else if ("wsse".equals(WebloggerRuntimeConfig.getProperty("webservices.atomPubAuth"))) {
            userName = authenticateWSSE(request);

        } else {
            // default to basic
            userName = authenticateBASIC(request);
        }

        if (userName != null) {
            try {
                this.user = roller.getUserManager().getUserByUserName(userName);
            } catch (Exception neverHappen) {
                log.debug("Getting user", neverHappen);
            } 
        }
        
        atomURL = WebloggerFactory.getWeblogger().getUrlStrategy().getAtomProtocolURL(true);
    }

    /**
     * Return weblogHandle of authenticated user or null if there is none.
     */
    public String getAuthenticatedUsername() {
        String ret = null;
        if (this.user != null) {
            ret = user.getUserName();
        }
        return ret;
    }
    
    //----------------------------------------------------------- introspection
    
    /**
     * Return Atom service document for site, getting blog-name from pathInfo.
     * The workspace will contain collections for entries, categories and resources.
     */
    public AtomService getAtomService(AtomRequest areq) throws AtomException {
        try {
            return new RollerAtomService(user, atomURL);
        } catch (WebloggerException ex) {
            log.error("Unable to create Service Document", ex);
            throw new AtomException("ERROR creating Service Document", ex);
        }
    }
     
    //----------------------------------------------------------------- create

    /**
     * Create entry in the entry collection (a Weblogger blog has only one).
     */
    public Entry postEntry(AtomRequest areq, Entry entry) throws AtomException {
        EntryCollection ecol = new EntryCollection(user, atomURL);
        return ecol.postEntry(areq, entry);
    }
    
    
    /**
     * Create new resource in generic collection (a Weblogger blog has only one).
     * TODO: can we avoid saving temporary file?
     * TODO: do we need to handle mutli-part MIME uploads?
     * TODO: use Jakarta Commons File-upload?
     */
    public Entry postMedia(AtomRequest areq, Entry entry)
            throws AtomException {
        MediaCollection mcol = new MediaCollection(user, atomURL);
        return mcol.postMedia(areq, entry);
    }
    

    //----------------------------------------------------------------- retrieve 
    
    /**
     * Return collection specified by pathinfo.
     * <pre>
     * Supports these URI forms:
     *    /<blog-name>/entries
     *    /<blog-name>/entries/offset
     *    /<blog-name>/resources
     *    /<blog-name>/resources/offset
     * </pre>
     */
    public Feed getCollection(AtomRequest areq) throws AtomException {
        String[] pathInfo = StringUtils.split(areq.getPathInfo(),"/");
        
        if (pathInfo.length > 0 && pathInfo[1].equals("entries")) {
            EntryCollection ecol = new EntryCollection(user, atomURL);
            return ecol.getCollection(areq);
            
        } else if (pathInfo.length > 0 && pathInfo[1].equals("resources")) {
            MediaCollection mcol = new MediaCollection(user, atomURL);
            return mcol.getCollection(areq);
        }
        throw new AtomNotFoundException("Cannot find collection specified");
    }
    
       
    public Categories getCategories(AtomRequest arg0) throws AtomException {
        throw new UnsupportedOperationException("Not supported yet.");
    }


    /**
     * Retrieve entry, URI like this /blog-name/entry/id
     */
    public Entry getEntry(AtomRequest areq) throws AtomException {
        log.debug("Entering");
        String[] pathInfo = StringUtils.split(areq.getPathInfo(),"/");
        // URI is /blogname/entries/entryid
        if (pathInfo.length > 2) {
            if (pathInfo[1].equals("entry")) {
                EntryCollection ecol = new EntryCollection(user, atomURL);
                return ecol.getEntry(areq);
            } else if (pathInfo[1].equals("resource") && pathInfo[pathInfo.length - 1].endsWith(".media-link")) {
                MediaCollection mcol = new MediaCollection(user, atomURL);
                return mcol.getEntry(areq);                    
            }
        }
        throw new AtomNotFoundException("Cannot find specified entry/resource");
    }
    
    /**
     * Expects pathInfo of form /blog-name/resource/path/name
     */
    public AtomMediaResource getMediaResource(AtomRequest areq) throws AtomException {
        MediaCollection mcol = new MediaCollection(user, atomURL);
        return mcol.getMediaResource(areq);
    }
    
    
    //----------------------------------------------------------------- update
    
    /**
     * Update entry, URI like this /blog-name/entry/id
     */
    public void putEntry(AtomRequest areq, Entry entry) throws AtomException {
        EntryCollection ecol = new EntryCollection(user, atomURL);
        ecol.putEntry(areq, entry);
    }


    /**
     * Update resource specified by pathInfo using data from input stream.
     * Expects pathInfo of form /blog-name/resource/path/name
     */
    public void putMedia(AtomRequest areq) throws AtomException {
        MediaCollection mcol = new MediaCollection(user, atomURL);
        mcol.putMedia(areq);
    }
    
    
    //----------------------------------------------------------------- delete
    
    /**
     * Delete entry, URI like this /blog-name/entry/id
     */
    public void deleteEntry(AtomRequest areq) throws AtomException {
        log.debug("Entering");
        String[] pathInfo = StringUtils.split(areq.getPathInfo(),"/");
        if (pathInfo.length > 2) {
            // URI is /blogname/entry/entryid
            if (pathInfo[1].equals("entry")) {
                EntryCollection ecol = new EntryCollection(user, atomURL);
                ecol.deleteEntry(areq);
                return;
            } else if (pathInfo[1].equals("resource")) {
                MediaCollection mcol = new MediaCollection(user, atomURL);
                mcol.deleteEntry(areq);
                return;
            }
        }
        throw new AtomNotFoundException("cannot find specified entry/resource");
    }

    
    //------------------------------------------------------------------ URI testers
    
    /**
     * True if URL is the introspection URI.
     */
    public boolean isAtomServiceURI(AtomRequest areq) {
        String[] pathInfo = StringUtils.split(areq.getPathInfo(),"/");
        return pathInfo.length == 0;
    }
    
    /**
     * True if URL is a entry URI.
     */
    public boolean isEntryURI(AtomRequest areq) {
        String[] pathInfo = StringUtils.split(areq.getPathInfo(),"/");
        if (pathInfo.length > 2 && pathInfo[1].equals("entry")) {
            return true;
        }
        if (pathInfo.length > 2 && pathInfo[1].equals("resource") && pathInfo[pathInfo.length-1].endsWith(".media-link")) {
            return true;
        }
        return false;
    }
        
    /**
     * True if URL is media edit URI. Media can be updated, but not metadata.
     */
    public boolean isMediaEditURI(AtomRequest areq) {
        String[] pathInfo = StringUtils.split(areq.getPathInfo(),"/");
        if (pathInfo.length > 1 && pathInfo[1].equals("resource")) {
            return true;
        }
        return false;
    }
        
    /**
     * True if URL is a collection URI of any sort.
     */
    public boolean isCollectionURI(AtomRequest areq) {
        String[] pathInfo = StringUtils.split(areq.getPathInfo(),"/");
        if (pathInfo.length > 1 && pathInfo[1].equals("entries")) {
            return true;
        }
        if (pathInfo.length > 1 && pathInfo[1].equals("resources")) {
            return true;
        }
        if (pathInfo.length > 1 && pathInfo[1].equals("categories")) {
            return true;
        }
        return false;
    }
    
    public boolean isCategoriesURI(AtomRequest arg0) {
        return false;
    }

    
    //------------------------------------------------------------------ permissions
    
    /**
     * Return true if user is allowed to edit an entry.
     */
    public static boolean canEdit(User u, WeblogEntry entry) {
        try {
            return entry.hasWritePermissions(u);
        } catch (Exception e) {
            log.error("Checking website.canSave()");
        }
        return false;
    }
    
    /**
     * Return true if user is allowed to create/edit weblog entries and file uploads in a website.
     */
    public static  boolean canEdit(User u, Weblog website) {
        try {
            return website.hasUserPermission(u, WeblogPermission.POST);
        } catch (Exception e) {
            log.error("Checking website.hasUserPermissions()");
        }
        return false;
    }
    
    /**
     * Return true if user is allowed to view an entry.
     */
    public static boolean canView(User u, WeblogEntry entry) {
        return canEdit(u, entry);
    }
    
    /**
     * Return true if user is allowed to view a website.
     */
    public static boolean canView(User u, Weblog website) {
        return canEdit(u, website);
    }
    
    //-------------------------------------------------------------- authentication
    
    /**
     * Perform WSSE authentication based on information in request.
     * Will not work if Weblogger password encryption is turned on.
     */
    protected String authenticateWSSE(HttpServletRequest request) {
        String wsseHeader = request.getHeader("X-WSSE");
        if (wsseHeader == null) {
            return null;
        }
        
        String ret = null;
        String userName = null;
        String created = null;
        String nonce = null;
        String passwordDigest = null;
        String[] tokens = wsseHeader.split(",");
        for (int i = 0; i < tokens.length; i++) {
            int index = tokens[i].indexOf('=');
            if (index != -1) {
                String key = tokens[i].substring(0, index).trim();
                String value = tokens[i].substring(index + 1).trim();
                value = value.replaceAll("\"", "");
                if (key.startsWith("UsernameToken")) {
                    userName = value;
                } else if (key.equalsIgnoreCase("nonce")) {
                    nonce = value;
                } else if (key.equalsIgnoreCase("passworddigest")) {
                    passwordDigest = value;
                } else if (key.equalsIgnoreCase("created")) {
                    created = value;
                }
            }
        }
        String digest = null;
        try {
            User inUser = roller.getUserManager().getUserByUserName(userName);
            digest = WSSEUtilities.generateDigest(
                    WSSEUtilities.base64Decode(nonce),
                    created.getBytes("UTF-8"),
                    inUser.getPassword().getBytes("UTF-8"));
            if (digest.equals(passwordDigest)) {
                ret = userName;
            }
        } catch (Exception e) {
            log.error("During wsseAuthenticataion: " + e.getMessage(), e);
        }
        return ret;
    }
    
    /**
     * BASIC authentication.
     */
    public String authenticateBASIC(HttpServletRequest request) {
        boolean valid = false;
        String userID = null;
        String password = null;
        try {
            String authHeader = request.getHeader("Authorization");
            if (authHeader != null) {
                StringTokenizer st = new StringTokenizer(authHeader);
                if (st.hasMoreTokens()) {
                    String basic = st.nextToken();
                    if (basic.equalsIgnoreCase("Basic")) {
                        String credentials = st.nextToken();
                        String userPass = new String(Base64.decodeBase64(credentials.getBytes()));
                        int p = userPass.indexOf(':');
                        if (p != -1) {
                            userID = userPass.substring(0, p);
                            User inUser = roller.getUserManager().getUserByUserName(userID);
                            boolean enabled = inUser.getEnabled();
                            if (enabled) {
                                // are passwords encrypted?
                                String encrypted =
                                        WebloggerConfig.getProperty("passwds.encryption.enabled");
                                password = userPass.substring(p+1);
                                if ("true".equalsIgnoreCase(encrypted)) {
                                    password = Utilities.encodePassword(password,
                                            WebloggerConfig.getProperty("passwds.encryption.algorithm"));
                                }
                                valid = inUser.getPassword().equals(password);
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            log.debug(e);
        }
        if (valid) {
            return userID;
        }
        return null;
    }

    
    private String authenticationOAUTH(
            HttpServletRequest request, HttpServletResponse response) {
        try {
            OAuthManager omgr = WebloggerFactory.getWeblogger().getOAuthManager();
            OAuthMessage requestMessage = OAuthServlet.getMessage(request, null);
            OAuthAccessor accessor = omgr.getAccessor(requestMessage);
            omgr.getValidator().validateMessage(requestMessage, accessor);
            return (String)accessor.consumer.getProperty("userId");

        } catch (Exception ex) {
            log.debug("ERROR authenticating user", ex);
            String realm = (request.isSecure())?"https://":"http://";
            realm += request.getLocalName();
            try {
                OAuthServlet.handleException(response, ex, realm, true);
            } catch (Exception ioe) {
                log.debug("ERROR writing error response", ioe);
            }
        }
        return null;
    }


    public static void oneSecondThrottle() {
        // Throttle one entry per second per weblog because time-
        // stamp in MySQL and other DBs has only 1 sec resolution
        if (THROTTLE) {
            try {
                synchronized (RollerAtomHandler.class) {
                    Thread.sleep(RollerConstants.SEC_IN_MS);
                }
            } catch (Exception ignored) {}
        }
    }

}
