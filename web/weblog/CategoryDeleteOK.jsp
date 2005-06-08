<% 
try { 
%>

<%@ include file="/theme/header.jsp" %>

<script type="text/javascript">
<!-- 
function deleteYes() 
{
    document.categoryDeleteForm.confirmDelete.value = "true";
    document.categoryDeleteForm.submit();
}
function deleteNo() 
{
    document.categoryDeleteForm.confirmDelete.value = "false";
    document.categoryDeleteForm.submit();
}
//-->
</script>


<h3>
<fmt:message key="categoryDeleteOK.removeCategory" />
[<c:out value="${categoryDeleteForm.name}" />]
</h3>

<html:form action="/categoryDelete" method="post">

	<html:hidden property="catid" />
	<html:hidden property="confirmDelete" />

	<c:if test="${categoryDeleteForm.inUse}" >
		<span class="warning">
		    <fmt:message key="categoryDeleteOK.warningCatInUse" />
		</span>
		<p><fmt:message key="categoryDeleteOK.youMustMoveEntries" /><p>
		<fmt:message key="categoryDeleteOK.moveToWhere" />
		<html:select property="moveToWeblogCategoryId" size="1">
			<html:optionsCollection property="cats" label="name" value="id" />
		</html:select>
		</p>
	</c:if>

	<c:if test="${!categoryDeleteForm.inUse}" >
		<p><fmt:message key="categoryDeleteOK.noEntriesInCat" /></p>
	</c:if>

	<p>
	<strong><fmt:message key="categoryDeleteOK.areYouSure" /></strong>
	</p>

	<input type="button" value="Yes" onclick="deleteYes()" />
	<input type="button" value="No" onclick="deleteNo()" />

</html:form>


<%@ include file="/theme/footer.jsp" %>

<% 
} catch (Throwable e) {
e.printStackTrace();
}
%>
