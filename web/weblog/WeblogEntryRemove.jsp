<%@ include file="/taglibs.jsp" %><%@ include file="/theme/header.jsp" %>

<h3>
<jsp:useBean id="weblogEntryFormEx"  scope="session"
    class="org.roller.presentation.weblog.formbeans.WeblogEntryFormEx"/>
<fmt:message key="weblogEntryRemove.removeWeblogEntry" /> [<jsp:getProperty name="weblogEntryFormEx" property="title"/>]
</h3>

<p><fmt:message key="weblogEntryRemove.areYouSure" /></p>
<p>
Entry title = [<jsp:getProperty name="weblogEntryFormEx" property="title"/>]<br />
Entry id = [<jsp:getProperty name="weblogEntryFormEx" property="id"/>]
</p>

<div style="float:left">
<html:form action="/editor/weblog" method="post">
    <input type="submit" value='<fmt:message key="weblogEntryRemove.yes" />' /></input>
    <html:hidden property="method" value="remove"/></input>
    <html:hidden property="id" /></input>
</html:form>
</div>

<div style="float:left">
<html:form action="/editor/weblog" method="post">
    <input type="submit" value='<fmt:message key="weblogEntryRemove.no" />' /></input>
    <html:hidden property="method" value="cancel"/></input>
    <html:hidden property="id" /></input>
</html:form>
</div>

<br />
<br />

<%@ include file="/theme/footer.jsp" %>
