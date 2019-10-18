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

import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandlerImpl;
import org.springframework.security.web.csrf.InvalidCsrfTokenException;
import org.springframework.security.web.csrf.MissingCsrfTokenException;
import org.springframework.stereotype.Component;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/*
 * Checks for CSRF violations usually due to expired session cookie.  Instead of 403 error,
 * forwards to login page or (in the case of AJAX calls) sends a 408 signal for the front-end
 * to handle appropriately.
 *
 * Note Ajax GETs do not trigger CSRF checking, so the AjaxInvalidSessionRedirectFilter is used
 * instead for those calls to send 408s.
 */
@Component
public class CustomAccessDeniedHandlerImpl extends AccessDeniedHandlerImpl {

    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response, AccessDeniedException adEx)
            throws IOException, ServletException {
        if (adEx instanceof MissingCsrfTokenException || adEx instanceof InvalidCsrfTokenException) {
            String ajaxHeader = request.getHeader("X-Requested-With");
            if ("XMLHttpRequest".equals(ajaxHeader)) {
                // Ajax call detected, return TIMEOUT (408), a signal used by the JavaScript
                // to regenerate the page that the script is on.
                // another detection option: http://stackoverflow.com/a/34399417/1207540
                response.sendError(HttpServletResponse.SC_REQUEST_TIMEOUT);
            } else {
                RequestDispatcher dispatcher = request.getRequestDispatcher("/tb-ui/app/login");
                dispatcher.forward(request, response);
            }
        } else {
            super.handle(request, response, adEx);
        }
    }
}
