<% response.setContentType("text/html; charset=UTF-8"); %><!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
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

    <%@ include file="messages.jsp" %>

