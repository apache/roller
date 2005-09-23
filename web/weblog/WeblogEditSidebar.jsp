<%@ include file="/taglibs.jsp" %>
<%@ page import="org.roller.presentation.weblog.actions.WeblogEntryPageModel" %>
<%@ page import="org.roller.presentation.RollerRequest" %>
<%
WeblogEntryPageModel model = (WeblogEntryPageModel)request.getAttribute("model");
%>

<div class="sidebarFade">
    <div class="menu-tr">
        <div class="menu-tl">
        
<div class="sidebarInner">

<h3><fmt:message key="weblogEdit.pendingEntries" /></h3>

<c:if test="${empty model.recentPendingEntries}">
   <span><fmt:message key="application.none" /></span>
</c:if>
<c:forEach var="post" items="${model.recentPendingEntries}">
    <span><roller:link page="/editor/weblog.do">
       <roller:linkparam
           id="<%= RollerRequest.WEBLOGENTRYID_KEY %>"
           name="post" property="id" />
           <roller:linkparam id="method" value="edit" />
           <img src='<c:url value="/images/Edit16.png"/>' 
                align="absmiddle" border="0" alt="icon" title="Edit" />
           <str:truncateNicely lower="50">
              <c:out value="${post.title}" />
           </str:truncateNicely>
    </roller:link></span>
    <br />
</c:forEach>
    
         
<hr size="1" noshade="noshade" />            
<h3><fmt:message key="weblogEdit.draftEntries" /></h3>

<c:if test="${empty model.recentDraftEntries}">
   <span><fmt:message key="application.none" /></span>
</c:if>
<c:forEach var="post" items="${model.recentDraftEntries}">
    <span><roller:link page="/editor/weblog.do">
       <roller:linkparam
           id="<%= RollerRequest.WEBLOGENTRYID_KEY %>"
           name="post" property="id" />
           <roller:linkparam id="method" value="edit" />
           <img src='<c:url value="/images/Edit16.png"/>' 
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
    <span><roller:link page="/editor/weblog.do">
       <roller:linkparam
           id="<%= RollerRequest.WEBLOGENTRYID_KEY %>"
           name="post" property="id" />
           <roller:linkparam id="method" value="edit" />
           <img src='<c:url value="/images/Edit16.png"/>' 
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



