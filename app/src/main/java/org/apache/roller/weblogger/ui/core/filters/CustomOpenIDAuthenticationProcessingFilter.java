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

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

import javax.servlet.Filter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.openid.OpenIDAuthenticationToken;
import org.springframework.security.openid.OpenIDAuthenticationFilter;
import org.springframework.security.openid.OpenIDConsumer;
//import org.springframework.security.userdetails.openid.OpenIDUserAttribute;


/**
 * Handle response from OpenID provider.
 * @author Tatyana Tokareva
 */
public class CustomOpenIDAuthenticationProcessingFilter 
        extends OpenIDAuthenticationFilter implements Filter {

    private OpenIDConsumer consumer;
    private String claimedIdentityFieldName = DEFAULT_CLAIMED_IDENTITY_FIELD;
    private static Log log = LogFactory.getLog(CustomOpenIDAuthenticationProcessingFilter.class);

    /**
     * @throws org.springframework.security.core.AuthenticationException
     */
    @Override
    public Authentication attemptAuthentication(HttpServletRequest req, HttpServletResponse rsp) throws IOException {
        OpenIDAuthenticationToken auth = null;

        // Processing standard OpenId user authentication    
        auth = (OpenIDAuthenticationToken) super.attemptAuthentication(req, rsp);

        // auth will be null on the first pass of super.attemptAuthentication()
        if (auth != null) {
            GrantedAuthority ga = (GrantedAuthority) auth.getAuthorities().toArray()[0];

            if (ga.getAuthority().equals("rollerOpenidLogin")) {

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

            }
        }
        return auth;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected String lookupRealm(String returnToUrl) {

        String mapping = super.lookupRealm(returnToUrl);

        if (mapping == null) {
            try {
                URL url = new URL(returnToUrl);
                int port = url.getPort();

                StringBuilder realmBuffer = new StringBuilder(returnToUrl.length())
                        .append(url.getProtocol())
                        .append("://")
                        .append(url.getHost());
                if (port != -1) {
                    realmBuffer.append(":").append(port);
                }
                realmBuffer.append("/");
                mapping = realmBuffer.toString();
            } catch (MalformedURLException e) {
                log.warn("returnToUrl was not a valid URL: [" + returnToUrl + "]", e);
            }
        }

        return mapping;
    }
}
