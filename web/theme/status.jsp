<%@ include file="/taglibs.jsp" %>

<logic:present name="loggedIn" scope="request" >
    <fmt:message key="mainPage.loggedInAs" /> [<bean:write name="userName"/>].<br /><br />
    <html:link forward="logout-redirect"><fmt:message key="navigationBar.logout"/></html:link>
</logic:present>

<logic:present name="allowNewUsers" scope="request" >
    <html:link forward="login-redirect"><fmt:message key="navigationBar.login"/></html:link><br /><br />
    <html:link forward="newUser"><fmt:message key="navigationBar.register"/></html:link>
</logic:present>

<logic:notPresent name="loggedIn">
    <logic:notPresent name="allowNewUsers">
    <html:link forward="login-redirect"><fmt:message key="navigationBar.login"/></html:link>
    </logic:notPresent>
</logic:notPresent>
