<%@ include file="/taglibs.jsp" %>


<p class="subtitle">
<fmt:message key="pageRemove.subtitle" />
</p>

<p>
<fmt:message key="pageRemove.youSure"> 
    <fmt:param value="${page.name}" />
</fmt:message>

</p>

<p>
<fmt:message key="pageRemove.pageId" /> = [<c:out value="${page.id}" />]
<br />
<fmt:message key="pageRemove.pageName" /> = [<c:out value="${page.name}" />]
</p>

<table>
<tr>
	<td>
		<html:form action="/editor/page" method="post">
			<input type="submit" value='<fmt:message key="application.yes" />' ></input>
			<html:hidden property="method" value="remove"/></input>
			<html:hidden property="id" /></input>
		</html:form>
	</td>
	<td>
		<html:form action="/editor/page" method="post">
			<input type="submit" value='<fmt:message key="application.no" />' ></input>
			<html:hidden property="id" /></input>
			<html:hidden property="method" value="cancel"/></input>
		</html:form>
	</td>
</tr>
</table>



