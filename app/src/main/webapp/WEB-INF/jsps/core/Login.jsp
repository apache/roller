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

    <s:if test="authMethod == 'OIDC' || authMethod == 'DB_OIDC'">
        <s:if test="oidcProviders.size() > 0">
            <div class="text-center mb-4">
                <h2 class="h5 mb-3"><s:text name="loginPage.oidcPrompt"/></h2>

                <div class="d-grid gap-2 col-10 mx-auto">
                    <s:iterator value="oidcProviders" var="provider">
                        <a href="${pageContext.request.contextPath}/oauth2/authorization/<s:property value="#provider.id"/>"
                           class="btn btn-primary">
                            <s:text name="loginPage.signInWith">
                                <s:param><s:property value="#provider.name"/></s:param>
                            </s:text>
                        </a>
                    </s:iterator>
                </div>
            </div>
        </s:if>
    </s:if>

    <s:if test="authMethod != 'OIDC'">

        <form method="post" id="loginForm"
              action="<c:url value="<%= securityCheckUrl %>"/>" onsubmit="saveUsername(this)">

            <div class="mb-3">
                <s:if test="authMethod == 'DB_OIDC'">
                    <legend><s:text name="loginPage.dbOidcPrompt"/></legend>
                </s:if>

                <s:else>
                    <legend><s:text name="loginPage.prompt"/></legend>
                </s:else>
            </div>

            <div class="mb-3">
                <label for="j_username" > <s:text name="loginPage.userName"/> </label>
                <input type="text" class="form-control" name="j_username" id="j_username" placeholder="Username"/>
            </div>

            <div class="mb-3">
                <label for="j_password" > <s:text name="loginPage.password"/> </label>
                <input type="password" class="form-control" name="j_password" id="j_password" placeholder="Password"/>
            </div>

            <c:if test="${rememberMeEnabled}">
                <div class="mb-3">
                    <input type="checkbox" name="_spring_security_remember_me" id="_spring_security_remember_me"/>
                    <label for="_spring_security_remember_me" > <s:text name="loginPage.rememberMe"/> </label>
                </div>
            </c:if>

            <div class="mb-3">
                <button class="btn btn-primary" type="submit" name="login" id="login">
                    <s:text name='loginPage.login'/>
                </button>

                <button class="btn btn-outline-secondary" type="reset" name="reset" id="reset"
                        onclick="document.getElementById('j_username').focus()">
                    <s:text name='loginPage.reset'/>
                </button>
            </div>

        </form>
    </s:if>

<script>
    <s:if test="authMethod != 'OIDC'">
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
        setCookie("username", theForm.j_username.value, expires);
    }
    </s:if>
</script>
