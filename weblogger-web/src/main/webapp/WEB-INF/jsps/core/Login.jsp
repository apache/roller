<!--
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
-->

<%-- Body of the login page, invoked from login.jsp --%>
<%@ page import="org.apache.roller.weblogger.config.WebloggerConfig" %>
<%@ include file="/WEB-INF/jsps/taglibs-struts2.jsp" %>

<%!
String securityCheckUrl = null;
boolean cmaEnabled = WebloggerConfig.getBooleanProperty("authentication.cma.enabled");
%>

<%
if (cmaEnabled) {
    securityCheckUrl = "/j_security_check";
} else {
    securityCheckUrl = "/roller_j_security_check";
}
%>


<s:if test="openIdConfiguration != 'disabled'">
    
    <p><s:text name="loginPage.openIdPrompt" /></p>
    
    <form method="post" id="loginOpenIDForm"       
          action="/roller/roller_j_openid_security_check"      
          onsubmit="saveUsername(this)">      
        <!-- action="<c:url value='roller_j_openid_security_check'/>"  -->
        <table width="80%">
            <tr>
                <td width="20%" align="right"><s:text name="loginPage.openID" /></td>
                <td width="80%">
                    <input type="text" name="j_username" id="j_username" class="f_openid_identifier" size="40" maxlength="255" />
                </td>
            </tr>    
            <tr>
                <td width="20%"></td>
                <td width="80%">
                    <input type="submit" name="submit" id="submit" value="<s:text name="loginPage.loginOpenID" />" />
                </td>
            </tr>
        </table> 
    </form>
</s:if>

<s:if test="openIdConfiguration != 'only'">

    <s:if test="openIdConfiguration == 'hybrid'">
        <p><s:text name="loginPage.openIdHybridPrompt" /></p>
    </s:if>
    
    <s:if test="openIdConfiguration == 'disabled'">
        <p><s:text name="loginPage.prompt" /></p>
    </s:if>
    
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
                    <input type="submit" name="login" id="login" value="<s:text name="loginPage.login" />" />
                    <input type="reset" name="reset" id="reset" value="<s:text name="loginPage.reset" />" 
                        onclick="document.getElementById('j_username').focus()" />
                </td>
            </tr>        

        </table>    
    </form>
</s:if>


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
