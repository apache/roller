/*
 * WeblogRequest.java
 *
 * Created on November 7, 2005, 12:29 PM
 */

package org.roller.presentation;

import java.util.Locale;
import javax.servlet.http.HttpServletRequest;


/**
 * An abstract class representing any request made to Roller that has been
 * parsed in order to extract relevant pieces of information from the url.
 *
 * NOTE: It is extremely important to mention that this class and all of its
 * subclasses are meant to be extremely light weight.  Meaning they should
 * avoid any time consuming operations at all costs, especially operations
 * which require a trip to the db.  Those operations should be used very, very
 * sparingly and should only be triggered when it's guaranteed that they are
 * needed.
 *
 * @author Allen Gilliland
 */
public abstract class ParsedRequest {
    
    HttpServletRequest request = null;
    
    private String language = null;
    private String authenticUser = null;
    
    
    ParsedRequest() {}
    
    
    /**
     * Parse the given http request and extract any information we can.
     *
     * This abstract version of the constructor gathers info likely to be
     * relevant to all requests to Roller.
     */
    public ParsedRequest(HttpServletRequest request) throws Exception {
        
        // keep a reference to the original request
        this.request = request;
        
        // login status
        java.security.Principal prince = request.getUserPrincipal();
        if(prince != null) {
            this.authenticUser = prince.getName();
        }
        
    }
    
    
    public String getLanguage() {
        if(this.language == null) {
            // determine language
            // this operation can possibly require a trip to the db, so we only
            // do it when we know our user needs to know the language
            Locale locale = LanguageUtil.getViewLocale(request);
            this.language = locale.getLanguage();
        }

        return this.language;
    }
    
    
    public String getAuthenticUser() {
        return this.authenticUser;
    }
    
    
    public boolean isLoggedIn() {
        return (this.authenticUser != null);
    }

    
    public HttpServletRequest getRequest() {
        return this.request;
    }
    
}
