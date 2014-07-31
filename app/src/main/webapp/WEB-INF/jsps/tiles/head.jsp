<%-- 
This default stuff goes in the HTML head element of each page
You can override it with your own file via WEB-INF/tiles-def.xml
--%>
<%@ include file="/WEB-INF/jsps/taglibs-struts2.jsp" %>

<link rel="stylesheet" media="all" href="<s:url value='/roller-ui/yui3/cssreset/cssreset-min.css'/>" />
<link rel="stylesheet" media="all" href="<s:url value='/roller-ui/yui3/cssfonts/cssfonts-min.css'/>" />
<link rel="stylesheet" media="all" href="<s:url value='/roller-ui/yui3/cssbase/cssbase-min.css'/>" />

<link rel="stylesheet" media="all" href='<s:url value="/roller-ui/styles/layout.css"/>' />
<link rel="stylesheet" media="all" href='<s:url value="/roller-ui/styles/roller.css"/>' />
<link rel="stylesheet" media="all" href='<s:url value="/roller-ui/styles/menu.css"/>' />

<link rel="stylesheet" media="all" href="<s:url value="/roller-ui/theme/"/><s:property value="getProp('editor.theme')"/>/colors.css" />

<script src="<s:url value="/theme/scripts/roller.js"/>"></script>
