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

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.tightblog.pojos.WeblogEntryComment;
import org.tightblog.pojos.WebloggerProperties;
import org.tightblog.repository.WebloggerPropertiesRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Validates comment if comment does not contain blacklisted words.
 */
@Component
public class BlacklistCommentValidator implements CommentValidator {

    private static Logger log = LoggerFactory.getLogger(BlacklistCommentValidator.class);

    private WebloggerPropertiesRepository webloggerPropertiesRepository;

    @Autowired
    public BlacklistCommentValidator(WebloggerPropertiesRepository webloggerPropertiesRepository) {
        this.webloggerPropertiesRepository = webloggerPropertiesRepository;
    }

    private List<Pattern> globalRegexRules = new ArrayList<>();

    // ensures site-wide rules have been retrieved
    private boolean globalRulesLoaded;

    /**
     * Notify this validator that the site-wide filter has possibly changed
     * @param globalCommentFilter new filter to use
     */
    public void setGlobalCommentFilter(String globalCommentFilter) {
        globalRegexRules = populateSpamRules(globalCommentFilter);
        globalRulesLoaded = true;
    }

    /**
     * Test comment, applying weblog and site blacklists (if available)
     * @return True if comment matches a blacklist term
     */
    @Override
    public ValidationResult validate(WeblogEntryComment comment, Map<String, List<String>> messages) {
        if (!globalRulesLoaded) {
            WebloggerProperties props = webloggerPropertiesRepository.findOrNull();
            setGlobalCommentFilter(props.getCommentSpamFilter());
        }

        List<Pattern> combinedRules = new ArrayList<>();
        combinedRules.addAll(globalRegexRules);
        combinedRules.addAll(comment.getWeblogEntry().getWeblog().getBlacklistRegexRules());

        if (isBlacklisted(combinedRules, comment.getUrl()) || isBlacklisted(combinedRules, comment.getEmail()) ||
                isBlacklisted(combinedRules, comment.getName()) || isBlacklisted(combinedRules, comment.getContent())) {
            messages.put("comment.validator.blacklistMessage", null);
            return ValidationResult.SPAM;
        }
        return ValidationResult.NOT_SPAM;
    }

    private boolean isBlacklisted(List<Pattern> combinedRules, String textToCheck) {
        if (!StringUtils.isEmpty(textToCheck)) {
            for (Pattern testPattern : combinedRules) {
                Matcher matcher = testPattern.matcher(textToCheck);
                if (matcher.find()) {
                    if (log.isDebugEnabled()) {
                        log.debug("{} matched by {}", matcher.group(), testPattern.pattern());
                    }
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Create a list of regex Pattern elements from a line-delimited list
     * @param blacklist String of regex rules, one per line delimited by \n
     **/
    public static List<Pattern> populateSpamRules(String blacklist) {
        List<Pattern> regexRules = new ArrayList<>();

        if (blacklist != null) {
            StringTokenizer tokenizer = new StringTokenizer(blacklist, "\n");

            while (tokenizer.hasMoreTokens()) {
                String token = tokenizer.nextToken().trim();
                if (token.startsWith("#")) {
                    continue;
                }
                regexRules.add(Pattern.compile(token, Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE));
            }
        }

        return regexRules;
    }
}
