<%@ page import="org.roller.presentation.RollerSession" %>
<%
String dest = "login.jsp?error=true";

// This server-side redirect may work on some servers.
// Comment it out on OC4J. 
response.sendRedirect(dest);
%>

<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN">
<html>
<head>
  <title></title>
<meta http-equiv="refresh" 
	content="0;url=<%= dest %>">
</head>
<body bgcolor="#ffffff">
</body>
</html>



