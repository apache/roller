<%@ include file="/taglibs.jsp" %><%@ include file="/theme/header.jsp" %>
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
    
    <h3><fmt:message key="planetConfig.pageTitle" /></h3>
    <p><i><fmt:message key="planetConfig.prompt" /></i></p>

    <div class="formrow">
        <label for="title" class="formrow" />
           <fmt:message key="planetConfig.title" />
        </label>
        <html:text property="title" size="40" maxlength="255" />
        <img src="../images/Help16.gif" alt="help" 
           title='<fmt:message key="planetConfig.tip.title" />' />
    </div>
    
    <div class="formrow">
        <label for="description" class="formrow" />
           <fmt:message key="planetConfig.description" />
        </label>
        <html:text property="description" size="40" maxlength="255" />
        <img src="../images/Help16.gif" alt="help" 
           title='<fmt:message key="planetConfig.tip.description" />' />
    </div>
    
    <div class="formrow">
        <label for="siteUrl" class="formrow" />
           <fmt:message key="planetConfig.siteUrl" />
        </label>
        <html:text property="siteUrl" size="40" maxlength="255" />
        <img src="../images/Help16.gif" alt="help" 
           title='<fmt:message key="planetConfig.tip.siteUrl" />' />
    </div>
    
    <div class="formrow">
        <label for="adminEmail" class="formrow" />
           <fmt:message key="planetConfig.adminEmail" />
        </label>
        <html:text property="adminEmail" size="40" maxlength="255" />
        <img src="../images/Help16.gif" alt="help" 
           title='<fmt:message key="planetConfig.tip.adminEmail" />' />
    </div>
    
    <div class="formrow">
        <label for="cacheDir" class="formrow" />
           <fmt:message key="planetConfig.cacheDir" />
        </label>
        <html:text property="cacheDir" size="40" maxlength="255" />
        <img src="../images/Help16.gif" alt="help" 
           title='<fmt:message key="planetConfig.tip.cacheDir" />' />
    </div>
    
    <div class="formrow">
        <label for="proxyHost" class="formrow" />
            <fmt:message key="planetConfig.proxyHost" />
        </label>
        <html:text property="proxyHost" size="40" maxlength="255" />
        <img src="../images/Help16.gif" alt="help" 
           title='<fmt:message key="planetConfig.tip.proxyHost" />' />
    </div>
    
    <div class="formrow">
        <label for="proxyPort" class="formrow" />
            <fmt:message key="planetConfig.proxyPort" />
        </label>
        <html:text property="proxyPort" size="40" maxlength="255" />
        <img src="../images/Help16.gif" alt="help" 
           title='<fmt:message key="planetConfig.tip.proxyPort" />' />
    </div>
    
    <br />
    <div class="control">
        <input type="submit" value='<fmt:message key="planetConfig.button.post" />' />
    </div>
    
    <br />      
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


<%@ include file="/theme/footer.jsp" %>



