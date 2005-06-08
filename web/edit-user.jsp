<%@ include file="/theme/header.jsp" %>

<roller:StatusMessage/>

<h1><fmt:message key="userSettings.userSettings" /></h1>
<html:form action="/user" method="post" focus="fullName">

    <table>
        <tr>
            <td><fmt:message key="userSettings.name" /><br />
                <html:text property="fullName" size="50"/>
            </td>
        </tr>
        <c:if test="${cookieLogin != 'true'}">
        <tr>
            <td><fmt:message key="userSettings.password" /><br />
                <html:password property="password" size="50"/>
            </td>
        </tr>
        </c:if>
        <tr>
            <td><fmt:message key="userSettings.email" /><br />
                <html:text property="emailAddress" size="50"/>
            </td>
        </tr>

<html:hidden property="theme" /></input>
<%-- Not implemented for the front end yet
        <tr>
            <td>Editor Theme<br />
            <html:select property="theme" size="1" >
                <html:options name="themes"/>
            </html:select>
            </td>
        </tr>
--%>
        <tr>
            <td><fmt:message key="userSettings.locale" /><br />
            <html:select property="locale" size="1" >
            <html:options collection="roller.locales" property="value" labelProperty="label"/>
            </html:select>
            </td>
        </tr>

        <tr>
            <td><fmt:message key="userSettings.timezone" /><br />
            <html:select property="timezone" size="1" >
            <html:options collection="roller.timezones" property="value" labelProperty="label"/>
            </html:select>
            </td>
        </tr>

        <tr>
            <td>
                <html:hidden property="id"/></input>
                <html:hidden property="userName" /></input>
                <html:hidden property="dateCreatedAsString" /></input>

                <br />
                <input type="submit" value='<fmt:message key="userSettings.save" />' /></input>
                <html:hidden property="method" value="update"/></input>
            </td>
        </tr>
    </table>
</html:form>

<%@ include file="/theme/footer.jsp" %>


