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
        <title><s:text name="PlanetsList.pageTitle" /></title>
        <link rel="stylesheet" type="text/css" href="<s:url value="/planet-ui/css/planet.css" />" />
        <script type="text/javascript">
        function confirmPlanetDelete(planetid, handle) {
          if (window.confirm('Are you sure you want to remove planet: ' + handle)) {
            document.location.href='<s:url action="PlanetsList" method="deletePlanet" />?planetid='+planetid;
          }
        }
        </script>
    </head>
    <body>
        <div id="wrapper">
            
            <%-- show the menu bar --%>
            <%@include file="/WEB-INF/jsps/admin/menu.jsp" %>
            
            <h2><s:text name="PlanetsList.heading" /></h2>
            
            <p><s:text name="PlanetsList.help" /></p>
            
            <%-- show a status message if needed --%>
            <%@include file="/WEB-INF/jsps/admin/statusMessage.jsp" %>
            
            <p/>
            
            <table class="data">
                <tr>
                    <th><s:text name="PlanetsList.planetTitle" /></th>
                    <th><s:text name="PlanetsList.planetURL" /></th>
                    <th><s:text name="PlanetsList.action" /></th>
                </tr>
                <s:iterator value="planets" status="status">
                    <s:url id="editPlanetUrl" action="PlanetForm">
                        <s:param name="planetid"><s:property value="id"/></s:param>
                    </s:url>
                    <s:url id="deletePlanetUrl" action="PlanetsList" method="delete">
                        <s:param name="planetid"><s:property value="id"/></s:param>
                    </s:url>
                    <tr class='<s:if test="#status.even">evenRow</s:if><s:else>oddRow</s:else>'>
                        <td><s:a href="%{editPlanetUrl}"><s:property value="title"/></s:a></td>
                        <td><img src='<s:url value="/planet-ui/images/world_link.png"/>' /><a href='<s:url value="/%{handle}/" />'><s:text name="PlanetsList.link" /></a></td>
                        <td><img src='<s:url value="/planet-ui/images/delete.png"/>' /><a href="javascript: void(0);" onclick="confirmPlanetDelete('<s:property value="id"/>', '<s:property value="handle"/>');"><s:text name="PlanetsList.deletePlanet"/></a></td>
                    </tr>
                </s:iterator>
            </table>
            
            <p><img src='<s:url value="/planet-ui/images/folder_add.png"/>' /><a href='<s:url action="PlanetForm"/>'><s:text name="PlanetsList.addPlanet"/></a></p>
            
        </div>
    </body>
</html>
