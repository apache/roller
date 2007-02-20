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
        <title><s:text name="PlanetGroupForm.pageTitle" /></title>
        <link rel="stylesheet" type="text/css" href="<s:url value="/planet-ui/css/planet.css" />" />
        <script type="text/javascript">
        function confirmSubDelete(subid, title) {
          if (window.confirm('Are you sure you want to remove subscription: ' + title)) {
            document.location.href='<s:url action="PlanetGroupForm" method="deleteSub" />?groupid=<s:property value="groupid"/>&subid='+subid;
          }
        }
        </script>
    </head>
    <body>
        <div id="wrapper">
            
            <%-- show the menu bar --%>
            <%@include file="/WEB-INF/jsps/admin/menu.jsp" %>
            
            <h2><s:text name="PlanetGroupForm.heading" /></h2>
            
            <p><s:text name="PlanetGroupForm.help" /></p>
            
            <%-- show a status message if needed --%>
            <%@include file="/WEB-INF/jsps/admin/statusMessage.jsp" %>
            
            <s:url id="planetformurl" action="PlanetForm">
                <s:param name="planetid"><s:property value="group.planet.id"/></s:param>
            </s:url>
            <p><s:a href="%{planetformurl}"><s:text name="PlanetGroupForm.returnToPlanet"/></s:a></p>
            
            <s:url id="action" action="PlanetGroupForm" method="save" />
            <s:form name="PlanetGroupForm" action="%{action}">
                <s:hidden name="planetid" value="%{group.planet.id}" />
                <s:hidden name="groupid" value="%{group.id}" />
                <s:textfield label="%{getText('PlanetGroupForm.handle')}" name="group.handle" size="40" />
                <s:textfield label="%{getText('PlanetGroupForm.title')}" name="group.title" size="40" />
                <s:textfield label="%{getText('PlanetGroupForm.description')}" name="group.description" size="40" />
                <s:textfield label="%{getText('PlanetGroupForm.maxPageEntries')}" name="group.maxPageEntries" size="4" />
                <s:textfield label="%{getText('PlanetGroupForm.maxFeedEntries')}" name="group.maxFeedEntries" size="4" />
                <s:submit />
            </s:form>
            
            <s:if test="group.id != null">
                
                <h2><s:text name="PlanetGroupForm.subsHeading"/></h2>
                
                <table class="data">
                    <tr>
                        <th><s:text name="PlanetGroupForm.subsTitle" /></th>
                        <th><s:text name="PlanetGroupForm.subsSiteURL" /></th>
                        <th><s:text name="PlanetGroupForm.subsFeedURL" /></th>
                        <th><s:text name="PlanetGroupForm.action" /></th>
                    </tr>
                    
                    <s:iterator value="group.subscriptions" status="status">
                        <s:url id="editurl" action="PlanetSubscriptionForm" includeParams="get">
                            <s:param name="subid"><s:property value="id"/></s:param>
                        </s:url>
                        <tr class='<s:if test="#status.even">evenRow</s:if><s:else>oddRow</s:else>'>
                            <td><s:a href="%{editurl}"><s:property value="title"/></s:a></td>
                            <td><img src='<s:url value="/planet-ui/images/world_link.png"/>' /><a href='<s:property value="siteURL"/>'><s:text name="PlanetGroupForm.subsSiteURL" /></a></td>
                            <td><img src='<s:url value="/planet-ui/images/feed_link.png"/>' /><a href='<s:property value="feedURL"/>'><s:text name="PlanetGroupForm.subsFeedURL" /></a></td>
                            <td><img src='<s:url value="/planet-ui/images/delete.png"/>' /><a href="javascript: void(0);" onclick="confirmSubDelete('<s:property value="id"/>', '<s:property value="title"/>');"><s:text name="PlanetGroupForm.deleteSub"/></a></td>
                        </tr>
                    </s:iterator>
                </table>
                
                <s:url id="addsuburl" action="PlanetSubscriptionForm">
                    <s:param name="groupid"><s:property value="group.id"/></s:param>
                </s:url>
                <p><img src='<s:url value="/planet-ui/images/feed_add.png"/>' /><s:a href="%{addsuburl}"><s:text name="PlanetGroupForm.addSub"/></s:a></p>
                
            </s:if>
            
        </div>
    </body>
</html>
