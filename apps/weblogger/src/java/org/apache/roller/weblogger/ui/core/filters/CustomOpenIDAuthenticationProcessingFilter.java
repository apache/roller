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

package org.apache.roller.weblogger.ui.core.filters;

import javax.servlet.Filter;
import javax.servlet.http.HttpServletRequest;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.security.Authentication;
import org.springframework.security.AuthenticationException;
import org.springframework.security.providers.openid.OpenIDAuthenticationToken;
import org.springframework.security.ui.openid.OpenIDAuthenticationProcessingFilter;
import org.springframework.security.userdetails.UsernameNotFoundException;
import org.springframework.security.ui.openid.OpenIDConsumer;
//import org.springframework.security.userdetails.openid.OpenIDUserAttribute;


/**
 * Handle response from OpenID provider.
 * @author Tatyana Tokareva
 */
public class CustomOpenIDAuthenticationProcessingFilter 
        extends OpenIDAuthenticationProcessingFilter implements Filter {

    private OpenIDConsumer consumer;
    private String claimedIdentityFieldName = DEFAULT_CLAIMED_IDENTITY_FIELD;
    private static Log log = LogFactory.getLog(CustomOpenIDAuthenticationProcessingFilter.class);

    @Override
    public Authentication attemptAuthentication(HttpServletRequest req) throws AuthenticationException {
        OpenIDAuthenticationToken auth = null;

        // Processing standard OpenId user authentication    
        auth = (OpenIDAuthenticationToken) super.attemptAuthentication(req);

        if (auth.getAuthorities()[0].getAuthority().equals("openidLogin")) {

            /* TODO: when Spring Security 2.1 is released, we can uncomment 
             * this code, which will allow us to pre-populate the new user 
             * registration form with information from the OpenID Provider.
             * 
            Collection<OpenIDUserAttribute> sREGAttributesList = auth.getAttributes();
            OpenIDUserAttribute openidName = new OpenIDUserAttribute(
                OpenIDUserAttribute.Attributes.openidname.toString(), "");
            openidName.setValue(auth.getIdentityUrl());
            sREGAttributesList.add(openidName);
            
            // TODO: find a better place to stash attributes
            UserManager mgr = WebloggerFactory.getWeblogger().getUserManager();            
            mgr.userAttributes.put(
                UserAttribute.Attributes.openidUrl.toString(),
                sREGAttributesList);
            */
            
            // Username not found in Roller for this user, so throw exception
            // which will route user to the new user registration page.
            throw new UsernameNotFoundException("ERROR no user: new openid user");
        }
        return auth;
    }
}
