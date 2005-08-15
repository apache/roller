<% response.setContentType("text/html; charset=UTF-8"); %><!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<%@ include file="/taglibs.jsp" %><html xmlns="http://www.w3.org/1999/xhtml">
<head>
    <title><%= RollerRuntimeConfig.getProperty("site.name") %></title>
    <%-- this is included so cached pages can still set contentType --%>
    <meta http-equiv="Content-Type" content="text/html; charset=utf-8" />
    <tiles:insert attribute="head" />     
    <link rel="stylesheet" type="text/css" media="all"
        href="<%= request.getContextPath() %>/theme/layout.css" />
<style type="text/css">
.formtable td.label { 
    text-align: right;
    width: 30%; 
}
.formtable td.field { 
    width: 50%; 
}
.formtable td.description { 
    width: 0%; 
}
</style>
</head>
<body>

<div id="wrapper">
   
    <div id="header">
        <tiles:insert attribute="status" />
    </div>

    <div id="centercontent">        
        <div id="menu">
            <roller:Menu model="admin-menu.xml" view="/menu-tabbed.vm" />
        </div>
        <tiles:insert attribute="messages" />    
        <tiles:insert attribute="content" />    
        <div id="footer" class="clearfix">
            <tiles:insert attribute="footer" />
        </div>
    </div>

    <div id="rightcontent"> 
       <tiles:insert attribute="sidebar" />
    </div>
    
</div>
</body>
</html>
