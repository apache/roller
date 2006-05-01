<!--
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
-->
<%@ page language="java" errorPage="/error.jsp" contentType="text/html; charset=UTF-8" %><%@ 
taglib uri="http://java.sun.com/jstl/core" prefix="c" %><%@ 
taglib uri="http://www.rollerweblogger.org/tags" prefix="roller" %><%@ 
page import="org.roller.model.*" %><%@
page import="org.roller.pojos.*" %><%@
page import="org.roller.config.RollerConfig" %><%@
page import="org.roller.presentation.RollerSession" %><%@
page import="java.util.List" %>
<%
Roller roller = RollerFactory.getRoller();
RollerSession rollerSession = RollerSession.getRollerSession(request);
UserData user = rollerSession.getAuthenticatedUser();
List websites = roller.getUserManager().getWebsites(user, Boolean.TRUE, null);
if (websites.size() == 1) {
    WebsiteData website = (WebsiteData)websites.get(0);
    website.hasUserPermissions(user, PermissionsData.LIMITED);
    response.sendRedirect(
        "editor/weblog.do?method=create&rmk=tabbedmenu.weblog&rmik=tabbedmenu.weblog.newEntry&weblog="+website.getHandle());
} else {
    response.sendRedirect(
       "editor/yourWebsites.do?method=edit&rmik=tabbedmenu.user.websites");
}
%>

