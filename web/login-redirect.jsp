<%@ page language="java" errorPage="/error.jsp" contentType="text/html; charset=UTF-8" %><%@ 
taglib uri="http://java.sun.com/jstl/core" prefix="c" %><%@ 
taglib uri="http://www.rollerweblogger.org/tags" prefix="roller" %>
<% request.setAttribute("secure_login", 
    org.roller.config.RollerConfig.getProperty("securelogin.enabled")); %>
<c:if test='${secure_login == "true"}' >
  <roller:secure mode="unsecured" />
</c:if>
<%
// ROLLER_2.0: if user has one website, then go to editor page instead (as before)
String dest = "editor/yourWebsites.do?method=edit&rmik=tabbedmenu.user.websites";
response.sendRedirect(dest);
%>

