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
<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %> 
<%@taglib uri="http://java.sun.com/jsf/core" prefix="f" %>  
<%@taglib uri="http://java.sun.com/jsf/html" prefix="h" %> 
<html>
<f:view>
<f:loadBundle basename="ApplicationResources" var="msgs" />
<head>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
    <title><h:outputText value="#{msgs.errorPageTitle}" /></title>
    <link rel="stylesheet" type="text/css" href='<c:url value="/planet-ui/planet.css" />' />
</head>
<body>
<div id="wrapper">
    <h:form>
    <h1><h:outputText value="#{msgs.errorPageTitle}" /></h1>
    <p>
        <a href='<c:url value="/planet-ui/main.faces" />'><h:outputText value="#{msgs.appHome}" /></a> | 
        <a href='<c:url value="/planet-ui/logout.jsp" />'><h:outputText value="#{msgs.appLogout}" /></a>
    </p>  
    <p><h:outputText value="#{msgs.errorPageMessage}" /></p>
    <h2><h:outputText value="#{msgs.errorStackTraceTitle}" /></h2>
    <p><h:inputTextarea value="#{error.stackTrace}" cols="100" rows="30" style="font-size:small" /></p>
    </h:form>
</div>
</body>
</f:view>
</html>
