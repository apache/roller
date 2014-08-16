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

<%-- Titling, processing actions different between add and edit --%>
<s:if test="actionName == 'modifyUser'">
    <s:set var="subtitleKey">userAdmin.subtitle.editUser</s:set>
    <s:set var="mainAction">modifyUser</s:set>
</s:if>
<s:else>
    <s:set var="subtitleKey">userAdmin.subtitle.createNewUser</s:set>
    <s:set var="mainAction">createUser</s:set>
</s:else>

<p class="subtitle">
    <s:text name="%{#subtitleKey}">
        <s:param value="bean.userName" />
    </s:text>
</p>

<p class="pagetip">
    <s:if test="actionName == 'createUser'">
        <s:text name="userAdmin.addInstructions"/>
    </s:if>
    <s:if test="authMethod == 'DB_OPENID'">
        <p class="pagetip">
            <s:text name="userAdmin.noPasswordForOpenID"/>
        </p>
    </s:if>
</p>

<s:form>
	<s:hidden name="salt" />
    <s:if test="actionName == 'modifyUser'">
        <%-- bean for add does not have a bean id yet --%>
        <s:hidden name="bean.id" />
    </s:if>

    <table class="formtable">
        <tr>
            <td class="label"><label for="userName" /><s:text name="userSettings.username" /></label></td>
            <td class="field">
                <s:if test="actionName == 'modifyUser'">
                    <s:textfield name="bean.userName" size="30" maxlength="30" readonly="true" cssStyle="background: #e5e5e5" />
                </s:if>
                <s:else>
                    <s:textfield name="bean.userName" size="30" maxlength="30" />
                </s:else>
            </td>
            <td class="description">
                <s:if test="actionName == 'modifyUser'">
                    <s:text name="userSettings.tip.username" />
                </s:if>
                <s:else>
                    <s:text name="userAdmin.tip.userName" />
                </s:else>
            </td>
        </tr>

        <tr>
            <td class="label"><label for="screenName" /><s:text name="userSettings.screenname" /></label></td>
            <td class="field"><s:textfield name="bean.screenName" size="30" maxlength="30" /></td>
            <td class="description"><s:text name="userAdmin.tip.screenName" /></td>
        </tr>
        
        <tr>
            <td class="label"><label for="fullName" /><s:text name="userSettings.fullname" /></label></td>
            <td class="field"><s:textfield name="bean.fullName" size="30" maxlength="30" /></td>
            <td class="description"><s:text name="userAdmin.tip.fullName" /></td>
        </tr>
        
        <s:if test="authMethod == 'ROLLERDB' || authMethod == 'DB_OPENID'">
            <tr>
                <td class="label"><label for="passwordText" /><s:text name="userSettings.password" /></label></td>
                <td class="field"><s:password name="bean.password" size="20" maxlength="20" /></td>
                <td class="description"><s:text name="userAdmin.tip.password" /></td>
            </tr>
        </s:if>

        <s:if test="authMethod == 'OPENID' || authMethod == 'DB_OPENID'">
            <tr>
                <td class="label"><label for="openIdUrl" /><s:text name="userSettings.openIdUrl" /></label></td>
                <td class="field"><s:textfield name="bean.openIdUrl" size="40" maxlength="255" style="width:75%" id="f_openid_identifier" /></td>
                <td class="description"><s:text name="userAdmin.tip.openIdUrl" /></td>
            </tr>
        </s:if>

        <tr>
            <td class="label"><label for="emailAddress" /><s:text name="userSettings.email" /></label></td>
            <td class="field"><s:textfield name="bean.emailAddress" size="40" maxlength="40" /></td>
            <td class="description"><s:text name="userAdmin.tip.email" /></td>
        </tr>
        
        <tr>
            <td class="label"><label for="locale" /><s:text name="userSettings.locale" /></label></td>
            <td class="field">
                <s:select name="bean.locale" size="1" list="localesList" listValue="displayName" />
            </td>
            <td class="description"><s:text name="userAdmin.tip.locale" /></td>
        </tr>
        
        <tr>
            <td class="label"><label for="timeZone" /><s:text name="userSettings.timeZone" /></label></td>
            <td class="field">
                <s:select name="bean.timeZone" size="1" list="timeZonesList" />
            </td>
            <td class="description"><s:text name="userAdmin.tip.timeZone" /></td>
        </tr>
        
        <tr>
            <td class="label"><label for="userEnabled" /><s:text name="userAdmin.enabled" /></label></td>
            <td class="field">
                <s:checkbox name="bean.enabled" />
            </td>
            <td class="description"><s:text name="userAdmin.tip.enabled" /></td>
        </tr>
        
        <tr>
            <td class="label"><label for="userAdmin" /><s:text name="userAdmin.userAdmin" /></label></td>
            <td class="field">
                <s:checkbox name="bean.administrator" />
            </td>
            <td class="description"><s:text name="userAdmin.tip.userAdmin" /></td>
        </tr>
        
    </table>
    
    <br />
    
    <s:if test="actionName == 'modifyUser'">
        <p class="subtitle"><s:text name="userAdmin.userWeblogs" /></p>

        <s:if test="permissions != null && !permissions.isEmpty() > 0">
            <p><s:text name="userAdmin.userMemberOf" />:</p>
            <table class="rollertable" style="width: 80%">
                <s:iterator id="perms" value="permissions">
                    <tr>
                        <td width="%30">
                            <a href='<s:property value="#perms.weblog.absoluteURL" />'>
                                <s:property value="#perms.weblog.name" /> [<s:property value="#perms.weblog.handle" />]
                            </a>
                        </td>
                        <td width="%15">
                            <s:url action="entryAdd" namespace="/roller-ui/authoring" id="newEntry">
                                <s:param name="weblog" value="#perms.weblog.handle" />
                            </s:url>
                            <img src='<s:url value="/images/page_white_edit.png"/>' />
                            <a href='<s:property value="newEntry" />'>
                            <s:text name="userAdmin.newEntry" /></a>
                        </td>
                        <td width="%15">
                            <s:url action="entries" namespace="/roller-ui/authoring" id="editEntries">
                                <s:param name="weblog" value="#perms.weblog.handle" />
                            </s:url>
                            <img src='<s:url value="/images/page_white_edit.png"/>' />
                            <a href='<s:property value="editEntries" />'>
                            <s:text name="userAdmin.editEntries" /></a>
                        </td>
                        <td width="%15">
                            <s:url action="weblogConfig" namespace="/roller-ui/authoring" id="manageWeblog">
                                <s:param name="weblog" value="#perms.weblog.handle" />
                            </s:url>
                            <img src='<s:url value="/images/page_white_edit.png"/>' />
                            <a href='<s:property value="manageWeblog" />'>
                            <s:text name="userAdmin.manage" /></a>
                        </td>
                    </tr>
                </s:iterator>
            </table>
        </s:if>
        <s:else>
            <s:text name="userAdmin.userHasNoWeblogs" />
        </s:else>
    </s:if>

    <br />
    <br />

    <div class="control">
        <s:submit value="%{getText('generic.save')}" action="%{#mainAction}!save"/>
        <s:submit value="%{getText('generic.cancel')}" action="modifyUser!cancel" />
    </div>
    
</s:form>
