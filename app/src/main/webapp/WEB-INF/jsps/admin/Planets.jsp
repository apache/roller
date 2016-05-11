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
<link rel="stylesheet" media="all" href='<s:url value="/tb-ui/jquery-ui-1.11.0/jquery-ui.min.css"/>'/>
<script src="<s:url value='/tb-ui/scripts/jquery-2.1.1.min.js'/>"></script>
<script src="<s:url value='/tb-ui/jquery-ui-1.11.0/jquery-ui.min.js'/>"></script>
<script src="<s:url value='/tb-ui/jquery.loadTemplate-1.5.7.min.js'/>"></script>
<script>
    var contextPath = "${pageContext.request.contextPath}";
    var msg = {
        confirmLabel: '<s:text name="generic.confirm"/>',
        saveLabel: '<s:text name="generic.save"/>',
        cancelLabel: '<s:text name="generic.cancel"/>',
        editTitle: '<s:text name="generic.edit"/>',
        addTitle: '<s:text name="categoryForm.add.title"/>'
    };
</script>
<script src="<s:url value='/tb-ui/scripts/planets.js'/>"></script>

<p class="subtitle"><s:text name="planets.subtitle"/></p>

<s:form id="planetsForm" action="planets">
    <s:hidden name="salt"/>
    <s:hidden name="bean.id"/>
</s:form>

<br style="clear:left"/>

<table class="rollertable">
  <col style="width:15%"/>
  <col style="width:15%"/>
  <col style="width:49%"/>
  <col style="width:7%"/>
  <col style="width:7%"/>
  <col style="width:7%"/>
<thead>
   <tr class="rHeaderTr">
        <th><s:text name="planets.column.title"/></th>
        <th><s:text name="planets.column.handle"/></th>
        <th><s:text name="generic.description"/></th>
        <th><s:text name="generic.edit"/></th>
        <th><s:text name="generic.view"/></th>
        <th><s:text name="generic.delete"/></th>
    </tr>
</thead>
<tbody class="tableBody">
  <script id="tableTemplate" type="text/x-query-tmpl"/>
    <tr data-id="id">
      <td class="title-cell" data-content="title"></td>
      <td data-content="handle"></td>
      <td style="text-align:center;" data-content="description"></td>
     </tr>
  </script>
</tbody>
</table>

<s:form id="planetAddForm" action="planetEdit">
  <s:hidden name="salt"/>

  <div class="control clearfix">
      <input type="submit" id="add-link" value="<s:text name='planets.add'/>"/>
  </div>
</s:form>

<div id="confirm-delete" title="<s:text name='generic.confirm'/>" style="display:none">
  <p><span class="ui-icon ui-icon-alert" style="float:left; margin:0 7px 20px 0;"></span><s:text name='planets.delete.confirm'/></p>
  <span id="test123"></span>
</div>
