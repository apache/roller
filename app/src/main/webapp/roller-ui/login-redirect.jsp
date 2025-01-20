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
<%@ page import="org.apache.roller.weblogger.business.*" %>
<%@ page import="org.apache.roller.weblogger.pojos.*" %>
<%@ page import="org.apache.roller.weblogger.ui.struts2.util.UIBeanFactory" %>
<%@ page import="org.apache.roller.weblogger.ui.core.RollerSession" %>
<%@ page import="java.util.List" %>
<%@ page import="org.apache.roller.weblogger.WebloggerException" %>
<%@ page import="java.util.Collections" %>
<%@ page import="org.apache.commons.logging.Log" %>
<%@ page import="org.apache.commons.logging.LogFactory" %>

<%
Log log = LogFactory.getLog("login-redirect.jsp");
RollerSession rollerSession = UIBeanFactory.getBean(RollerSession.class, request);
User user = rollerSession.getAuthenticatedUser();

List<Weblog> weblogs;
try {
    weblogs = WebloggerFactory.getWeblogger().getWeblogManager().getUserWeblogs(user, true);
} catch (WebloggerException e) {
    log.error("Error getting user weblogs for user: " + user.getUserName(), e);
    weblogs = Collections.emptyList();
}

if (user == null) {
    response.sendRedirect(request.getContextPath()+"/roller-ui/register.rol");
} else if (weblogs.size() == 1) {
    Weblog weblog = (Weblog) weblogs.get(0);
    response.sendRedirect(request.getContextPath()+"/roller-ui/authoring/entryAdd.rol?weblog="+weblog.getHandle());
} else {
    response.sendRedirect(request.getContextPath()+"/roller-ui/menu.rol");
}
%>
