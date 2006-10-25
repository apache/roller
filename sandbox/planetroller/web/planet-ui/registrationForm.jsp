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
    <title><h:outputText value="#{msgs.regPageTitle}" /></title>
    <link rel="stylesheet" type="text/css" href='<c:url value="/planet-ui/planet.css" />' />
</head>
<body>
<div id="wrapper">

<h2><h:outputText value="#{msgs.regFormTitle}" /></h2>

<p><h:outputLink value="../index.jsp">
    <h:outputText value="#{msgs.appReturnToSite}" />
</h:outputLink></p>

<p><h:outputText value="#{msgs.regHelp}" /></p>

<h:form>

    <h:panelGrid columns="2" styleClass="form" columnClasses="labelColumn,valueColumn" >

        <h:outputText value="#{msgs.regBlogTitle}" />
        <h:panelGroup>
            <h:inputText id="blogTitle" value="#{registrationForm.blogTitle}" required="true" size="30">
                <f:validateLength minimum="1" />
            </h:inputText>
            <h:message for="blogTitle" styleClass="fieldError" />
        </h:panelGroup>         
        
        <h:outputText value="#{msgs.regFeedURL}" /> 
        <h:panelGroup>
            <h:inputText id="feedURL" value="#{registrationForm.feedURL}" size="50"
                required="true" validator="#{registrationForm.checkURL}">
                <f:validateLength minimum="1" />
            </h:inputText>
            <h:message for="feedURL" styleClass="fieldError" />
        </h:panelGroup>         
        
        <h:outputText value="#{msgs.regBlogURL}" />
        <h:panelGroup>
            <h:inputText id="blogURL" value="#{registrationForm.blogURL}" size="50" 
                required="true" validator="#{registrationForm.checkURL}">
                <f:validateLength minimum="1" />
            </h:inputText>
            <h:message for="blogURL" styleClass="fieldError" />
        </h:panelGroup>  
        
    </h:panelGrid>
    
<p><h:outputText value="#{msgs.regPrivateFields}" /></p>

    <h:panelGrid columns="2" styleClass="form" columnClasses="labelColumn,valueColumn" >
        
        <h:outputText value="#{msgs.regRelationship}" />
        <h:panelGroup>
            <h:inputText id="relationship" value="#{registrationForm.relationship}" required="true" size="30">
                <f:validateLength minimum="1" />
            </h:inputText>
            <h:message for="relationship" styleClass="fieldError" />
        </h:panelGroup>         
        
        <h:outputText value="#{msgs.regFullName}" />
        <h:panelGroup>
            <h:inputText id="fullName" value="#{registrationForm.fullName}" required="true" size="30">
                <f:validateLength minimum="1" />
            </h:inputText>
            <h:message for="fullName" styleClass="fieldError" />
        </h:panelGroup>         
        
        <h:outputText value="#{msgs.regEmail}" />
        <h:panelGroup>
            <h:inputText id="email" value="#{registrationForm.email}" required="true" size="30">
                <t:validateEmail />
            </h:inputText>
            <h:message for="email" styleClass="fieldError" />
        </h:panelGroup>         
        
        <h:outputText value="#{msgs.regOtherURL}" />
        <h:panelGroup>
            <h:inputText id="otherURL" value="#{registrationForm.otherURL}" size="50"
                validator="#{registrationForm.checkURL}">
                <f:validateLength minimum="1" />
            </h:inputText>
            <h:message for="otherURL" styleClass="fieldError" />
        </h:panelGroup>         
        
        <h:outputText value="#{msgs.regOtherID}" />
        <h:panelGroup>
            <h:inputText id="otherID" value="#{registrationForm.otherID}" size="20">
                <f:validateLength minimum="1" />
            </h:inputText>
            <h:message for="otherID" styleClass="fieldError" />
        </h:panelGroup>            
        
    </h:panelGrid>
    
    <p />
    <h:selectBooleanCheckbox id="agreeToTerms" value="#{registrationForm.agreeToTerms}" 
        validator="#{registrationForm.checkAgreeToTerms}"/>
    <h:outputText value="#{msgs.regAgreeToTerms}" />
    <h:message for="agreeToTerms" styleClass="fieldError" />     
    
    <p />
    <h:commandButton value="#{msgs.appSave}" action="#{registrationForm.register}" />
</h:form>
</div>
</body>
</f:view>
</html>






