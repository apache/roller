<%@ include file="/taglibs.jsp" %><%@ include file="/theme/header.jsp"%>
<%
// this just makes the name for a custom theme available to our jstl EL
String customTheme = org.roller.pojos.Theme.CUSTOM;
request.setAttribute("customTheme", customTheme);
boolean allowCustom = org.roller.config.RollerRuntimeConfig.getBooleanProperty("themes.customtheme.allowed");
request.setAttribute("allowCustom", new Boolean(allowCustom));

String username = "";
try {
    RollerRequest rreq = RollerRequest.getRollerRequest(request);
    UserData ud = rreq.getUser();
    username = ud.getUserName();
} catch (Exception e) {
    throw new ServletException(e);
}
%>
<h1><fmt:message key="themeEditor.title" /></h1>

<form action="themeEditor.do" method="post">

    <input type=hidden name="method" value="preview" />

    <table width="95%">

        <tr>
            <td>
                <p>
                    Your current theme is : <b><c:out value="${currentTheme}"/></b><br/>
                    
                    <c:choose>
                        <c:when test="${currentTheme ne previewTheme}" >
                            <fmt:message key="themeEditor.themeBelowIsCalled" /> <b><c:out value="${previewTheme}" /></b><br/>
                            <fmt:message key="themeEditor.savePrompt" /><br/>
                            <input type="button" 
                                value='<fmt:message key="themeEditor.save" />'
                                name="saveButton" 
                                onclick="this.form.method.value='save';this.form.submit()"
                                tabindex="4" />
                            &nbsp;&nbsp;
                            <input type="button" 
                                value='<fmt:message key="themeEditor.cancel" />'
                                name="cancelButton" 
                                onclick="this.form.method.value='edit';this.form.submit()"
                                tabindex="4" />
                        </c:when>
                        
                        <c:when test="${(currentTheme ne customTheme) and allowCustom}">
                            If you like you may customize a personal copy of this theme.<br/>
                            <fmt:message key="themeEditor.saveWarning" /><br/>
                            <input type="button" 
                                value='<fmt:message key="themeEditor.customize" />'
                                name="customizeButton" 
                                onclick="this.form.method.value='customize';this.form.submit()"
                                tabindex="4" />
                        </c:when>
                  </c:choose>	
		</p>
            </td>
        </tr>

        <tr>
            <td>&nbsp;</td>
        </tr>

        <tr>
            <td>	
                <p>
                <fmt:message key="themeEditor.selectTheme" /> : 
                <select name="theme" size="1" onchange="this.form.submit()" >
                    <c:forEach var="themeName" items="${themesList}">
                        <c:choose>
                            <c:when test="${themeName eq previewTheme}">
                                <option value="<c:out value="${themeName}"/>" selected>
                                    <c:out value="${themeName}"/>
                                </option>
                            </c:when>
                            <c:otherwise>
                                <option value="<c:out value="${themeName}"/>">
                                    <c:out value="${themeName}"/>
                                </option>
                            </c:otherwise>
                        </c:choose>
                    </c:forEach>
                </select>
                </p>
            </td>
        </tr>
	
        <tr>
            <td>
                <iframe name="preview" id="preview" 
                src='<%= request.getContextPath() %>/preview/<%= username %>?theme=<c:out value="${previewTheme}"/>' 
                frameborder=1 width="100%" height="400" 
                marginheight="0" marginwidth="0"></iframe>
            </td>
        </tr>
	
    </table>

</form>


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
