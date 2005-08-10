<%@ include file="/taglibs.jsp" %>
<%@ page import="org.roller.presentation.weblog.actions.WeblogEntryPageModel" %>
<%@ page import="org.roller.presentation.RollerRequest" %>
<%
WeblogEntryPageModel model = (WeblogEntryPageModel)request.getAttribute("model");
%>

<%@ include file="/theme/status.jsp" %>

<table class="sidebarBox" >
    <tr>
       <td class="sidebarBox">
          <div class="menu-tr"><div class="menu-tl">
             <fmt:message key="weblogEdit.draftEntries" />
          </div></div>
       </td>
    </tr>    
    <tr>
        <td>
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
			           <str:truncateNicely lower="50">
			              <c:out value="${post.title}" />
			           </str:truncateNicely>
			    </roller:link>
			    <br />
			</c:forEach>
        </td>
    </tr>
</table>

<br />

<c:if test="${model.rollerSession.userAuthorizedToAuthor}">
    <table class="sidebarBox">
        <tr>
           <td class="sidebarBox">
              <div class="menu-tr"><div class="menu-tl">
                 <fmt:message key="weblogEdit.publishedEntries" />
              </div></div>
           </td>
        </tr>    
        <tr>
            <td>
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
                           <str:truncateNicely lower="50">
                              <c:out value="${post.title}" />
                           </str:truncateNicely>
                    </roller:link>
                    <br />
                </c:forEach>
            </td>
        </tr>
    </table>    
    
    <br />
    
    <table class="sidebarBox">
        <tr>
           <td class="sidebarBox">
              <div class="menu-tr"><div class="menu-tl">
                 <fmt:message key="weblogEdit.pendingEntries" />
              </div></div>
           </td>
        </tr>    
        <tr>
            <td>
                <c:if test="${empty model.recentPendingEntries}">
                   <fmt:message key="application.none" />
                </c:if>
                <c:forEach var="post" items="${model.recentPendingEntries}">
                    <roller:link page="/editor/weblog.do">
                       <roller:linkparam
                           id="<%= RollerRequest.WEBLOGENTRYID_KEY %>"
                           name="post" property="id" />
                           <roller:linkparam id="method" value="edit" />
                           <img src='<c:url value="/images/Edit16.png"/>' align="absmiddle" border="0" alt="icon" title="Edit" />
                           <str:truncateNicely lower="50">
                              <c:out value="${post.title}" />
                           </str:truncateNicely>
                    </roller:link>
                    <br />
                </c:forEach>
            </td>
        </tr>
    </table>
</c:if>

<br />


