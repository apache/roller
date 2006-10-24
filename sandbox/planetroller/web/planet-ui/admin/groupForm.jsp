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
    <title><h:outputText value="#{msgs.groupPageTitle}" /></title>
    <link rel="stylesheet" type="text/css" href='<c:url value="/planet-ui/planet.css" />' />
</head>
<body>
<div id="wrapper">    
<%@include file="/planet-ui/admin/menu.jsp" %> 

<h2><h:outputText value="#{msgs.groupPageTitle}" /></h2>
<p><h:outputText value="#{msgs.groupHelp}" /></p>

<h:form id="groupForm">
<p><h:outputLink value="./groupsList.faces">
    <h:outputText value="#{msgs.groupReturnToList}" />
</h:outputLink></p>

<h:inputHidden value="#{groupForm.group.id}" />
<h:panelGrid columns="2" columnClasses="labelColumn,valueColumn">
    
    <h:outputText value="#{msgs.groupTitle}" />
    <h:panelGroup>        
        <h:inputText id="title" value="#{groupForm.group.title}" required="true" size="20">
            <f:validateLength minimum="1" />
        </h:inputText>
        <h:message for="title" styleClass="fieldError" />
    </h:panelGroup>

    <h:outputText value="#{msgs.groupHandle}" />
    <h:panelGroup>        
        <h:inputText id="handle" value="#{groupForm.group.handle}" required="true" size="20">
            <f:validateLength minimum="1" />
        </h:inputText>
        <h:message for="handle" styleClass="fieldError" />
    </h:panelGroup>
    
    <h:outputText value="#{msgs.groupDescription}" />
    <h:panelGroup>        
        <h:inputText id="description" value="#{groupForm.group.description}" required="false" size="60">
        </h:inputText>
        <h:message for="description" styleClass="fieldError" />
    </h:panelGroup>
    
</h:panelGrid>

<p />
<h:commandButton value="#{msgs.appSave}" action="#{groupForm.save}" />    

<c:if test="${groupForm.group.id != null}">
<h2><h:outputText value="#{msgs.groupSubscriptions}" /></h2>

<h:dataTable value="#{groupForm.subscriptions}" var="sub"
    styleClass="data" rowClasses="oddRow,evenRow" 
    columnClasses=",narrowColumn,narrowColumn,narrowColumn">
            
    <h:column>
        <f:facet name="header">
            <h:outputText value="#{msgs.groupSubscriptionTitle}" />
        </f:facet>  
        <h:outputLink value="./subscriptionForm.faces?subid=#{sub.id}&groupid=#{groupForm.group.id}">
            <h:outputText value="#{sub.title}" />
        </h:outputLink>
    </h:column> 
        
    <h:column>
        <f:facet name="header">
            <h:outputText value="#{msgs.groupSubscriptionSiteURL}" />
        </f:facet>  
        <h:outputLink value="#{sub.siteURL}" target="_blank"
            title="#{sub.siteURL}" >
            <h:graphicImage style="" value="../images/world_link.png"  />
            <h:outputText value="link" />
        </h:outputLink>
    </h:column> 
        
    <h:column>
        <f:facet name="header">
            <h:outputText value="#{msgs.groupSubscriptionFeedURL}" />
        </f:facet>  
        <h:outputLink value="#{sub.feedURL}" target="_blank"
            title="#{sub.feedURL}" >
            <h:graphicImage style="" value="../images/feed_link.png"  />
            <h:outputText value="link" />
        </h:outputLink>
    </h:column> 
        
    <h:column>
        <f:facet name="header">
            <h:outputText value="#{msgs.appAction}" />
        </f:facet>
        <h:outputLink value="javascript:removeSubscription('#{sub.id}','#{sub.title}')">
            <h:graphicImage value="../images/delete.png"  />
            <h:outputText value="#{msgs.appRemove}" />
        </h:outputLink> 
    </h:column>  

</h:dataTable>

<p />
<t:commandLink id="addSubscriptionLink" forceId="true" action="#{subscriptionForm.add}" >
    <h:graphicImage style="" value="../images/feed_add.png"  />
    <h:outputText value="#{msgs.groupAddSubscription}" />
    <f:param name="groupid" value="#{groupForm.group.id}" />
</t:commandLink> 

<%-- Kludgey way to get a confirm remove popup --%>
<t:commandLink id="removeSubscriptionLink" forceId="true" action="#{groupForm.removeSubscription}" >
    <f:param name="subid" value="" />
    <f:param name="groupid" value="#{groupForm.group.id}" />
</t:commandLink> 
<script type="text/javascript">
function removeSubscription(subid, handle) {
    if (window.confirm('<h:outputText value="#{msgs.groupConfirmSubscriptionRemove} " />' + handle)) {
        clear_groupForm();
        var f = document.forms['groupForm'];
        f.elements['groupForm:_link_hidden_'].value='removeSubscriptionLink';
        f.elements['subid'].value=subid;
        f.submit();
    }
}
</script>        
</c:if>
</h:form>

</div>
</body>
</f:view>
</html>






