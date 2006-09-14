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
--%><%@ page language="java" contentType="text/html; charset=UTF-8" %>

<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt"  prefix="fmt" %>
<%@ taglib uri="http://struts.apache.org/tags-bean"   prefix="bean" %>
<%@ taglib uri="http://struts.apache.org/tags-html"   prefix="html" %>
<%@ taglib uri="http://struts.apache.org/tags-logic"  prefix="logic" %>
<%@ taglib uri="http://struts.apache.org/tags-tiles"  prefix="tiles" %>
<%@ taglib uri="http://jakarta.apache.org/taglibs/string-1.0.1" prefix="str" %>

<%@ taglib uri="http://www.rollerweblogger.org/tags" prefix="roller" %>

<%@ page import="javax.servlet.jsp.jstl.core.Config" %>

<%@ page import="org.apache.roller.model.Roller" %>
<%@ page import="org.apache.roller.model.RollerFactory" %>

<%@ page import="org.apache.roller.pojos.UserData" %>
<%@ page import="org.apache.roller.pojos.WebsiteData" %>
<%@ page import="org.apache.roller.pojos.RollerConfigData" %>

<%@ page import="org.apache.roller.config.RollerConfig" %>
<%@ page import="org.apache.roller.config.RollerRuntimeConfig" %>
<%@ page import="org.apache.roller.config.RollerConfig" %>

<%@ page import="org.apache.roller.ui.core.RequestConstants" %>
<%@ page import="org.apache.roller.ui.core.BasePageModel" %>
<%@ page import="org.apache.roller.ui.core.RollerContext" %>
<%@ page import="org.apache.roller.ui.core.RollerSession" %>

<%   
// see if we have an authenticated user so we can set the display locale
RollerSession rSession = RollerSession.getRollerSession(request);
UserData mUser = null;
if(rSession != null) {
    mUser = rSession.getAuthenticatedUser();
}

if(mUser != null) {
    request.setAttribute("mLocale", mUser.getLocale());
} else {
    request.setAttribute("mLocale", request.getLocale());
}
%>
<fmt:setLocale value="${mLocale}" />
<fmt:setBundle basename="ApplicationResources" />

<%-- Set Struts tags to use XHTML --%>
<html:xhtml />