<%@ include file="/taglibs.jsp" %>

<%
String theme = request.getParameter("theme");
if (theme == null) theme = RollerConfig.getProperty("editor.theme");
String logourl = "/theme/" + theme + "/logo.gif";
request.setAttribute("logourl", logourl);
%>
<div class="bannerBox">
   <img class="bannerlogo" src='<c:url value="${logourl}" />' />
</div>
