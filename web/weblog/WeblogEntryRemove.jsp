<%@ include file="/taglibs.jsp" %>

<h2>
<jsp:useBean id="weblogEntryFormEx"  scope="session"
    class="org.roller.presentation.weblog.formbeans.WeblogEntryFormEx"/>
<fmt:message key="weblogEntryRemove.removeWeblogEntry" /> [<jsp:getProperty name="weblogEntryFormEx" property="title"/>]
</h2>

<p><fmt:message key="weblogEntryRemove.areYouSure" /></p>
<p>
<fmt:message key="weblogEntryRemove.entryTitle" /> = [<jsp:getProperty name="weblogEntryFormEx" property="title"/>]<br />
<fmt:message key="weblogEntryRemove.entryId" /> = [<jsp:getProperty name="weblogEntryFormEx" property="id"/>]
</p>

<table>
    <tr>
        <td>
            <html:form action="/editor/weblog" method="post">
                <input type="submit" value='<fmt:message key="weblogEntryRemove.yes" />' /></input>
                <html:hidden property="method" value="remove"/></input>
                <html:hidden property="id" /></input>
            </html:form>
        </div>
        </td>
        <td>
            <html:form action="/editor/weblog" method="post">
                <input type="submit" value='<fmt:message key="weblogEntryRemove.no" />' /></input>
                <html:hidden property="method" value="cancel"/></input>
                <html:hidden property="id" /></input>
            </html:form>
        </td>
    </tr>
</table>

