/*
 * Copyright 2016 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.tightblog.security;

import org.springframework.security.web.util.matcher.RegexRequestMatcher;
import org.springframework.security.web.util.matcher.RequestMatcher;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import java.util.regex.Pattern;

/**
 * Turn off CSRF checking for Comment Posting, as no login auth needed for it.
 * See http://blogs.sourceallies.com/2014/04/customizing-csrf-protection-in-spring-security/
 */
@Component
public class CsrfSecurityRequestMatcher implements RequestMatcher {
    private static Pattern allowedMethods = Pattern.compile("^(GET|HEAD|TRACE|OPTIONS)$");
    private static RegexRequestMatcher unprotectedMatcher = new RegexRequestMatcher(".*/entrycomment/.*", null);

    @Override
    /*
     * Determine if CSRF checking needed for particular request
     * @param request request being processed
     * @return true if request subject to CSRF checking, false otherwise
     */
    public boolean matches(HttpServletRequest request) {
        return !(allowedMethods.matcher(request.getMethod()).matches() || unprotectedMatcher.matches(request));
    }
}
