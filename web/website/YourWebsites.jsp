<%@ include file="/taglibs.jsp" %>
<% pageContext.setAttribute("leftPage","/website/YourWebsitesSidebar.jsp"); %>

<div class="prop"></div> <%-- force minimum height --%>

<script type="text/javascript">
<!-- 
function acceptInvite(id) 
{
    document.yourWebsitesForm.method.value = "accept";
    document.yourWebsitesForm.inviteId.value = id;
    document.yourWebsitesForm.submit();
} 
function declineInvite(id) 
{
    document.yourWebsitesForm.method.value = "decline";
    document.yourWebsitesForm.inviteId.value = id;
    document.yourWebsitesForm.submit();
} 
function resignWebsite(id,handle)
{
    if (confirm('<fmt:message key="yourWebsites.confirmResignation" /> [' + handle +"] ?"))
    {
        document.yourWebsitesForm.method.value = "resign";
        document.yourWebsitesForm.websiteId.value = id;
        document.yourWebsitesForm.submit();
    }
}
-->
</script>

<html:form action="/editor/yourWebsites" method="post">
    <input type="hidden" name="inviteId" value="" />
    <input type="hidden" name="websiteId" value="" />
    <input type="hidden" name="method" value="select" />		  

<%-- TITLE: Main Menu --%>
<p class="subtitle"><fmt:message key="yourWebsites.subtitle" /></p>

<%-- Choose appropriate prompt at start of page --%>
<c:choose>

    <%-- PROMPT: Welcome... you have no blog --%>
    <c:when test="${model.groupBloggingEnabled && empty model.permissions && empty model.pendings}"> 
        <p><fmt:message key="yourWebsites.prompt.noBlog" />
        <roller:link page="/editor/createWebsite.do">
           <fmt:message key="yourWebsites.createOne" />
        </roller:link></p>
    </c:when>
    
    <c:when test="${!model.groupBloggingEnabled && empty model.permissions && empty model.pendings}"> 
        <p><fmt:message key="yourWebsites.prompt.noBlogNoCreate" /></p>        
    </c:when>
    
    <%-- PROMPT: You have invitation(s) --%>
    <c:when test="${!empty model.pendings}">      
        <p><fmt:message key="yourWebsites.invitationsPrompt" /></p>
        
        <c:forEach var="invite" items="${model.pendings}">
            <fmt:message key="yourWebsites.youAreInvited" >
               <fmt:param value="${invite.website.handle}" />
            </fmt:message>
            <a href='javascript:acceptInvite("<c:out value='${invite.id}'/>")'>
                <fmt:message key="yourWebsites.accept" />
            </a> 
            &nbsp;|&nbsp;
            <a href='javascript:declineInvite("<c:out value='${invite.id}'/>")'>
                <fmt:message key="yourWebsites.decline" />
            </a><br />
        </c:forEach>
        <br />
    </c:when>
    
    <c:otherwise> 
        <p><fmt:message key="yourWebsites.prompt.hasBlog" /></p>        
    </c:otherwise>

</c:choose>
            
<c:if test="${!empty model.permissions}">
    <br />
    <c:forEach var="perms" items="${model.permissions}">

        <div class="yourWeblogBox">  

               <table width="100%">
               <tr>
               <td width="75%" style="padding: 0px 10px 0px 10px">

                   <h3 style="border-bottom: 1px #e5e5e5 solid; margin:0px; padding:5px">
                       <img src='<c:url value="/images/Folder16.png"/>' />
                       <c:out value="${perms.website.name}" />
                       [<c:out value="${perms.website.handle}" />] 
                   </h3>

                   <table>
                       <tr>
                           <td width="30%"><b><fmt:message key='yourWebsites.weblog' /></b></td>
                           <td width="80%"><a href='<c:out value="${model.baseURL}" />/page/<c:out value="${perms.website.handle}" />'>
                               <c:out value="${perms.website.handle}" />
                           </a></td>                          
                       </tr>
                       <tr>
                           <td><b><fmt:message key='yourWebsites.permission' /></b></td>
                           <td><c:if test="${perms.permissionMask == 0}" >LIMITED</c:if>
                           <c:if test="${perms.permissionMask == 1}" >AUTHOR</c:if>
                           <c:if test="${perms.permissionMask == 3}" >ADMIN</c:if></td>
                       </tr>
                       <tr>
                           <td><b><fmt:message key='yourWebsites.description' /></b></td>   
                           <td><c:out value="${perms.website.description}" /></td>
                       </tr>
                   </table>

               </td>

               <td class="actions" width="25%" align="left" style="padding: 4px">

                       <fmt:message key='yourWebsites.actions' />                       
                       <br />

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

                   </div>

               </td>
               </tr>

           </table>

        </div>

    </c:forEach>

</c:if>

</html:form>

<div class="clear"></div> <%-- force minimum height --%>


