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
<script src="<s:url value='/tb-ui/scripts/commonjquery.js'/>"></script>
<p class="subtitle"><s:text name="inviteMember.subtitle" /></p>
<p><s:text name="inviteMember.prompt" /></p>

<s:form>
    <sec:csrfInput/>
    <s:hidden id="invite_weblog" name="weblog" value="%{actionWeblog.handle}"/>

    <select name="userId" id="membersinvite-select-user"/>

    <div style="clear:left">
       <label for="permissionString" class="formrow" />
           <s:text name="inviteMember.permissions" /></label>
       <input type="radio" name="permissionString" value="OWNER"  />
       <s:text name="inviteMember.administrator" />
       <input type="radio" name="permissionString" value="POST" checked />
       <s:text name="inviteMember.author" />
       <input type="radio" name="permissionString" value="EDIT_DRAFT" />
       <s:text name="inviteMember.limited" />
    </div>

    <br />
    <s:submit id="invite_button" value="%{getText('inviteMember.button.save')}" action="invite!save"/>
    <s:submit value="%{getText('generic.cancel')}" action="invite!cancel" />

</s:form>

<script>
var contextPath = "${pageContext.request.contextPath}";
$(function() {
  $.ajax({
     type: "GET",
     url: contextPath + '/tb-ui/authoring/rest/' + $('#invite_weblog').attr('value') + '/potentialmembers',
     success: function(data, textStatus, xhr) {
       for (var key in data) {
         $('#membersinvite-select-user').append('<option value="' + key + '">' + data[key] + '</option>');
       }
       if (document.getElementById('membersinvite-select-user').length == 0) {
         document.getElementById('invite_button').disabled = true;
       }
     }
  });
});
</script>
