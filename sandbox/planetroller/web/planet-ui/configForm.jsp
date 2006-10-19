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
    <title><h:outputText value="#{msgs.configPageTitle}" /></title>
    <link rel="stylesheet" type="text/css" href='<c:url value="/planet.css" />' />
</head>
<body>
<div id="wrapper">
<%@include file="/planet-ui/menu.jsp" %> 

<h2><h:outputText value="#{msgs.configFormTitle}" /></h2>
<p><h:outputText value="#{msgs.configHelp}" /></p>
<h:form>
    <h:inputHidden value="#{configForm.planetConfig.id}" />
    <h:panelGrid columns="2" styleClass="form" columnClasses="labelColumn,valueColumn" >

        <h:outputText value="#{msgs.configTitle}" />
        <h:panelGroup>
            <h:inputText id="title" value="#{configForm.planetConfig.title}" required="true" size="20">
                <f:validateLength minimum="1" />
            </h:inputText>
            <h:message for="title" styleClass="fieldError" />
        </h:panelGroup>
          
        <h:outputText value="#{msgs.configDescription}" />
        <h:panelGroup>
            <h:inputText id="description" value="#{configForm.planetConfig.description}" 
                required="false" size="60">
            </h:inputText>
            <h:message for="description" styleClass="fieldError" />
        </h:panelGroup>
    
        <h:outputText value="#{msgs.configSiteURL}" />
        <h:panelGroup>
             <h:inputText id="siteURL" value="#{configForm.planetConfig.siteURL}" 
                required="true" size="60" validator="#{subscriptionForm.checkURL}">
             </h:inputText>
            <h:message for="siteURL" styleClass="fieldError" />
        </h:panelGroup>
        
        <h:outputText value="#{msgs.configAdminName}" />
        <h:panelGroup>
             <h:inputText id="adminName" value="#{configForm.planetConfig.adminName}" required="true" size="20">
                <f:validateLength minimum="3" />
             </h:inputText>
            <h:message for="adminName" styleClass="fieldError" />
        </h:panelGroup>
        
        <h:outputText value="#{msgs.configAdminEmail}" />
        <h:panelGroup>
            <h:inputText id="adminEmail" value="#{configForm.planetConfig.adminEmail}" required="false" size="20">
                <t:validateEmail />
            </h:inputText>
            <h:message for="adminEmail" styleClass="fieldError" />
        </h:panelGroup>
    
        <h:outputText value="#{msgs.configDefaultGroup}" />
        <h:panelGroup>
            <h:selectOneMenu id="defaultGroup" value="#{configForm.groupHandle}" required="false">
                <f:selectItems value="#{configForm.groupHandles}" />
            </h:selectOneMenu>
            <h:message for="defaultGroup" styleClass="fieldError" />
        </h:panelGroup>
    
        <h:outputText value="#{msgs.configProxyHost}" />
        <h:panelGroup>
            <h:inputText id="proxyHost" value="#{configForm.planetConfig.proxyHost}" required="false" size="20">
            </h:inputText>
            <h:message for="proxyHost" styleClass="fieldError" />
        </h:panelGroup>
    
        <h:outputText value="#{msgs.configProxyPort}" />
        <h:panelGroup>
            <h:inputText id="proxyPort" value="#{configForm.planetConfig.proxyPort}" required="false" size="6">
            </h:inputText>
            <h:message for="proxyPort" styleClass="fieldError" />
        </h:panelGroup>
        
    </h:panelGrid>
    
    <p />
    <h:commandButton value="#{msgs.appSave}" action="#{configForm.save}" />
</h:form>
</div>
</body>
</f:view>
</html>






