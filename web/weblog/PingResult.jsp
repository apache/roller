<%@ page import="org.roller.presentation.RollerRequest"%>
<%@ include file="/taglibs.jsp" %><%@ include file="/theme/header.jsp" %>

<!-- This page consists only of result messages from the ping -->

<div align="center">
<p/>
&nbsp;
<p/>
<form action="pingSetup.do" method="post">
<input type="submit" value='<fmt:message key="pingResult.OK" />' />
<form>
</div>

<%@ include file="/theme/footer.jsp" %>
