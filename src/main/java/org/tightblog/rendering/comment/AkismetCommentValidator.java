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
package org.tightblog.rendering.comment;

import org.tightblog.business.URLStrategy;
import org.tightblog.pojos.WeblogEntry;
import org.tightblog.pojos.WeblogEntryComment;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.URL;
import java.net.URLConnection;
import java.util.List;
import java.util.Map;

/**
 * Check against Akismet service. Expects to a valid Akismet API key in the
 * Spring configuration ("apiKey") property.
 * You can get a free personal use key by registering as a user at wordpress.com.
 * See Akismet site for API details (https://akismet.com/development/api/#comment-check)
 * <p>
 * Per the Akismet docs, to test for non-blatant spam, use a commenter name of "viagra-test-123".
 * <p>
 * To test for blatant spam, set test_discard=1 and is_test=1 to the request created
 * in the validate() method below.
 * <p>
 * Before using Akismet, good to verify your apiKey works using cURL or similar tool:
 * curl --data "key=...your key...&blog=http://www.myblogurl.com/blog/" https://rest.akismet.com/1.1/verify-key
 * Returns "valid" if good, "invalid" otherwise.
 */
public class AkismetCommentValidator implements CommentValidator {
    private static Logger log = LoggerFactory.getLogger(AkismetCommentValidator.class);
    private String apiKey;

    // All spam is marked as spam and sent to the moderation queue (even if
    // moderation turned off.)
    //
    // Akismet identifies some spam as "blatant" spam.  By configuring to true,
    // administrator may choose to have such super-spam automatically deleted
    // without ever appearing in the moderation queue.
    // see: https://blog.akismet.com/2014/04/23/theres-a-ninja-in-your-akismet/
    private boolean deleteBlatantSpam;

    public void setDeleteBlatantSpam(boolean deleteBlatantSpam) {
        this.deleteBlatantSpam = deleteBlatantSpam;
    }

    private URLStrategy urlStrategy;

    private AkismetCaller akismetCaller;

    /**
     * Creates a new instance of AkismetCommentValidator
     */
    public AkismetCommentValidator(URLStrategy urlStrategy, String apiKey) {
        this.urlStrategy = urlStrategy;
        this.apiKey = apiKey;
        this.akismetCaller = new AkismetCaller();
    }

    void setAkismetCaller(AkismetCaller akismetCaller) {
        this.akismetCaller = akismetCaller;
    }

    String createAPIRequestBody(WeblogEntryComment comment) {
        WeblogEntry entry = comment.getWeblogEntry();

        String apiCall = "blog=" + urlStrategy.getWeblogURL(entry.getWeblog());
        apiCall += "&user_ip=" + comment.getRemoteHost();
        apiCall += "&user_agent=" + comment.getUserAgent();
        apiCall += "&referrer=" + comment.getReferrer();
        apiCall += "&permalink=" + urlStrategy.getWeblogEntryURL(entry);
        apiCall += "&comment_type=comment&comment_author=" + comment.getName();
        apiCall += "&comment_author_email=" + comment.getEmail();
        apiCall += "&comment_author_url=" + comment.getUrl();
        apiCall += "&comment_content=" + comment.getContent();
        return apiCall;
    }

    static class AkismetCaller {

        ValidationResult makeAkismetCall(String apiKey, String apiRequestBody) throws IOException {
            URL url = new URL("http://" + apiKey + ".rest.akismet.com/1.1/comment-check");
            URLConnection conn = url.openConnection();
            conn.setDoOutput(true);

            conn.setRequestProperty("User_Agent", "TightBlog");
            conn.setRequestProperty("Content-type", "application/x-www-form-urlencoded;charset=utf8");
            conn.setRequestProperty("Content-length", Integer.toString(apiRequestBody.length()));

            OutputStreamWriter osr = new OutputStreamWriter(conn.getOutputStream());
            osr.write(apiRequestBody, 0, apiRequestBody.length());
            osr.flush();
            osr.close();

            BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            String response = br.readLine();
            if ("true".equals(response)) {
                if ("discard".equalsIgnoreCase(conn.getHeaderField("X-akismet-pro-tip"))) {
                    return ValidationResult.BLATANT_SPAM;
                }
                return ValidationResult.SPAM;
            } else {
                return ValidationResult.NOT_SPAM;
            }
        }
    }

    @Override
    public ValidationResult validate(WeblogEntryComment comment, Map<String, List<String>> messages) {

        String apiRequestBody = createAPIRequestBody(comment);

        try {
            ValidationResult response = akismetCaller.makeAkismetCall(apiKey, apiRequestBody);
            if (ValidationResult.BLATANT_SPAM.equals(response) && !deleteBlatantSpam) {
                // with no autodelete, downgrade blatant spam to spam
                messages.put("comment.validator.akismetMessage.blatantNoDelete", null);
                return ValidationResult.SPAM;
            } else if (ValidationResult.SPAM.equals(response)) {
                messages.put("comment.validator.akismetMessage.spam", null);
            }
            return response;
        } catch (Exception e) {
            log.error("ERROR checking comment against Akismet", e);
            messages.put("comment.validator.akismetMessage.error", null);
            // interpreting error as spam, better safe than sorry.
            return ValidationResult.SPAM;
        }
    }
}
