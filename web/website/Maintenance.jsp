<%@ include file="/taglibs.jsp" %><%@ include file="/theme/header.jsp" %>

<h1><fmt:message key="maintenance.title" /></h1>
    
<html:form action="/editor/maintenance" method="post">

    <input type="hidden" name="method" value="" />

    <fmt:message key="maintenance.prompt.index" /><br />
    <input type="submit" value='<fmt:message key="maintenance.button.index" />' 
    		  onclick="this.form.method.value='index'" />
    	<br />	  
    	<br />	  
    		  
    <fmt:message key="maintenance.prompt.flush" /><br />
    <input type="submit" value='<fmt:message key="maintenance.button.flush" />' 
    		  onclick="this.form.method.value='flushCache'" />
    		  
</html:form>

<%@ include file="/theme/footer.jsp" %>


