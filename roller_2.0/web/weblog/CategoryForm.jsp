<%@ include file="/taglibs.jsp" %>


<p class="subtitle">
<c:if test="${state == 'add'}">
    <fmt:message key="categoryForm.add.subtitle" />
    
</c:if>
<c:if test="${state == 'edit'}">
    <fmt:message key="categoryForm.edit.subtitle" />
</c:if>
<c:if test="${state == 'correct'}">
    <fmt:message key="categoryForm.correct.subtitle" />
</c:if>
</p>

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
    <c:url var="categoriesUrl" value="/editor/categories.do">
       <c:param name="method" value="selectCategory" />
       <c:param name="weblog" value="${model.website.handle}" />
       <c:param name="categoryid" value="${requestScope.parentId}" />
    </c:url>
    <input type="button" value="<fmt:message key='categoryForm.cancel' />" 
        onclick="window.location = '<c:out value="${categoriesUrl}" />'" />
    </p>

</html:form>

