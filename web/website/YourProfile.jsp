<%@ include file="/taglibs.jsp" %><%@ include file="/theme/header.jsp" %>
<script type="text/javascript">
<!--
function cancel() {
    document.userFormEx.method.value="cancel"; 
    document.userFormEx.submit();
}
-->
</script>

<h1><fmt:message key="yourProfile.title" /></h1>
<p><fmt:message key="yourProfile.description" /></p>

<html:form action="/editor/yourProfile" method="post" focus="fullName">
    <input type="hidden" name="method" value="save"></input> 
    <html:hidden property="userName" /></input>
    <html:hidden property="id" /></input>  
    <html:hidden property="password" /></input>  
    <html:hidden property="enabled" /></input>
           
<table class="formtable">

<tr>
    <td class="label"><label for="fullName" /><fmt:message key="userSettings.fullname" /></label></td>
    <td class="field"><html:text property="fullName" size="30" maxlength="30" /></td>
    <td class="description"><fmt:message key="userRegister.tip.fullName" /></td>
</tr>

<c:if test="${cookieLogin != 'true'}">
    <tr>
        <td class="label"><label for="passwordText" /><fmt:message key="userSettings.password" /></label></td>
        <td class="field">
           <html:password property="passwordText" size="20" maxlength="20" />
       </td>
        <td class="description"><fmt:message key="userRegister.tip.password" /></td>
    </tr>

    <tr>
        <td class="label"><label for="passwordConfirm" /><fmt:message key="userSettings.passwordConfirm" /></label></td>
        <td class="field"><html:password property="passwordConfirm" size="20" maxlength="20" /></td>
        <td class="description"><fmt:message key="userRegister.tip.passwordConfirm" /></td>
    </tr>
</c:if>
    
<tr>
    <td class="label"><label for="emailAddress" /><fmt:message key="userSettings.email" /></label></td>
    <td class="field"><html:text property="emailAddress" size="40" maxlength="40" /></td>
    <td class="description"><fmt:message key="userRegister.tip.email" /></td>
</tr>

<tr>
    <td class="label"><label for="locale" /><fmt:message key="userSettings.locale" /></label></td>
    <td class="field">
       <html:select property="locale" size="1" >
          <html:options collection="locales" property="value" labelProperty="label"/>
       </html:select>
    </td>
    <td class="description"><fmt:message key="userRegister.tip.locale" /></td>
</tr>
    
<tr>
    <td class="label"><label for="timeZone" /><fmt:message key="userSettings.timeZone" /></label></td>
    <td class="field">
       <html:select property="timeZone" size="1" >
           <html:options collection="timeZones" property="value" labelProperty="label"/>
       </html:select>
    </td>
    <td class="description"><fmt:message key="userRegister.tip.timeZone" /></td>
</tr>
    
</table>

<br />
<br />

<input type="submit" value='<fmt:message key="userSettings.button.save" />'></input>
<input type="button" value='<fmt:message key="userSettings.button.cancel" />' onclick="cancel()"></input>

</html:form>



