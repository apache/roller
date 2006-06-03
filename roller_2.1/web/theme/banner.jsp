<%@ include file="/taglibs.jsp" %>

<%
String theme = request.getParameter("look");
if (theme == null) theme = RollerConfig.getProperty("editor.theme");
String logourl = "/theme/" + theme + "/logo.gif";
request.setAttribute("logourl", logourl);
%>
<!--
<div class="bannerBox">
    <div id="logoshadow">
       <div id="logoimage">
       </div>
    </div>
</div>
-->

<div class="bannerBox">

<div id="logo">
    <!-- Transparent PNG fix for IE, thanks to Kenneth M. Kolano -->
    <div id="logoshadow" 
        style="_background-image:none; filter:progid:DXImageTransform.Microsoft.AlphaImageLoader(src='<c:out value='${model.baseURL}' />/theme/logo-shadow.png',sizingMethod='crop');">
        <div id="logobackground">
            <a href='<c:out value="${model.baseURL}" />/main.do' id="logoimage" 
                style="_background-image:none; filter:progid:DXImageTransform.Microsoft.AlphaImageLoader(src='<c:out value='${model.baseURL}' />/theme/transparent-logo.png', sizingMethod='crop');">
            </a>
        </div>
    </div>
</div>

</div>