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

<p class="subtitle"><fmt:message key="yourWebsites.subtitle" /></p>

<%-- Choose appropriate prompt at start of page --%>
<c:choose>

    <c:when test="${empty model.permissions && empty model.pendings}"> 
        <p><fmt:message key="yourWebsites.subtitle.welcomeNoBlog" /></p>        
        <p>
        <fmt:message key="yourWebsites.prompt.welcomeNoBlog" />
        <roller:link page="/editor/createWebsite.do">
           <fmt:message key="yourWebsites.createOne" />
        </roller:link>?
        </p>
    </c:when>
    
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
    
</c:choose>
            
<c:if test="${!empty model.permissions}">
    <h2><fmt:message key="yourWebsites.weblogs.title" /></h2>
    <p class="subtitle"><fmt:message key="yourWebsites.weblogs.prompt" /></p>

        <c:forEach var="perms" items="${model.permissions}">

            <div class="yourWeblogBox">  

                   <table width="100%">
                   <tr>
                   <td width="80%" style="padding: 0px 10px 0px 10px">

                       <h3 style="border-bottom: 1px #e5e5e5 solid; margin:0px; padding:5px">
                           <img src='<c:url value="/images/Folder16.png"/>' />
                           <c:out value="${perms.website.name}" />
                           [<c:out value="${perms.website.handle}" />] 
                       </h3>

                       <div class="formrow">
                           <label class="formrow"><fmt:message key='yourWebsites.weblog' /> URL</b></label>
                           <a href='<c:out value="${model.baseURL}" />/page/<c:out value="${perms.website.handle}" />'>
                               <c:out value="${model.baseURL}" />/page/<c:out value="${perms.website.handle}" />
                           </a>                           
                       </div>

                       <div class="formrow">
                           <label class="formrow"><fmt:message key='yourWebsites.permission' /></label>
                           <c:if test="${perms.permissionMask == 0}" >LIMITED</c:if>
                           <c:if test="${perms.permissionMask == 1}" >AUTHOR</c:if>
                           <c:if test="${perms.permissionMask == 3}" >ADMIN</c:if>
                       </div>

                       <div class="formrow">
                           <label class="formrow"><fmt:message key='yourWebsites.description' /></label>    
                           <c:out value="${perms.website.description}" />
                       </div>

                   </td>

                   <td width="20%" align="left">

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
                           
                           <c:url value="/editor/website.do" var="manageWeblog">
                               <c:param name="method" value="edit" />
                               <c:param name="weblog" value="${perms.website.handle}" />
                           </c:url>
                           <img src='<c:url value="/images/Edit16.png"/>' />
                           <a href='<c:out value="${manageWeblog}" />'>
                               <fmt:message key="yourWebsites.manage" /></a> 
                           <br />
                           
                           <c:choose>
                               <c:when test="${perms.website.adminUserCount == 1 && perms.permissionMask == 3}">
                                   <%-- <fmt:message key="yourWebsites.notAllowed" /> --%>
                               </c:when>
                               <c:otherwise>
                                  <img src='<c:url value="/images/Remove16.gif"/>' />
                                  <a href='javascript:resignWebsite("<c:out value='${perms.website.id}'/>","<c:out value="${perms.website.handle}" />")'>
                                      <fmt:message key='yourWebsites.resign' />
                                  </a>
                               </c:otherwise>
                           </c:choose>
                       </div>

                   </td>
                   </tr>

               </table>

            </div>

        </c:forEach>

</c:if>

</html:form>

<div class="clear"></div> <%-- force minimum height --%>


