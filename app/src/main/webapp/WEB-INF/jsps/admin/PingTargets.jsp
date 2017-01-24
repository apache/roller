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
<%@ include file="/WEB-INF/jsps/tightblog-taglibs.jsp" %>
<link rel="stylesheet" media="all" href='<s:url value="/tb-ui/jquery-ui-1.11.4/jquery-ui.min.css"/>' />
<script src='<s:url value="/tb-ui/scripts/jquery-2.2.3.min.js" />'></script>
<script src='<s:url value="/tb-ui/jquery-ui-1.11.4/jquery-ui.min.js"/>'></script>
<script src="https://cdnjs.cloudflare.com/ajax/libs/jsrender/0.9.75/jsrender.min.js"></script>
<script>
var contextPath = "${pageContext.request.contextPath}";
var msg= {
    confirmLabel: '<fmt:message key="generic.confirm"/>',
    saveLabel: '<fmt:message key="generic.save"/>',
    cancelLabel: '<fmt:message key="generic.cancel"/>',
    editTitle: '<fmt:message key="generic.edit"/>',
    addTitle: '<fmt:message key="pingTarget.addTarget"/>'
};
</script>
<script src="<s:url value='/tb-ui/scripts/commonjquery.js'/>"></script>
<script src="<s:url value='/tb-ui/scripts/pingtargets.js'/>"></script>

<div id="errorMessageDiv" class="errors" style="display:none">
  <span>Error message here.</span>
</div>

<div id="successMessageDiv" style="display:none">
  <span>Information has been saved.</span>
</div>

<p class="subtitle">
    <fmt:message key="pingTargets.subtitle" />
</p>

<p/><fmt:message key="pingTargets.explanation"/><p/>

<input type="hidden" id="refreshURL" value="<s:url action='pingTargets'/>"/>

<table class="rollertable">
  <thead>
    <tr>
        <th width="20%"><fmt:message key="generic.name" /></th>
        <th width="50%"><fmt:message key="pingTarget.pingUrl" /></th>
        <th width="15%" colspan="2"><fmt:message key="pingTarget.autoEnabled" /></th>
        <th width="5%"><fmt:message key="generic.edit" /></th>
        <th width="5%"><fmt:message key="pingTarget.test" /></th>
        <th width="5%"><fmt:message key="pingTarget.remove" /></th>
    </tr>
  </thead>
  <tbody id="tableBody">
    <script id="tableTemplate" type="text/x-jsrender">
      <tr id="{{:id}}">
        <td class="name-cell">{{:name}}</td>
        <td class="url-cell">{{:pingUrl}}</td>
        <td class="current-state-cell" align="center">
           <span style="font-weight: bold;">
           {{if enabled}}
               <fmt:message key="pingTarget.enabled"/>
           {{else}}
               <fmt:message key="pingTarget.disabled"/>
           {{/if}}
           </span>
        </td>
        <td class="change-state-cell" align="center">
           <a href="#" class="enable-toggle" data-enabled='{{:enabled}}'>
           {{if enabled}}
               <fmt:message key="pingTarget.disable"/>
           {{else}}
               <fmt:message key="pingTarget.enable"/>
           {{/if}}
           </a>
        </td>
        <td align="center">
            <a class="edit-link">
                <img src='<s:url value="/images/page_white_edit.png"/>' alt="<fmt:message key="generic.edit" />" />
            </a>
        </td>
        <td align="center">
            <a href="#" class="test-link">
                <fmt:message key="pingTarget.test" />
            </a>
        </td>
        <td align="center">
            <a class="delete-link">
                <img src='<s:url value="/images/delete.png"/>' alt="<fmt:message key="pingTarget.remove" />" />
            </a>
        </td>
      </tr>
    </script>
  </tbody>
</table>

<div class="control clearfix">
    <input type="submit" id="add-link" value="<fmt:message key='pingTarget.addTarget'/>"/>
</div>

<div id="confirm-delete" title="<fmt:message key='generic.confirm'/>" style="display:none">
   <p><span class="ui-icon ui-icon-alert" style="float:left; margin:0 7px 20px 0;"></span><fmt:message key="pingTarget.confirmRemove"/></p>
</div>

<div id="pingtarget-edit" style="display:none">
    <span id="pingtarget-edit-error" style="display:none"><fmt:message key='pingTarget.nameOrUrlNotUnique'/></span>
    <table>
        <tr>
            <td style="width:30%"><label for="pingtarget-edit-name"><fmt:message key='generic.name'/></label></td>
            <td><input id="pingtarget-edit-name" maxlength="40" size="50" onBlur="this.value=this.value.trim()"/></td>
        </tr>
        <tr>
            <td><label for="pingtarget-edit-url"><fmt:message key='pingTarget.pingUrl'/></label></td>
            <td><input id="pingtarget-edit-url" maxlength="128" size="50" onBlur="this.value=this.value.trim()"/></td>
        </tr>
    </table>
</div>
