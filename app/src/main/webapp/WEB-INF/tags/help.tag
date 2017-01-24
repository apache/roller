<%@tag%>
<%@ attribute name="key" required="true" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt"  prefix="fmt" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>

<img src="<c:url value='/images/help.png'/>" border="0" alt="icon" title="<fmt:message key='${key}'/>" />
