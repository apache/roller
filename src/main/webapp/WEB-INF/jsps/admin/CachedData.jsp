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
<%@ include file="/WEB-INF/jsps/tightblog-taglibs.jsp" %>
<script src="https://cdn.jsdelivr.net/npm/vue/dist/vue.js"></script>
<script src="https://cdn.jsdelivr.net/npm/axios/dist/axios.min.js"></script>

<!--script-- src="https://cdn.jsdelivr.net/npm/vue"></!--script-->

<script src="<c:url value='/tb-ui/scripts/jquery-2.2.3.min.js'/>"></script>

<script>
    var contextPath = "${pageContext.request.contextPath}";
    var msg = {
        confirmLabel: '<fmt:message key="generic.confirm"/>',
        cancelLabel: '<fmt:message key="generic.cancel"/>',
    };
</script>

<div id="template">

<input id="refreshURL" type="hidden" value="<c:url value='/tb-ui/app/admin/cachedData'/>"/>

<div id="successMessageDiv" class="alert alert-success" role="alert" v-if="successMessage" ng-cloak>
    {{successMessage}}
    <button type="button" class="close" v-on:click="successMessage = null" aria-label="Close">
       <span aria-hidden="true">&times;</span>
    </button>
</div>

<div id="errorMessageDiv" class="alert alert-danger" role="alert" v-if="errorMessage" v-cloak>
    {{errorMessage}}
    <button type="button" class="close" v-on:click="errorMessage = null" aria-label="Close">
       <span aria-hidden="true">&times;</span>
    </button>
</div>

<p class="subtitle">
    <fmt:message key="cachedData.subtitle" />
<p>

<p><fmt:message key="cachedData.explanation"/></p>

<br style="clear:left"/>

<table class="table table-sm table-bordered table-striped">
<thead class="thead-light">
   <tr>
        <th style="width:10%"><fmt:message key="generic.name"/></th>
        <th style="width:9%"><fmt:message key="cachedData.maxEntries"/></th>
        <th style="width:9%"><fmt:message key="cachedData.currentSize"/></th>
        <th style="width:9%"><fmt:message key="cachedData.incoming"/></th>
        <th style="width:9%"><fmt:message key="cachedData.handledBy304"/></th>
        <th style="width:9%"><fmt:message key="cachedData.cacheHits"/></th>
        <th style="width:9%"><fmt:message key="cachedData.cacheMisses"/></th>
        <th style="width:9%"><fmt:message key="cachedData.304Efficiency"/></th>
        <th style="width:9%"><fmt:message key="cachedData.cacheEfficiency"/></th>
        <th style="width:9%"><fmt:message key="cachedData.totalEfficiency"/></th>
        <th style="width:9%"></th>
    </tr>
</thead>
<tbody id="tableBody" v-cloak>
    <tr v-for="item in cacheData">
        <td>{{item.cacheHandlerId}}</td>
        <td>{{item.maxEntries}}</td>
        <td>{{item.estimatedSize}}</td>
        <td>{{item.incomingRequests}}</td>
        <td>{{item.requestsHandledBy304}}</td>
        <td>{{item.cacheHitCount}}</td>
        <td>{{item.cacheMissCount}}</td>
        <td>{{item.incomingRequests > 0 ? (item.requestsHandledBy304 / item.incomingRequests).toFixed(3) : ''}}</td>
        <td>{{item.cacheRequestCount > 0 ? item.cacheHitRate.toFixed(3) : ''}}</td>
        <td>{{item.incomingRequests > 0 ? ((item.requestsHandledBy304 + item.cacheHitCount) / item.incomingRequests).toFixed(3) : ''}}</td>
        <td class="buttontd">
            <input v-if="item.maxEntries > 0" type="button" value="<fmt:message key='cachedData.clear'/>" v-on:click="clearCache(item.cacheHandlerId)"/>
        </td>
       </tr>
</tbody>
</table>

<div class="control clearfix">
    <input v-on:click="loadCacheData()" type="button" value="<fmt:message key='generic.refresh'/>"/>
</div>
  
<div v-if="metadata.weblogList">
    <br><br>
    <fmt:message key="cachedData.prompt.reset"/>:
    <br>
    <input v-on:click="resetHitCounts()" type="button" value="<fmt:message key='cachedData.button.reset'/>"/>
    <br><br>

    <fmt:message key="cachedData.prompt.index"/>:
    <br>
    <select v-model="weblogToReindex" size="1" required>
        <option v-for="value in metadata.weblogList" v-bind:value="value">{{value}}</option>
    </select>
    <input v-on:click="reindexWeblog()" type="button" value="<fmt:message key='cachedData.button.index'/>"/>
</div>

</div>

<script src="<c:url value='/tb-ui/scripts/cacheddata.js'/>"></script>
