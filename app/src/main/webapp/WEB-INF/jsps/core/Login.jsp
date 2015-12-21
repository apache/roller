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
<%@ include file="/WEB-INF/jsps/taglibs-struts2.jsp" %>

<%!
    String securityCheckUrl = null;
    boolean cmaEnabled = "CMA".equals( WebloggerConfig.getAuthMethod() );
%>

<%
    if (cmaEnabled) {
        securityCheckUrl = "/j_security_check";
    } else {
        securityCheckUrl = "/roller_j_security_check";
    }
%>

<div class="container">

    <s:if test="authMethod == 'OPENID' || authMethod == 'DB_OPENID'">

        <form method="post" id="loginOpenIDForm" class="form-signin"
              action="/roller/roller_j_openid_security_check" onsubmit="saveOpenidIdentifier(this)">

            <h2 class="form-signin-heading"><s:text name="loginPage.openIdPrompt"/></h2>

            <label for="openid_identifier" class="sr-only"><s:text name="loginPage.openID"/></label>
            <input class="form-control" type="text" name="openid_identifier" id="openid_identifier"/>

            <button type="submit" name="submit" id="submit" class="btn btn-lg btn-primary btn-block"
                    value="<s:text name='loginPage.loginOpenID'/>"></button>

        </form>

    </s:if>

    <s:if test="authMethod != 'OPENID'">


        <form method="post" id="loginForm" class="form-signin"
              action="<c:url value="<%= securityCheckUrl %>"/>"
              onsubmit="saveUsername(this)">

            <s:if test="authMethod == 'DB_OPENID'">
                <h2 class="form-signin-heading"><s:text name="loginPage.openIdHybridPrompt"/></h2>
            </s:if>

            <s:else>
                <h2 class="form-signin-heading"><s:text name="loginPage.prompt"/></h2>
            </s:else>

            <label for="j_username" class="sr-only"> <s:text name="loginPage.userName"/> </label>
            <input type="text" class="form-control" name="j_username" id="j_username" placeholder="Username"/>

            <label for="j_password" class="sr-only"> <s:text name="loginPage.password"/> </label>
            <input type="password" class="form-control" name="j_password" id="j_password" placeholder="Password"/>

            <c:if test="${rememberMeEnabled}">
                <label>
                    <input type="checkbox" name="_spring_security_remember_me" id="_spring_security_remember_me"/>
                    <s:text name="loginPage.rememberMe"/>
                </label>
            </c:if>

            <button class="btn btn-lg btn-primary btn-block" type="submit" name="login" id="login">
                <s:text name='loginPage.login'/>
            </button>

            <button class="btn btn-lg btn-primary btn-block" type="reset" name="reset" id="reset"
                    onclick="document.getElementById('j_username').focus()">
                <s:text name='loginPage.reset'/>
            </button>

        </form>
    </s:if>

</div>

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
        setCookie("openid_identifier", theForm.openid_identifier.value, expires);
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
        setCookie("username", theForm.j_username.value, expires);
        setCookie("favorite_authentication_method", "username");
    }
    </s:if>
</script>