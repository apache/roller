<%@ page import="javax.servlet.*" %>
<%
   request.getSession().invalidate();
   RequestDispatcher rd = request.getRequestDispatcher("/index.jsp");
   rd.forward(request, response);
%>
