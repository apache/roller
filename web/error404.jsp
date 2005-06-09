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
<br />
<h2 class="error"><fmt:message key="error.title.404" /></h2>
<p><fmt:message key="error.text.404" /></p>

<%@ include file="/theme/footer.jsp" %>

    
