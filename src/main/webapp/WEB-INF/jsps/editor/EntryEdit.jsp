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

<script src="<c:url value='/tb-ui/scripts/jquery-2.2.3.min.js'/>"></script>
<script src="<c:url value='/tb-ui/jquery-ui-1.11.4/jquery-ui.min.js'/>"></script>
<script src="//ajax.googleapis.com/ajax/libs/angularjs/1.7.0/angular.min.js"></script>
<script src="//ajax.googleapis.com/ajax/libs/angularjs/1.7.0/angular-sanitize.min.js"></script>

<script>
    var contextPath = "${pageContext.request.contextPath}";
    var weblogId = "<c:out value='${actionWeblog.id}'/>";
    var entryId = "<c:out value='${param.entryId}'/>";
    var newEntryUrl = "<c:url value='/tb-ui/app/authoring/entryAdd'/>?weblogId=" + weblogId;
    var loginUrl = "<c:url value='/tb-ui/app/login-redirect'/>";
    var msg = {
        confirmDeleteTmpl: "<fmt:message key='entryEdit.confirmDeleteTmpl'/>",
        commentCountTmpl: "<fmt:message key='entryEdit.hasComments'/>",
        sessionTimeoutTmpl: "<fmt:message key='entryEdit.sessionTimedOut'/>"
    };
</script>

<script src="<c:url value='/tb-ui/scripts/commonangular.js'/>"></script>
<script src="<c:url value='/tb-ui/scripts/entryedit.js'/>"></script>

<div id="successMessageDiv" class="alert alert-success" role="alert" ng-show="ctrl.successMessage" ng-cloak>
    {{ctrl.successMessage}}
    <button type="button" class="close" data-ng-click="ctrl.successMessage = null" aria-label="Close">
       <span aria-hidden="true">&times;</span>
    </button>
</div>

<div id="errorMessageDiv" class="alert alert-danger" role="alert" ng-show="ctrl.errorObj.errors" ng-cloak>
    <button type="button" class="close" data-ng-click="ctrl.errorObj.errors = null" aria-label="Close">
       <span aria-hidden="true">&times;</span>
    </button>
    <ul class="list-unstyled">
       <li ng-repeat="item in ctrl.errorObj.errors">{{item.message}}</li>
    </ul>
</div>

