
<%@ include file="/theme/header.jsp" %>

<h3>Export Website to XML</h3>

<p>The link below will retrieve all of your user data in Roller's own XML format.</p>

<p>
<a href="<%= request.getContextPath()+"/ExportServlet" %>">EXPORT</a>
</p>

<%@ include file="/theme/footer.jsp" %>

