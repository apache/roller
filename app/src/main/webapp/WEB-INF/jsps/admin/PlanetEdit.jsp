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
<link rel="stylesheet" media="all" href='<s:url value="/tb-ui/jquery-ui-1.11.4/jquery-ui.min.css"/>' />
<script src="<s:url value='/tb-ui/scripts/jquery-2.2.3.min.js'/>"></script>
<script src="<s:url value='/tb-ui/jquery-ui-1.11.4/jquery-ui.min.js'/>"></script>
<script src="https://cdnjs.cloudflare.com/ajax/libs/jsrender/0.9.75/jsrender.min.js"></script>
<script>
var contextPath = "${pageContext.request.contextPath}";
var msg= {
    confirmLabel: '<s:text name="generic.confirm"/>',
    saveLabel: '<s:text name="generic.save"/>',
    cancelLabel: '<s:text name="generic.cancel"/>',
    editTitle: '<s:text name="generic.edit"/>',
    addTitle: '<s:text name="categoryForm.add.title"/>'
};
</script>
<script src="<s:url value='/tb-ui/scripts/commonjquery.js'/>"></script>
<script src="<s:url value='/tb-ui/scripts/planetEdit.js'/>"></script>

<input type="hidden" id="recordId" value="<s:property value='%{#parameters.planetId}'/>"/>
<input type="hidden" id="refreshURL" value="<s:url action='planetEdit'/>?planetId=<s:property value='%{#parameters.planetId}'/>"/>

<div id="formBody"></div>
<script id="formTemplate" type="text/x-jsrender">
  <div class="formrow">
      <label for="edit-title" class="formrow"><s:text name="planets.title"/></label>
      <input type="text" id="edit-title" size="48" maxlength="64" value="{{:title}}" onBlur="this.value=this.value.trim()"/>
  </div>

  <div class="formrow">
      <label for="edit-handle" class="formrow"><s:text name="planets.handle" /></label>
      <input type="text" id="edit-handle" size="48" maxlength="48" value="{{:handle}}" onBlur="this.value=this.value.trim()"/>
  </div>

  <div class="formrow">
      <label for="edit-description" class="formrow"><s:text name="generic.description" /></label>
      <input type="text" id="edit-description" size="90" maxlength="255" value="{{:description}}" onBlur="this.value=this.value.trim()"/>
  </div>
</script>

<div class="formrow">
    <label class="formrow">&nbsp;</label>
    <input type="button" value="<s:text name="generic.save" />" id="save-planet"/>
    <input type="reset" id='reset-planet'/>
</div>

<div id="feedManagement" style="display:none">

<br style="clear:left"><br>

<p><s:text name="planetSubscriptions.prompt.add" /></p>

<div class="formrow">
    <label for="feedUrl" class="formrow"><s:text name="planetSubscription.feedUrl" /></label>
    <input type="text" id="feedUrl" size="60" maxlength="255" onBlur="this.value=this.value.trim()"/>
    <input type="button" id="add-link" value="<s:text name="generic.save" />"/>
</div>

<br style="clear:left" />

<h2>
    <s:text name="planetSubscriptions.existingTitle" />
</h2>

<table class="rollertable">
  <thead>
    <tr class="rHeaderTr">
        <th style="width:30%"><s:text name="planetSubscriptions.column.title" /></th>
        <th style="width:60%"><s:text name="planetSubscriptions.column.feedUrl" /></th>
        <th width="width:10%"><s:text name="generic.delete" /></th>
    </tr>
  </thead>
  <tbody id="tableBody">
    <script id="tableTemplate" type="text/x-jsrender">
        <tr id="{{:id}}">
          <td class="title-cell">{{:title}}</td>
          <td>{{:feedURL}}</td>
          <td align="center">
              <a href="#" class="delete-link"><img src='<s:url value="/images/delete.png"/>' alt="icon"/></a>
          </td>
         </tr>
    </script>
  </tbody>
</table>

<div id="confirm-delete" title="<s:text name='generic.confirm'/>" style="display:none">
   <p><span class="ui-icon ui-icon-alert" style="float:left; margin:0 7px 20px 0;"></span><s:text name="planetSubscriptions.delete.confirm"/></p>
</div>

</div>
