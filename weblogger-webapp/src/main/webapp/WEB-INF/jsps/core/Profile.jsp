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

<p class="subtitle"><s:text name="yourProfile.description" /></p>

<s:form action="profile!save">
    
    <table class="formtable">
        
        <tr>
            <td class="label"><label for="userName" /><s:text name="userSettings.username" /></label></td>
            <td class="field"><s:textfield name="bean.userName" size="30" maxlength="30" readonly="true" cssStyle="background: #e5e5e5" /></td>
            <td class="description"><s:text name="yourProfile.tip.userName" /></td>
        </tr>
        
        <tr>
            <td class="label"><label for="screenName" /><s:text name="userSettings.screenname" /></label></td>
            <td class="field"><s:textfield name="bean.screenName" size="30" maxlength="30" /></td>
            <td class="description"><s:text name="userRegister.tip.screenName" /></td>
        </tr>
        
        <tr>
            <td class="label"><label for="fullName" /><s:text name="userSettings.fullname" /></label></td>
            <td class="field"><s:textfield name="bean.fullName" size="30" maxlength="30" /></td>
            <td class="description"><s:text name="userRegister.tip.fullName" /></td>
        </tr>
        
        <tr>
            <td class="label"><label for="emailAddress" /><s:text name="userSettings.email" /></label></td>
            <td class="field"><s:textfield name="bean.emailAddress" size="40" maxlength="40" /></td>
            <td class="description"><s:text name="userRegister.tip.email" /></td>
        </tr>
        
        <s:if test="openIdConfiguration != 'only'">
        <tr>
            <td class="label"><label for="passwordText" /><s:text name="userSettings.password" /></label></td>
            <td class="field">
                <s:password name="bean.passwordText" size="20" maxlength="20" />
            </td>
            <td class="description"><s:text name="userRegister.tip.password" /></td>
        </tr>
        
        <tr>
            <td class="label"><label for="passwordConfirm" /><s:text name="userSettings.passwordConfirm" /></label></td>
            <td class="field"><s:password name="bean.passwordConfirm" size="20" maxlength="20" /></td>
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
                <td class="field"><s:textfield name="bean.openIdUrl" size="40" maxlength="255" id="f_openid_identifier" /></td>
                <td class="description"><s:text name="userRegister.tip.openIdUrl" /></td>
            </tr>     
        </s:if>
        
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
    
    <s:submit value="%{getText('userSettings.button.save')}" />
    <input type="button" value="<s:text name="userSettings.button.cancel"/>" onclick="window.location='<s:url action="menu"/>'" />

</s:form>
