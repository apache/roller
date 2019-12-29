/*
   Copyright 2019 Glen Mazza

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
*/
package org.tightblog.rendering.service;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.tightblog.dao.WebloggerPropertiesDao;
import org.tightblog.domain.WeblogEntry;
import org.tightblog.domain.WeblogEntryComment;
import org.tightblog.domain.WeblogEntryComment.SpamCheckResult;
import org.tightblog.domain.WebloggerProperties;
import org.tightblog.service.URLService;

import java.net.URI;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class CommentSpamChecker {

    private static Logger log = LoggerFactory.getLogger(CommentSpamChecker.class);
    private static final Pattern LINK_PATTERN = Pattern.compile("<a\\s*href\\s*=");

    private URLService urlService;
    private WebloggerPropertiesDao webloggerPropertiesDao;
    private List<Pattern> globalBlacklistRules = new ArrayList<>();

    private boolean excessSizeEnabled;
    private boolean blacklistEnabled;
    private boolean akismetEnabled;

    private int sizeLimit;
    private int linksLimit;
    private URI akismetURI;
    private boolean akismetOnlyBlatantSpamIsSpam;
    private boolean initialized;

    private String webloggerVersion;

    @Autowired
    CommentSpamChecker(
            URLService urlService,
            WebloggerPropertiesDao webloggerPropertiesDao,
            @Value("${weblogger.version}") String webloggerVersion,
            @Value("${commentSpamChecker.excessSize.enabled:true}") boolean excessSizeEnabled,
            @Value("${commentSpamChecker.blacklist.enabled:true}") boolean blacklistEnabled,
            @Value("${commentSpamChecker.akismet.enabled:false}") boolean akismetEnabled,
            @Value("${commentSpamChecker.excessSize.sizeLimit:1000}") int sizeLimit,
            @Value("${commentSpamChecker.excessSize.linksLimit:3}") int linksLimit,
            @Value("${commentSpamChecker.akismet.apiKey:}") String akismetApiKey,
            @Value("${commentSpamChecker.akismet.onlyBlatantSpamIsSpam:false}") boolean akismetOnlyBlatantSpamIsSpam) {
        this.webloggerVersion = webloggerVersion;
        this.excessSizeEnabled = excessSizeEnabled;
        this.blacklistEnabled = blacklistEnabled;
        this.akismetEnabled = akismetEnabled;
        this.urlService = urlService;
        this.webloggerPropertiesDao = webloggerPropertiesDao;
        this.sizeLimit = sizeLimit;
        this.linksLimit = linksLimit;
        this.akismetOnlyBlatantSpamIsSpam = akismetOnlyBlatantSpamIsSpam;

        if (akismetApiKey != null) {
            akismetURI = URI.create("https://" + akismetApiKey + ".rest.akismet.com/1.1/comment-check");
        }
    }

    public void refreshGlobalBlacklist() {
        // cannot be @PostConstruct as DB might not be available at startup (e.g., initial install)
        WebloggerProperties props = webloggerPropertiesDao.findOrNull();
        globalBlacklistRules = compileBlacklist(props.getGlobalSpamFilter());
    }

    public SpamCheckResult evaluate(WeblogEntryComment comment,
                                    Map<String, List<String>> messages) {

        if (!initialized) {
            refreshGlobalBlacklist();
            initialized = true;
        }

        SpamCheckResult vr = SpamCheckResult.NOT_SPAM;
        if (excessSizeEnabled) {
            vr = evaluateViaExcessSize(comment, messages);
        }
        if (blacklistEnabled && SpamCheckResult.NOT_SPAM.equals(vr)) {
            vr = evaluateViaBlacklist(comment, messages);
        }
        if (akismetEnabled && SpamCheckResult.NOT_SPAM.equals(vr)) {
            vr = evaluateViaAkismet(comment, messages);
        }
        return vr;
    }

    SpamCheckResult evaluateViaExcessSize(WeblogEntryComment comment,
                                          Map<String, List<String>> messages) {

        if (comment.getContent() != null) {
            // check size
            if (sizeLimit >= 0 && comment.getContent().length() > sizeLimit) {
                messages.put("commentSpamChecker.excessSizeMessage",
                        Collections.singletonList(Integer.toString(sizeLimit)));
                return SpamCheckResult.SPAM;
            }

            // check # of links
            if (linksLimit >= 0) {
                Matcher m = LINK_PATTERN.matcher(comment.getContent());
                int count = 0;
                while (m.find()) {
                    if (++count > linksLimit) {
                        messages.put("commentSpamChecker.excessLinksMessage",
                                Collections.singletonList(Integer.toString(linksLimit)));
                        return SpamCheckResult.SPAM;
                    }
                }
            }
        }
        return SpamCheckResult.NOT_SPAM;
    }

    /**
     * Test comment, applying weblog and site blacklists (if available)
     * @return True if comment matches a blacklist term
     */
    SpamCheckResult evaluateViaBlacklist(WeblogEntryComment comment, Map<String, List<String>> messages) {
        if (isBlacklisted(comment, globalBlacklistRules) ||
                isBlacklisted(comment, comment.getWeblogEntry().getWeblog().getBlacklistRegexRules())) {
            messages.put("commentSpamChecker.blacklistMessage", null);
            return SpamCheckResult.SPAM;
        }
        return SpamCheckResult.NOT_SPAM;
    }

    private static boolean isBlacklisted(WeblogEntryComment comment, List<Pattern> rules) {
        return isBlacklisted(comment.getUrl(), rules)
                || isBlacklisted(comment.getEmail(), rules)
                || isBlacklisted(comment.getName(), rules)
                || isBlacklisted(comment.getContent(), rules);
    }

    private static boolean isBlacklisted(String textToCheck, List<Pattern> rules) {
        if (!StringUtils.isEmpty(textToCheck)) {
            for (Pattern testPattern : rules) {
                Matcher matcher = testPattern.matcher(textToCheck);
                if (matcher.find()) {
                    log.debug("{} matched by {}", matcher.group(), testPattern.pattern());
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Create a list of regex Pattern elements from a comma-separated list of Strings to block.
     * @param blacklist Comma-separated list of terms to block.
     **/
    public static List<Pattern> compileBlacklist(String blacklist) {
        List<Pattern> regexRules = new ArrayList<>();

        if (StringUtils.isNotBlank(blacklist)) {
            String[] termsToBlock = blacklist.split("\\s*,\\s*");

            for (String term : termsToBlock) {
                // as we're converting strings to regexs, treat the period as it literally (and not as a wildcard)
                term = term.replace(".", "\\.");
                term = String.format("\\b(%s)\\b", term);
                regexRules.add(Pattern.compile(term, Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE));
            }
        }

        return regexRules;
    }

    private SpamCheckResult evaluateViaAkismet(WeblogEntryComment comment, Map<String, List<String>> messages) {
        if (akismetURI != null) {
            String apiRequestBody = createAkismetRequestBody(comment);

            try {
                HttpHeaders headers = new HttpHeaders();
                headers.setContentLength(apiRequestBody.length());
                headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
                headers.set("User_Agent", "TightBlog/" + webloggerVersion);

                RequestEntity<String> akismetCheck = new RequestEntity<>(apiRequestBody, headers, HttpMethod.POST,
                        akismetURI, String.class);

                RestTemplate template = new RestTemplate();
                ResponseEntity<String> result = template.exchange(akismetCheck, String.class);

                if ("true".equals(result.getBody())) {
                    if (!akismetOnlyBlatantSpamIsSpam ||
                            // discard = blatant spam (higher level of certainty)
                            result.getHeaders().getValuesAsList("X-akismet-pro-tip").contains("discard")) {
                        messages.put("commentSpamChecker.akismetMessage.spam", null);
                        return SpamCheckResult.SPAM;
                    }
                }
            } catch (RestClientException e) {
                log.error("ERROR checking comment against Akismet", e);
                messages.put("commentSpamChecker.akismetMessage.error", null);
                return SpamCheckResult.NOT_SPAM;
            }
        } else {
            log.warn("Skipping Akismet spam check (commentSpamChecker.akismet.apiKey not provided)." +
                    "Set commentSpamChecker.akismet.enabled=false to disable.");
        }

        return SpamCheckResult.NOT_SPAM;
    }

    String createAkismetRequestBody(WeblogEntryComment comment) {
        WeblogEntry entry = comment.getWeblogEntry();

        return String.format("blog=%s&user_ip=%s&user_agent=%s&referrer=%s&permalink=%s" +
                        "&comment_type=comment&comment_author=%s&comment_author_email=%s" +
                        "&comment_author_url=%s&comment_content=%s",
                urlService.getWeblogURL(entry.getWeblog()), comment.getRemoteHost(),
                comment.getUserAgent(), comment.getReferrer(), urlService.getWeblogEntryURL(entry),
                comment.getName(), comment.getEmail(), comment.getUrl(), comment.getContent());
    }
}
