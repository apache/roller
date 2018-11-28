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

<s:form action="register!save" >
	<s:hidden name="salt" />
    <s:hidden name="bean.id" />
    <s:hidden name="bean.enabled" />

<table class="formtable">
    <tr>
        <td colspan="3">
            <h2><s:text name="userRegister.heading.identification" /></h2>
            <p><s:text name="userRegister.tip.identification" /></p>
        </td>
    </tr>
        
    <s:if test="fromSso">
        <tr>
            <td class="label"><label for="userName" /><s:text name="userSettings.username" /></label></td>
            <td class="field"><strong><s:property value="bean.userName" /></strong></td>
            <td class="description"><s:text name="userRegister.tip.userName" /></td>
        </tr>
    </s:if>
    <s:else>
        <tr>
            <td class="label"><label for="userName" /><s:text name="userSettings.username" /></label></td>
            <td class="field"><s:textfield name="bean.userName" size="30" maxlength="30" onkeyup="onChange()" /></td>
            <td class="description"><s:text name="userRegister.tip.userName" /></td>
        </tr>
    </s:else>
     
    <tr>
        <td class="label"><label for="screenName" /><s:text name="userSettings.screenname" /></label></td>
        <td class="field"><s:textfield name="bean.screenName" size="30" maxlength="30" onkeyup="onChange()" /></td>
        <td class="description"><s:text name="userRegister.tip.screenName" /></td>
    </tr>

    <tr>
        <td class="label"><label for="fullName" /><s:text name="userSettings.fullname" /></label></td>
        <td class="field"><s:textfield name="bean.fullName" size="30" maxlength="30" onkeyup="onChange()" /></td>
        <td class="description"><s:text name="userRegister.tip.fullName" /></td>
    </tr>

    <tr>
        <td class="label"><label for="emailAddress" /><s:text name="userSettings.email" /></label></td>
        <td class="field"><s:textfield name="bean.emailAddress" size="40" maxlength="40" onkeyup="onChange()" /></td>
        <td class="description"><s:text name="userRegister.tip.email" /></td>
    </tr>

    <s:if test="!fromSso">
        <tr>
            <td colspan="3">
                <h2><s:text name="userRegister.heading.authentication" /></h2>

                <s:if test="openIdConfiguration == 'disabled'">
                <p><s:text name="userRegister.tip.openid.disabled" /></p>                    
                </s:if>

                <s:if test="openIdConfiguration == 'hybrid'">
                <p><s:text name="userRegister.tip.openid.hybrid" /></p>                    
                </s:if>

                <s:if test="openIdConfiguration == 'only'">
                <p><s:text name="userRegister.tip.openid.only" /></p>                    
                </s:if>
            </td>
        </tr>
        
        <s:if test="openIdConfiguration != 'only'">
        <tr>
            <td class="label"><label for="passwordText" /><s:text name="userSettings.password" /></label></td>
            <td class="field">
               <s:password name="bean.passwordText" size="20" maxlength="20" onkeyup="onChange()" />
               <s:hidden name="bean.password" />
           </td>
            <td class="description"><s:text name="userRegister.tip.password" /></td>
        </tr>

        <tr>
            <td class="label"><label for="passwordConfirm" /><s:text name="userSettings.passwordConfirm" /></label></td>
            <td class="field"><s:password name="bean.passwordConfirm" size="20" maxlength="20" onkeyup="onChange()" /></td>
            <td class="description"><s:text name="userRegister.tip.passwordConfirm" /></td>
        </tr>
        </s:if>
        <s:else>
            <s:hidden name="bean.password" />
            <s:hidden name="bean.passwordConfirm" />
        </s:else>
    

        <s:if test="openIdConfiguration != 'disabled'">
            <tr>
                <td class="label"><label for="openIdUrl" /><s:text name="userSettings.openIdUrl" /></label></td>
                <td class="field"><s:textfield name="bean.openIdUrl" size="40" maxlength="255" id="f_openid_identifier"  onkeyup="onChange()"/></td>
                <td class="description"><s:text name="userRegister.tip.openIdUrl" /></td>
            </tr>  
        </s:if> 

    </s:if>

    <tr>
        <td colspan="3">    
            <h2><s:text name="userRegister.heading.locale" /></h2>
            <p><s:text name="userRegister.tip.localeAndTimeZone" /></p>
        </td>
    </tr>

    <tr>
        <td class="label"><label for="locale" /><s:text name="userSettings.locale" /></label></td>
        <td class="field">
           <s:select name="bean.locale" size="1" list="localesList" listValue="displayName" />
        </td>
        <td class="description"><s:text name="userRegister.tip.locale" /></td>
    </tr>

    <tr>
        <td class="label"><label for="timeZone" /><s:text name="userSettings.timeZone" /></label></td>
        <td class="field">
           <s:select name="bean.timeZone" size="1" list="timeZonesList" />
        </td>
        <td class="description"><s:text name="userRegister.tip.timeZone" /></td>
    </tr>

</table>

<br />

<h2><s:text name="userRegister.heading.ready" /></h2>

<p id="readytip"><s:text name="userRegister.tip.ready" /></p>

<s:submit id="submit" key="userRegister.button.save" />
<input type="button" value="<s:text name="userSettings.button.cancel"/>" 
    onclick="window.location='<s:url value="/"/>'" />

</s:form>

<script type="text/javascript">
function onChange() {
    var disabled = true;
    var openIdConfig    = '<s:property value="openIdConfiguration" />';
    var ssoEnabled      = <s:property value="fromSso" />;
    var passwordText    = document.register['bean.passwordText'].value;
    var passwordConfirm = document.register['bean.passwordConfirm'].value;
    var userName        = document.register['bean.userName'].value;
    var emailAddress    = document.register['bean.emailAddress'].value;
    var openIdUrl       = "";
    if (openIdConfig != 'disabled') {
        openIdUrl = document.register['bean.openIdUrl'].value;
    }
    
    if (ssoEnabled) {
        if (emailAddress) disabled = false;

    } else if (openIdConfig == 'disabled') {
        if (emailAddress && userName && passwordText && passwordConfirm && emailAddress) disabled = false;
        
    } else if (openIdConfig == 'only') {
        if (emailAddress && openIdUrl) disabled = false;
        
    } else if (openIdConfig == 'hybrid') {
        if (emailAddress && ((passwordText && passwordConfirm) || (openIdUrl)) ) disabled = false;
    }

    if ((passwordText || passwordConfirm) && !(passwordText == passwordConfirm)) {
        document.getElementById('readytip').innerHTML = '<s:text name="userRegister.error.mismatchedPasswords" />';
        disabled = true;
    } else if (disabled) {
        document.getElementById('readytip').innerHTML = '<s:text name="userRegister.tip.ready" />'
    } else {
        document.getElementById('readytip').innerHTML = '<s:text name="userRegister.success.ready" />'
    }
    document.getElementById('submit').disabled = disabled;
}
document.getElementById('submit').disabled = true;
</script>


