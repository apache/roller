<%@ include file="/taglibs.jsp" %>
<%@ page import="org.roller.presentation.weblog.actions.WeblogEntryPageModel" %>
<%@ page import="org.roller.presentation.RollerRequest" %>
<%
WeblogEntryPageModel model = (WeblogEntryPageModel)request.getAttribute("model");
%>

<div style="font-size: x-small;">

<span class="leftTitle">
    <fmt:message key="weblogEdit.draftEntries" />
</span>
<br />
<c:if test="${empty model.recentDraftEntries}">
   <fmt:message key="application.none" />
</c:if>
<c:forEach var="post" items="${model.recentDraftEntries}">
    <roller:link page="/editor/weblog.do">
       <roller:linkparam
           id="<%= RollerRequest.WEBLOGENTRYID_KEY %>"
           name="post" property="id" />
           <roller:linkparam id="method" value="edit" />
           <img src='<c:url value="/images/Edit16.png"/>' align="absmiddle" border="0" alt="icon" title="Edit" />
           <str:truncateNicely lower="23" upper="27">
              <c:out value="${post.title}" />
           </str:truncateNicely>
    </roller:link>
    <br />
</c:forEach>
<br />
<br />

<span class="leftTitle">
   <fmt:message key="weblogEdit.publishedEntries" />
</span>
<br />
<c:if test="${empty model.recentPublishedEntries}">
   <fmt:message key="application.none" />
</c:if>
<c:forEach var="post" items="${model.recentPublishedEntries}">
    <roller:link page="/editor/weblog.do">
       <roller:linkparam
           id="<%= RollerRequest.WEBLOGENTRYID_KEY %>"
           name="post" property="id" />
           <roller:linkparam id="method" value="edit" />
           <img src='<c:url value="/images/Edit16.png"/>' align="absmiddle" border="0" alt="icon" title="Edit" />
           <str:truncateNicely lower="23" upper="27">
              <c:out value="${post.title}" />
           </str:truncateNicely>
    </roller:link>
    <br />
</c:forEach>

</div>
