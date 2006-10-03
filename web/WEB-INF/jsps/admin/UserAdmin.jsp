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
<%@ include file="/taglibs.jsp" %>
<script type="text/javascript">
// <!--
function cancel() {
    document.userAdminForm.method.value="cancel"; 
    document.userAdminForm.submit();
}
<%@ include file="/roller-ui/scripts/ajax-user.js" %>
// -->
</script> 

<%-- If user name is not specified, then allow user to choose a user to be loaded --%>
<c:if test="${empty userAdminForm.userName && userAdminForm.newUser == false}">

    <p class="subtitle"><fmt:message key="userAdmin.subtitle.searchUser" /></p>
    <p><fmt:message key="userAdmin.prompt.searchUser" /></p>

    <html:form action="/roller-ui/admin/user" method="post" focus="userName">
        <input name="method" type="hidden" value="edit" />    
        
        <span style="margin:4px"><fmt:message key="inviteMember.userName" /></span>
        <input name="userName" id="userName" size="30" maxlength="30" 
            onfocus="onUserNameFocus(null)" onkeyup="onUserNameChange(null)" 
            style="margin:4px" />
        <input type="submit" value='<fmt:message key="userAdmin.edit" />' 
            style="margin:4px" />
        <br />
        <select id="userList" size="10" onchange="onUserSelected()" 
            style="width:300px; margin:4px" ></select>
                        
    </html:form>

    <p class="subtitle"><fmt:message key="userAdmin.subtitle.userCreation" /></p>
    <fmt:message key="userAdmin.prompt.orYouCan" />
    <c:url value="/roller-ui/admin/user.do" var="newUser">
        <c:param name="method" value="newUser" />
    </c:url>
    <a href='<c:out value="${newUser}" />'>
        <fmt:message key="userAdmin.prompt.createANewUser" />
    </a>

</c:if>

