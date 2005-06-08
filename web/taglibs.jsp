<%@ page language="java" errorPage="/error.jsp" contentType="text/html; charset=UTF-8" %>
<%@ taglib uri="http://java.sun.com/jstl/core"                  prefix="c" %>
<%@ taglib uri="http://java.sun.com/jstl/fmt"                   prefix="fmt" %>
<%@ taglib uri="http://jakarta.apache.org/struts/tags-bean"     prefix="bean" %>
<%@ taglib uri="http://jakarta.apache.org/struts/tags-html"     prefix="html" %>
<%@ taglib uri="http://jakarta.apache.org/struts/tags-logic"    prefix="logic" %>
<%@ taglib uri="http://jakarta.apache.org/taglibs/string-1.0.1" prefix="str" %>
<%@ taglib uri="http://www.rollerweblogger.org/tags"            prefix="roller" %>
<%@ page import="org.roller.model.Roller" %>
<%@ page import="org.roller.pojos.UserData" %>
<%@ page import="org.roller.pojos.RollerConfig" %>
<%@ page import="org.roller.presentation.RollerContext" %>
<%@ page import="org.roller.presentation.RollerRequest" %>
<%@ page import="org.roller.presentation.LanguageUtil" %>
<%@ page import="javax.servlet.jsp.jstl.core.Config" %>

<%   // fmt:setLocale doesn't accept an expression, hence this hack
     Config.set(pageContext, Config.FMT_LOCALE, LanguageUtil.getViewLocale(request), pageContext.PAGE_SCOPE); %>
<fmt:setBundle basename="org.roller.presentation.ApplicationResources" />

<%-- Set all pages that include this page to use XHTML --%>
<html:xhtml />


