<%
   // just invalidate session and redirect
   request.getSession().invalidate();
   response.sendRedirect(request.getContextPath()+"/");
%>
