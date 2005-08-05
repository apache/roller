<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN"
"http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">    
<% try { %><%@ page import="org.roller.model.Roller" %>
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

<h2><fmt:message key="error.permissionDenied.title" /></h2>
<fmt:message key="error.permissionDenied.prompt" />
<ul>
<li><fmt:message key="error.permissionDenied.reason0" /></li>
<li><fmt:message key="error.permissionDenied.reason1" /></li>
<li><fmt:message key="error.permissionDenied.reason2" /></li>
<li><fmt:message key="error.permissionDenied.reason3" /></li>
</ul>

<br />
<br />

        <div id="footer" class="clearfix">
            <a href="http://www.rollerweblogger.org">
                Powered by Roller Weblogger</a> | 
                
            <a href="http://opensource.atlassian.com/projects/roller/Dashboard.jspa">
                <fmt:message key="footer.reportIssue" /></a> | 
                
            <a href="http://www.rollerweblogger.org/wiki/Wiki.jsp?page=UserGuide">
                <fmt:message key="footer.userGuide" /></a> | 
                
            <a href="http://www.rollerweblogger.org/wiki/Wiki.jsp?page=RollerMacros">
                <fmt:message key="footer.macros" /></a> | 
                
            <a href="http://sourceforge.net/mail/?group_id=47722">
                <fmt:message key="footer.mailingLists" /></a>
        </div><!-- end footer -->
    
</div> <!-- end centercontent --> 

<div id="rightcontent"> 
</div>

</div> <!-- end wrapper -->

</body>
</html>

<% } catch (Exception e) { e.printStackTrace(); } %>

