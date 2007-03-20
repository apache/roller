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
<%@ include file="/WEB-INF/jsps/taglibs.jsp" %><%
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

<c:if test="${model.website.editorTheme ne customTheme}">
<p><fmt:message key="pagesForm.themesReminder"><fmt:param value="${model.website.editorTheme}"/></fmt:message></p>
</c:if>

<%-- table of pages --%>
<table class="rollertable">
    <tr>
        <th width="30%"><fmt:message key="pagesForm.name" /></th>
        <th width="60%"><fmt:message key="pagesForm.description" /></th>
        <th width="10"><fmt:message  key="pagesForm.remove" /></th>
    </tr>
    <c:forEach var="p" items="${model.pages}" >
        <roller:row oddStyleClass="rollertable_odd" evenStyleClass="rollertable_even">

            <td style="vertical-align:middle">
                <c:choose>
                    <c:when test="${!p.hidden}">
                        <img src='<c:url value="/images/page_white.png"/>' border="0" alt="icon" />
                        <roller:link forward="editPage">
                            <roller:linkparam id="pageId" name="p" property="id" />
                            <c:out value="${p.name}" />
                        </roller:link>
                    </c:when>
                    <c:otherwise>
                        <img src='<c:url value="/images/page_white_gear.png"/>' border="0" alt="icon" />
                        <roller:link forward="editPage">
                            <roller:linkparam id="pageId" name="p" property="id" />
                            <c:out value="${p.name}" />
                        </roller:link>
                    </c:otherwise>
                </c:choose>
            </td>
            
            <td style="vertical-align:middle"><c:out value="${p.description}" /></td>
                        
            <td class="center" style="vertical-align:middle">
               <c:choose>
                 <c:when test="${!p.required}">
                   <roller:link forward="removePage.ok">
                      <roller:linkparam id="pageId" name="p" property="id" />
                      <img src='<c:url value="/images/delete.png"/>' border="0" alt="icon" 
                          title'<fmt:message  key="pagesForm.remove" />' />
                   </roller:link>
                 </c:when>
                 <c:otherwise>
                    <img src='<c:url value="/images/lock.png"/>' border="0" alt="icon" 
                        title='<fmt:message key="pagesForm.required"/>' />
                 </c:otherwise>
               </c:choose>
            </td>

        </roller:row>
    </c:forEach>
</table>




