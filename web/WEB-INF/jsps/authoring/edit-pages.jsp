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
<%@ include file="/taglibs.jsp" %><%
request.setAttribute("customTheme", org.apache.roller.pojos.Theme.CUSTOM); %>

<roller:StatusMessage/>

<p class="subtitle">
   <fmt:message key="pagesForm.subtitle" >
       <fmt:param value="${model.website.handle}" />
   </fmt:message>
</p>  
<p class="pagetip">
   <fmt:message key="pagesForm.tip" />
</p>

<c:if test="${website.editorTheme ne customTheme}">
<p><fmt:message key="pagesForm.themesReminder"><fmt:param value="${website.editorTheme}"/></fmt:message></p>
</c:if>

<%-- table of pages --%>
<table class="rollertable">
    <tr>
        <th width="10%"><fmt:message key="pagesForm.name" /></th>
        <th width="60%"><fmt:message key="pagesForm.description" /></th>
        <th width="10%"><fmt:message key="pagesForm.link" /></th>
        <th width="5%"><fmt:message key="pagesForm.column.navbar" /></th>
        <th width="5%"><fmt:message key="pagesForm.column.hidden" /></th>
        <th width="5%"><fmt:message key="pagesForm.edit" /></th>
        <th width="5%"><fmt:message key="pagesForm.remove" /></th>
    </tr>
    <logic:iterate id="p" name="pages" >
        <roller:row oddStyleClass="rollertable_odd" evenStyleClass="rollertable_even">

            <td><bean:write name="p" property="name" /></td>
            <td><bean:write name="p" property="description" /></td>
            <td><bean:write name="p" property="link" /></td>
            <td class="center">
                <logic:equal name="p" property="navbar" value="true">
                    <fmt:message key="application.true" />
                </logic:equal>
            </td>
            <td class="center">
                <logic:equal name="p" property="hidden" value="true">
                    <fmt:message key="application.true" />
                </logic:equal>
            </td>

            <td class="center">
               <roller:link forward="editPage">
                  <roller:linkparam id="username" name="user" property="userName" />
                  <roller:linkparam id="pageId" name="p" property="id" />
                  <img src='<c:url value="/images/page_edit.png"/>' border="0" alt="icon" />
               </roller:link>
            </td>

            <td class="center">
               <c:choose>
                 <c:when test="${!p.required}">
                   <roller:link forward="removePage.ok">
                      <roller:linkparam id="username" name="user" property="userName" />
                      <roller:linkparam id="pageId" name="p" property="id" />
                      <img src='<c:url value="/images/delete.png"/>' border="0" alt="icon" />
                   </roller:link>
                 </c:when>
                 <c:otherwise>
                    <fmt:message key="pagesForm.required"/>
                 </c:otherwise>
               </c:choose>
            </td>

        </roller:row>
    </logic:iterate>
</table>




