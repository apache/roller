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
<%@ page import="org.apache.roller.ui.authoring.struts.actions.WeblogEntryManagementAction" %>
<%
WeblogEntryManagementAction.PageModel model = 
    (WeblogEntryManagementAction.PageModel)request.getAttribute("model");
%>

<p class="subtitle">
    <fmt:message key="weblogEntryQuery.subtitle" >
        <fmt:param value="${model.website.handle}" />
    </fmt:message>
</p>
<p class="pagetip">
    <fmt:message key="weblogEntryQuery.tip" />
</p>


<%-- ============================================================= --%>
<%-- Number of comments and date message --%>
<%-- ============================================================= --%>

<div class="tablenav">

<div style="float:left;">
    <fmt:message key="weblogEntryQuery.nowShowing">
        <fmt:param value="${model.weblogEntryCount}" />
    </fmt:message>
</div>
<div style="float:right;">
    <fmt:formatDate value="${model.latestDate}" type="both" 
        dateStyle="short" timeStyle="short" />
    --- 
    <fmt:formatDate value="${model.earliestDate}" type="both" 
        dateStyle="short" timeStyle="short" />
</div>
<br />


<%-- ============================================================= --%>
<%-- Next / previous links --%>
<%-- ============================================================= --%>

<c:choose>
    <c:when test="${!empty model.prevLink && !empty model.nextLink}">
        <br /><center>
            &laquo;
            <a href='<c:out value="${model.prevLink}" />'>
                <fmt:message key="weblogEntryQuery.prev" /></a>
            | <a href='<c:out value="${model.nextLink}" />'>
                <fmt:message key="weblogEntryQuery.next" /></a>
            &raquo;
        </center><br />
    </c:when>
    <c:when test="${!empty model.prevLink}">
        <br /><center>
            &laquo;
            <a href='<c:out value="${model.prevLink}" />'>
                <fmt:message key="weblogEntryQuery.prev" /></a>
            | <fmt:message key="weblogEntryQuery.next" />
            &raquo;
        </center><br />
    </c:when>
    <c:when test="${!empty model.nextLink}">
        <br /><center>
            &laquo;
            <fmt:message key="weblogEntryQuery.prev" />
            | <a class="" href='<c:out value="${model.nextLink}" />'>
                <fmt:message key="weblogEntryQuery.next" /></a>
            &raquo;
        </center><br />
    </c:when>
    <c:otherwise><br /></c:otherwise>
</c:choose>

</div> <%-- class="tablenav" --%>

        
<%-- ============================================================= --%>
<%-- Entry table--%>
<%-- ============================================================= --%>

<p>
<span class="draftEntryBox">&nbsp;&nbsp;&nbsp;&nbsp;</span> 
<fmt:message key="weblogEntryQuery.draft" />&nbsp;&nbsp;
<span class="pendingEntryBox">&nbsp;&nbsp;&nbsp;&nbsp;</span>
<fmt:message key="weblogEntryQuery.pending" />&nbsp;&nbsp;
</p>      
        
<table class="rollertable" width="100%">
    
<tr>
    <th class="rollertable" width="5%">
        <fmt:message key="weblogEntryQuery.pubTime" />
    </th>
    <th class="rollertable" width="5%">
        <fmt:message key="weblogEntryQuery.updateTime" />
    </th>
    <th class="rollertable">
        <fmt:message key="weblogEntryQuery.title" />
    </th>
    <th class="rollertable" width="5%">
        <fmt:message key="weblogEntryQuery.category" />
    </th>
    <th class="rollertable" width="5%">
    </th>
    <th class="rollertable" width="5%">
    </th>
</tr>

<c:forEach var="post" items="${model.recentWeblogEntries}">
        <%-- <td> with style if comment is spam or pending --%>               
        <c:choose>
            <c:when test='${post.status == "DRAFT"}'>
                <tr class="draftentry"> 
            </c:when>
            <c:when test='${post.status == "PENDING"}'>
                <tr class="pendingentry"> 
            </c:when>
            <c:otherwise>
                <tr>
            </c:otherwise>
        </c:choose>
        
        <td>
            <c:out value="${post.pubTime}" />
        </td>
        
        <td>
            <c:out value="${post.updateTime}" />
        </td>
        
        <td>
            <str:truncateNicely upper="80">
                <c:out value="${post.displayTitle}" />
            </str:truncateNicely>
        </td>
        
        <td>
            <c:out value="${post.category.name}" />
        </td>
        
        <td>
            <roller:link page="/roller-ui/authoring/weblog.do">
                <roller:linkparam
                    id="<%= RollerRequest.WEBLOGENTRYID_KEY %>"
                    name="post" property="id" />
                <roller:linkparam id="method" value="edit" />
                <fmt:message key="weblogEntryQuery.edit" />
            </roller:link>
        </td>
                
        <td>
            <c:if test='${post.status == "PUBLISHED"}'>
                <a href='<c:out value="${post.permaLink}" />'>
                    <fmt:message key="weblogEntryQuery.view" />
                </a>
            </c:if>
        </td>
        
    </tr>
</c:forEach>

</table>

<c:if test="${empty model.recentWeblogEntries}" >
   <fmt:message key="weblogEntryQuery.noneFound" />
   <br /><br /><br /><br /><br /><br /><br /><br /><br /><br /><br /><br />
</c:if>

<script type="text/javascript">
<!--
try {Start();} catch (e) {};
-->
</script>


