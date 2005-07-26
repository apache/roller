<%@ include file="/taglibs.jsp" %><%@ include file="/theme/header.jsp" %>

<h1><fmt:message key="yourWebsites.title" /></h1>
    
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
-->
</script>

<html:form action="/editor/yourWebsites" method="post">
    <input type="hidden" name="inviteId" value="" />
    <input type="hidden" name="websiteId" value="" />
    <input type="hidden" name="method" value="select" />		  

    <c:if test="${!empty model.pendings}">
        <fmt:message key="yourWebsites.invitations" /><br />
	    <c:forEach var="invite" items="${model.pendings}">
            <c:out value="${invite.website.handle}" />
            <a href='javascript:acceptInvite("<c:out value='${invite.id}'/>")'>
                <fmt:message key="yourWebsites.accept" />
            </a> 
            &nbsp;|&nbsp;
            <a href='javascript:acceptInvite("<c:out value='${invite.id}'/>")'>
                <fmt:message key="yourWebsites.decline" />
            </a><br />
	    </c:forEach>
    </c:if>

    <p><fmt:message key="yourWebsites.websites" /></p>
    <c:forEach var="website" items="${model.websites}">
        <a href='javascript:selectWebsite("<c:out value='${website.id}'/>")'>
            <c:out value="${website.handle}" /><br />
        </a>
    </c:forEach>

</html:form>

<%@ include file="/theme/footer.jsp" %>
