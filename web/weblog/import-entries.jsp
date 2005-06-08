<%@ include file="/theme/header.jsp" %>

<h1><fmt:message key="weblogEntryImport.title" /></h1>

<roller:StatusMessage/>

<html:form action="/importEntries" method="post" focus="title">

    <html:hidden name="method" property="method" value="importEntries"/>

    <h3><fmt:message key="weblogEntryImport.selectXML" /></h3>

    <table cellspacing="0" cellpadding="0" class="edit">
        <tr>
            <td><fmt:message key="weblogEntryImport.XMLFile" /><br />
            <html:select property="importFileName" size="1" >
                <html:options property="xmlFiles" />
            </html:select>
            </td>
        </tr>

        <tr>
            <td class="buttonBox" colspan="1">
                <input type="button" name="post" value='<fmt:message key="weblogEntryImport.button.import" />'
                        onclick="submit()" />
            </td>
        </tr>
    </table>

</html:form>

<%@ include file="/theme/footer.jsp" %>