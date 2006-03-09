<%@ page language="java" contentType="text/html; charset=UTF-8" %>

<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt"  prefix="fmt" %>
<%@ taglib uri="http://struts.apache.org/tags-bean"   prefix="bean" %>
<%@ taglib uri="http://struts.apache.org/tags-html"   prefix="html" %>
<%@ taglib uri="http://struts.apache.org/tags-logic"  prefix="logic" %>
<%@ taglib uri="http://struts.apache.org/tags-tiles"  prefix="tiles" %>
<%@ taglib uri="http://jakarta.apache.org/taglibs/string-1.0.1" prefix="str" %>

<%@ taglib uri="http://www.rollerweblogger.org/tags" prefix="roller" %>

<%@ page import="javax.servlet.jsp.jstl.core.Config" %>

<%@ page import="org.roller.model.Roller" %>
<%@ page import="org.roller.model.RollerFactory" %>

<%@ page import="org.roller.pojos.UserData" %>
<%@ page import="org.roller.pojos.WebsiteData" %>
<%@ page import="org.roller.pojos.RollerConfigData" %>

<%@ page import="org.roller.config.RollerConfig" %>
<%@ page import="org.roller.config.RollerRuntimeConfig" %>
<%@ page import="org.roller.config.RollerConfig" %>

<%@ page import="org.roller.presentation.RollerRequest" %>
<%@ page import="org.roller.presentation.BasePageModel" %>
<%@ page import="org.roller.presentation.RollerContext" %>
<%@ page import="org.roller.presentation.RollerSession" %>
<%@ page import="org.roller.presentation.LanguageUtil" %>

<%   
// see if we have an authenticated user so we can set the display locale
RollerSession rSession = RollerSession.getRollerSession(request);
UserData mUser = null;
if(rSession != null) {
    mUser = rSession.getAuthenticatedUser();
}

if(mUser != null) {
    request.setAttribute("mLocale", mUser.getLocale());
} else {
    request.setAttribute("mLocale", 
            LanguageUtil.getViewLocale(request).getDisplayName());
}
%>
<fmt:setLocale value="${mLocale}" />
<fmt:setBundle basename="ApplicationResources" />

<%-- Set Struts tags to use XHTML --%>
<html:xhtml />