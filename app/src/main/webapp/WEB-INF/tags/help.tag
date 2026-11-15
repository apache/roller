<%@tag%>
<%@ attribute name="key" required="true" rtexprvalue="true" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt"%>

<span class="bi bi-question-circle" role="img" aria-label="<fmt:message key='${key}'/>" title="<fmt:message key='${key}'/>"></span>
