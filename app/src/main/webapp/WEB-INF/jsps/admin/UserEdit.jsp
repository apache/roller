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
<s:if test="actionName == 'createUser'">
    <s:set var="subtitleKey">userAdmin.subtitle.createNewUser</s:set>
</s:if>
<s:else>
    <s:set var="subtitleKey">userAdmin.subtitle.editUser</s:set>
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
         <s:text name="userAdmin.noPasswordForOpenID"/>
    </s:if>
</p>

<s:form theme="bootstrap" cssClass="form-horizontal">
	<s:hidden name="salt" />
    <s:if test="actionName == 'modifyUser'">
        <%-- bean for add does not have a bean id yet --%>
        <s:hidden name="bean.id" />
    </s:if>

    <s:if test="actionName == 'modifyUser'">
        <s:textfield name="bean.userName" size="30" maxlength="30" onkeyup="formChanged()"
                label="%{getText('userSettings.username')}"
                tooltip="%{getText('userSettings.tip.username')}"
                readonly="true" cssStyle="background: #e5e5e5" />
    </s:if>
    <s:else>
        <s:textfield name="bean.userName" size="30" maxlength="30" onkeyup="formChanged()"
                label="%{getText('userSettings.username')}"
                tooltip="%{getText('userAdmin.tip.username')}" />
    </s:else>

    <s:textfield id="bean_userName" name="bean.screenName" size="30" maxlength="30" onkeyup="formChanged()"
                label="%{getText('userSettings.screenname')}"
                tooltip="%{getText('userAdmin.tip.screenName')}" />

    <s:textfield id="bean_fullName" name="bean.fullName" size="30" maxlength="30" onkeyup="formChanged()"
                 label="%{getText('userSettings.fullname')}"
                 tooltip="%{getText('userAdmin.tip.fullName')}" />

    <s:if test="authMethod == 'ROLLERDB' || authMethod == 'DB_OPENID'">
        <s:password name="bean.password" size="30" maxlength="30" onkeyup="formChanged()"
                     label="%{getText('userSettings.password')}"
                     tooltip="%{getText('userAdmin.tip.password')}" />
    </s:if>

    <s:if test="authMethod == 'OPENID' || authMethod == 'DB_OPENID'">
        <s:textfield name="bean.openIdUrl" size="30" maxlength="255" id="f_openid_identifier"
                     label="%{getText('userSettings.openIdUrl')}"
                     tooltip="%{getText('userAdmin.tip.openIdUrl')}" />
    </s:if>

    <s:textfield id="bean_email" name="bean.emailAddress" size="30" maxlength="255" onkeyup="formChanged()"
                 label="%{getText('userSettings.email')}"
                 tooltip="%{getText('userAdmin.tip.email')}" />

    <s:select name="bean.locale" size="1" list="localesList" listValue="displayName"
                 label="%{getText('userSettings.locale')}"
                 tooltip="%{getText('userAdmin.tip.locale')}" />

    <s:select name="bean.timeZone" size="1" list="timeZonesList"
                 label="%{getText('userSettings.timeZone')}"
                 tooltip="%{getText('userAdmin.tip.timeZone')}" />

    <s:checkbox name="bean.enabled" size="30" maxlength="30"
                 label="%{getText('userAdmin.enabled')}"
                 tooltip="%{getText('userAdmin.tip.userEnabled')}" />

    <s:checkbox name="bean.administrator" size="30" maxlength="30"
                 label="%{getText('userAdmin.userAdmin')}"
                 tooltip="%{getText('userAdmin.tip.userAdmin')}" />


    <s:if test="actionName == 'modifyUser'">
        <h2><s:text name="userAdmin.userWeblogs" /></h2>

        <s:if test="permissions != null && !permissions.isEmpty() > 0">
            <p><s:text name="userAdmin.userMemberOf" />:</p>
            <table class="table" style="width: 80%">
                <s:iterator var="perms" value="permissions">
                    <tr>
                        <td width="%30">
                            <a href='<s:property value="#perms.weblog.absoluteURL" />'>
                                <s:property value="#perms.weblog.name" /> [<s:property value="#perms.weblog.handle" />]
                            </a>
                        </td>
                        <td width="%15">
                            <s:url action="entryAdd" namespace="/roller-ui/authoring" var="newEntry">
                                <s:param name="weblog" value="#perms.weblog.handle" />
                            </s:url>
                            <img src='<s:url value="/images/page_white_edit.png"/>' />
                            <a href='<s:property value="newEntry" />'>
                            <s:text name="userAdmin.newEntry" /></a>
                        </td>
                        <td width="%15">
                            <s:url action="entries" namespace="/roller-ui/authoring" var="editEntries">
                                <s:param name="weblog" value="#perms.weblog.handle" />
                            </s:url>
                            <img src='<s:url value="/images/page_white_edit.png"/>' />
                            <a href='<s:property value="editEntries" />'>
                            <s:text name="userAdmin.editEntries" /></a>
                        </td>
                        <td width="%15">
                            <s:url action="weblogConfig" namespace="/roller-ui/authoring" var="manageWeblog">
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
        <s:if test="actionName == 'createUser'">
            <s:submit cssClass="btn btn-default" id="save_button"
                      value="%{getText('generic.save')}" action="createUser!save"/>
            <s:submit cssClass="btn"
                      value="%{getText('generic.cancel')}" action="createUser!cancel" />
        </s:if>
        <s:else>
            <s:submit cssClass="btn btn-default" id="save_button"
                      value="%{getText('generic.save')}" action="modifyUser!save"/>
            <s:submit cssClass="btn"
                      value="%{getText('generic.cancel')}" action="modifyUser!cancel" />
        </s:else>
    </div>

</s:form>


<script>

    document.forms[0].elements[0].focus();
    let saveButton;

    $( document ).ready(function() {
        saveButton = $("#save_button");
        formChanged()
    });

    function formChanged() {
        let userName = $("#bean_userName:first").val();
        let fullName = $("#bean_fullName:first").val();
        let email = $("#bean_email:first").val();

        let valid = (userName && userName.trim().length > 0
            && fullName && fullName.trim().length > 0
            && email && email.trim().length > 0
            && validateEmail(email));

        if (valid) {
            saveButton.attr("disabled", false);
        } else {
            saveButton.attr("disabled", true);
        }
    }

</script>

