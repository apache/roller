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
<%@ page import="org.apache.roller.ui.authoring.struts.actions.WeblogEntryPageModel" %>
<%
WeblogEntryPageModel model = (WeblogEntryPageModel)request.getAttribute("model");
%>

<div class="sidebarFade">
    <div class="menu-tr">
        <div class="menu-tl">
        
<div class="sidebarInner">

<h3><fmt:message key="weblogEdit.comments" /></h3>

<c:choose>
<c:when test="${model.commentCount > 0}">
    <c:url value="/roller-ui/authoring/commentManagement.do" var="commentManagement">
       <c:param name="method" value="query" />
       <c:param name="weblog" value="${model.website.handle}" />
       <c:param name="entryId" value="${model.weblogEntry.id}" />
    </c:url>
    <span class="entryEditSidebarLink">
        <a href='<c:out value="${commentManagement}" />'>
           <img src='<c:url value="/images/comment.png"/>' 
                align="absmiddle" border="0" alt="icon" title="Comments" />
           <fmt:message key="weblogEdit.hasComments">
                <fmt:param value="${model.commentCount}" />
           </fmt:message>
        </a> 
    </span><br />
</c:when>
<c:otherwise>
   <span><fmt:message key="application.none" /></span>
</c:otherwise>
</c:choose>

<hr size="1" noshade="noshade" />  
<h3><fmt:message key="weblogEdit.pendingEntries" /></h3>

<c:if test="${empty model.recentPendingEntries}">
   <span><fmt:message key="application.none" /></span>
</c:if>
<c:forEach var="post" items="${model.recentPendingEntries}">
    <span class="entryEditSidebarLink"><roller:link page="/roller-ui/authoring/weblog.do">
       <roller:linkparam
           id="<%= RequestConstants.WEBLOGENTRY_ID %>"
           name="post" property="id" />
           <roller:linkparam id="method" value="edit" />
           <img src='<c:url value="/images/table_error.png"/>' 
                align="absmiddle" border="0" alt="icon" title="Edit" />
           <str:truncateNicely lower="50">
              <c:out value="${post.title}" />
           </str:truncateNicely>
        </roller:link>
    </span><br />
</c:forEach>
    
         
<hr size="1" noshade="noshade" />            
<h3><fmt:message key="weblogEdit.draftEntries" /></h3>

<c:if test="${empty model.recentDraftEntries}">
   <span><fmt:message key="application.none" /></span>
</c:if>
<c:forEach var="post" items="${model.recentDraftEntries}">
    <span class="entryEditSidebarLink"><roller:link page="/roller-ui/authoring/weblog.do">
       <roller:linkparam
           id="<%= RequestConstants.WEBLOGENTRY_ID %>"
           name="post" property="id" />
           <roller:linkparam id="method" value="edit" />
           <img src='<c:url value="/images/table_edit.png"/>' 
                align="absmiddle" border="0" alt="icon" title="Edit" />
           <str:truncateNicely lower="50">
              <c:out value="${post.title}" />
           </str:truncateNicely>
    </roller:link></span>
    <br />
</c:forEach>             


<c:if test="${model.userAuthorizedToAuthor}">
            
<hr size="1" noshade="noshade" />
<h3><fmt:message key="weblogEdit.publishedEntries" /></h3>

<c:if test="${empty model.recentPublishedEntries}">
   <span><fmt:message key="application.none" /></span>
</c:if>
<c:forEach var="post" items="${model.recentPublishedEntries}">
    <span class="entryEditSidebarLink"><roller:link page="/roller-ui/authoring/weblog.do">
       <roller:linkparam
           id="<%= RequestConstants.WEBLOGENTRY_ID %>"
           name="post" property="id" />
           <roller:linkparam id="method" value="edit" />
           <img src='<c:url value="/images/table_edit.png"/>' 
                align="absmiddle" border="0" alt="icon" title="Edit" />
           <str:truncateNicely lower="50">
              <c:out value="${post.title}" />
           </str:truncateNicely>
    </roller:link></span>                    
    <br />
</c:forEach>

<br />
<br />
</div>
       
        </div>
    </div>
</div>
 
</c:if>



