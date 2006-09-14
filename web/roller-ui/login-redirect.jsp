<%--
  Licensed to the Apache Software Foundation (ASF) under one or more
   contributor license agreements.  The ASF licenses this file to You
  under the Apache License, Version 2.0 (the "License"); you may not
  use this file except in compliance with the License.
  You may obtain a copy of the License at

      http://www.apache.org/licenses/LICENSE-2.0

  Unless required by applicable law or agreed to in writing, software
  distributed under the License is distributed on an "AS IS" BASIS,
  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  See the License for the specific language governing permissions and
  limitations under the License.  For additional information regarding
  copyright in this work, please see the NOTICE file in the top level
  directory of this distribution.
--%>
<%@ page language="java" contentType="text/html; charset=UTF-8" %>
<%@ page import="org.apache.roller.model.*" %>
<%@ page import="org.apache.roller.pojos.*" %>
<%@ page import="org.apache.roller.ui.core.RollerSession" %>
<%@ page import="java.util.List" %>
<%
UserData user = RollerSession.getRollerSession(request).getAuthenticatedUser();
List websites = RollerFactory.getRoller().getUserManager().getWebsites(user, Boolean.TRUE, null, null, null, 0, Integer.MAX_VALUE);

if (websites.size() == 1) {
    WebsiteData website = (WebsiteData) websites.get(0);
    response.sendRedirect(request.getContextPath()+"/roller-ui/authoring/weblog.do?method=create&weblog="+website.getHandle());
} else {
    response.sendRedirect(request.getContextPath()+"/roller-ui/yourWebsites.do");
}

%>

