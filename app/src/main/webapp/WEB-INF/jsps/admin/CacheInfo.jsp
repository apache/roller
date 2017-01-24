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
<link rel="stylesheet" media="all" href='<s:url value="/tb-ui/jquery-ui-1.11.4/jquery-ui.min.css"/>'/>
<script src="<s:url value='/tb-ui/scripts/jquery-2.2.3.min.js'/>"></script>
<script src="<s:url value='/tb-ui/jquery-ui-1.11.4/jquery-ui.min.js'/>"></script>
<script src="https://cdnjs.cloudflare.com/ajax/libs/jsrender/0.9.75/jsrender.min.js"></script>
<script>
    $.views.converters("todate", function(val) {
      return new Date(val).toISOString();
    });
    var contextPath = "${pageContext.request.contextPath}";
    var msg = {
        confirmLabel: '<fmt:message key="generic.confirm"/>',
        cancelLabel: '<fmt:message key="generic.cancel"/>',
    };
</script>
<script src="<s:url value='/tb-ui/scripts/commonjquery.js'/>"></script>
<script src="<s:url value='/tb-ui/scripts/cacheInfo.js'/>"></script>

<div id="success-message" class="messages" style="display:none">
    <span class="textSpan"></span>
</div>

<div id="failure-message" class="errors" style="display:none">
    <span class="textSpan"></span>
</div>

<p class="subtitle"><fmt:message key="cacheInfo.subtitle" />
<p><fmt:message key="cacheInfo.prompt" />

<input type="hidden" id="refreshURL" value="<s:url action='cacheInfo'/>"/>

<br style="clear:left"/>

<table class="rollertable">
<thead>
   <tr>
        <th style="width:20%"><fmt:message key="generic.name"/></th>
        <th style="width:20%"><fmt:message key="cacheInfo.startTime"/></th>
        <th style="width:10%"><fmt:message key="cacheInfo.puts"/></th>
        <th style="width:10%"><fmt:message key="cacheInfo.removes"/></th>
        <th style="width:10%"><fmt:message key="cacheInfo.hits"/></th>
        <th style="width:10%"><fmt:message key="cacheInfo.misses"/></th>
        <th style="width:10%"><fmt:message key="cacheInfo.efficiency"/></th>
        <th style="width:10%"></th>
    </tr>
</thead>
<tbody id="tableBody">
  <script id="tableTemplate" type="text/x-jsrender">
    {{props}}
      <tr id="{{:key}}">
        <td class="title-cell">{{:key}}</td>
        <td>{{todate:prop.startTime}}</td>
        <td>{{:prop.puts}}</td>
        <td>{{:prop.removes}}</td>
        <td>{{:prop.hits}}</td>
        <td>{{:prop.misses}}</td>
        <td>{{:prop.efficiency}}</td>
        <td align="center">
            <a href="#" class="reset-link"><fmt:message key="cacheInfo.clear"/></a>
        </td>
       </tr>
     {{/props}}
  </script>

</tbody>
</table>

<div class="control clearfix">
  <input id="refresh-cache-stats" type="button" value="<fmt:message key='generic.refresh'/>"/>
  <input id="clear-all-caches" type="button" value="<fmt:message key='cacheInfo.clearAll'/>"/>
</div>

<br><br>
<fmt:message key="maintenance.prompt.reset"/>:
<br><br>
<input id="reset-hit-counts" type="button" value="<fmt:message key='maintenance.button.reset'/>"/>

<br><br>
<fmt:message key="maintenance.prompt.index"/>:
<br><br>
<select id="weblog-to-reindex"/> <input id="index-weblog" type="button" value="<fmt:message key='maintenance.button.index'/>"/>
<br><br>

<div id="confirm-resetall" title="<fmt:message key='generic.confirm'/>" style="display:none">
   <p><span class="ui-icon ui-icon-alert" style="float:left; margin:0 7px 20px 0;"></span>
   <fmt:message key='cacheInfo.confirmResetAll'/></p>
</div>
