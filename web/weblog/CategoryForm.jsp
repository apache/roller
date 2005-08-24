<%@ include file="/taglibs.jsp" %>


<h3>
<img src='<c:url value="/images/Folder24.png"/>' alt="category" align="absmiddle" />
<c:if test="${state == 'add'}">
    <fmt:message key="categoryForm.addCategory" /></h3>
</c:if>
<c:if test="${state == 'edit'}">
    <fmt:message key="categoryForm.editCategory" />
</c:if>
<c:if test="${state == 'correct'}">
    <fmt:message key="categoryForm.correctCategory" />
</c:if>
</h3>

<p>
<b><fmt:message key="categoriesForm.path" /></b>:
<c:if test="${!empty parentCategory.parent.path}">
    <c:out value="${parentCategory.parent.path}" />
</c:if>
<c:if test="${empty parentCategory.parent.path}">
    /
</c:if>
</p>

<html:form action="/editor/categorySave" method="post" focus="name">

    <html:hidden property="method" name="method" value="update"/>
    <html:hidden property="id" />
    <html:hidden property="parentId" />
    
    <input type="hidden" name="<%= RollerRequest.PARENTID_KEY %>" 
    	value="<%= request.getAttribute(RollerRequest.PARENTID_KEY) %>" />

    <table>

    <tr>
        <td><fmt:message key="categoryForm.name" /></td>
        <td><html:text property="name" size="70" maxlength="255" /></td>
    </tr>

    <tr>
        <td><fmt:message key="categoryForm.description" /></td>
        <td><html:textarea property="description" rows="5" cols="50" /></td>
    </tr>

    <tr>
        <td><fmt:message key="categoryForm.image" /></td>
        <td><html:textarea property="image" rows="5" cols="50" /></td>
    </tr>

    </table>
    
    <p>
    <input type="submit" value="<fmt:message key='categoryForm.save' />" />
    <input type="button" value="<fmt:message key='categoryForm.cancel' />" 
        onclick="window.location = 'categories.do?method=selectCategory&amp;categoryid=<%=
        request.getAttribute(RollerRequest.PARENTID_KEY) %>'" />
    </p>

</html:form>

