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
--%>

<%-- Body of the login page, invoked from login.jsp --%>
<%@ page import="org.apache.roller.weblogger.config.WebloggerConfig" %>
<%@ page import="org.apache.roller.weblogger.config.AuthMethod" %>
<%@ include file="/WEB-INF/jsps/taglibs-struts2.jsp" %>

<%!
String securityCheckUrl = null;
boolean cmaEnabled = "CMA".equals(WebloggerConfig.getAuthMethod());
%>

<%
if (cmaEnabled) {
    securityCheckUrl = "/j_security_check";
} else {
    securityCheckUrl = "/roller_j_security_check";
}
%>

<s:if test="authMethod == 'OPENID' || authMethod == 'DB_OPENID'">
    
    <p><s:text name="loginPage.openIdPrompt" /></p>
    
    <form method="post" id="loginOpenIDForm"       
          action="/roller/roller_j_openid_security_check"      
          onsubmit="saveOpenidIdentifier(this)">      
        <!-- action="<c:url value='roller_j_openid_security_check'/>"  -->
        <table width="80%">
            <tr>
                <td width="20%" align="right"><s:text name="loginPage.openID" /></td>
                <td width="80%">
                    <input type="text" name="openid_identifier" id="openid_identifier" class="f_openid_identifier" size="40" maxlength="255" style="width: 35%"/>
                </td>
            </tr>    
            <tr>
                <td width="20%"></td>
                <td width="80%">
                    <input type="submit" name="submit" id="submit" value="<s:text name='loginPage.loginOpenID'/>" />
                </td>
            </tr>
        </table> 
    </form>
</s:if>

<s:if test="authMethod != 'OPENID'">

    <s:if test="authMethod == 'DB_OPENID'">
        <p><s:text name="loginPage.openIdHybridPrompt" /></p>
    </s:if>
    
    <s:else>
        <p><s:text name="loginPage.prompt" /></p>
    </s:else>
    
    <form method="post" id="loginForm" 
          action="<c:url value="<%= securityCheckUrl %>"/>"
          onsubmit="saveUsername(this)">

        <table width="80%">

            <tr>
                <td width="20%" align="right"><s:text name="loginPage.userName" /></td>
                <td width="80%">
                    <input type="text" name="j_username" id="j_username" size="25" />
                </td>
            </tr>

            <tr>
                <td width="20%" align="right"><s:text name="loginPage.password" /></td>
                <td width="80%">
                    <input type="password" name="j_password" id="j_password" size="20" />
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
</s:if>

<script>
<s:if test="authMethod == 'OPENID' || authMethod == 'DB_OPENID'">
function focusToOpenidForm() {
    return (document.getElementById && document.getElementById("j_username") === null) ||
        getCookie("favorite_authentication_method") !== "username";
}

if (document.getElementById) {
    if (document.getElementById && getCookie("openid_identifier") !== null) {
        document.getElementById("openid_identifier").value = getCookie("openid_identifier");
    }
    if (focusToOpenidForm()) {
        document.getElementById("openid_identifier").focus();
    }
}

function saveOpenidIdentifier(theForm) {
    var expires = new Date();
    expires.setTime(expires.getTime() + 24 * 30 * 60 * 60 * 1000); // sets it for approx 30 days.
    setCookie("openid_identifier",theForm.openid_identifier.value,expires);
    setCookie("favorite_authentication_method", "openid");
}
</s:if>

<s:if test="authMethod != 'OPENID'">
function focusToUsernamePasswordForm() {
    return (document.getElementById && document.getElementById("openid_identifier") === null) ||
        getCookie("favorite_authentication_method") === "username";
}

if (document.getElementById) {
    if (getCookie("username") != null) {
        if (document.getElementById) {
            document.getElementById("j_username").value = getCookie("username");
            if (focusToUsernamePasswordForm()) {
                document.getElementById("j_password").focus();
            }
        }
    } else if (focusToUsernamePasswordForm()) {
        document.getElementById("j_username").focus();
    }
}

function saveUsername(theForm) {
    var expires = new Date();
    expires.setTime(expires.getTime() + 24 * 30 * 60 * 60 * 1000); // sets it for approx 30 days.
    setCookie("username",theForm.j_username.value,expires);
    setCookie("favorite_authentication_method", "username");
}
</s:if>
</script>