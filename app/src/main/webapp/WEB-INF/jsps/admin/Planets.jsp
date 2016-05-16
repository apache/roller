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
<link rel="stylesheet" media="all" href='<s:url value="/tb-ui/jquery-ui-1.11.4/jquery-ui.min.css"/>'/>
<script src="<s:url value='/tb-ui/scripts/jquery-2.2.3.min.js'/>"></script>
<script src="<s:url value='/tb-ui/jquery-ui-1.11.4/jquery-ui.min.js'/>"></script>
<script src="https://cdnjs.cloudflare.com/ajax/libs/jsrender/0.9.75/jsrender.min.js"></script>
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
<script src="<s:url value='/tb-ui/scripts/commonjquery.js'/>"></script>
<script src="<s:url value='/tb-ui/scripts/planets.js'/>"></script>

<p class="subtitle"><s:text name="planets.subtitle"/></p>

<s:form id="planetsForm" action="planets">
    <sec:csrfInput/>
</s:form>

<br style="clear:left"/>

<table class="rollertable">
<thead>
   <tr class="rHeaderTr">
        <th style="width:15%"><s:text name="planets.column.title"/></th>
        <th style="width:15%"><s:text name="planets.column.handle"/></th>
        <th style="width:49%"><s:text name="generic.description"/></th>
        <th style="width:7%"><s:text name="generic.edit"/></th>
        <th style="width:7%"><s:text name="generic.view"/></th>
        <th style="width:7%"><s:text name="generic.delete"/></th>
    </tr>
</thead>
<tbody id="tableBody">
  <script id="tableTemplate" type="text/x-jsrender">
    <tr id="{{:id}}">
      <td class="title-cell">{{:title}}</td>
      <td>{{:handle}}</td>
      <td style="text-align:center;">{{:description}}</td>
      <td align="center">
          <s:url var="edit" action="planetEdit"/>
          <s:a href='%{edit}?planetId={{:id}}'>
            <img src='<s:url value="/images/page_white_edit.png"/>' alt="icon"/>
          </s:a>
      </td>
      <td align="center">
          <a href='{{:absoluteURL}}' target="_blank">
              <img src='<s:url value="/images/world_go.png"/>' alt="icon" title="<s:text name='planets.column.viewFeed.tip' />"/>
          </a>
      </td>
      <td align="center">
          <a href="#" class="delete-link"><img src='<s:url value="/images/delete.png"/>' alt="icon"/></a>
      </td>
     </tr>
  </script>
</tbody>
</table>

<s:form id="planetAddForm" action="planetEdit">
  <sec:csrfInput/>

  <div class="control clearfix">
      <input type="submit" id="add-link" value="<s:text name='planets.add'/>"/>
  </div>
</s:form>

<div id="confirm-delete" title="<s:text name='generic.confirm'/>" style="display:none">
  <p><span class="ui-icon ui-icon-alert" style="float:left; margin:0 7px 20px 0;"></span><s:text name='planets.delete.confirm'/></p>
  <span id="test123"></span>
</div>
