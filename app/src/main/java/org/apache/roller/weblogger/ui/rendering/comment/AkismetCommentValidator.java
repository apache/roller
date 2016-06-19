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
 *
 * Source file modified from the original ASF source; all changes made
 * are also under Apache License.
 */
package org.apache.roller.weblogger.ui.rendering.comment;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.URL;
import java.net.URLConnection;
import org.apache.roller.weblogger.WebloggerCommon;
import org.apache.roller.weblogger.business.URLStrategy;
import org.apache.roller.weblogger.business.WebloggerStaticConfig;
import org.apache.roller.weblogger.pojos.WeblogEntryComment;
import org.apache.roller.weblogger.util.RollerMessages;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Check against Akismet service. Expects to a valid Akismet API key in the
 * Roller startup config property comment.validator.akismet.apikey.
 * You can get a free personal use key by registering as a user at wordpress.com.
 * See Akismet site for API details (http://akismet.com/development/api/)
 */
public class AkismetCommentValidator implements CommentValidator {
    private static Logger log = LoggerFactory.getLogger(AkismetCommentValidator.class);
    private String apikey;

    private URLStrategy urlStrategy;

    public void setUrlStrategy(URLStrategy urlStrategy) {
        this.urlStrategy = urlStrategy;
    }

    /** Creates a new instance of AkismetCommentValidator */
    public AkismetCommentValidator(String apikey) {
        this.apikey = apikey;
    }

    public String getName() {
        return "Akismet Comment Validator";
    }

    public int validate(WeblogEntryComment comment, RollerMessages messages) {
        StringBuilder sb = new StringBuilder();
        sb.append("blog=").append(
            urlStrategy.getWeblogURL(comment.getWeblogEntry().getWeblog(), true)).append("&");
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

            conn.setRequestProperty("User_Agent", "TightBlog " + WebloggerStaticConfig.getProperty("weblogger.version", "Unknown"));
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
            else {
                return WebloggerCommon.PERCENT_100;
            }
        } catch (Exception e) {
            log.error("ERROR checking comment against Akismet", e);
        }
        // interpreting error as spam: better safe than sorry?
        return 0;
    }
}