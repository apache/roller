<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN"
"http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">    
<%@ page import="org.roller.model.Roller" %>
<%@ page import="org.roller.pojos.UserData" %>
<%@ page import="org.roller.presentation.RollerRequest" %>
<%@ include file="/taglibs.jsp" %>
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<title>Roller :: Editor</title>
    <meta http-equiv="Content-Type" content="text/html; charset=utf-8" />
    <script type="text/javascript" 
        src="<%= request.getContextPath() %>/theme/scripts/roller.js"></script>
    <link rel="stylesheet" type="text/css" title="The Original"
        href="<%= request.getContextPath() %>/theme/roller.css" />
    <link rel="stylesheet" type="text/css" title="The Original"
        href="<%= request.getContextPath() %>/theme/layout.css" />
    </head>
<body>
<div id="content">
		
<% try { %>
   <roller:NavigationBar/>    	
<% } 
catch (Exception e) 
{
   System.err.println("ERROR: while displaying error page error page");
   e.printStackTrace();
} %>

<%@ page import="java.io.*,org.roller.util.Utilities" isErrorPage="true" %>
    
<%
java.util.Locale locale = request.getLocale();
java.util.ResourceBundle bundle = 
   java.util.ResourceBundle.getBundle("ApplicationResources",locale);

final Object codeObj, messageObj, typeObj, exceptionObj;
codeObj = request.getAttribute("javax.servlet.error.status_code");
messageObj = request.getAttribute("javax.servlet.error.message");
typeObj = request.getAttribute("javax.servlet.error.type");

String code=null, message=null, type=null;
if ( null != codeObj ) code = codeObj.toString();
if ( null != messageObj ) message = messageObj.toString();
if ( null != typeObj ) type = typeObj.toString();
String reason = null != code ? code : type;

exception = (Exception)request.getAttribute("javax.servlet.error.exception");
%>
<br />
<h2 class="error"><fmt:message key="errorPage.title" /></h2>
<p><fmt:message key="errorPage.message" /></p>
<p><%= message %></p>
<p><b><fmt:message key="errorPage.reason" /></b>: <%= reason %></p>
<% if ( null != exception ) 
{ %>
   <form><textarea rows="30" style="font-size:8pt;width:95%">
   <% exception.printStackTrace(new java.io.PrintWriter(out)); %>
   </textarea></form>
<% } %>
<%@ include file="/theme/footer.jsp" %>

    
