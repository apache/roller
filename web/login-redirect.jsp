<%@ page language="java" errorPage="/error.jsp" contentType="text/html; charset=UTF-8" %><%@ 
taglib uri="http://java.sun.com/jstl/core" prefix="c" %><%@ 
taglib uri="http://www.rollerweblogger.org/tags" prefix="roller" %>
<% request.setAttribute("secure_login", 
    org.roller.config.RollerConfig.getProperty("securelogin.enabled")); %>
<c:if test='${secure_login == "true"}' >
  <roller:secure mode="unsecured" />
</c:if>
<%
String dest = "editor/weblog.do?method=create&rmk=tabbedmenu.weblog&rmik=tabbedmenu.weblog.newEntry";
response.sendRedirect(dest);
%>

