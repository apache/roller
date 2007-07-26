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

<p class="subtitle">
    <s:text name="userAdmin.subtitle.editUser">
        <s:param value="bean.userName" />
    </s:text>
</p>

<s:form action="modifyUser!save">
    <s:hidden name="bean.id" />
    
    <table class="formtable">
        <tr>
            <td class="label"><label for="userName" /><s:text name="userSettings.username" /></label></td>
            <td class="field"><s:textfield name="bean.userName" size="30" maxlength="30" readonly="true" cssStyle="background: #e5e5e5" /></td>
            <td class="description"><s:text name="userAdmin.tip.userName" /></td>
        </tr>
        
        <tr>
            <td class="label"><label for="passwordText" /><s:text name="userSettings.password" /></label></td>
            <td class="field"><s:password name="bean.password" size="20" maxlength="20" /></td>
            <td class="description"><s:text name="userAdmin.tip.password" /></td>
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
    
    <p class="subtitle"><s:text name="userAdmin.userWeblogs" /></p>
    
    <s:if test="!user.permissions.isEmpty"> 
        
        <p><s:text name="userAdmin.userMemberOf" /></p>  
        <table class="rollertable" style="width: 80%">
            <s:iterator id="perms" value="user.permissions">
                <tr>
                    <td width="%30">
                        <a href='<s:property value="baseURL" />/<s:property value="#perms.website.handle" />'>
                            <s:property value="#perms.website.name" /> [<s:property value="#perms.website.handle" />] 
                        </a>
                    </td>
                    <td width="%15">
                        <s:url action="entryAdd" namespace="/roller-ui/authoring" id="newEntry">
                            <s:param name="weblog" value="#perms.website.handle" />
                        </s:url>
                        <img src='<s:url value="/images/page_white_edit.png"/>' />
                        <a href='<s:property value="newEntry" />'>
                        <s:text name="userAdmin.newEntry" /></a>
                    </td>
                    <td width="%15">
                        <s:url action="entries" namespace="/roller-ui/authoring" id="editEntries">
                            <s:param name="weblog" value="#perms.website.handle" />
                        </s:url>
                        <img src='<s:url value="/images/page_white_edit.png"/>' />
                        <a href='<s:property value="editEntries" />'>
                        <s:text name="userAdmin.editEntries" /></a> 
                    </td>
                    <td width="%15">
                        <s:url action="weblogConfig" namespace="/roller-ui/authoring" id="manageWeblog">
                            <s:param name="weblog" value="#perms.website.handle" />
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
    
    <br />
    <br />
    
    <div class="control">
        <s:submit key="userAdmin.save" />
        <input type="button" value="<s:text name="application.cancel"/>" onclick="window.location='<s:url action="userAdmin"/>'" />
    </div>
    
</s:form>
