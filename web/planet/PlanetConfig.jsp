<%@ include file="/taglibs.jsp" %>
<script type="text/javascript">
<!--
function refreshEntries()
{
    document.planetConfigForm.method.value = "refreshEntries";
    document.planetConfigForm.submit();
}
function syncWebsites()
{
    document.planetConfigForm.method.value = "syncWebsites";
    document.planetConfigForm.submit();
}
// -->
</script>

<html:form action="/admin/planetConfig" method="post">
    <html:hidden property="method" value="saveConfig" />
    <html:hidden property="id" />
    
    <p class="subtitle"><fmt:message key="planetConfig.subtitle" /></p>
    <p><fmt:message key="planetConfig.prompt" /></p>

    <table class="formtable">

    <tr>
        <td class="label"><label for="title" /><fmt:message key="planetConfig.title" /></label></td>
        <td class="field"><html:text property="title" size="40" maxlength="255" /></td>
        <td class="description"><fmt:message key="planetConfig.tip.title" /></td>
    </tr>

    <tr>
        <td class="label"><label for="description" /><fmt:message key="planetConfig.description" /></label></td>
        <td class="field"><html:text property="description" size="40" maxlength="255" /></td>
        <td class="description"><fmt:message key="planetConfig.tip.description" /></td>
    </tr>

    <tr>
        <td class="label"><label for="siteUrl" /><fmt:message key="planetConfig.siteUrl" /></label></td>
        <td class="field"><html:text property="siteUrl" size="40" maxlength="255" /></td>
        <td class="description"><fmt:message key="planetConfig.tip.siteUrl" /></td>
    </tr>

    <tr>
        <td class="label"><label for="adminEmail" /><fmt:message key="planetConfig.adminEmail" /></label></td>
        <td class="field"><html:text property="adminEmail" size="40" maxlength="255" /></td>
        <td class="description"><fmt:message key="planetConfig.tip.adminEmail" /></td>
    </tr>

    <tr>
        <td class="label"><label for="proxyHost" /><fmt:message key="planetConfig.proxyHost" /></label></td>
        <td class="field"><html:text property="proxyHost" size="40" maxlength="255" /></td>
        <td class="description"><fmt:message key="planetConfig.tip.proxyHost" /></td>
    </tr>

    <tr>
        <td class="label"><label for="proxyPort" /><fmt:message key="planetConfig.proxyPort" /></label></td>
        <td class="field"><html:text property="proxyPort" size="6" maxlength="6" /></td>
        <td class="description"><fmt:message key="planetConfig.tip.proxyPort" /></td>
    </tr>

    </table>

    <br />
    <div class="control">
        <input type="submit" value='<fmt:message key="planetConfig.button.post" />' />
    </div>
    
    <br />           
    <h3><fmt:message key="planetConfig.title.control" /></h3>
    <p><i><fmt:message key="planetConfig.prompt.control" /></i></p>
    
    <input type="button" name="refresh"
       value='<fmt:message key="planetConfig.button.refreshEntries" />'
       onclick="refreshEntries()" />  

    <input type="button" name="sync"
       value='<fmt:message key="planetConfig.button.syncWebsites" />'
       onclick="syncWebsites()" /> 

</html:form>





