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

package org.apache.roller.business;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.StringReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.roller.RollerException;
import org.apache.roller.config.RollerConfig;
import org.apache.roller.pojos.WeblogEntryData;
import org.apache.roller.util.RollerMessages;
import org.apache.roller.util.RollerMessages.RollerMessage;
import org.apache.roller.util.Utilities;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;

/**
 * Persistence independent WeblogManager methods.
 */
public abstract class WeblogManagerImpl implements WeblogManager {
    
    private static Log mLogger =
            LogFactory.getFactory().getInstance(WeblogManagerImpl.class);
    
    public RollerMessages sendTrackback(
            WeblogEntryData entry, String trackbackURL) throws RollerException {
        
        RollerMessages messages = new RollerMessages();
        
        // Make sure trackback to URL is alled
        boolean allowTrackback = true;
        String allowedURLs = RollerConfig.getProperty("trackback.allowedURLs");
        if (allowedURLs != null && allowedURLs.trim().length() > 0) {
            // in the case that the administrator has enabled trackbacks
            // for only specific URLs, set it to false by default
            allowTrackback = false;
            String[] splitURLs = allowedURLs.split("\\|\\|");
            for (int i=0; i<splitURLs.length; i++) {
                Matcher m = Pattern.compile(splitURLs[i]).matcher(trackbackURL);
                if (m.matches()) {
                    allowTrackback = true;
                    break;
                }
            }
        }
        if (!allowTrackback) {
             messages.addError("error.trackbackNotAllowed");             
        } else try {
            // Construct data
            String content = entry.getDisplayContent();
            String title = entry.getTitle();
            String excerpt = StringUtils.left( Utilities.removeHTML(content),255 );

            String url = entry.getPermalink();
            String blog_name = entry.getWebsite().getName();

            String data = URLEncoder.encode("title", "UTF-8")
            +"="+URLEncoder.encode(title, "UTF-8");

            data += ("&" + URLEncoder.encode("excerpt", "UTF-8")
            +"="+URLEncoder.encode(excerpt,"UTF-8"));

            data += ("&" + URLEncoder.encode("url", "UTF-8")
            +"="+URLEncoder.encode(url,"UTF-8"));

            data += ("&" + URLEncoder.encode("blog_name", "UTF-8")
            +"="+URLEncoder.encode(blog_name,"UTF-8"));

            // Send data
            URL tburl = new URL(trackbackURL);
            HttpURLConnection conn = (HttpURLConnection)tburl.openConnection();
            conn.setDoOutput(true);

            OutputStreamWriter wr =
                    new OutputStreamWriter(conn.getOutputStream());
            BufferedReader rd = null;
            try {
                wr.write(data);
                wr.flush();

                // Get the response
                boolean inputAvailable = false;
                try {
                    rd = new BufferedReader(new InputStreamReader(
                            conn.getInputStream()));
                    inputAvailable = true;
                } catch (Throwable e) {
                    mLogger.debug(e);
                }

                // read repsonse only if there is one
                if (inputAvailable) {
                    String line;
                    StringBuffer resultBuff = new StringBuffer();
                    while ((line = rd.readLine()) != null) {
                        resultBuff.append(
                            StringEscapeUtils.escapeHtml(line));
                    }
                    try {
                        messages = parseTrackbackResponse(resultBuff.toString(), messages);
                    } catch (Exception e) {
                        messages.addError("weblogEdit.trackbackUnknownError",                            
                           new String[] {Integer.toString(conn.getResponseCode()), conn.getResponseMessage()});
                    }
                } else {
                    messages.addError("weblogEdit.trackbackUnknownError",                            
                       new String[] {Integer.toString(conn.getResponseCode()), conn.getResponseMessage()});                    
                }
            } finally {
                if (wr != null) wr.close();
                if (rd != null) rd.close();
            }
        } catch (IOException e) {
            messages.addError("error.trackback");
            mLogger.error("ERROR: sending trackback", e);
        } 
        return messages;
    }
    
    private RollerMessages parseTrackbackResponse(
        String response, RollerMessages messages) throws JDOMException, IOException {
        
        SAXBuilder builder = new SAXBuilder();
        Document doc = builder.build( 
            new StringReader(StringEscapeUtils.unescapeHtml(response))); 
        Element root = doc.getRootElement();
        if ("response".equals(root.getName())) {
            int code = -99;
            boolean failure = true;
            try {
                code = Integer.parseInt(root.getChildText("error"));
                failure = code != 0;
            } catch (NumberFormatException ignoredByDesign) {}
            String message = root.getChildText("message");
            if (failure) {
                messages.addError("weblogEdit.trackbackStatusCodeBad", 
                    new String[] {Integer.toString(code), Utilities.removeHTML(message)});
            } else {
                messages.addMessage("weblogEdit.trackbackStatusCodeGood", 
                    Utilities.removeHTML(message));
            }
        }
        return messages;
    }
}
