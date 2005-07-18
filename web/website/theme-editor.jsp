<%@ include file="/taglibs.jsp" %><%@ include file="/theme/header.jsp"%>
<%
String username = "";
try
{
    RollerSession rollerSession = RollerSession.getRollerSession(request);
    WebsiteData wd = rollerSession.getCurrentWebsite();
    username = wd.getHandle();
}
catch (Exception e)
{
    throw new ServletException(e);
}
%>
<h1><fmt:message key="themeEditor.title" /></h1>

<html:form action="/editor/themeEditor" method="post">

<input type=hidden name="method" value='preview' />

<table width="95%">

	<tr>
		<td>
      <c:if test='${empty themeEditorForm.themeName || themeEditorForm.themeName == "Custom"}' >
          <p><fmt:message key="themeEditor.yourThemeIsShownBelow" /></p>
      </c:if>
      <c:if test='${!empty themeEditorForm.themeName && themeEditorForm.themeName != "Custom"}' >
          <p><fmt:message key="themeEditor.themeBelowIsCalled" /> <b><c:out value="${themeEditorForm.themeName}" /></b></p>
          <p><fmt:message key="themeEditor.savePrompt" /></p>
          <p><fmt:message key="themeEditor.saveWarning" /></p>  
		      <input type="button" value='<fmt:message key="themeEditor.save" />' name="saveButton" 
			       onclick="this.form.method.value='save';this.form.submit()" tabindex="4" />   
		         &nbsp; &nbsp;	
		      <input type="button" value='<fmt:message key="themeEditor.cancel" />' name="cancelButton" 
			       onclick="this.form.method.value='cancel';this.form.submit()" tabindex="5" />
      </c:if>			
		</td>
	</tr>

	<tr>
		<td>&nbsp;</td>
	</tr>

	<tr>
		<td>	
		    <p><fmt:message key="themeEditor.selectTheme" />:        
		    <html:select property="themeName" size="1" onchange="this.form.submit()" >
	          <html:options name="themes"/>
		    </html:select>	</p>
		</td>
	</tr>
	
	<tr>
		<td>
		<iframe name="preview" id="preview" 
			src="<%= request.getContextPath() %>/preview/<%= username %>/" 
			frameborder=1 width="100%" height="400" 
			marginheight="0" marginwidth="0"></iframe>
		</td>
	</tr>
	
</table>

</html:form>


<script type="text/javascript">
<!--
    function save()
    {
        //alert(document.themeEditorForm.method.value);
        document.themeEditorForm.method.value = "save";
        document.themeEditorForm.submit();
    }
// -->
</script>

<%@ include file="/theme/footer.jsp"%>