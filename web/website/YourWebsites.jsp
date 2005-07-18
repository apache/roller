<%@ include file="/taglibs.jsp" %><%@ include file="/theme/header.jsp" %>

<h1><fmt:message key="yourWebsites.title" /></h1>
    
<script type="text/javascript">
<!--
function selectWebsite(id) {
    document.yourWebsitesForm.websiteId.value = id;
    document.yourWebsitesForm.submit();
} 
-->
</script>

<html:form action="/editor/yourWebsites" method="post">
    <input type="hidden" name="websiteId" value="" />
    <input type="hidden" name="method" value="select" />		  
</html:form>

<c:forEach var="website" items="${model.websites}">
    <a href='javascript:selectWebsite("<c:out value='${website.id}'/>")'>
        <c:out value="${website.handle}" /><br />
    </a>
</c:forEach>

<%@ include file="/theme/footer.jsp" %>


