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
package org.apache.roller.weblogger.ui.struts2.core;

import net.oauth.OAuthConsumer;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.roller.weblogger.business.OAuthManager;
import org.apache.roller.weblogger.business.WebloggerFactory;
import org.apache.roller.weblogger.config.WebloggerRuntimeConfig;
import org.apache.roller.weblogger.pojos.User;
import org.apache.roller.weblogger.ui.struts2.util.UIAction;
import org.apache.struts2.interceptor.validation.SkipValidation;


/**
 * Allows user to view his/her OAuth consumer key and secret.
 */
public class OAuthKeys extends UIAction {
    private static Log log = LogFactory.getLog(OAuthKeys.class);
    private OAuthConsumer userConsumer;
    private OAuthConsumer siteWideConsumer;

    public OAuthKeys() {
        this.pageTitle = "oauthKeys.title";
    }
    
    
    // override default security, we do not require an action weblog
    public boolean isWeblogRequired() {
        return false;
    }


    @SkipValidation
    public String execute() {
        boolean flush = false;
        
        try {
            User ud = getAuthenticatedUser();
            OAuthManager omgr = WebloggerFactory.getWeblogger().getOAuthManager();
            userConsumer = omgr.getConsumerByUsername(ud.getUserName());
            if (userConsumer == null) {
                String consumerKey = DigestUtils.md5Hex(ud.getUserName());
                userConsumer = omgr.addConsumer(ud.getUserName(), consumerKey);
                flush = true;
            }

            if (isUserIsAdmin()) {
                siteWideConsumer = omgr.getConsumer();
                if (siteWideConsumer == null) {
                    String consumerKey = DigestUtils.md5Hex(
                        WebloggerRuntimeConfig.getAbsoluteContextURL());
                    siteWideConsumer = omgr.addConsumer(consumerKey);
                    flush = true;
                }
            }
            
            if (flush) {
                WebloggerFactory.getWeblogger().flush();
            }

        } catch (Exception ex) {
            log.error("ERROR creating or retrieving your OAuth information", ex);
        }

        return SUCCESS;
    }

    /**
     * @return the user's consumer
     */
    public OAuthConsumer getUserConsumer() {
        return userConsumer;
    }

    /**
     * @return the site's consumer
     */
    public OAuthConsumer getSiteWideConsumer() {
        return siteWideConsumer;
    }

    public String getRequestTokenURL() {
        return WebloggerFactory.getWeblogger().getUrlStrategy().getOAuthRequestTokenURL();
    }

    public String getAuthorizationURL() {
        return WebloggerFactory.getWeblogger().getUrlStrategy().getOAuthAuthorizationURL();
    }

    public String getAccessTokenURL() {
        return WebloggerFactory.getWeblogger().getUrlStrategy().getOAuthAccessTokenURL();
    }


}
