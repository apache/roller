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
<%@ include file="/WEB-INF/jsps/taglibs-struts2.jsp" %>

<p><s:text name="userRegister.prompt" /></p>

<s:form action="register!save"  theme="bootstrap" cssClass="form-horizontal">
	<s:hidden name="salt" />
    <s:hidden name="bean.id" />

    <h2><s:text name="userRegister.heading.identification" /></h2>
    <p><s:text name="userRegister.tip.identification" /></p>

    <s:if test="authMethod == 'LDAP'">

        <div class="form-group">

            <label class="col-sm-3 control-label">
                <s:text name="userSettings.username" />
            </label>

            <div class="col-sm-9 controls">
                <p class="form-control-static">
                    <s:property value="bean.userName"/>
                </p>
            </div>

        </div>

    </s:if>
    <s:else>
        <s:textfield label="%{getText('userSettings.username')}"
                     tooltip="%{getText('userRegister.tip.userName')}"
                     onkeyup="onChange()"
                     name="bean.userName" size="30" maxlength="30" />
    </s:else>

    <s:textfield label="%{getText('userSettings.screenname')}"
                 tooltip="%{getText('userRegister.tip.screenName')}"
                 onkeyup="onChange()"
                 name="bean.screenName" size="30" maxlength="30" />

    <s:textfield label="%{getText('userSettings.fullname')}"
                 tooltip="%{getText('userRegister.tip.fullName')}"
                 onkeyup="onChange()"
                 name="bean.fullName" size="30" maxlength="30" />

    <s:textfield label="%{getText('userSettings.email')}"
                 tooltip="%{getText('userRegister.tip.email')}"
                 onkeyup="onChange()"
                 name="bean.emailAddress" size="40" maxlength="255" />

    <s:if test="authMethod != 'LDAP'">

        <h2><s:text name="userRegister.heading.authentication" /></h2>

        <s:if test="authMethod == 'ROLLERDB'">
            <p><s:text name="userRegister.tip.openid.disabled" /></p>
        </s:if>

        <s:if test="authMethod == 'DB_OPENID'">
            <p><s:text name="userRegister.tip.openid.hybrid" /></p>
        </s:if>

        <s:if test="authMethod == 'OPENID'">
            <p><s:text name="userRegister.tip.openid.only" /></p>
        </s:if>

        <s:if test="authMethod == 'ROLLERDB' || authMethod == 'DB_OPENID'">

            <s:password label="%{getText('userSettings.password')}"
                         tooltip="%{getText('userRegister.tip.password')}"
                         onkeyup="onChange()"
                         name="bean.passwordText" size="20" maxlength="20" />

            <s:password label="%{getText('userSettings.passwordConfirm')}"
                         tooltip="%{getText('userRegister.tip.passwordConfirm')}"
                         onkeyup="onChange()"
                         name="bean.passwordConfirm" size="20" maxlength="20" />

        </s:if>
        <s:else>
            <s:hidden name="bean.password" />
            <s:hidden name="bean.passwordText" />
            <s:hidden name="bean.passwordConfirm" />
        </s:else>

        <s:if test="authMethod == 'OPENID' || authMethod == 'DB_OPENID'">

            <s:textfield label="%{getText('userSettings.openIdUrl')}"
                         tooltip="%{getText('userRegister.tip.openIdUrl')}"
                         onkeyup="onChange()"
                         name="bean.openIdUrl" size="40" maxlength="255" />
        </s:if>

    </s:if>

    <h2><s:text name="userRegister.heading.locale" /></h2>
    <p><s:text name="userRegister.tip.localeAndTimeZone" /></p>

    <s:select label="%{getText('userSettings.locale')}"
            tooltip="%{getText('userRegister.tip.locale')}"
            onkeyup="onChange()"
            list="localesList" listValue="displayName"
            name="bean.locale" />

    <s:select label="%{getText('userSettings.timeZone')}"
            tooltip="%{getText('userRegister.tip.timeZone')}"
            onkeyup="onChange()"
            list="timeZonesList"
            name="bean.timeZone" />

    <h2><s:text name="userRegister.heading.ready" /></h2>

    <p id="readytip"><s:text name="userRegister.tip.ready" /></p>

    <s:submit id="submit" key="userRegister.button.save" cssClass="btn btn-default" />
    <input type="button" class="btn btn-cancel"
           value="<s:text name="generic.cancel"/>" onclick="window.location='<s:url value="/"/>'" />

</s:form>

<%-- ============================================================================== --%>

<script type="text/javascript">

    function onChange() {
        var disabled = true;
        var authMethod    = "<s:property value='authMethod' />";
        var emailAddress    = document.register['bean.emailAddress'].value;
        var userName = passwordText = passwordConfirm = openIdUrl = "";

        if (!validateEmail(emailAddress)) {
            document.getElementById('submit').disabled = true;
            return;
        }

        if (authMethod === 'LDAP') {
            userName = '<s:property value="bean.userName" />';
        } else {
            userName = document.register['bean.userName'].value;
        }

        if (authMethod === "ROLLERDB" || authMethod === "DB_OPENID") {
            passwordText    = document.register['bean.passwordText'].value;
            passwordConfirm = document.register['bean.passwordConfirm'].value;
        }
        if (authMethod === "OPENID" || authMethod === "DB_OPENID") {
            openIdUrl = document.register['bean.openIdUrl'].value;
        }

        if (authMethod === "LDAP") {
            if (emailAddress) disabled = false;
        } else if (authMethod === "ROLLERDB") {
            if (emailAddress && userName && passwordText && passwordConfirm) disabled = false;
        } else if (authMethod === "OPENID") {
            if (emailAddress && openIdUrl) disabled = false;
        } else if (authMethod === "DB_OPENID") {
            if (emailAddress && ((passwordText && passwordConfirm) || (openIdUrl)) ) disabled = false;
        }

        if (authMethod !== 'LDAP') {
            if ((passwordText || passwordConfirm) && !(passwordText === passwordConfirm)) {
                document.getElementById('readytip').innerHTML = '<s:text name="userRegister.error.mismatchedPasswords" />';
                disabled = true;
            } else if (disabled) {
                document.getElementById('readytip').innerHTML = '<s:text name="userRegister.tip.ready" />'
            } else {
                document.getElementById('readytip').innerHTML = '<s:text name="userRegister.success.ready" />'
            }
        }
        document.getElementById('submit').disabled = disabled;
    }
    document.getElementById('submit').disabled = true;

</script>
