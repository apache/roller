<%@ page language="java" errorPage="/error.jsp" contentType="text/html; charset=UTF-8" %><%@ 
taglib uri="http://java.sun.com/jstl/core"   prefix="c" %><%@ 
taglib uri="http://java.sun.com/jstl/fmt"    prefix="fmt" %><%@ 
taglib uri="http://struts.apache.org/tags-bean"  prefix="bean" %><%@ 
taglib uri="http://struts.apache.org/tags-html"  prefix="html" %><%@ 
taglib uri="http://struts.apache.org/tags-logic" prefix="logic" %><%@ 
taglib uri="http://struts.apache.org/tags-tiles" prefix="tiles" %><%@ 
taglib uri="http://jakarta.apache.org/taglibs/string-1.0.1" prefix="str" %><%@ 
taglib uri="http://www.rollerweblogger.org/tags" prefix="roller" %><%@ 
page import="org.roller.model.Roller" %><%@ 
page import="org.roller.model.RollerFactory" %><%@ 
page import="org.roller.pojos.UserData" %><%@ 
page import="org.roller.pojos.WebsiteData" %><%@ 
page import="org.roller.pojos.RollerConfigData" %><%@ 
page import="org.roller.presentation.RollerContext" %><%@ 
page import="org.roller.presentation.RollerSession" %><%@ 
page import="org.roller.config.RollerConfig" %><%@ 
page import="org.roller.presentation.RollerRequest" %><%@ 
page import="org.roller.config.RollerRuntimeConfig" %><%@ 
page import="org.roller.config.RollerConfig" %><%@ 
page import="org.roller.presentation.LanguageUtil" %><%@ 
page import="javax.servlet.jsp.jstl.core.Config" %><%   
// fmt:setLocale doesn't accept an expression, hence this hack
Config.set(pageContext, Config.FMT_LOCALE, LanguageUtil.getViewLocale(request), pageContext.PAGE_SCOPE); 
%><fmt:setBundle basename="ApplicationResources" /><%-- 
Set all pages that include this page to use XHTML --%><html:xhtml />