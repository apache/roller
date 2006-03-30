/*
 * Handler.java
 *
 * Created on January 17, 2006, 12:44 PM
 */
package org.roller.presentation.atomadminapi;

import java.io.Reader;
import javax.servlet.http.HttpServletRequest;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.roller.RollerException;
import org.roller.config.RollerConfig;
import org.roller.model.Roller;
import org.roller.model.RollerFactory;
import org.roller.pojos.UserData;
import org.roller.presentation.RollerContext;
import org.roller.presentation.atomadminapi.sdk.EntrySet;
import org.roller.util.StringUtils;

/**
 * This class is the abstract notion of an AAPP request handler.
 * It processes HTTP requests for each of the four HTTP verbs:
 * GET, POST, PUT, DELETE, for a given weblog resource. 
 *
 * @author jtb
 */
abstract class Handler {
    protected static final String ENDPOINT = "/aapp";
    
    static class URI {
        private String type;
        private String[] entryIds;
        
        public URI(HttpServletRequest request) {
            String pathInfo = request.getPathInfo();
            pathInfo = (pathInfo!=null) ? pathInfo : "";
            String[] elements = StringUtils.split(pathInfo,"/");
            
            if (elements.length > 0) {
                type = elements[0];
            }
            if (elements.length > 1) {
                entryIds = new String[elements.length-1];
                System.arraycopy(elements, 1, entryIds, 0, entryIds.length);
            }
        }
        
        public String getType() {
            return type;
        }        
        
        public String[] getEntryIds() {
            return entryIds;
        }
        
        public String getEntryId() {
            if (getEntryIds() == null) {
                return null;
            }
            if (getEntryIds().length == 0) {
                return null;
            }
            return getEntryIds()[0];
        }
        
        public boolean isIntrospection() {
            return getEntryId() == null && type == null;
        }
        
        public boolean isCollection() {
            return getEntryId() == null && type != null;
        }
        
        public boolean isEntry() {
            return getEntryId() != null && type != null;
        }
    }    
    
    protected static final Log logger = LogFactory.getFactory().getInstance(Handler.class);
    
    private HttpServletRequest request;
    private Roller roller;
    private RollerContext rollerContext;    
    private String username;
    private URI uri;
    private String urlPrefix;
    
    /** Get a Handler object implementation based on the given request. */
    public static Handler getHandler(HttpServletRequest req) throws HandlerException {
        
         boolean enabled = RollerConfig.getBooleanProperty(
              "webservices.adminprotocol.enable");
         if (!enabled) { 
              throw new NotAllowedException("ERROR: Admin protocol not enabled");    
         }
         
         URI uri = new URI(req);
         Handler handler;
         
         if (uri.isIntrospection()) {
             handler = new IntrospectionHandler(req);
         } else if (uri.isCollection() || uri.isEntry()) {
             String type = uri.getType();
             if (type.equals(EntrySet.Types.WEBLOGS)) {
                 handler = new RollerWeblogHandler(req);
             } else if (type.equals(EntrySet.Types.USERS)) {
                 handler = new RollerUserHandler(req); 
             } else if (type.equals(EntrySet.Types.MEMBERS)) {
                 handler = new RollerMemberHandler(req);                  
             } else {
                 throw new BadRequestException("ERROR: Unknown type: " + uri.getType());
             }
         } else {
             throw new BadRequestException("ERROR: Unknown URI type");
         }
    
         return handler;
    }
    
    public Handler(HttpServletRequest request) {
        this.request = request;
        this.uri = new URI(request);
        this.rollerContext = RollerContext.getRollerContext();
        this.roller = RollerFactory.getRoller();
        this.urlPrefix = getRollerContext().getAbsoluteContextUrl(getRequest()) + ENDPOINT;        
        
        // TODO: decide what to do about authentication, is WSSE going to fly?
        //Authenticator auth = new WSSEAuthenticator(request);
        Authenticator auth = new BasicAuthenticator(request);
        
        //
        // if this results in username not being set, then the client code
        // should understand that means failed authentication
        //
        
        if (auth.authenticate()) {
            try {
                UserData user = getRoller().getUserManager().getUser(auth.getUserId());
                if (user != null && user.hasRole("admin") && user.getEnabled().booleanValue()) {
                    // success!
                    getRoller().setUser(user);
                    username = auth.getUserId();
                } else {
                    logger.warn("WARN: Is not global admin user: " + user.getUserName());
                }
            } catch (RollerException re) {
                logger.error("ERROR: Could not create RollerAtomAdminHandler", re);
            }
        } else {
            logger.warn("WARN: Authentication failed");
        }
       
    }
    
    /**
     * Get the authenticated user name.
     * If this method returns null, then authentication has failed.
     */
    public String getUsername() {
        return username;
    }   
    
    /** Process an HTTP GET request. */
    public abstract EntrySet processGet() throws HandlerException;
    /** Process an HTTP POST request. */
    public abstract EntrySet processPost(Reader r) throws HandlerException;
    /** Process an HTTP PUT request. */
    public abstract EntrySet processPut(Reader r) throws HandlerException;
    /** Process an HTTP DELETE request. */
    public abstract EntrySet processDelete() throws HandlerException;
    
    protected URI getUri() {
        return uri;
    }

    protected HttpServletRequest getRequest() {
        return request;
    }

    protected RollerContext getRollerContext() {
        return rollerContext;
    }

    protected Roller getRoller() {
        return roller;
    }

    protected String getUrlPrefix() {
        return urlPrefix;
    }
}

