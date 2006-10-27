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
    <title><h:outputText value="#{msgs.subscriptionPageTitle}" /></title>
    <link rel="stylesheet" type="text/css" href='<c:url value="/planet-ui/planet.css" />' />
</head>
<body>
<div id="wrapper">    
<%@include file="/planet-ui/admin/menu.jsp" %> 

<h:form id="subscriptionForm">

<h2><h:outputText value="#{msgs.subscriptionPageTitle}" /></h2>
<p><h:outputText value="#{msgs.subscriptionHelp}" /></p>

<c:if test="${!empty subscriptionForm.groupid}">
<p><h:outputLink value="./groupForm.faces?groupid=#{subscriptionForm.groupid}">
    <h:outputText value="#{msgs.subscriptionReturnToGroup}" />
</h:outputLink></p> 
</c:if>

<h:inputHidden value="#{subscriptionForm.subscription.id}" />
<h:inputHidden value="#{subscriptionForm.groupid}" />

<h:panelGrid columns="2" columnClasses="labelColumn,valueColumn">

    <h:outputText value="#{msgs.subscriptionTitle}" />
    <h:panelGroup>        
        <h:inputText id="title" required="true" size="60" onchange="dirty()"
            value="#{subscriptionForm.subscription.title}" >
            <f:validateLength minimum="1" />
        </h:inputText>
        <h:message for="title" styleClass="fieldError" />
    </h:panelGroup>
   
    <h:outputText value="#{msgs.subscriptionFeedURL}" />
    <h:panelGroup>        
        <h:inputText id="feedURL" required="true" size="60" onchange="dirty()"
            value="#{subscriptionForm.subscription.feedURL}"
            validator="#{subscriptionForm.checkURL}" />
        <h:message for="feedURL" styleClass="fieldError" />
    </h:panelGroup>

    <h:outputText value="#{msgs.subscriptionSiteURL}" />
    <h:panelGroup>        
        <h:inputText id="siteURL" required="false" size="60" onchange="dirty()"
            value="#{subscriptionForm.subscription.siteURL}"  
            validator="#{subscriptionForm.checkURL}"/>
        <h:message for="siteURL" styleClass="fieldError" />
    </h:panelGroup>

</h:panelGrid>

<p />
<h:commandButton value="#{msgs.appSave}" action="#{subscriptionForm.save}" />  
<script type="text/javascript">
function dirty() {
    messages = document.getElementById("messages");
    messages.className = "warning";
    var n1 = messages.childNodes[0];
    var n2 = document.createTextNode("<h:outputText value="#{msgs.appUnsavedChanges} " />");
    messages.replaceChild(n2, n1);
}
</script>
<c:choose>
    <c:when test="${subscriptionForm.lastSave != null}">
        <span id="messages" class="success"><h:outputText value="#{msgs.appSavedAt}" />
        <h:outputText value="#{subscriptionForm.lastSave}">
            <f:convertDateTime pattern="h:mm:ss a" />
        </h:outputText>
        </span>
    </c:when>
    <c:otherwise>
        <span id="messages"><h:outputText value="#{msgs.appUnchanged}" /></span>
    </c:otherwise>
</c:choose>

<c:if test="${!empty subscriptionForm.subscription.id}">
    <br />
    <br />
    <t:commandLink id="addSubscriptionLink" action="#{subscriptionForm.add}" >
        <h:graphicImage style="" value="../images/feed_add.png"  />
        <h:outputText value="#{msgs.subscriptionAddSubscription}" />
        <f:param name="groupid" value="#{subscriptionForm.groupid}" />
    </t:commandLink> 
</c:if>

</h:form>    

</div>
</body>
</f:view>
</html>






