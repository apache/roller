<%@ include file="/taglibs.jsp" %>

<p class="subtitle">
<fmt:message key="websiteRemove.subtitle" />
</p>

<p>
<fmt:message key="websiteRemove.youSure"> 
    <fmt:param value="${website.name}" />
</fmt:message>
<br/>
<span class="warning">
    <fmt:message key="websiteSettings.removeWebsiteWarning" />
</span>
</p>

<p>
<fmt:message key="websiteRemove.websiteId" /> = [<c:out value="${website.id}" />]
<br />
<fmt:message key="websiteRemove.websiteName" /> = [<c:out value="${website.name}" />]
</p>

<table>
<tr>
	<td>
		<html:form action="/editor/website" method="post">
			<input type="submit" value='<fmt:message key="application.yes" />' ></input>
			<html:hidden property="method" value="remove"/></input>
			<html:hidden property="id" /></input>
		</html:form>
	</td>
	<td>
		<html:form action="/editor/website" method="post">
			<input type="submit" value='<fmt:message key="application.no" />' ></input>
			<input type="hidden" name="weblog" value='<c:out value="${website.handle}" />' />
			<html:hidden property="id" /></input>
			<html:hidden property="method" value="edit"/></input>
		</html:form>
	</td>
</tr>
</table>



