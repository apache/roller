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
<fmt:setBundle basename="ApplicationResources" />
<head>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
    <title><h:outputText value="#{msgs.groupsPageTitle}" /></title>
    <link rel="stylesheet" type="text/css" href='<c:url value="/planet-ui/planet.css" />' />
</head>
<body>
<div id="wrapper">    
<%@include file="/planet-ui/admin/menu.jsp" %> 

<h2><h:outputText value="#{msgs.groupsListTitle}" /></h2>
<p><h:outputText value="#{msgs.groupsHelp}" /></p>

<h:form id="groupsForm">
<h:dataTable value="#{groupsList.groups}" var="group"
    styleClass="data" rowClasses="oddRow,evenRow" columnClasses="narrowColumn,,narrowColumn,narrowColumn">
    
    <h:column>
        <f:facet name="header">
            <h:outputText value="#{msgs.groupsHandle}" />
        </f:facet>
        <h:outputLink value="./groupForm.faces?groupid=#{group.id}">
            <h:outputText value="#{group.handle}" />
        </h:outputLink>
    </h:column> 
    
    <h:column>
        <f:facet name="header">
            <h:outputText value="#{msgs.groupsTitle}" />
        </f:facet>
        <h:outputText value="#{group.title}" />
    </h:column>  
    
    <h:column>
        <f:facet name="header">
            <h:outputText value="#{msgs.groupsGroupURL}" />
        </f:facet>       
        <h:outputLink value="#{groupsList.siteURL}/#{group.handle}" target="_blank"> 
            <h:graphicImage style="" value="../images/page_white_world.png"  />
            <h:outputText value="link" />
        </h:outputLink> 
    </h:column> 
    
    <h:column>
        <f:facet name="header">
            <h:outputText value="#{msgs.appAction}" />
        </f:facet>       
        <h:outputLink value="javascript:deleteGroup('#{group.id}','#{group.handle}')">
            <h:graphicImage style="" value="../images/delete.png"  />
            <h:outputText value="#{msgs.appDelete}" />
        </h:outputLink> 
    </h:column>  
    
</h:dataTable>

<p />
<h:commandLink action="#{groupForm.add}">
    <h:graphicImage value="../images/folder_add.png"  />
    <h:outputText value="#{msgs.groupsAddGroup}" />
</h:commandLink> 

<%-- Kludgey way to get a confirm delete popup --%> 
<t:commandLink id="deleteGroupLink" forceId="true" action="#{groupsList.deleteGroup}" >
    <f:param name="groupid" value="" />
</t:commandLink> 
<script type="text/javascript">
function deleteGroup(groupid, handle) {
    if (window.confirm('<h:outputText value="#{msgs.groupsConfirmGroupRemove} " />' + handle)) {
        clear_groupsForm();
        var f = document.forms['groupsForm'];
        f.elements['groupsForm:_link_hidden_'].value='deleteGroupLink';
        f.elements['groupid'].value=groupid;
        f.submit();
    }
}
</script> 
</h:form>

</div>
</body>
</f:view>
</html>






