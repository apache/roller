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

package org.apache.roller.weblogger.ui.rendering.plugins.comments;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.URL;
import java.net.URLConnection;
import java.util.ResourceBundle;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.roller.weblogger.business.WebloggerFactory;
import org.apache.roller.weblogger.config.WebloggerConfig;
import org.apache.roller.weblogger.pojos.WeblogEntryComment;
import org.apache.roller.weblogger.util.RollerMessages;


/**
 * Check against Akismet service. Expects to a valid Akismet API key in the
 * Roller startup config property comment.validator.akismet.apikey.
 * You can get a free personal use key by registering as a user at wordpress.com.
 * See Akismet site for API details (http://akismet.com/development/api/)
 */
public class AkismetCommentValidator implements CommentValidator { 
    private static Log log = LogFactory.getLog(AkismetCommentValidator.class);    
    private ResourceBundle bundle = ResourceBundle.getBundle("ApplicationResources");
    private String apikey;
    
    /** Creates a new instance of AkismetCommentValidator */
    public AkismetCommentValidator() {
        apikey = WebloggerConfig.getProperty("comment.validator.akismet.apikey");
    }

    public String getName() {
        return bundle.getString("comment.validator.akismetName");
    }

    public int validate(WeblogEntryComment comment, RollerMessages messages) {
        StringBuffer sb = new StringBuffer();
        sb.append("blog=").append(
            WebloggerFactory.getWeblogger().getUrlStrategy().getWeblogURL(comment.getWeblogEntry().getWebsite(), null, true)).append("&");
        sb.append("user_ip="        ).append(comment.getRemoteHost()).append("&");
        sb.append("user_agent="     ).append(comment.getUserAgent()).append("&");
        sb.append("referrer="       ).append(comment.getReferrer()).append("&");
        sb.append("permalink="      ).append(comment.getWeblogEntry().getPermalink()).append("&");
        sb.append("comment_type="   ).append("comment").append("&");
        sb.append("comment_author=" ).append(comment.getName()).append("&");
        sb.append("comment_author_email=").append(comment.getEmail()).append("&");
        sb.append("comment_author_url="  ).append(comment.getUrl()).append("&");
        sb.append("comment_content="     ).append(comment.getContent());

        try {
            URL url = new URL("http://" + apikey + ".rest.akismet.com/1.1/comment-check");
            URLConnection conn = url.openConnection();
            conn.setDoOutput(true);

            conn.setRequestProperty("User_Agent", "Roller " + WebloggerFactory.getWeblogger().getVersion()); 
            conn.setRequestProperty("Content-type", "application/x-www-form-urlencoded;charset=utf8"); 
            conn.setRequestProperty("Content-length", Integer.toString(sb.length()));

            OutputStreamWriter osr = new OutputStreamWriter(conn.getOutputStream());
            osr.write(sb.toString(), 0, sb.length());
            osr.flush();
            osr.close();

            BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream())); 
            String response = br.readLine();
            if ("true".equals(response)) {
                messages.addError("comment.validator.akismetMessage");
                return 0;
            }
            else return 100;
        } catch (Exception e) {
            log.error("ERROR checking comment against Akismet", e);
        }
        return 0; // interpret error as spam: better safe than sorry? 
    }
}



