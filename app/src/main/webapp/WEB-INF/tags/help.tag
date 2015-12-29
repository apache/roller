<%@tag%>
<%@ attribute name="key" required="true" %>
<%@ taglib uri="/struts-tags" prefix="s" %>

<s:set var="name">${key}</s:set>
<img src="<s:url value='/images/help.png'/>" border="0" alt="icon" title="<s:text name='%{#name}'/>" />
