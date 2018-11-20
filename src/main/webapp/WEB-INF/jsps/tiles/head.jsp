<%-- 
This default stuff goes in the HTML head element of each page
You can override it with your own file via WEB-INF/tiles-def.xml
--%>
<%@ include file="/WEB-INF/jsps/tightblog-taglibs.jsp" %>

<link rel="stylesheet" href="https://stackpath.bootstrapcdn.com/bootstrap/4.1.3/css/bootstrap.min.css" integrity="sha384-MCw98/SFnGE8fJT3GXwEOngsV7Zt27NXFoaoApmYm81iuXoPkFOJwJ8ERdknLPMO" crossorigin="anonymous">

<link rel="stylesheet" media="all" href="<c:url value='/tb-ui/styles/tbeditorui.css'/>" />
<link rel="stylesheet" media="all" href="<c:url value='/tb-ui/theme/colors.css'/>" />

<sec:csrfMetaTags />