<%-- If a user name is specified, then show the admin user form --%>
<c:if test="${!(empty userAdminForm.userName && userAdminForm.newUser == false)}">

    <c:choose>
        <c:when test="${userAdminForm.newUser == false}">		
            <p class="subtitle"><fmt:message key="userAdmin.subtitle.editUser" /></p>
            <p><fmt:message key="userAdmin.prompt.editUser" /></p>
        </c:when>
        <c:otherwise>			
            <p class="subtitle"><fmt:message key="userAdmin.subtitle.createNewUser" /></p>
        </c:otherwise>
    </c:choose>
    
    <html:form action="/roller-ui/admin/user" method="post">
        <html:hidden property="method" value="update"/></input>        
        <html:hidden property="id"/></input>
        <html:hidden property="adminCreated" /></input>
        <html:hidden property="newUser" /></input>
        
    <table class="formtable">
    <tr>
        <td class="label"><label for="fullName" /><fmt:message key="userSettings.fullname" /></label></td>
        <td class="field"><html:text property="fullName" size="30" maxlength="30" /></td>
        <td class="description"><fmt:message key="userAdmin.tip.fullName" /></td>
    </tr>

    <tr>
        <td class="label"><label for="userName" /><fmt:message key="userSettings.username" /></label></td>
        <td class="field">
            <c:choose>
                <c:when test="${userAdminForm.newUser == true}">
                    <html:text property="userName" size="30" maxlength="30" />
                </c:when>
                <c:otherwise>
                    <html:text readonly="true" style="background: #e5e5e5" 
                        property="userName" size="30" maxlength="30" />
                </c:otherwise>
            </c:choose>
        </td>
        <td class="description"><fmt:message key="userAdmin.tip.userName" /></td>
    </tr>

    <tr>
        <td class="label"><label for="passwordText" /><fmt:message key="userSettings.password" /></label></td>
        <td class="field">
           <html:password property="passwordText" size="20" maxlength="20" />
           <html:hidden property="password" />
       </td>
        <td class="description"><fmt:message key="userAdmin.tip.password" /></td>
    </tr>

    <tr>
        <td class="label"><label for="passwordConfirm" /><fmt:message key="userSettings.passwordConfirm" /></label></td>
        <td class="field"><html:password property="passwordConfirm" size="20" maxlength="20" /></td>
        <td class="description"><fmt:message key="userAdmin.tip.passwordConfirm" /></td>
    </tr>

    <tr>
        <td class="label"><label for="emailAddress" /><fmt:message key="userSettings.email" /></label></td>
        <td class="field"><html:text property="emailAddress" size="40" maxlength="40" /></td>
        <td class="description"><fmt:message key="userAdmin.tip.email" /></td>
    </tr>

    <tr>
        <td class="label"><label for="locale" /><fmt:message key="userSettings.locale" /></label></td>
        <td class="field">
           <html:select property="locale" size="1" >
              <html:options collection="locales" property="value" labelProperty="label"/>
           </html:select>
        </td>
        <td class="description"><fmt:message key="userAdmin.tip.locale" /></td>
    </tr>

    <tr>
        <td class="label"><label for="timeZone" /><fmt:message key="userSettings.timeZone" /></label></td>
        <td class="field">
           <html:select property="timeZone" size="1" >
               <html:options collection="timeZones" property="value" labelProperty="label"/>
           </html:select>
        </td>
        <td class="description"><fmt:message key="userAdmin.tip.timeZone" /></td>
    </tr>

    <tr>
        <td class="label"><label for="userEnabled" /><fmt:message key="userAdmin.enabled" /></label></td>
        <td class="field">
           <html:checkbox property="enabled" value="true" />
        </td>
        <td class="description"><fmt:message key="userAdmin.tip.enabled" /></td>
    </tr>
    
    <tr>
        <td class="label"><label for="userAdmin" /><fmt:message key="userAdmin.userAdmin" /></label></td>
        <td class="field">
           <html:checkbox property="userAdmin" value="true" />
        </td>
        <td class="description"><fmt:message key="userAdmin.tip.userAdmin" /></td>
    </tr>

    </table>
    
    <html:hidden property="delete" />
    <br />
    
    
    <c:if test="${userAdminForm.newUser == false}">
        <p class="subtitle"><fmt:message key="userAdmin.userWeblogs" /></p>

        <c:choose>
            <c:when test="${!empty model.permissions}"> 

                <p><fmt:message key="userAdmin.userMemberOf" /></p>  
                <table class="rollertable" style="width: 80%">
                <c:forEach var="perms" items="${model.permissions}">
                   <tr>
                       <td width="%30">
                           <a href='<c:out value="${model.baseURL}" />/page/<c:out value="${perms.website.handle}" />'>
                               <c:out value="${perms.website.name}" /> [<c:out value="${perms.website.handle}" />] 
                           </a>
                       </td>
                       <td width="%15">
                           <c:url value="/roller-ui/authoring/weblog.do" var="newEntry">
                               <c:param name="method" value="create" />
                               <c:param name="weblog" value="${perms.website.handle}" />
                           </c:url>
                           <img src='<c:url value="/images/page_white_edit.png"/>' />
                           <a href='<c:out value="${newEntry}" />'>
                               <fmt:message key="userAdmin.newEntry" /></a>
                       </td>
                       <td width="%15">
                           <c:url value="/roller-ui/authoring/weblogEntryManagement.do" var="editEntries">
                               <c:param name="method" value="query" />
                               <c:param name="weblog" value="${perms.website.handle}" />
                           </c:url>
                           <img src='<c:url value="/images/page_white_edit.png"/>' />
                           <a href='<c:out value="${editEntries}" />'>
                               <fmt:message key="userAdmin.editEntries" /></a> 
                       </td>
                       <td width="%15">
                           <c:url value="/roller-ui/authoring/website.do" var="manageWeblog">
                               <c:param name="method" value="edit" />
                               <c:param name="weblog" value="${perms.website.handle}" />
                           </c:url>
                           <img src='<c:url value="/images/page_white_edit.png"/>' />
                           <a href='<c:out value="${manageWeblog}" />'>
                               <fmt:message key="userAdmin.manage" /></a>
                       </td>
                   </tr>
                </c:forEach>    
                </table>

            </c:when>

            <c:otherwise>
                <fmt:message key="userAdmin.userHasNoWeblogs" />
            </c:otherwise>

        </c:choose>
    </c:if>
    
    <br />
    <br />

    <div class="control">
       <input type="submit" value='<fmt:message key="userAdmin.save" />'/></input>
       <input type="button" value='<fmt:message key="application.cancel" />' onclick="cancel()"></input>
    </div>
        
    </html:form>
    
</c:if>




