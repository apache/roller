<%@ include file="/taglibs.jsp" %>

<%-- 
This default stuff goes in the HTML head element of each page
You can override it with your own file via WEB-INF/tiles-def.xml
--%>

<script type="text/javascript"
    src="<%= request.getContextPath() %>/theme/scripts/roller.js"></script> 
   
<link rel="stylesheet" type="text/css" media="all"
     href="<%= request.getContextPath() %>/theme/roller.css" />
     
<link rel="stylesheet" type="text/css" media="all"
     href="<%= request.getContextPath() %>/theme/tan/colors.css" />
     
<link rel="stylesheet" type="text/css" media="all"
     href="<%= request.getContextPath() %>/theme/menu.css" />
<link rel="stylesheet" type="text/css" media="all"
     href="<%= request.getContextPath() %>/theme/calendar.css" />        
<script type="text/javascript"
    src="<%= request.getContextPath() %>/tags/calendar.js"></script>
<script type="text/javascript"
    src="<%= request.getContextPath() %>/theme/scripts/overlib.js"
    ><!-- overLIB (c) Erik Bosrup --></script>  



