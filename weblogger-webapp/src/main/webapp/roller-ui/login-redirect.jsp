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
<%@ page import="org.apache.roller.weblogger.ui.core.RollerSession" %>
<%@ page import="java.util.List" %>
<%
User user = RollerSession.getRollerSession(request).getAuthenticatedUser();
List weblogs = WebloggerFactory.getWeblogger().getWeblogManager().getUserWeblogs(user, true);

if (weblogs.size() == 1) {
    Weblog weblog = (Weblog) weblogs.get(0);
    response.sendRedirect(request.getContextPath()+"/roller-ui/authoring/entryAdd.rol?weblog="+weblog.getHandle());
} else {
    response.sendRedirect(request.getContextPath()+"/roller-ui/menu.rol");
}

%>

