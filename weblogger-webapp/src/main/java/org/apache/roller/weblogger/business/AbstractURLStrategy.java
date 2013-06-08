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

package org.apache.roller.weblogger.business;

import java.util.HashMap;
import java.util.Map;
import org.apache.roller.weblogger.config.WebloggerRuntimeConfig;
import org.apache.roller.weblogger.util.URLUtilities;


/**
 * An abstract Weblogger URLStrategy which implements some of the url methods
 * which are not likely to change for any alternate url strategies.
 */
public abstract class AbstractURLStrategy implements URLStrategy {
    
    public AbstractURLStrategy() {}
    
    
    /**
     * Url to login page.
     */
    public String getLoginURL(boolean absolute) {
        
        StringBuffer url = new StringBuffer();
        
        if(absolute) {
            url.append(WebloggerRuntimeConfig.getAbsoluteContextURL());
        } else {
            url.append(WebloggerRuntimeConfig.getRelativeContextURL());
        }
        
        url.append("/roller-ui/login-redirect.rol");
        
        return url.toString();
    }
    
    
    /**
     * Url to logout page.
     */
    public String getLogoutURL(boolean absolute) {
        
        StringBuffer url = new StringBuffer();
        
        if(absolute) {
            url.append(WebloggerRuntimeConfig.getAbsoluteContextURL());
        } else {
            url.append(WebloggerRuntimeConfig.getRelativeContextURL());
        }
        
        url.append("/roller-ui/logout.rol");
        
        return url.toString();
    }
    
    
    /**
     * Get a url to a UI action in a given namespace, optionally specifying
     * a weblogHandle parameter if that is needed by the action.
     */
    public String getActionURL(String action,
                                            String namespace,
                                            String weblogHandle,
                                            Map<String, String> parameters,
                                            boolean absolute) {
        
        StringBuffer url = new StringBuffer();
        
        if(absolute) {
            url.append(WebloggerRuntimeConfig.getAbsoluteContextURL());
        } else {
            url.append(WebloggerRuntimeConfig.getRelativeContextURL());
        }
        
        url.append(namespace);
        url.append("/").append(action).append(".rol");
        
        // put weblog handle parameter, if necessary
        Map<String, String> params = new HashMap();
        if(weblogHandle != null) {
            params.put("weblog", weblogHandle);
        }
        
        // add custom parameters if they exist
        if(parameters != null) {
            params.putAll(parameters);
        }
        
        if(!params.isEmpty()) {
            return url.toString() + URLUtilities.getQueryString(params);
        } else {
            return url.toString();
        }
    }
    
    
    /**
     * Get a url to add a new weblog entry.
     */
    public String getEntryAddURL(String weblogHandle,
                                              boolean absolute) {
        
        StringBuffer url = new StringBuffer();
        
        if(absolute) {
            url.append(WebloggerRuntimeConfig.getAbsoluteContextURL());
        } else {
            url.append(WebloggerRuntimeConfig.getRelativeContextURL());
        }
        
        url.append("/roller-ui/authoring/entryAdd.rol");
        
        Map params = new HashMap();
        params.put("weblog", weblogHandle);
        
        return url.toString() + URLUtilities.getQueryString(params);
    }
    
    
    /**
     * Get a url to edit a specific weblog entry.
     */
    public String getEntryEditURL(String weblogHandle,
                                               String entryId,
                                               boolean absolute) {
        
        StringBuffer url = new StringBuffer();
        
        if(absolute) {
            url.append(WebloggerRuntimeConfig.getAbsoluteContextURL());
        } else {
            url.append(WebloggerRuntimeConfig.getRelativeContextURL());
        }
        
        url.append("/roller-ui/authoring/entryEdit.rol");
        
        Map params = new HashMap();
        params.put("weblog", weblogHandle);
        params.put("bean.id", entryId);
        
        return url.toString() + URLUtilities.getQueryString(params);
    }
    
    
    /**
     * Get a url to weblog config page.
     */
    public String getWeblogConfigURL(String weblogHandle,
                                                  boolean absolute) {
        
        StringBuffer url = new StringBuffer();
        
        if(absolute) {
            url.append(WebloggerRuntimeConfig.getAbsoluteContextURL());
        } else {
            url.append(WebloggerRuntimeConfig.getRelativeContextURL());
        }
        
        url.append("/roller-ui/authoring/weblogConfig.rol");
        
        Map params = new HashMap();
        params.put("weblog", weblogHandle);
        
        return url.toString() + URLUtilities.getQueryString(params);
    }
    
    
    public String getXmlrpcURL(boolean absolute) {
        
        StringBuffer url = new StringBuffer();
        
        if(absolute) {
            url.append(WebloggerRuntimeConfig.getAbsoluteContextURL());
        } else {
            url.append(WebloggerRuntimeConfig.getRelativeContextURL());
        }
        
        url.append("/roller-services/xmlrpc");
        
        return url.toString();
    }
    
    
    public String getAtomProtocolURL(boolean absolute) {
        
        StringBuffer url = new StringBuffer();
        
        if(absolute) {
            url.append(WebloggerRuntimeConfig.getAbsoluteContextURL());
        } else {
            url.append(WebloggerRuntimeConfig.getRelativeContextURL());
        }
        
        url.append("/roller-services/app");
        
        return url.toString();
    }
    
}