<div>
    <table class="entryEditTable" cellpadding="0" cellspacing="0" style="width:100%">

        <tr>
            <td class="entryEditFormLabel">
                <label for="title"><fmt:message key="entryEdit.entryTitle" /></label>
            </td>
            <td>
                <input id="title" type="text" ng-model="ctrl.entry.title" maxlength="255" tabindex="1" style="width:60%">
            </td>
        </tr>

        <tr>
            <td class="entryEditFormLabel">
                <fmt:message key="entryEdit.status" />
            </td>
            <td ng-cloak>
                <fmt:message key="generic.date.toStringFormat" var="dateFormat"/>
                <span ng-show="ctrl.entry.status == 'PUBLISHED'" style="color:green; font-weight:bold">
                    <fmt:message key="entryEdit.published" />
                    (<fmt:message key="entryEdit.updateTime" /> {{ctrl.entry.updateTime | date:'short'}})
                </span>
                <span ng-show="ctrl.entry.status == 'DRAFT'" style="color:orange; font-weight:bold">
                    <fmt:message key="entryEdit.draft" />
                    (<fmt:message key="entryEdit.updateTime" /> {{ctrl.entry.updateTime | date:'short'}})
                </span>
                <span ng-show="ctrl.entry.status == 'PENDING'" style="color:orange; font-weight:bold">
                    <fmt:message key="entryEdit.pending" />
                    (<fmt:message key="entryEdit.updateTime" /> {{ctrl.entry.updateTime | date:'short'}})
                </span>
                <span ng-show="ctrl.entry.status == 'SCHEDULED'" style="color:orange; font-weight:bold">
                    <fmt:message key="entryEdit.scheduled" />
                    (<fmt:message key="entryEdit.updateTime"/> {{ctrl.entry.updateTime | date:'short'}})
                </span>
                <span ng-show="!ctrl.entry.status" style="color:red; font-weight:bold">
                    <fmt:message key="entryEdit.unsaved" />
                </span>
            </td>
        </tr>

        <tr ng-show="ctrl.entry.id" ng-cloak>
            <td class="entryEditFormLabel">
                <label for="permalink"><fmt:message key="entryEdit.permalink" /></label>
            </td>
            <td>
                <span ng-show="ctrl.entry.status == 'PUBLISHED'">
                    <a id="permalink" ng-href='{{ctrl.entry.permalink}}' target="_blank">{{ctrl.entry.permalink}}</a>
                    <img src='<c:url value="/images/launch-link.png"/>' />
                </span>
                <span ng-show="ctrl.entry.status != 'PUBLISHED'">
                    {{ctrl.entry.permalink}}
                </span>
            </td>
        </tr>

        <tr>
            <td class="entryEditFormLabel">
                <label for="categoryId"><fmt:message key="generic.category" /></label>
            </td>
            <td ng-cloak>
                <select id="categoryId" ng-model="ctrl.entry.category.id" size="1" required>
                   <option ng-repeat="(key, value) in ctrl.metadata.categories" value="{{key}}">{{value}}</option>
                </select>
            </td>
        </tr>

        <tr>
            <td class="entryEditFormLabel">
                <label for="tags"><fmt:message key="generic.tags" /></label>
            </td>
            <td>
                <input id="tags" type="text" cssClass="entryEditTags" ng-model="ctrl.entry.tagsAsString"
                    maxlength="255" tabindex="3" style="width:60%">
            </td>
        </tr>

        <tr ng-cloak>
            <td class="entryEditFormLabel">
                <label for="title"><fmt:message key="entryEdit.editFormat" /></label>
            </td>
            <td ng-cloak>
                <select ng-model="ctrl.entry.editFormat" size="1" required>
                   <option ng-repeat="(key, value) in ctrl.metadata.editFormats" value="{{key}}">{{value}}</option>
                </select>
            </td>
        </tr>

    </table>

    <%-- ================================================================== --%>
    <%-- Weblog editor --%>

    <p class="toplabel">

    <div id="accordion">
        <h3>
            <fmt:message key="entryEdit.content" />
        </h3>
        <div>
            <textarea id="edit_content" cols="75" rows="25" style="width:100%" ng-model="ctrl.entry.text" tabindex="5"></textarea>
        </div>
        <h3><fmt:message key="entryEdit.summary"/><tags:help key="entryEdit.summary.tooltip"/></h3>
        <div>
            <textarea id="edit_summary" cols="75" rows="10" style="width:100%" ng-model="ctrl.entry.summary" tabindex="6"></textarea>
        </div>
        <h3><fmt:message key="entryEdit.notes"/><tags:help key="entryEdit.notes.tooltip"/></h3>
        <div>
            <textarea id="edit_notes" cols="75" rows="10" style="width:100%" ng-model="ctrl.entry.notes" tabindex="7"></textarea>
        </div>
    </div>

    <%-- ================================================================== --%>
    <%-- advanced settings  --%>

    <div class="controlToggle">
        <fmt:message key="entryEdit.miscSettings" />
    </div>

    <label for="link"><fmt:message key="entryEdit.specifyPubTime" />:</label>
    <div>
        <input type="number" min="0" max="23" step="1" ng-model="ctrl.entry.hours"/>
        :
        <input type="number" min="0" max="59" step="1" ng-model="ctrl.entry.minutes"/>
        &nbsp;&nbsp;
        <input type="text" id="publishDateString" size="12" readonly ng-model="ctrl.entry.dateString"/>
        {{ctrl.metadata.timezone}}
    </div>
    <br />

    <span ng-show="ctrl.metadata.commentingEnabled">
        <fmt:message key="entryEdit.allowComments" />
        <fmt:message key="entryEdit.commentDays" />
        <select id="commentDaysId" ng-model="ctrl.entry.commentDays" size="1" required>
           <option ng-repeat="(key, value) in ctrl.metadata.commentDayOptions" value="{{key}}">{{value}}</option>
        </select>
        <br />
    </span>

    <br />

    <table>
        <tr>
            <td><fmt:message key="entryEdit.searchDescription" />:<tags:help key="entryEdit.searchDescription.tooltip"/></td>
            <td style="width:75%"><input type="text" style="width:100%" maxlength="255" ng-model="ctrl.entry.searchDescription"></td>
        </tr>
        <tr>
            <td><fmt:message key="entryEdit.enclosureURL" />:<tags:help key="entryEdit.enclosureURL.tooltip"/></td>
            <td><input type="text" style="width:100%" maxlength="255" ng-model="ctrl.entry.enclosureUrl"></td>
        </tr>
        <tr ng-show="entryId">
            <td></td>
            <td>
                <span ng-show="ctrl.entry.enclosureType">
                    <fmt:message key="entryEdit.enclosureType" />: {{ctrl.entry.enclosureType}}
                </span>
                <span ng-show="ctrl.entry.enclosureLength">
                    <fmt:message key="entryEdit.enclosureLength" />: {{ctrl.entry.enclosureLength}}
                </span>
            </td>
        </tr>
    </table>

    <%-- ================================================================== --%>
    <%-- the button box --%>

    <br>
    <div class="control">
        <span style="padding-left:7px">
            <input type="button" value="<fmt:message key='entryEdit.save'/>" ng-click="ctrl.saveEntry('DRAFT')"/>
            <span ng-show="ctrl.entry.id">
                <input type="button" value="<fmt:message key='entryEdit.fullPreviewMode' />" ng-click="ctrl.previewEntry()" />
            </span>
            <span ng-show="ctrl.metadata.author">
                <input type="button" value="<fmt:message key='entryEdit.post'/>" ng-click="ctrl.saveEntry('PUBLISHED')"/>
            </span>
            <span ng-show="!ctrl.metadata.author">
                <input type="button" value="<fmt:message key='entryEdit.submitForReview'/>" ng-click="ctrl.saveEntry('PENDING')"/>
            </span>
        </span>

        <span style="float:right" ng-show="ctrl.entry.id">
            <input type="button" value="<fmt:message key='entryEdit.deleteEntry'/>" data-title="{{ctrl.entry.title}}" data-toggle="modal" data-target="#deleteEntryModal"/>
        </span>
    </div>
</div>

<!-- Delete entry modal -->
<div class="modal fade" id="deleteEntryModal" tabindex="-1" role="dialog" aria-labelledby="deleteEntryModalTitle" aria-hidden="true">
  <div class="modal-dialog modal-dialog-centered" role="document">
    <div class="modal-content">
      <div class="modal-header">
        <h5 class="modal-title" id="deleteEntryModalTitle"><fmt:message key="generic.confirm.delete"/></h5>
        <button type="button" class="close" data-dismiss="modal" aria-label="Close">
          <span aria-hidden="true">&times;</span>
        </button>
      </div>
      <div class="modal-body">
          <span id="confirmDeleteMsg"></span>
      </div>
      <div class="modal-footer">
        <button type="button" class="btn btn-secondary" data-dismiss="modal"><fmt:message key='generic.cancel'/></button>
        <button type="button" class="btn btn-danger" ng-click="ctrl.deleteWeblogEntry()"><fmt:message key='generic.delete'/></button>
      </div>
    </div>
  </div>
</div>
