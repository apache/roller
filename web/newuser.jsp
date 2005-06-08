
<%@ include file="/theme/header.jsp"%>

<%
String ctxPath = request.getContextPath();
boolean allowNewUsers =
    org.roller.presentation.RollerContext.getRollerContext(
        request ).getRollerConfig().getNewUserAllowed().booleanValue();
if (!allowNewUsers)
{
%>
    <span style="warning"><fmt:message key="newuser.newUserCreationDisabled" /></span>
<%
}
else
{
%>

<script>
    function previewImage(theme)
    {
        document.preview.src="<%= ctxPath %>/images/preview/sm-theme-" + theme + ".png";
    }
</script>

<table>
<tr>
    <td>
    <html:form action="/user" method="post" focus="userName">

        <html:hidden property="id" /></input>

        <fmt:message key="newuser.loginName" /><br />
        <html:text property="userName" size="50" /></input><br />

        <fmt:message key="newuser.fullName" /><br />
        <html:text property="fullName" size="50" /></input><br />

        <fmt:message key="newuser.password" /><br />
        <html:password property="password" size="50" /></input><br />

        <fmt:message key="newuser.eMailAddress" /><br />
        <html:text property="emailAddress" size="50" /></input><br />

        <fmt:message key="newuser.theme" /><br />
        <html:select property="theme" size="1" onchange="previewImage(this[selectedIndex].value)">
            <html:options name="themes"/>
        </html:select><br />

        <fmt:message key="newuser.locale" /><br />
        <html:select property="locale" size="1" >
            <html:options collection="roller.locales" property="value" labelProperty="label"/>
        </html:select><br />

        <fmt:message key="newuser.timezone" /><br />
        <html:select property="timezone" size="1" >
            <html:options collection="roller.timezones" property="value" labelProperty="label"/>
        </html:select>
        <br />
        <br />

        <html:submit /></input>

        <html:hidden property="method" name="method" value="add"/></input>

    </html:form>
    </td>
    <td>
        <img name="preview" src="<%= ctxPath %>/images/preview/sm-theme-basic.png" height="268" width="322" />
    </td>
</tr>
</table>
<%
} // end allowNewUsers check
%>

<%@ include file="/theme/footer.jsp"%>

