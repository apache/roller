<%--
  Licensed to the Apache Software Foundation (ASF) under one
  or more contributor license agreements.  See the NOTICE file
  distributed with this work for additional information
  regarding copyright ownership.  The ASF licenses this file
  to you under the Apache License, Version 2.0 (the
  "License"); you may not use this file except in compliance
  with the License.  You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

  Unless required by applicable law or agreed to in writing,
  software distributed under the License is distributed on an
  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
  KIND, either express or implied.  See the License for the
  specific language governing permissions and limitations
  under the License.
--%>
<%@ page language="java" contentType="text/html; charset=UTF-8" %>
<%@ page import="org.apache.roller.weblogger.config.WebloggerRuntimeConfig" %>
<%@ page import="org.apache.roller.weblogger.ui.core.RollerSession" %>
<%@ page import="org.apache.roller.weblogger.ui.core.security.LogoutRedirect" %>
<%@ page import="org.springframework.security.core.Authentication" %>
<%@ page import="org.springframework.security.core.context.SecurityContextHolder" %>
<%@ page import="jakarta.servlet.http.Cookie" %>
<%@ page import="org.springframework.security.web.authentication.rememberme.TokenBasedRememberMeServices" %>

<%
// capture how the user signed in before the session goes away
Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

request.getSession().removeAttribute(RollerSession.ROLLER_SESSION);
request.getSession().invalidate(); 

// Mimic exactly TokenBasedRememberMeServices.makeCancelCookie()
Cookie terminate = new Cookie(TokenBasedRememberMeServices.SPRING_SECURITY_REMEMBER_ME_COOKIE_KEY, null);
String contextPath = request.getContextPath();
terminate.setPath(contextPath != null && contextPath.length() > 0 ? contextPath : "/");
terminate.setMaxAge(0);
response.addCookie(terminate);

// OIDC users also end their provider session; everyone else lands on the front page
String absoluteUrl = WebloggerRuntimeConfig.getAbsoluteContextURL();
LogoutRedirect.forServletContext(application).sendRedirect(request, response, authentication,
        absoluteUrl != null && !absoluteUrl.isBlank() ? absoluteUrl + "/" : null);
%>
