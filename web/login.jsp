<% response.setContentType("text/html; charset=UTF-8"); %><!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<%@ include file="/taglibs.jsp" %>
<% request.setAttribute("secure_login", 
    org.roller.config.RollerConfig.getProperty("securelogin.enabled")); %>
<c:if test='${secure_login == "true"}' >
  <roller:secure mode="secured" />
</c:if>
<% try { %><html xmlns="http://www.w3.org/1999/xhtml">
<head>
    <title><%= RollerRuntimeConfig.getProperty("site.name") %></title>
    <%-- this is included so cached pages can still set contentType --%>
    <meta http-equiv="Content-Type" content="text/html; charset=utf-8" />
    <script type="text/javascript"
        src="<%= request.getContextPath() %>/theme/scripts/roller.js"></script>    
    <link rel="stylesheet" type="text/css" media="all"
         href="<%= request.getContextPath() %>/theme/layout.css" />
    <link rel="stylesheet" type="text/css" media="all"
         href="<%= request.getContextPath() %>/theme/roller.css" />
    <link rel="stylesheet" type="text/css" media="all"
         href="<%= request.getContextPath() %>/theme/menu.css" />
    <link rel="stylesheet" type="text/css" media="all"
         href="<%= request.getContextPath() %>/theme/calendar.css" />        
    <script type="text/javascript"
        src="<%= request.getContextPath() %>/tags/calendar.js"></script>
    <script type="text/javascript"
        src="<%= request.getContextPath() %>/theme/scripts/overlib.js"
        ><!-- overLIB (c) Erik Bosrup --></script>       
</head>
<body>

<div id="wrapper">
    
<div id="banner">
</div>

<div id="loginTable">

<form method="post" 
      id="loginForm" 
	  action="<c:url value="/auth/"/>"
	  onsubmit="saveUsername(this)">
<table width="100%">
    <tr>
        <td colspan="2">
            <logic:present parameter="error">
                <div class="error">
                    <bean:message key="error.password.mismatch"/>
                </div>
            </logic:present>
        </td>
    </tr>
    <tr>
        <th><fmt:message key="loginPage.userName" />:</th>
        <td>
            <input type="text" name="j_username" id="j_username" size="25" />
        </td>
    </tr>
    <tr>
        <th><fmt:message key="loginPage.password" />:</th>
        <td>
            <input type="password" name="j_password" id="j_password" size="20" />
            <!-- for Resin -->
            <input type="hidden" name="j_uri" id="j_uri" value="" />
        </td>
    </tr>
    <c:if test="${rememberMeEnabled}">
    <tr>
        <td></td>
        <td>
            <input type="checkbox" name="rememberMe" id="rememberMe" />
            <label for="rememberMe">
                <fmt:message key="loginPage.rememberMe" />
            </label>
        </td>
    </tr>
    </c:if>
    <tr>
        <td></td>
        <td>
            <input type="submit" name="login" id="login" value="<fmt:message key="loginPage.login" />" />
        	<input type="reset" name="reset" id="reset" value="<fmt:message key="loginPage.reset" />" 
                onclick="document.getElementById('j_username').focus()" />
        </td>
    </tr>
</table>
</form>

<script type="text/javascript">
<!--

if (document.getElementById) {
    if (getCookie("username") != null) {
        if (document.getElementById) {
            document.getElementById("j_username").value = getCookie("username");
            document.getElementById("j_password").focus();
        }
    } else {
        document.getElementById("j_username").focus();
    }
}

function saveUsername(theForm) {
    var expires = new Date();
    expires.setTime(expires.getTime() + 24 * 30 * 60 * 60 * 1000); // sets it for approx 30 days.
    setCookie("username",theForm.j_username.value,expires);
}
//-->
</script>
    
</div> <!-- end centercontent --> 

</div>

<div id="rightcontent"> 
   <tiles:insert attribute="sidebar" />
</div>

</div> <!-- end wrapper -->

</body>
</html>

<% } catch (Exception e) { e.printStackTrace(); } %>

