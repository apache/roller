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
<script src="<s:url value='/tb-ui/scripts/jquery-2.2.3.min.js'/>"></script>
<script src="<s:url value='/tb-ui/scripts/commonjquery.js'/>"></script>
<script>
var contextPath = "${pageContext.request.contextPath}";
$(function() {
  $.ajax({
     type: "GET",
     url: contextPath + '/tb-ui/admin/rest/useradmin/userlist',
     success: function(data, textStatus, xhr) {
       for (var key in data) {
         $('#useradmin-select-user').append('<option value="' + key + '">' + data[key] + '</option>');
       }
     }
  });
  $("#select-user").click(function(e) {
     e.preventDefault();
     var selectedUserId = $('#useradmin-select-user').val();
     window.location.replace(contextPath + '/tb-ui/admin/modifyUser.rol?bean.id=' + selectedUserId);
  });
});
</script>
<p class="subtitle"><s:text name="userAdmin.subtitle.searchUser" /></p>
<br />

<select id="useradmin-select-user"/>
<input id="select-user" type="button" style="margin:4px" value='<s:text name="generic.edit" />'/>

<%-- LDAP uses external user creation --%>
<s:if test="getProp('authentication.method') != 'ldap'">
    <p class="subtitle"><s:text name="userAdmin.subtitle.userCreation" /></p>
    <s:text name="userAdmin.prompt.orYouCan" />
    <s:url action="createUser" id="createUser" />
    <a href="<s:property value="createUser" />">
        <s:text name="userAdmin.prompt.createANewUser" />
    </a>
</s:if>
