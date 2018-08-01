<%-- 
This default stuff goes in the HTML head element of each page
You can override it with your own file via WEB-INF/tiles-def.xml
--%>
<%@ include file="/WEB-INF/jsps/tightblog-taglibs.jsp" %>

<link rel="stylesheet" media="all" href="<c:url value='/tb-ui/yui3/cssreset/cssreset-min.css'/>" />
<link rel="stylesheet" media="all" href="<c:url value='/tb-ui/yui3/cssfonts/cssfonts-min.css'/>" />
<link rel="stylesheet" media="all" href="<c:url value='/tb-ui/yui3/cssbase/cssbase-min.css'/>" />

<link rel="stylesheet" media="all" href="<c:url value='/tb-ui/styles/layout.css'/>" />
<link rel="stylesheet" media="all" href="<c:url value='/tb-ui/styles/tightblog.css'/>" />
<link rel="stylesheet" media="all" href="<c:url value='/tb-ui/styles/menu.css'/>" />

<link rel="stylesheet" media="all" href="<c:url value='/tb-ui/theme/colors.css'/>" />

<sec:csrfMetaTags />
