<%@ include file="/taglibs.jsp" %><%@ include file="/theme/header.jsp" %>
<% pageContext.setAttribute("leftPage","/website/YourWebsitesSidebar.jsp"); %>

<script type="text/javascript">
<!--
function selectWebsite(id) 
{
    document.yourWebsitesForm.websiteId.value = id;
    document.yourWebsitesForm.submit();
} 
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

    <c:if test="${!empty model.pendings}">
        <h1><fmt:message key="yourWebsites.invitations" /></h1>    
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
    </c:if>
    
    <c:choose>
	    <c:when test="${!empty model.permissions}">
            <h1><fmt:message key="yourWebsites.title" /></h1>    
		    <p><fmt:message key="yourWebsites.description" /></p>
		    <table class="rollertable">
		        <tr class="rHeaderTr">
		           <th class="rollertable" width="20%">
		               <fmt:message key="yourWebsites.tableTitle" />
		           </th>
		           <th class="rollertable" width="20%">
		               <fmt:message key="yourWebsites.tableDescription" />
		           </th>
		           <th class="rollertable" width="20%">
		               <fmt:message key="yourWebsites.permissions" />
		           </th>
		           <th class="rollertable" width="20%">
		               <fmt:message key="yourWebsites.resign" />
		           </th>
		        </tr>
		        <c:forEach var="perms" items="${model.permissions}">
		            <roller:row oddStyleClass="rollertable_odd" evenStyleClass="rollertable_even">  
		               <td class="rollertable">
		                   <a href='javascript:selectWebsite("<c:out value='${perms.website.id}'/>")'>
		                       <c:out value="${perms.website.name}" />
		                   </a>
		               </td>
		               <td class="rollertable">
		                   <c:out value="${perms.website.description}" />
		               </td>
		               <td class="rollertable" align="center">
		                   <c:if test="${perms.permissionMask == 0}" >LIMITED</c:if>
		                   <c:if test="${perms.permissionMask == 1}" >AUTHOR</c:if>
		                   <c:if test="${perms.permissionMask == 3}" >ADMIN</c:if>
		               </td>
		               <td class="rollertable" align="center">
		                   <a href='javascript:resignWebsite("<c:out value='${perms.website.id}'/>","<c:out value="${perms.website.handle}" />")'>
		                       <fmt:message key="yourWebsites.resign" />
		                   </a>
		               </td>
		            </roller:row>
		        </c:forEach>
		    </table>
        </c:when>
        <c:when test="${empty model.permissions}">
            <h1><fmt:message key="yourWebsites.title" /></h1>    
            <p><fmt:message key="yourWebsites.youHaveNone" /></p>
            <p>
                <roller:link page="/editor/createWebsite.do">
                    <fmt:message key="yourWebsites.youCanCreateOne" />
                </roller:link>
            </p>            
        </c:when>
    </c:choose>
    
</html:form>

<%@ include file="/theme/footer.jsp" %>
