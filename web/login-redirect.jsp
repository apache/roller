<%@ page import="org.roller.presentation.RollerSession" %>
<%
// Send em back to where they where when they clicked 'login'
String dest = RollerSession.getBreadCrumb( request );
if (dest == null 
   || dest.indexOf("login.jsp") != -1 
   || dest.indexOf("login-redirect.jsp") != -1 
   || dest.indexOf("user.do") != -1
   || dest.indexOf("comment.do") != -1 )
{
    // Else send em to the Editor UI edit-weblog page
    dest = "weblog.do?method=create&rmk=tabbedmenu.weblog&rmik=tabbedmenu.weblog.newEntry";
}
// This server-side redirect may work on some servers.
// Comment it out on OC4J. 
response.sendRedirect(dest);
//request.getRequestDispatcher(dest).forward(request, response);
%>
<%= dest %>



