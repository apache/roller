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
<%@ taglib uri="http://myfaces.apache.org/tomahawk" prefix="t" %>
<html>
<f:view>
<f:loadBundle basename="ApplicationResources" var="msgs" />
<head>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
    <title><h:outputText value="#{msgs.regThanksPageTitle}" /></title>
    <link rel="stylesheet" type="text/css" href='<c:url value="/planet-ui/planet.css" />' />
</head>
<body>
<div id="wrapper">

<h2><h:outputText value="#{msgs.regThanksPageTitle}" /></h2>

<p><h:outputText value="#{msgs.regThanksText}" /></p>

<a href='<c:url value="/index.jsp" />'><h:outputText value="#{msgs.appReturnToSite}" /></a>

</div>
</body>
</f:view>
</html>






