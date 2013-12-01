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

package org.apache.roller.weblogger.util;

import java.io.IOException;
import java.io.StringReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.roller.weblogger.WebloggerException;
import org.apache.roller.weblogger.config.WebloggerConfig;
import org.apache.roller.weblogger.pojos.WeblogEntry;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.input.SAXBuilder;


/**
 * Represents a trackback request.
 */
public class Trackback {
    
    private static final Log LOG = LogFactory.getLog(Trackback.class);
    
    private final WeblogEntry entry;
    private final String trackbackURL;
    
    
    public Trackback(WeblogEntry tEntry, String tURL)
            throws TrackbackNotAllowedException {
        
        // Make sure trackback to URL is allowed
        boolean allowTrackback = true;
        String allowedURLs = WebloggerConfig.getProperty("trackback.allowedURLs");
        if (!StringUtils.isEmpty(allowedURLs)) {
            // in the case that the administrator has enabled trackbacks
            // for only specific URLs, set it to false by default
            allowTrackback = false;
            String[] splitURLs = allowedURLs.split("\\|\\|");
            for (int i=0; i < splitURLs.length; i++) {
                Matcher m = Pattern.compile(splitURLs[i]).matcher(tURL);
                if (m.matches()) {
                    allowTrackback = true;
                    break;
                }
            }
        }
        
        if(!allowTrackback) {
            throw new TrackbackNotAllowedException(tURL);
        } else {
            // test url
            try {
                new URL(tURL);
            } catch(MalformedURLException ex) {
                // bad url
                throw new IllegalArgumentException("bad url: "+tURL);
            }
            
            entry = tEntry;
            trackbackURL = tURL;
        }
        
    }
    
    
    /**
     * Sends trackback from entry to remote URL.
     * See Trackback spec for details: http://www.sixapart.com/pronet/docs/trackback_spec
     */
    public RollerMessages send() throws WebloggerException {
        
        RollerMessages messages = new RollerMessages();
        
        LOG.debug("Sending trackback to url - " + trackbackURL);
        
        // Construct data
        String title = entry.getTitle();
        String excerpt = StringUtils.left( Utilities.removeHTML(entry.getDisplayContent()),255 );
        String url = entry.getPermalink();
        String blog_name = entry.getWebsite().getName();
        
        // build trackback post parameters as query string
        Map params = new HashMap();
        params.put("title", URLUtilities.encode(title));
        params.put("excerpt", URLUtilities.encode(excerpt));
        params.put("url", URLUtilities.encode(url));
        params.put("blog_name", URLUtilities.encode(blog_name));
        String queryString = URLUtilities.getQueryString(params);
        
        LOG.debug("query string - " + queryString);
        
        // prepare http request
        HttpClient client = new HttpClient();
        client.setConnectionTimeout(45 * 1000);
        HttpMethod method = new PostMethod(trackbackURL);
        method.setQueryString(queryString);
        
        try {
            // execute trackback
            int statusCode = client.executeMethod(method);
            
            // read response
            byte[] response = method.getResponseBody();
            String responseString = Utilities.escapeHTML(new String(response, "UTF-8"));
            
            LOG.debug("result = " + statusCode + " " + method.getStatusText());
            LOG.debug("response:\n" + responseString);
            
            if(statusCode == HttpStatus.SC_OK) {
                // trackback request succeeded, message will give details
                try {
                    messages = parseTrackbackResponse(new String(response, "UTF-8"), messages);
                } catch (Exception e) {
                    // Cannot parse response, indicates failure
                    messages.addError("weblogEdit.trackbackErrorParsing", responseString);
                }
            } else if(statusCode == HttpStatus.SC_NOT_FOUND) {
                // 404, invalid trackback url
                messages.addError("weblogEdit.trackbackError404");
            } else {
                // some other kind of error with url, like 500, 403, etc
                // just provide a generic error message and give the http response text
                messages.addError("weblogEdit.trackbackErrorResponse",
                        new String[] {""+statusCode, method.getStatusText()});
            }
            
        } catch (IOException e) {
            // some kind of transport error sending trackback post
            LOG.debug("Error sending trackback", e);
            messages.addError("weblogEdit.trackbackErrorTransport");
        } finally {
            // release used connection
            method.releaseConnection();
        }
        
        return messages;
    }
    
    
    /**
     * Parse XML returned from trackback POST, returns error or success message
     * in RollerMessages object.
     */
    private RollerMessages parseTrackbackResponse(String response, RollerMessages messages) 
            throws JDOMException, IOException {
        
        SAXBuilder builder = new SAXBuilder();
        Document doc = builder.build(
                new StringReader(StringEscapeUtils.unescapeHtml(response)));
        Element root = doc.getRootElement();
        
        if ("response".equals(root.getName())) {
            int code = -99;
            try {
                code = Integer.parseInt(root.getChildText("error"));
            } catch (NumberFormatException ignoredByDesign) {}
            
            String message = root.getChildText("message");
            if (code != 0) {
                messages.addError("weblogEdit.trackbackFailure", Utilities.removeHTML(message));
            } else {
                messages.addMessage("weblogEdit.trackbackSuccess");
            }
        } else {
            messages.addError("weblogEdit.trackbackErrorParsing", Utilities.removeHTML(response));
        }
        
        return messages;
    }
    
}
