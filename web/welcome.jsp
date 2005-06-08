
<%@ include file="/theme/header.jsp" %>

<h2><fmt:message key="welcome.title" /></h2>

<p><fmt:message key="welcome.accountCreated" /></p>

<p><b><fmt:message key="welcome.addressIs" />:</b> <c:out value="${weblogURL}"/></p>

<p><b><fmt:message key="welcome.rssAddressIs" />:</b> <c:out value="${rssURL}"/></p>

<p><a href="weblog.do?method=create&amp;rmik=New%20Entry"><fmt:message key="welcome.clickHere" /></a> 
<fmt:message key="welcome.toLoginAndPost" /></p>

<%@ include file="/theme/footer.jsp" %>

