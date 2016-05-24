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

  Source file modified from the original ASF source; all changes made
  are also under Apache License.
--%>
<%@ include file="/WEB-INF/jsps/taglibs-struts2.jsp" %>
<script src="<s:url value='/tb-ui/scripts/jquery-2.2.3.min.js'/>"></script>
<script src="https://cdnjs.cloudflare.com/ajax/libs/jsviews/0.9.75/jsviews.min.js"></script>
<script>
var contextPath = "${pageContext.request.contextPath}";
</script>
<script src="<s:url value='/tb-ui/scripts/commonjquery.js'/>"></script>
<script src="<s:url value='/tb-ui/scripts/useradmin.js'/>"></script>

<%-- Titling, processing actions different between add and edit --%>
<s:if test="actionName == 'modifyUser'">
    <s:set var="subtitleKey">userAdmin.subtitle.editUser</s:set>
    <s:set var="mainAction">modifyUser</s:set>
</s:if>
<s:else>
    <s:set var="subtitleKey">userAdmin.subtitle.createNewUser</s:set>
    <s:set var="mainAction">createUser</s:set>
</s:else>

<p class="subtitle"><s:text name="userAdmin.subtitle.searchUser" /></p><br>
<select id="useradmin-select-user"></select>

<s:if test="actionName == 'createUser'">
<input id="select-user" type="button" style="margin:4px" value='<s:text name="generic.edit" />'/>
</s:if>

<input type="hidden" id="recordIdOld" value="<s:property value='%{#parameters.bean.id}'/>"/>
<input type="hidden" id="refreshURL" value="<s:url action='createUser'/>"/>

<%-- LDAP uses external user creation --%>
<s:if test="actionName == 'modifyUser' || getProp('authentication.method') != 'ldap'">

    <p class="subtitle">
        <s:text name="%{#subtitleKey}">
            <s:param value="bean.userName" />
        </s:text>
    </p>

    <p class="pagetip">
        <s:if test="actionName == 'createUser'">
            <s:text name="userAdmin.addInstructions"/>
        </s:if>
    </p>

    <s:form action="%{#mainAction}!save">
        <sec:csrfInput/>
        <s:if test="actionName == 'modifyUser'">
            <%-- bean for add does not have a bean id yet --%>
            <s:hidden name="bean.id" />
        </s:if>

        <table class="formtable">
          <tbody id="formBody">
            <script id="formTemplate" type="text/x-jsrender">
              <tr id="recordId" data-id="{{:id}}">
                  <td class="label"><label for="userName"><s:text name="userSettings.username" /></label></td>
                  <!-- todo: make read-only for updates readonly="true" cssStyle="background: #e5e5e5" -->
                  <td class="field"><input type="text" size="30" maxlength="30" data-link="userName"></td>
                  <td id="someVal" class="description">
                      <s:if test="actionName == 'modifyUser'">
                          <s:text name="userSettings.tip.username" />
                      </s:if>
                      <s:else>
                          <s:text name="userAdmin.tip.userName" />
                      </s:else>
                  </td>
              </tr>

              <tr>
                  <td class="label"><label for="screenName"><s:text name="userSettings.screenname" /></label></td>
                  <td class="field"><input type="text" size="30" maxlength="30" data-link="screenName" onBlur="this.value=this.value.trim()"></td>
                  <td class="description"><s:text name="userAdmin.tip.screenName" /></td>
              </tr>

              <s:if test="getProp('authentication.method') == 'db'">
                  <tr>
                      <td class="label"><label for="passwordText"><s:text name="userSettings.password" /></label></td>
                      <td class="field"><input type="password" size="20" maxlength="20" data-link="password" onBlur="this.value=this.value.trim()"></td>
                      <td class="description"><s:text name="userAdmin.tip.password" /></td>
                  </tr>
              </s:if>

              <tr>
                  <td class="label"><label for="emailAddress"><s:text name="userSettings.email" /></label></td>
                  <td class="field"><input type="text" size="40" maxlength="40" data-link="emailAddress" onBlur="this.value=this.value.trim()"></td>
                  <td class="description"><s:text name="userAdmin.tip.email" /></td>
              </tr>

              <tr>
                  <td class="label"><label for="locale"><s:text name="userSettings.locale" /></label></td>
                  <td class="field">
                      <s:select name="locale" size="1" list="localesList" listValue="displayName" />
                  </td>
                  <td class="description"><s:text name="userAdmin.tip.locale" /></td>
              </tr>

              <tr>
                  <td class="label"><label for="userEnabled"><s:text name="userAdmin.enabled" /></label></td>
                  <td class="field">
                      <input type="checkbox" name="enabled" data-link="enabled"/>
                  </td>
                  <td class="description"><s:text name="userAdmin.tip.enabled" /></td>
              </tr>

              <tr>
                  <td class="label"><label for="userAdmin"><s:text name="userAdmin.userAdmin" /></label></td>
                  <td class="field">
                      <input type="checkbox" name="globalAdmin" data-link="globalAdmin"/>
                  </td>
                  <td class="description"><s:text name="userAdmin.tip.userAdmin" /></td>
              </tr>
            </script>
          </tbody>
        </table>

        <br>

        <s:if test="actionName == 'modifyUser'">
            <p class="subtitle"><s:text name="userAdmin.userWeblogs" /></p>

            <s:if test="permissions != null && !permissions.isEmpty() > 0">
                <p><s:text name="userAdmin.userMemberOf" />:</p>
                <table class="rollertable" style="width: 80%">
                  <tbody id="tableBody">
                    <script id="tableTemplate" type="text/x-jsrender">
                      <tr id="{{:id}}">
                          <td style="width:30%">
                              <a href='{{:weblog.absoluteURL}}'>
                                  <s:property value="{{:weblog.name}}" /> [<s:property value="{{:weblog.handle}}" />]
                              </a>
                          </td>
                          <td style="width:15%">
                              <s:url action="entryAdd" namespace="/tb-ui/authoring" id="newEntry">
                                  <s:param name="weblog" value="{{:weblog.handle}}" />
                              </s:url>
                              <img src='<s:url value="/images/page_white_edit.png"/>' />
                              <a href='<s:property value="newEntry" />'>
                              <s:text name="userAdmin.newEntry" /></a>
                          </td>
                          <td style="width:15%">
                              <s:url action="entries" namespace="/tb-ui/authoring" id="editEntries">
                                  <s:param name="weblog" value="{{:weblog.handle}}" />
                              </s:url>
                              <img src='<s:url value="/images/page_white_edit.png"/>' />
                              <a href='<s:property value="editEntries" />'>
                              <s:text name="userAdmin.editEntries" /></a>
                          </td>
                          <td style="width:15%">
                              <s:url action="weblogConfig" namespace="/tb-ui/authoring" id="manageWeblog">
                                  <s:param name="weblog" value="{{:weblog.handle}}" />
                              </s:url>
                              <img src='<s:url value="/images/page_white_edit.png"/>' />
                              <a href='<s:property value="manageWeblog" />'>
                              <s:text name="userAdmin.manage" /></a>
                          </td>
                      </tr>
                    </script>
                  </tbody>
                </table>
            </s:if>
            <s:else>
                <s:text name="userAdmin.userHasNoWeblogs" />
            </s:else>
        </s:if>

        <br>
        <br>

        <div class="control">
            <s:submit id="save-link" value="%{getText('generic.save')}" />
            <s:submit value="%{getText('generic.cancel')}" action="modifyUser!cancel" />
        </div>

   </s:form>

</s:if>
