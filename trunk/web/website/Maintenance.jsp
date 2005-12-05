<%@ include file="/taglibs.jsp" %>

<p class="subtitle"><fmt:message key="maintenance.subtitle" /></p>
    
<html:form action="/editor/maintenance" method="post">
    <input type="hidden" name="method" value="" />
    <input type="hidden" name="weblog" value='<c:out value="${model.website.handle}" />' /> 

    <fmt:message key="maintenance.prompt.flush" /><br /><br />
    <input type="submit" value='<fmt:message key="maintenance.button.flush" />' 
        onclick="this.form.method.value='flushCache'" />

    <%
    boolean searchEnabled = RollerConfig.getBooleanProperty("search.enabled");
    request.setAttribute("searchEnabled", new Boolean(searchEnabled));
    %>
    <c:if test="${searchEnabled}">
        <br /><br />	  	  
        <fmt:message key="maintenance.prompt.index" /><br /><br />
        <input type="submit" value='<fmt:message key="maintenance.button.index" />' 
            onclick="this.form.method.value='index'" />    	
    </c:if>

</html:form>




