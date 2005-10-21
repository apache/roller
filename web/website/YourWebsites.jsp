<%@ include file="/taglibs.jsp" %>
<% pageContext.setAttribute("leftPage","/website/YourWebsitesSidebar.jsp"); %>

<%-- Choose appropriate prompt at start of page --%>
<c:choose>

    <%-- PROMPT: Welcome... you have no blog --%>
    <c:when test="${empty model.permissions && empty model.pendings}"> 
        <p><fmt:message key="yourWebsites.prompt.noBlog" />
        <roller:link page="/editor/createWebsite.do">
           <fmt:message key="yourWebsites.createOne" />
        </roller:link></p>
    </c:when>      
    
    <%-- PROMPT: You have invitation(s) --%>
    <c:when test="${!empty model.pendings}">      
        <p><fmt:message key="yourWebsites.invitationsPrompt" /></p>
        
        <c:forEach var="invite" items="${model.pendings}">
            <fmt:message key="yourWebsites.youAreInvited" >
               <fmt:param value="${invite.website.handle}" />
            </fmt:message>
            <c:url value="/editor/yourWebsites.do" var="acceptInvite">
                <c:param name="method" value="accept" />
                <c:param name="inviteId" value="${invite.id}" />
            </c:url>
            <a href='<c:out value="${acceptInvite}" />'>
                <fmt:message key="yourWebsites.accept" />
            </a> 
            &nbsp;|&nbsp;
            <c:url value="/editor/yourWebsites.do" var="declineInvite">
                <c:param name="method" value="decline" />
                <c:param name="inviteId" value="${invite.id}" />
            </c:url>
            <a href='<c:out value="${declineInvite}" />'>
                <fmt:message key="yourWebsites.decline" />
            </a><br />
        </c:forEach>
        <br />
    </c:when>
    
    <%-- PROMPT: default ... select a weblog to edit --%>
    <c:otherwise> 
        <p class="subtitle"><fmt:message key="yourWebsites.prompt.hasBlog" /></p>        
    </c:otherwise>

</c:choose>

<%-- if we have weblogs, then loop through and list them --%>
<c:if test="${!empty model.permissions}">
    
    <c:forEach var="perms" items="${model.permissions}">

        <div class="yourWeblogBox">  

            <span class="mm_weblog_name"><img src='<c:url value="/images/Folder16.png"/>' />&nbsp;<c:out value="${perms.website.name}" /></span>
                
            <table class="mm_table" width="100%" cellpadding="0" cellspacing="0">
               <tr>
               <td valign="top">

                   <table cellpadding="0" cellspacing="0">
                       <tr>
                           <td class="mm_subtable_label"><fmt:message key='yourWebsites.weblog' /></td>
                           <td><a href='<c:out value="${model.baseURL}" />/page/<c:out value="${perms.website.handle}" />'>
                               <c:out value="${model.baseURL}" />/page/<c:out value="${perms.website.handle}" />
                           </a></td>                          
                       </tr>
                       <tr>
                           <td class="mm_subtable_label"><fmt:message key='yourWebsites.permission' /></td>
                           <td><c:if test="${perms.permissionMask == 0}" >LIMITED</c:if>
                           <c:if test="${perms.permissionMask == 1}" >AUTHOR</c:if>
                           <c:if test="${perms.permissionMask == 3}" >ADMIN</c:if></td>
                       </tr>
                       <tr>
                           <td class="mm_subtable_label"><fmt:message key='yourWebsites.description' /></td>   
                           <td><c:out value="${perms.website.description}" /></td>
                       </tr>
                   </table>

               </td>
               
               <td class="mm_table_actions" width="20%" align="left" >

                       <c:url value="/editor/weblog.do" var="newEntry">
                           <c:param name="method" value="create" />
                           <c:param name="weblog" value="${perms.website.handle}" />
                       </c:url>
                       <img src='<c:url value="/images/New16.gif"/>' />
                       <a href='<c:out value="${newEntry}" />'>
                           <fmt:message key="yourWebsites.newEntry" /></a>
                       <br />

                       <c:url value="/editor/weblogQuery.do" var="editEntries">
                           <c:param name="method" value="query" />
                           <c:param name="weblog" value="${perms.website.handle}" />
                       </c:url>
                       <img src='<c:url value="/images/Edit16.png"/>' />
                       <a href='<c:out value="${editEntries}" />'>
                           <fmt:message key="yourWebsites.editEntries" /></a> 
                       <br />

                       <c:if test="${perms.permissionMask == 3}">
                           <c:url value="/editor/website.do" var="manageWeblog">
                               <c:param name="method" value="edit" />
                               <c:param name="weblog" value="${perms.website.handle}" />
                           </c:url>
                           <img src='<c:url value="/images/Edit16.png"/>' />
                           <a href='<c:out value="${manageWeblog}" />'>
                               <fmt:message key="yourWebsites.manage" /></a> 
                           <br />
                       </c:if>

                       <%-- authors and limited bloggers can resign, but admin cannot resign if he/she is the last admin in the blog --%>
                       <c:if test="${perms.permissionMask == 0 || perms.permissionMask == 1 || perms.website.adminUserCount > 1 }">
                          <img src='<c:url value="/images/Remove16.gif"/>' />
                          <c:url value="/editor/yourWebsites.do" var="resignWeblog">
                               <c:param name="method" value="resign" />
                               <c:param name="weblog" value="${perms.website.handle}" />
                           </c:url>
                          <a href='<c:out value="${resignWeblog}" />'>
                              <fmt:message key='yourWebsites.resign' />
                          </a>
                       </c:if>

               </td>
               </tr>
            </table>
            
        </div>
        
    </c:forEach>

</c:if>


