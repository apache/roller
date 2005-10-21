<%@ include file="/taglibs.jsp" %><%@ include file="/theme/header.jsp" %>

<h3>
<img src='<c:url value="/images/Folder24.png" />' alt="folder" align="absmiddle" />
<c:if test="${state == 'add'}">
    <fmt:message key="folderForm.addFolder" /></h3>
</c:if>
<c:if test="${state == 'edit'}">
    <fmt:message key="folderForm.editFolder" />
</c:if>
<c:if test="${state == 'correct'}">
    <fmt:message key="folderForm.correctFolder" />
</c:if>
</h3>

<p>
<b><fmt:message key="bookmarksForm.path" /></b>:
<c:if test="${!empty parentFolder.parent.path}">
    <c:out value="${parentFolder.parent.path}" />
</c:if>
<c:if test="${empty parentFolder.parent.path}">
    /
</c:if>
</p>

<html:form action="/editor/folderSave" method="post" focus="name">

    <html:hidden property="method" name="method" value="update"/>
    <html:hidden property="id" />
    
    <input type="hidden" name="<%= RollerRequest.PARENTID_KEY %>" 
    	value="<%= request.getAttribute(RollerRequest.PARENTID_KEY) %>" />

    <table>

    <tr>
        <td><fmt:message key="folderForm.name" /></td>
        <td><html:text property="name" size="70" maxlength="255" /></td>
    </tr>

    <tr>
        <td><fmt:message key="folderForm.description" /></td>
        <td><html:textarea property="description" rows="5" cols="50" /></td>
    </tr>
    </table>
    
    <p>
    <input type="submit" value="<fmt:message key='folderForm.save' />" />
    <input type="button" value="<fmt:message key='folderForm.cancel' />" 
        onclick="window.location = 'bookmarks.do?method=selectFolder&amp;folderid=<%=
        request.getAttribute(RollerRequest.PARENTID_KEY) %>'" />
    </p>

</html:form>

<%@ include file="/theme/footer.jsp" %>