<%@ include file="/taglibs.jsp" %>
<%
String themename = RollerConfig.getProperty("editor.theme");
String logourl = "/theme/" + themename + "/logo.gif";
request.setAttribute("logourl", logourl);
%>
<div class="bannerBox">
   <img class="bannerlogo" src='<c:url value="${logourl}" />' />
</div>
