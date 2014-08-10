<%@tag%>
<%@ attribute name="key" required="true" rtexprvalue="true" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt"%>
<%@ taglib uri="/struts-tags" prefix="s" %>

<img src="<s:url value='/images/help.png'/>" border="0" alt="icon" title="<fmt:message key='${key}'/>" />
