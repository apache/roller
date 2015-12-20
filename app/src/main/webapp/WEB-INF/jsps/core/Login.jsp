<%--
  Licensed to the Apache Software Foundation (ASF) under one or more
   contributor license agreements.  The ASF licenses this file to You
  under the Apache License, Version 2.0 (the "License"); you may not
  use this file except in compliance with the License.
  You may obtain a copy of the License at

      http://www.apache.org/licenses/LICENSE-2.0

  Unless required by applicable law or agreed to in writing, software
  distributed under the License is distributed on an "AS IS" BASIS,
  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  See the License for the specific language governing permissions and
  limitations under the License.  For additional information regarding
  copyright in this work, please see the NOTICE file in the top level
  directory of this distribution.

  Source file modified from the original ASF source; all changes made
  are also under Apache License.
--%>

<%-- Body of the login page, invoked from login.jsp --%>
<%@ page import="org.apache.roller.weblogger.config.WebloggerConfig" %>
<%@ page import="org.apache.roller.weblogger.WebloggerCommon.AuthMethod" %>
<%@ include file="/WEB-INF/jsps/taglibs-struts2.jsp" %>

<%!
String securityCheckUrl = "/roller_j_security_check";
%>

<p><s:text name="loginPage.prompt" /></p>

<form method="post" id="loginForm"
      action="<c:url value="<%= securityCheckUrl %>"/>"
      onsubmit="saveUsername(this)">

    <table width="80%">

        <tr>
            <td width="20%" align="right"><s:text name="loginPage.userName" /></td>
            <td width="80%">
                <input type="text" name="username" id="j_username" size="25" onBlur="this.value=this.value.trim()"/>
            </td>
        </tr>

        <tr>
            <td width="20%" align="right"><s:text name="loginPage.password" /></td>
            <td width="80%">
                <input type="password" name="password" id="j_password" size="20" onBlur="this.value=this.value.trim()"/>
            </td>
        </tr>

        <c:if test="${rememberMeEnabled}">
        <tr>
            <td width="20%"></td>
            <td width="80%">
                <input type="checkbox" name="_spring_security_remember_me" id="_spring_security_remember_me" />
                <label for="rememberMe">
                    <s:text name="loginPage.rememberMe" />
                </label>
            </td>
        </tr>
        </c:if>

        <tr>
            <td width="20%"></td>
            <td width="80%">
                <input type="submit" name="login" id="login" value="<s:text name='loginPage.login' />" />
                <input type="reset" name="reset" id="reset" value="<s:text name='loginPage.reset' />"
                    onclick="document.getElementById('j_username').focus()" />
            </td>
        </tr>

    </table>
</form>

<script>
if (document.getElementById) {
    if (getCookie("username") != null) {
        document.getElementById("j_username").value = getCookie("username");
        document.getElementById("j_password").focus();
    } else {
        document.getElementById("j_username").focus();
    }
}

function saveUsername(theForm) {
    var expires = new Date();
    expires.setTime(expires.getTime() + 24 * 30 * 60 * 60 * 1000); // sets it for approx 30 days.
    setCookie("username",theForm.j_username.value,expires);
    setCookie("favorite_authentication_method", "username");
}
</script>