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
<%@ taglib prefix="s" uri="/struts-tags" %>

<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <title><s:text name="PlanetSubscriptionForm.pageTitle" /></title>
        <link rel="stylesheet" type="text/css" href="<s:url value="/planet-ui/css/planet.css" />" />
    </head>
    <body>
        <div id="wrapper">
            
            <%-- show the menu bar --%>
            <%@include file="/WEB-INF/jsps/admin/menu.jsp" %>
            
            <h2><s:text name="PlanetSubscriptionForm.heading" /></h2>
            
            <p><s:text name="PlanetSubscriptionForm.help" /></p>
            
            <%-- show a status message if needed --%>
            <%@include file="/WEB-INF/jsps/admin/statusMessage.jsp" %>
            
            <s:url id="groupformurl" action="PlanetGroupForm">
                <s:param name="groupid"><s:property value="groupid"/></s:param>
            </s:url>
            <p><s:a href="%{groupformurl}"><s:text name="PlanetSubscriptionForm.returnToGroup"/></s:a></p>
            
            <s:url id="action" action="PlanetSubscriptionForm" method="save" />
            <s:form name="PlanetSubscriptionForm" action="PlanetSubscriptionForm!save">
                <s:hidden name="groupid" />
                <s:hidden name="subid" value="%{subscription.id}" />
                <s:textfield label="%{getText('PlanetSubscriptionForm.title')}" name="subscription.title" size="40" />
                <s:textfield label="%{getText('PlanetSubscriptionForm.feedURL')}" name="subscription.feedURL" size="60" />
                <s:textfield label="%{getText('PlanetSubscriptionForm.siteURL')}" name="subscription.siteURL" size="60" />
                <s:submit />
            </s:form>
            
            <s:if test="subscription.id != null">
                <s:url id="addsuburl" action="PlanetSubscriptionForm" >
                    <s:param name="groupid"><s:property value="groupid"/></s:param>
                </s:url>
                <p><img src='<s:url value="/planet-ui/images/feed_add.png"/>' /><s:a href="%{addsuburl}"><s:text name="PlanetSubscriptionForm.addSubscription"/></s:a></p>
            </s:if>
            
        </div>
    </body>
</html>
