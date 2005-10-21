<%@ include file="/taglibs.jsp" %>
<script type="text/javascript">
<!--
function previewImage(theme) {
    <% String ctxPath = request.getContextPath(); %>
    document.preview.src="<%= ctxPath %>/images/preview/sm-theme-" + theme + ".png";
}
function cancel() {
    document.weblogTemplateForm.method.value="cancel"; 
    document.weblogTemplateForm.submit();
}
-->
</script>

<roller:StatusMessage/>

<html:form action="/editor/page" method="post">

	<p class="subtitle">
        <fmt:message key="pageForm.editPage" />:
        <bean:write name="weblogTemplateForm" property="name" />
	</p>
	
    <table>
        <tr>
            <td><fmt:message key="pageForm.name" /><br />
            <html:text property="name" size="50"/>
            </td>
        </tr>
        <tr>
            <td><fmt:message key="pageForm.link" /><br />
            <html:text property="link" size="50"/>
            </td>
        </tr>
        <tr>
            <td><fmt:message key="pageForm.description" /><br />
            <html:textarea property="description" rows="3" cols="50"/>
            </td>
        </tr>
    </table>
    
    <fmt:message key="pageForm.template" /><br />
    <html:textarea property="contents" cols="80" rows="30" style="width: 100%" />
            
    <html:hidden property="id"/>
    <html:hidden property="name"/>
    <input type="hidden" name="method" value="update" />
    <input type="hidden" name="weblog" value='<c:out value="${model.website.handle}" />' />

    <br />
    <input type="submit" value='<fmt:message key="pageForm.save" />' /></input>
    <input type="button" value='<fmt:message key="application.done" />' onclick="cancel()" /></input>

</html:form>

<%--
Added by Matt Raible since the focus javascript generated by Struts 
doesn't seem to work for forms with duplicate named elements.
--%>
<script type="text/javascript">
<!--
    document.forms[0].elements[0].focus();
// -->
</script>



