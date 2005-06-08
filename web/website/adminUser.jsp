
<%@ include file="/theme/header.jsp" %>

<roller:StatusMessage/>

<h1><fmt:message key="userAdmin.title" /></h1>
    
<html:form action="/adminUser" method="post">
    <p>
    <strong><fmt:message key="userAdmin.editUser" />:</strong>         
    <html:text property="userName" size="10" />
    <input type="submit" value='<fmt:message key="userAdmin.edit" />' />
    <input type="submit" value='<fmt:message key="userAdmin.rebuildIndex" />' onclick="this.form.method.value='index'" />
    <html:hidden property="method" value="edit" />
    </p>
</html:form>

<br />

<c:if test="${not empty userAdminForm.userName}">
    <h2><fmt:message key="userAdmin.userSettings" /></h2>
    <html:form action="/adminUser" method="post">

        <table>
            <tr>
                <td><fmt:message key="userAdmin.delete" /><br />
                <html:checkbox property="delete" />
                <span class="warning"><fmt:message key="userAdmin.warning" /></span>
                </td>
            </tr>
			<tr>
				<td><fmt:message key="userAdmin.enabled" /><br />
				    <html:checkbox property="userEnabled" value="true" />
				</td>
            <tr>
                <td><fmt:message key="userAdmin.name" /><br />
                    <html:text property="fullName" size="50"/>
                </td>
            </tr>
            <c:if test="${cookieLogin != 'true'}">
            <tr>
                <td><fmt:message key="userAdmin.password" /><br />
                    <html:password property="password" size="50"/>
                </td>
            </tr>
            </c:if>
            <tr>
                <td><fmt:message key="userAdmin.email" /><br />
                <html:text property="emailAddress" size="50"/></input>
                </td>
            </tr>
        </table>

        <html:hidden property="id"/></input>
        <html:hidden property="userName" /></input>

        <input type="submit" value='<fmt:message key="userAdmin.save" />'/></input>
        <html:hidden property="method" value="update"/></input>

    </html:form>
</c:if>

<%@ include file="/theme/footer.jsp" %>


