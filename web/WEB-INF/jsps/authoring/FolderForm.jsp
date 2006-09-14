<!--
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
-->
<%@ include file="/taglibs.jsp" %>

<c:if test="${!empty parentFolder.parent.path}">
    <c:set var="folderName" value="${parentFolder.parent.path}" />
</c:if>
<c:if test="${empty parentFolder.parent.path}">
    <c:set var="folderName" value="/" />
</c:if>

<p class="subtitle">
<c:if test="${state == 'add'}">
    <fmt:message key="folderForm.add.subtitle" >
        <fmt:param value="${folderName}" />
    </fmt:message>
</c:if>
<c:if test="${state == 'edit'}">
    <fmt:message key="folderForm.edit.subtitle" >
        <fmt:param value="${folderName}" />
    </fmt:message>
</c:if>
<c:if test="${state == 'correct'}">
    <fmt:message key="folderForm.correct.subtitle" >
        <fmt:param value="${folderName}" />
    </fmt:message>
</c:if>
</p>

<p>
<b><fmt:message key="bookmarksForm.path" /></b>:
<c:out value="${folderName}" />
</p>

<html:form action="/roller-ui/authoring/folderSave" method="post" focus="name">

    <html:hidden property="method" name="method" value="update"/>
    <html:hidden property="id" />
    
    <input type="hidden" name="<%= RequestConstants.PARENT_ID %>" 
    	value="<%= request.getAttribute(RequestConstants.PARENT_ID) %>" />

    <table>

    <tr>
        <td><fmt:message key="folderForm.name" /></td>
        <td><html:text property="name" size="70" maxlength="255" /></td>
    </tr>

    <tr>
        <td><fmt:message key="folderForm.description" /></td>
        <td><html:textarea property="description" rows="5" cols="50" /></td>
    </tr>
    </table>
    
    <p>
    <input type="submit" value="<fmt:message key='folderForm.save' />" />
    <input type="button" value="<fmt:message key='folderForm.cancel' />" 
        onclick="window.location = 'bookmarks.do?method=selectFolder&amp;folderId=<%=
        request.getAttribute(RequestConstants.PARENT_ID) %>'" />
    </p>

</html:form>

