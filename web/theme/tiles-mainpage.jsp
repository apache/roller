<% response.setContentType("text/html; charset=UTF-8"); %><!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<%@ include file="/taglibs.jsp" %>
<% try { %><html xmlns="http://www.w3.org/1999/xhtml">
<head>
    <title><%= RollerRuntimeConfig.getProperty("site.name") %></title>
    <%-- this is included so cached pages can still set contentType --%>
    <meta http-equiv="Content-Type" content="text/html; charset=utf-8" />
    <script type="text/javascript"
        src="<%= request.getContextPath() %>/theme/scripts/roller.js"></script>    
    <link rel="stylesheet" type="text/css" media="all"
         href="<%= request.getContextPath() %>/theme/layout.css" />
    <link rel="stylesheet" type="text/css" media="all"
         href="<%= request.getContextPath() %>/theme/roller.css" />
    <link rel="stylesheet" type="text/css" media="all"
         href="<%= request.getContextPath() %>/theme/menu.css" />
    <link rel="stylesheet" type="text/css" media="all"
         href="<%= request.getContextPath() %>/theme/calendar.css" />        
    <script type="text/javascript"
        src="<%= request.getContextPath() %>/tags/calendar.js"></script>
    <script type="text/javascript"
        src="<%= request.getContextPath() %>/theme/scripts/overlib.js"
        ><!-- overLIB (c) Erik Bosrup --></script>       
</head>
<body>

<div id="wrapper">
    
<div id="banner">
</div>
  
<div id="centercontent">
    <roller:Menu model="editor-menu.xml" view="/menu-tabbed.vm" />   
    <br />
    <br />
    <tiles:insert attribute="content" />
    
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
   <tiles:insert attribute="sidebar" />
</div>

</div> <!-- end wrapper -->

</body>
</html>

<% } catch (Exception e) { e.printStackTrace(); } %>
