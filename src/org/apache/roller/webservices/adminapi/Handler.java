/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 *  contributor license agreements.  The ASF licenses this file to You
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
package org.apache.roller.webservices.adminapi;

import java.io.IOException;
import java.io.Reader;
import javax.servlet.http.HttpServletRequest;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.roller.config.RollerConfig;
import org.apache.roller.model.Roller;
import org.apache.roller.model.RollerFactory;
import org.apache.roller.ui.core.RollerContext;
import org.apache.roller.webservices.adminapi.sdk.EntrySet;

import java.util.regex.Pattern;
import java.util.regex.Matcher;
import org.apache.roller.config.RollerRuntimeConfig;
import org.apache.roller.webservices.adminapi.sdk.MissingElementException;
import org.apache.roller.webservices.adminapi.sdk.UnexpectedRootElementException;
import org.jdom.Document;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;

/**
 * This class is the abstract notion of an AAPP request handler.
 * It processes HTTP requests for each of the four HTTP verbs:
 * GET, POST, PUT, DELETE, for a given weblog resource.
 */
abstract class Handler {
    protected static final String ENDPOINT = "/aapp";
    
    static class URI {
        private static Pattern PATHINFO_PATTERN = Pattern.compile("^/(users|weblogs|members)(?:/(.*))?$");
        
        private String type;
        private String entryId;
        
        public URI(HttpServletRequest request) throws BadRequestException {
            String pi = request.getPathInfo();
            
            if (pi == null || pi.length() == 0) {
                type = null;
                entryId = null;
            } else {
                Matcher m = PATHINFO_PATTERN.matcher(pi);
                if (!m.matches()) {
                    throw new BadRequestException("ERROR: Invalid path info: " + pi);
                }
                
                type = m.group(1);
                entryId = m.group(2);
            }
        }
        
        public String getType() {
            return type;
        }
        
        public String getEntryId() {
            return entryId;
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
    private String userName;
    private URI uri;
    private String urlPrefix;
    
    /** Get a Handler object implementation based on the given request. */
    public static Handler getHandler(HttpServletRequest req) throws HandlerException {
        boolean enabled = RollerConfig.getBooleanProperty("webservices.adminprotocol.enabled");
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
    
    public Handler(HttpServletRequest request) throws HandlerException {
        this.request = request;
        this.uri = new URI(request);
        this.rollerContext = RollerContext.getRollerContext();
        this.roller = RollerFactory.getRoller();
        //TODO: is this the right thing to do? hardcode roller-services?
        this.urlPrefix = RollerRuntimeConfig.getAbsoluteContextURL() + "/roller-services" + ENDPOINT;
        
        // TODO: decide what to do about authentication, is WSSE going to fly?
        //Authenticator auth = new WSSEAuthenticator(request);
        Authenticator auth = new BasicAuthenticator(request);
        auth.authenticate();
        setUserName(auth.getUserName());
    }
    
    /**
     * Get the authenticated user name.
     * If this method returns null, then authentication has failed.
     */
    public String getUserName() {
        return userName;
    }
    
    private void setUserName(String userName) {
        this.userName = userName;
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
    
    protected abstract EntrySet getEntrySet(Document d) throws MissingElementException, UnexpectedRootElementException;
    
    protected EntrySet getEntrySet(Reader r) throws HandlerException {
        try {
            SAXBuilder builder = new SAXBuilder();
            Document d = builder.build(r);
            EntrySet c = getEntrySet(d);
            
            return c;
        } catch (UnexpectedRootElementException ure) {
            throw new BadRequestException("ERROR: Failed to parse content", ure);            
        } catch (MissingElementException mee) {
            throw new BadRequestException("ERROR: Failed to parse content", mee);            
        } catch (JDOMException jde) {
            throw new BadRequestException("ERROR: Failed to parse content", jde);
        } catch (IOException ioe) {
            throw new InternalException("ERROR: Failed to parse content", ioe);
        }
    }    
}

