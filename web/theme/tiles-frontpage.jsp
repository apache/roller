<% response.setContentType("text/html; charset=UTF-8"); %><!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<%@ include file="/taglibs.jsp" %><html xmlns="http://www.w3.org/1999/xhtml">
<%
boolean planetEnabled = 
    RollerConfig.getBooleanProperty("planet.aggregator.enabled");
request.setAttribute("planetEnabled", new Boolean(planetEnabled));

String siteTitle = RollerRuntimeConfig.getProperty("site.name");
request.setAttribute("siteTitle", siteTitle);

String siteDescription = RollerRuntimeConfig.getProperty("site.description");
request.setAttribute("siteDescription", siteDescription);
%>
<head>
<title><%= RollerRuntimeConfig.getProperty("site.shortName") %></title>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8" />
<tiles:insert attribute="head" />     
<style type="text/css">
.menuItemTable {
    width: 100%;
    height: 2px;
    padding: 0px 0px 0px 10px;
}
   <tiles:insert attribute="styles" />
</style>
</head>
<body>

<div id="wrapper">
   
    <div id="banner">
        <tiles:insert attribute="banner" />
        <tiles:insert attribute="bannerStatus" />
    </div>
    
    <div id="leftcontent"> 
    </div>
    
    <div id="centercontent"> 
        <h1><c:out value="${siteTitle}" /></h1>
        <p class="subtitle"><c:out value="${siteDescription}" /></p>
        <c:choose>
            <c:when test="${planetEnabled}">
               <roller:Menu model="planet-menu.xml" view="/menu-tabbed.vm" />
            </c:when>
            <c:otherwise>
            </c:otherwise>
        </c:choose>       
        <tiles:insert attribute="content" />    
        <div id="footer">
            <tiles:insert attribute="footer" />
        </div> 
    </div>
    
    <div id="rightcontent">
       <tiles:insert attribute="search" />
       <tiles:insert attribute="sidebar" />
    </div>  
 
</div>

</body>
</html>
