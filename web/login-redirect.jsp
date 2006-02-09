<%@ page language="java" errorPage="/error.jsp" contentType="text/html; charset=UTF-8" %><%@ 
taglib uri="http://java.sun.com/jstl/core" prefix="c" %><%@ 
taglib uri="http://www.rollerweblogger.org/tags" prefix="roller" %><%@ 
page import="org.roller.model.*" %><%@
page import="org.roller.pojos.*" %><%@
page import="org.roller.config.RollerConfig" %><%@
page import="org.roller.presentation.RollerSession" %><%@
page import="java.util.List" %>
<%
Roller roller = RollerFactory.getRoller();
RollerSession rollerSession = RollerSession.getRollerSession(request);
UserData user = rollerSession.getAuthenticatedUser();
List websites = roller.getUserManager().getWebsites(user, Boolean.TRUE, null);
if (websites.size() == 1) {
    WebsiteData website = (WebsiteData)websites.get(0);
    website.hasUserPermissions(user, PermissionsData.LIMITED);
    response.sendRedirect(
        "editor/weblog.do?method=create&rmk=tabbedmenu.weblog&rmik=tabbedmenu.weblog.newEntry&weblog="+website.getHandle());
} else {
    response.sendRedirect(
       "editor/yourWebsites.do?method=edit&rmik=tabbedmenu.user.websites");
}
%>

