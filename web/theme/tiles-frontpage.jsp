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
#banner {
    height: 30px; 
}
#centercontent {
    margin: 0px 230px 15px 15px;
    padding: 0px 25px 0px 0px;
}
#rightcontent {
    position: absolute;
    top:     35px;
    right:   10px;
    width:  230px;
}
</style>
</head>
<body>

<div id="wrapper">
   
    <div id="banner">
        <tiles:insert attribute="status" /><br />
    </div>
    
    <div id="leftcontent"> 
    </div>
    
    <div id="centercontent">   
        <tiles:insert attribute="content" />    
    </div>
    
    <div id="rightcontent"> 
       <tiles:insert attribute="sidebar" />
    </div>  
 
    <div id="footer">
        <tiles:insert attribute="footer" />
    </div> 

</div>

</body>
</html>
