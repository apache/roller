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
        <title><s:text name="PlanetForm.pageTitle" /></title>
        <link rel="stylesheet" type="text/css" href="<s:url value="/planet-ui/css/planet.css" />" />
        <script type="text/javascript">
        function confirmGroupDelete(groupid, handle) {
          if (window.confirm('Are you sure you want to remove group: ' + handle)) {
            document.location.href='<s:url action="PlanetForm" method="deleteGroup" />?groupid='+groupid;
          }
        }
        </script>
    </head>
    <body>
        <div id="wrapper">
            
            <%-- show the menu bar --%>
            <%@include file="/WEB-INF/jsps/admin/menu.jsp" %>
            
            <h2><s:text name="PlanetForm.heading" /></h2>
            
            <p><s:text name="PlanetForm.help" /></p>
            
            <%-- show a status message if needed --%>
            <%@include file="/WEB-INF/jsps/admin/statusMessage.jsp" %>
            
            <p><a href='<s:url action="PlanetsList" includeParams="get" />'><s:text name="PlanetForm.returnToList"/></a></p>
            
            <s:form name="PlanetForm" action="PlanetForm!save">
                <s:hidden name="planetid" />
                <s:textfield label="%{getText('PlanetForm.handle')}" name="planet.handle" size="40" />
                <s:textfield label="%{getText('PlanetForm.title')}" name="planet.title" size="40" />
                <s:textarea label="%{getText('PlanetForm.description')}" name="planet.description" cols="47" rows="3" />
                <s:submit />
            </s:form>
            
            
            <s:if test="planet.id != null">
                
                <h2><s:text name="PlanetForm.groupsHeading"/></h2>
                
                <table class="data">
                    <tr>
                        <th><s:text name="PlanetForm.groupTitle" /></th>
                        <th><s:text name="PlanetForm.groupURL" /></th>
                        <th><s:text name="PlanetForm.action" /></th>
                    </tr>
                    
                    <s:iterator value="planet.groups" status="status">
                        <s:url id="editgroupurl" action="PlanetGroupForm" >
                            <s:param name="groupid"><s:property value="id"/></s:param>
                        </s:url>
                        <tr class='<s:if test="#status.even">evenRow</s:if><s:else>oddRow</s:else>'>
                            <td><s:a href="%{editgroupurl}"><s:property value="title"/></s:a></td>
                            <td><img src='<s:url value="/planet-ui/images/world_link.png"/>' /><a href='<s:url value="/%{planet.handle}/group/%{handle}/" />'><s:text name="PlanetForm.link" /></a></td>
                            <td><img src='<s:url value="/planet-ui/images/delete.png"/>' /><a href="javascript: void(0);" onclick="confirmGroupDelete('<s:property value="id"/>', '<s:property value="handle"/>');"><s:text name="PlanetForm.deleteGroup"/></a></td>
                        </tr>
                    </s:iterator>
                </table>
                
                <s:url id="addurl" action="PlanetGroupForm">
                    <s:param name="planetid"><s:property value="planet.id"/></s:param>
                </s:url>
                <p><img src='<s:url value="/planet-ui/images/folder_add.png"/>' /><s:a href="%{addurl}"><s:text name="PlanetForm.addGroup"/></s:a></p>
                
            </s:if>
            
        </div>
    </body>
</html>
