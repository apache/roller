<%
response.setStatus(response.SC_MOVED_PERMANENTLY);
response.setHeader("Location", request.getContextPath()+"/");
%>