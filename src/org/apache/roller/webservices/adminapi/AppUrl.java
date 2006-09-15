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

import java.net.MalformedURLException;
import java.net.URL;
import java.util.regex.Pattern;
import java.util.regex.Matcher;
/**
 * This class generates Atom Publishing Protocol (APP) URls.
 */
public class AppUrl {
    private static final String ENDPOINT = "/app";
    private static Pattern ID_PATTERN = Pattern.compile("^http://.*/(.*)/(?:entries|resources)$");
    private static Pattern ENDPOINT_PATTERN = Pattern.compile("^(http://.*)/.*/(?:entries|resources)$");
    
    private URL entryUrl;
    private URL resourceUrl;
    private String handle;
    
    public AppUrl(String urlPrefix, String handle) throws MalformedURLException {
        //TODO: is this the right thing to do? hardcode roller-services?
        entryUrl = new URL(urlPrefix + "/roller-services" + ENDPOINT + "/" + handle + "/entries");
        resourceUrl = new URL(urlPrefix + "/roller-services" + ENDPOINT + "/" + handle + "/resources");        
    }    

    public AppUrl(URL url) throws MalformedURLException {
        handle = parseHandle(url);
        URL endpoint = parseEndpoint(url);
        
        entryUrl = new URL(endpoint + "/" + handle + "/entries");
        resourceUrl = new URL(endpoint + "/" + handle + "/resources");        
    }    
    
    private String parseHandle(URL url) {
        String urlString = url.toString();
        String handle = null;
        
        Matcher m = ID_PATTERN.matcher(urlString);
        
        if (m.matches()) {
            handle = m.group(1);
        }
        
        return handle;
    }
    
    private URL parseEndpoint(URL url) throws MalformedURLException {
        String urlString = url.toString();
        String endpointString = null;
        
        Matcher m = ENDPOINT_PATTERN.matcher(urlString);
        
        if (m.matches()) {
            endpointString = m.group(1);
        }
        
        URL endpoint = null;
        if (endpointString != null) {
            endpoint = new URL(endpointString);
        }
        
        return endpoint;
    }
    
    
    public URL getEntryUrl() {
        return entryUrl;
    }

    public URL getResourceUrl() {
        return resourceUrl;
    }
    
    public String getHandle() {
        return handle;
    }
}
