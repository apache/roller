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
<%@ include file="/WEB-INF/jsps/taglibs-struts2.jsp" %>

<p class="subtitle"><s:text name="userAdmin.title.editUser"/></p>

<s:if test="authMethod == 'DB_OPENID'">
    <p class="pagetip">
        <s:text name="userAdmin.noPasswordForOpenID"/>
    </p>
</s:if>


<s:form action="profile!save" theme="bootstrap" cssClass="form-horizontal">
    <s:hidden name="salt"/>

    <s:textfield label="%{getText('userSettings.username')}"
                 tooltip="%{getText('userRegister.tip.userName')}"
                 onchange="formChanged()" onkeyup="formChanged()"
                 name="bean.userName" size="30" maxlength="30" readonly="true"/>

    <s:textfield label="%{getText('userSettings.screenname')}"
                 tooltip="%{getText('userRegister.tip.screenName')}"
                 onchange="formChanged()" onkeyup="formChanged()"
                 name="bean.screenName" size="30" maxlength="30"/>

    <s:textfield label="%{getText('userSettings.fullname')}"
                 tooltip="%{getText('')}"
                 onchange="formChanged()" onkeyup="formChanged()"
                 name="bean.fullName" size="30" maxlength="30"/>

    <s:textfield label="%{getText('userSettings.email')}"
                 tooltip="%{getText('userRegister.tip.email')}"
                 onchange="formChanged()" onkeyup="formChanged()"
                 name="bean.emailAddress" size="40" maxlength="40"/>

    <s:if test="authMethod == 'ROLLERDB' || authMethod == 'DB_OPENID'">
        <s:password label="%{getText('userSettings.password')}"
                    tooltip="%{getText('userSettings.tip.password')}"
                    onchange="formChanged()" onkeyup="formChanged()"
                    name="bean.passwordText" size="20" maxlength="20"/>

        <s:password label="%{getText('userSettings.passwordConfirm')}"
                    tooltip="%{getText('userRegister.tip.passwordConfirm')}"
                    onchange="formChanged()" onkeyup="formChanged()"
                    name="bean.passwordConfirm" size="20" maxlength="20"/>
    </s:if>
    <s:else>
        <s:hidden name="bean.password"/>
    </s:else>

    <s:if test="authMethod == 'OPENID' || authMethod == 'DB_OPENID'">
        <s:textfield label="%{getText('userSettings.openIdUrl')}"
                     tooltip="%{getText('userRegister.tip.openIdUrl')}"
                     name="bean.openIdUrl" size="40" maxlength="255"
                     style="width:75%" id="f_openid_identifier"/>
    </s:if>

    <s:select label="%{getText('userSettings.locale')}"
              tooltip="%{getText('userRegister.tip.locale')}"
              name="bean.locale" size="1" list="localesList" listValue="displayName"/>

    <s:select label="%{getText('userSettings.timeZone')}"
              tooltip="%{getText('userRegister.tip.timeZone')}"
              name="bean.timeZone" size="1" list="timeZonesList"/>

    <s:submit cssClass="btn btn-default" value="%{getText('generic.save')}"/>

    <input class="btn" type="button" value="<s:text name="generic.cancel"/>"
           onclick="window.location='<s:url action="menu"/>'"/>

</s:form>

<%-- -------------------------------------------------------- --%>

<script type="text/javascript">

    var saveButton;

    $(document).ready(function () {
        saveButton = $("#profile_0");
        formChanged();
    });

    function formChanged() {
        var valid = false;

        var screenName = $("#profile_bean_screenName:first").val();
        var fullName = $("#profile_bean_fullName:first").val();
        var email = $("#profile_bean_emailAddress:first").val();
        var password = $("#profile_bean_passwordText:first").val();
        var passwordConfirm = $("#profile_bean_passwordConfirm:first").val();

        if (screenName && screenName.trim().length > 0
            && fullName && fullName.trim().length > 0
            && email && email.trim().length > 0 && validateEmail(email)) {
            valid = true;

        } else {
            valid = false;
        }

        if ((password && password.trim().length) || (passwordConfirm && passwordConfirm.trim().length > 0)) {
            if (password !== passwordConfirm) {
                valid = false;
            }
        }

        if (valid) {
            saveButton.attr("disabled", false);
        } else {
            saveButton.attr("disabled", true);
        }

    }

</script>
