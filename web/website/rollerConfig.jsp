
<%@ include file="/theme/header.jsp" %>

<roller:StatusMessage/>

<h1><fmt:message key="configForm.title" /></h1>
<html:form action="/rollerConfig" method="post">

    <html:hidden property="id"/></input>
    <html:hidden property="databaseVersion"/></input>

    <table border="0">

    <tr>
        <td colspan="2"><h2><fmt:message key="configForm.siteSettings" /></h2></td>
    </tr>

        <tr>
            <td class="propname"><fmt:message key="configForm.siteName" /></td>
            <td><html:text property="siteName" size="50" /></td>
        </tr>

        <tr>
            <td class="propname"><fmt:message key="configForm.siteDescription" /></td>
            <td><html:text property="siteDescription" size="50" /></td>
        </tr>

        <tr>
            <td class="propname"><fmt:message key="configForm.siteAdminEmail" /></td>
            <td><html:text property="emailAddress" size="50" /></td>
        </tr>

        <tr>
            <td class="propname"><fmt:message key="configForm.absoluteUrl" /></td>
            <td><html:text property="absoluteURL" size="50" /></td>
        </tr>

        <tr>
            <td class="propname"><fmt:message key="configForm.enableLinkback" /></td>
            <td><html:checkbox property="enableLinkback" /></td>
        </tr>

        <tr>
            <td class="propname"><fmt:message key="configForm.searchIndexDir" /></td>
            <td><html:text property="indexDir" size="50" /></td>
        </tr>

    <tr>
        <td colspan="2"><h2><fmt:message key="configForm.userSettings" /></h2></td>
    </tr>

        <tr>
            <td class="propname"><fmt:message key="configForm.allowNewUsers" /></td>
            <td><html:checkbox property="newUserAllowed" /></td>
        </tr>

        <tr>
            <td class="propname"><fmt:message key="configForm.adminUsers" /></td>
            <td><html:text property="adminUsers" size="30"  /></td>
        </tr>

        <tr>
            <td class="propname"><fmt:message key="configForm.encryptPasswords" /></td>
            <td><html:checkbox property="encryptPasswords" /></td>
        </tr>

        <tr>
            <td class="propname"><fmt:message key="configForm.algorithm" /></td>
            <td><html:text property="algorithm" size="5" /></td>
        </tr>
        
        <tr>
            <td class="propname"><fmt:message key="configForm.newUserThemes" /></td>
            <td><html:text property="userThemes"/></td>
        </tr>

        <tr>
            <td class="propname"><fmt:message key="configForm.editorPages" /></td>
            <td><html:textarea property="editorPages" cols="40" rows="2"  /></td>
        </tr>

        <tr>
            <td class="propname"><fmt:message key="configForm.autoformatComments" /></td>
            <td><html:checkbox property="autoformatComments" /></td>
        </tr>

        <tr>
            <td class="propname"><fmt:message key="configForm.escapeCommentHtml" /></td>
            <td><html:checkbox property="escapeCommentHtml" /></td>
        </tr>

        <tr>
            <td class="propname"><fmt:message key="configForm.emailComments" /></td>
            <td><html:checkbox property="emailComments" /></td>
        </tr>

        <tr>
             <td colspan="2">&nbsp;</td>
        </tr>

    <tr>
        <td colspan="2"><h2><fmt:message key="configForm.fileUploadSettings" /></h2></td>
    </tr>

        <tr>
            <td class="propname"><fmt:message key="configForm.enableFileUploads" /></td>
            <td><html:checkbox property="uploadEnabled" /></td>
        </tr>

        <tr>
            <td class="propname"><fmt:message key="configForm.allowedExtensions" /></td>
            <td><html:text property="uploadAllow" size="30"  /></td>
        </tr>

        <tr>
            <td class="propname"><fmt:message key="configForm.forbiddenExtensions" /></td>
            <td><html:text property="uploadForbid" size="30"  /></td>
        </tr>

        <tr>
            <td class="propname"><fmt:message key="configForm.maxFileSize" /></td>
            <td><html:text property="uploadMaxFileMB" size="5" /></td>
        </tr>

        <tr>
            <td class="propname"><fmt:message key="configForm.maxDirSize" /></td>
            <td><html:text property="uploadMaxDirMB" size="5" /></td>
        </tr>

        <tr>
            <td class="propname"><fmt:message key="configForm.uploadDir" /></td>
            <td><html:text property="uploadDir" /></td>
        </tr>

        <tr>
            <td class="propname"><fmt:message key="configForm.uploadPath" /></td>
            <td><html:text property="uploadPath" /></td>
        </tr>

        <tr>
             <td colspan="2">&nbsp;</td>
        </tr>

    <tr>
        <td colspan="2"><h2><fmt:message key="configForm.rssAggregatorSettings" /></h2></td>
    </tr>

        <tr>
            <td class="propname"><fmt:message key="configForm.enableRssAggregator" /></td>
            <td><html:checkbox property="enableAggregator" /></td>
        </tr>

        <tr>
            <td class="propname"><fmt:message key="configForm.cacheIncomingRss" /></td>
            <td><html:checkbox property="rssUseCache" /></td>
        </tr>

        <tr>
            <td class="propname"><fmt:message key="configForm.rssNewsfeedCacheTime" /></td>
            <td><html:text property="rssCacheTime" size="4" /></td>
        </tr>

    <tr>
        <td colspan="2"><h2><fmt:message key="configForm.debuggingSettings" /></h2></td>
    </tr>

        <tr>
            <td class="propname"><fmt:message key="configForm.enableMemoryDebugging" /></td>
            <td><html:checkbox property="memDebug" /></td>
        </tr>
        <tr>
            <td>&nbsp;</td>
        </tr>
        <tr>
            <td class="buttonBox" colspan="2">
                <input class="buttonBox" type="submit" value='<fmt:message key="configForm.save" />'/>
            </td>
        </tr>
    </table>

    <html:hidden property="method" value="update"/>

</html:form>


<%@ include file="/theme/footer.jsp" %>