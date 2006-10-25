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
--%><!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<%@page contentType="text/html"%>
<%@page pageEncoding="UTF-8"%>
<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>  
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt"  prefix="fmt" %>
<fmt:setBundle basename="ApplicationResources" />
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <title><fmt:message key="loginPageTitle" /></title>
        <link rel="stylesheet" type="text/css" href='<c:url value="/planet-ui/planet.css" />' />
    </head>
    <body>
        <div id="wrapper">
        <h1><fmt:message key="loginPageTitle" /></h1>
       
        <a href='<c:url value="/index.jsp" />'><fmt:message key="appReturnToSite" /></a>

        <c:if test="${requestScope['error'] == 'true'}">
            <fmt:message key="loginTryAgain" />
        </c:if>
        <form id="loginForm" method="post" action="j_security_check">
            <p><fmt:message key="loginUsername" /> <input type="text" name="j_username" /></p>
            <p><fmt:message key="loginPassword" /> <input type="password" name="j_password" /></p>
            <p><button type="submit"><fmt:message key="loginButton" /></button></p>
        </form> 
        </div>
    </body>
</html>
