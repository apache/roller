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
<link rel="stylesheet" media="all" href='<c:url value="/tb-ui/jquery-ui-1.11.4/jquery-ui.min.css"/>' />
<script src="<c:url value="/tb-ui/scripts/jquery-2.2.3.min.js" />"></script>
<script src='<c:url value="/tb-ui/jquery-ui-1.11.4/jquery-ui.min.js"/>'></script>
<script src="//ajax.googleapis.com/ajax/libs/angularjs/1.6.1/angular.min.js"></script>

<script>
    var contextPath = "${pageContext.request.contextPath}";
    var weblogId = "<c:out value='${actionWeblog.id}'/>";
    var msg = {
        deleteLabel: "<fmt:message key='generic.delete'/>",
        cancelLabel: "<fmt:message key='generic.cancel'/>"
    };
</script>

<script src="<c:url value='/tb-ui/scripts/commonangular.js'/>"></script>
<script src="<c:url value='/tb-ui/scripts/entries.js'/>"></script>

<input id="refreshURL" type="hidden" value="<c:url value='/tb-ui/app/authoring/entries'/>?weblogId=<c:out value='${param.weblogId}'/>"/>

<p class="subtitle">
    <fmt:message key="entries.subtitle" >
        <fmt:param value="${actionWeblog.handle}" />
    </fmt:message>
</p>

<p class="pagetip">
    <fmt:message key="entries.tip" />
</p>

{{entryArr = ctrl.entriesData.entries;""}}
<div class="sidebarFade">
    <div class="menu-tr">
        <div class="menu-tl">
            <div class="sidebarInner">

                <h3><fmt:message key="entries.sidebarTitle" /></h3>
                <hr size="1" noshade="noshade" />

                <p><fmt:message key="entries.sidebarDescription" /></p>

                <div>
                    <div class="sideformrow">
                        <label for="categoryId" class="sideformrow">
                        <fmt:message key="entries.label.category" /></label>
                        <select ng-model="ctrl.searchParams.categoryName" size="1" required>
                           <option ng-repeat="(key, value) in ctrl.lookupFields.categories" value="{{key}}">{{value}}</option>
                        </select>
                    </div>
                    <br /><br />

                    <div class="sideformrow">
                        <label for="startDateString" class="sideformrow"><fmt:message key="entries.label.startDate" />:</label>
                        <input type="text" id="startDateString" ng-model="ctrl.searchParams.startDateString" size="12" readonly="true"/>
                    </div>

                    <div class="sideformrow">
                        <label for="endDateString" class="sideformrow"><fmt:message key="entries.label.endDate" />:</label>
                        <input type="text" id="endDateString" ng-model="ctrl.searchParams.endDateString" size="12" readonly="true"/>
                    </div>
                    <br /><br />

                    <div class="sideformrow">
                        <label for="status" class="sideformrow">
                            <fmt:message key="entries.label.status" />:
                        </label>
                        <div>
                            <select ng-model="ctrl.searchParams.status" size="1" required>
                                <option ng-repeat="(key, value) in ctrl.lookupFields.statusOptions" value="{{key}}">{{value}}</option>
                            </select>
                        </div>
                    </div>

                    <div class="sideformrow">
                        <label for="status" class="sideformrow">
                            <fmt:message key="entries.label.sortby" />:
                            <br /><br />
                        </label>
                        <div>
                            <div ng-repeat="(key, value) in ctrl.lookupFields.sortByOptions">
                                <input type="radio" name="sortBy" ng-model="ctrl.searchParams.sortBy" ng-value='key'> {{value}}<br>
                            </div>
                        </div>
                    </div>
                    <br />
                    <input ng-click="ctrl.loadEntries()" type="button" value="<fmt:message key='entries.button.query'/>" />
                </div>
            </div> <!-- sidebarInner -->
        </div>
    </div>
</div>


<%-- ============================================================= --%>
<%-- Number of entries and date message --%>
<%-- ============================================================= --%>

<div class="tablenav">

    <div style="float:left;">
        {{entryArr.length}} <fmt:message key="entries.nowShowing"/>
    </div>
    <span ng-if="entryArr.length > 0">
        <div style="float:right;">
            <span ng-if="entryArr[0].pubTime != null">
                {{entryArr[0].pubTime | date:'short'}}
            </span>
            ---
            <span ng-if="entryArr[entryArr.length - 1].pubTime != null">
                {{entryArr[entryArr.length - 1].pubTime | date:'short'}}
            </span>
        </div>
    </span>
    <br><br>

    <%-- ============================================================= --%>
    <%-- Next / previous links --%>
    <%-- ============================================================= --%>

    <span ng-if="ctrl.pageNum > 0 || ctrl.entriesData.hasMore">
        <center>
            &laquo;
            <input type="button" value="<fmt:message key='entries.prev'/>"
                ng-disabled="ctrl.pageNum <= 0" ng-click="ctrl.previousPage()">
            |
            <input type="button" value="<fmt:message key='entries.next'/>"
                ng-disabled="!ctrl.entriesData.hasMore" ng-click="ctrl.nextPage()">
            &raquo;
        </center>
    </span>

    <br>
</div>


<%-- ============================================================= --%>
<%-- Entry table--%>
<%-- ============================================================= --%>

<p>
    <span class="draftEntryBox">&nbsp;&nbsp;&nbsp;&nbsp;</span>
    <fmt:message key="entries.draft" />&nbsp;&nbsp;
    <span class="pendingEntryBox">&nbsp;&nbsp;&nbsp;&nbsp;</span>
    <fmt:message key="entries.pending" />&nbsp;&nbsp;
    <span class="scheduledEntryBox">&nbsp;&nbsp;&nbsp;&nbsp;</span>
    <fmt:message key="entries.scheduled" />&nbsp;&nbsp;
</p>

<table class="rollertable" width="100%">

    <tr>
        <th width="5%"><fmt:message key="entries.pubTime" /></th>
        <th width="5%"><fmt:message key="entries.updateTime" /></th>
        <th><fmt:message key="entries.title" /></th>
        <th width="5%"><fmt:message key="entries.category" /></th>
        <th width="5%"></th>
        <th width="5%"></th>
        <th width="5%"></th>
    </tr>

    <tr ng-repeat="entry in ctrl.entriesData.entries"
        ng-class="{DRAFT : 'draftentry', PENDING : 'pendingentry', SCHEDULED : 'scheduledentry'}[entry.status]">

        <td>
            <span ng-if="entry.pubTime != null">
              {{entry.pubTime | date:'short'}}
            </span>
        </td>

        <td>
            <span ng-if="entry.updateTime != null">
              {{entry.updateTime | date:'short'}}
            </span>
        </td>

        <td>
            {{entry.title | limitTo:80}}
        </td>

        <td>
            {{entry.category.name}}
        </td>

        <td>
            <span ng-if="entry.status == 'PUBLISHED'">
                <a ng-href='{{entry.permalink}}' target="_blank"><fmt:message key="entries.view" /></a>
            </span>
        </td>

        <td>
            <a target="_blank" ng-href="<c:url value='/tb-ui/app/authoring/entryEdit'/>?weblogId=<c:out value='${param.weblogId}'/>&entryId={{entry.id}}">
                <fmt:message key="generic.edit" />
            </a>
        </td>

        <td>
            <button confirm-delete-dialog="confirm-delete" id-to-delete="{{entry.id}}"><fmt:message key="generic.delete" /></button>
        </td>

        </tr>
</table>

<div id="confirm-delete" title="<fmt:message key='entryEdit.deleteEntry'/>" style="display:none">
   <p><span class="ui-icon ui-icon-alert" style="float:left; margin:0 7px 20px 0;"></span><fmt:message key="entryEdit.confirmDelete"/></p>
</div>

<span ng-if="ctrl.entriesData.entries.length == 0">
    <fmt:message key="entries.noneFound" />
    <br><br><br><br><br><br><br><br><br><br><br><br>
</span>

