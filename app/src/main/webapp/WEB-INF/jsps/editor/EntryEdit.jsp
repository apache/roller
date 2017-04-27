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
<link rel="stylesheet" href="//netdna.bootstrapcdn.com/bootstrap/3.3.4/css/bootstrap.min.css">
<link rel="stylesheet" href="//cdn.quilljs.com/0.20.1/quill.snow.css">

<script src="<c:url value="/tb-ui/scripts/jquery-2.2.3.min.js" />"></script>
<script src='<c:url value="/tb-ui/jquery-ui-1.11.4/jquery-ui.min.js"/>'></script>
<script src="//ajax.googleapis.com/ajax/libs/angularjs/1.5.5/angular.min.js"></script>
<script src="//ajax.googleapis.com/ajax/libs/angularjs/1.5.5/angular-sanitize.min.js"></script>
<script src="//cdn.quilljs.com/0.20.1/quill.js"></script>

<script>
    var contextPath = "${pageContext.request.contextPath}";
    var weblogId = "<c:out value='${actionWeblog.id}'/>";
    var entryId = "<c:out value='${param.entryId}'/>";
    var newEntryUrl = "<c:url value='/tb-ui/app/authoring/entryAdd'/>?weblogId=" + weblogId;
    var commentCountTmpl = "<fmt:message key='entryEdit.hasComments'/>";
    var msg = {
        deleteLabel: "<fmt:message key='generic.delete'/>",
        cancelLabel: "<fmt:message key='generic.cancel'/>",
    };
    var mediaFileChooserUrl = "<c:url value='/tb-ui/app/authoring/mediaFileChooser'/>?weblogId=" + weblogId;
</script>

<script src="<c:url value='/tb-ui/scripts/commonangular.js'/>"></script>
<script src="<c:url value='/tb-ui/scripts/entryedit.js'/>"></script>

<%-- Titling, processing actions different between entry add and edit --%>
<c:choose>
    <c:when test="${actionName == 'entryEdit'}">
        <c:set var="subtitleKey">entryEdit.subtitle.editEntry</c:set>
        <c:set var="mainAction">entryEdit</c:set>
        <input id="refreshURL" type="hidden"
            value="<c:url value='/tb-ui/app/authoring/entryEdit'/>?weblogId=<c:out value='${param.weblogId}'/>"\
                "&entryId=<c:out value='${param.entryId}'/>"/>
    </c:when>
    <c:otherwise>
        <c:set var="subtitleKey">entryEdit.subtitle.newEntry</c:set>
        <c:set var="mainAction">entryAdd</c:set>
        <input id="refreshURL" type="hidden" value="<c:url value='/tb-ui/app/authoring/entryAdd'/>"/>
    </c:otherwise>
</c:choose>

    <div id="errorMessageDiv" class="errors" ng-show="ctrl.errorObj.errorMessage" ng-cloak>
      <b>{{ctrl.errorObj.errorMessage}}</b>
      <ul>
         <li ng-repeat="em in ctrl.errorObj.errors">{{em}}</li>
      </ul>
    </div>

    <div id="successMessageDiv" class="messages" ng-show="ctrl.saveResponseMessage" ng-cloak>
        <p>{{ctrl.saveResponseMessage}}</p>
    </div>

<p class="subtitle">
    <fmt:message key="${subtitleKey}">
        <fmt:param value="${actionWeblog.handle}"/>
    </fmt:message>
</p>

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
                <label for="status"><fmt:message key="entryEdit.status" /></label>
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
                    <a id="permalink" ng-href='{{ctrl.entry.permalink}}'>{{ctrl.entry.permalink}}</a>
                    <img src='<c:url value="/images/launch-link.png"/>' />
                </span>
                <span ng-show="ctrl.entry.status != 'PUBLISHED'">
                    {{ctrl.entry.permalink}}
                </span>
            </td>
        </tr>

        <tr>
            <td class="entryEditFormLabel">
                <label for="categoryId"><fmt:message key="entryEdit.category" /></label>
            </td>
            <td ng-cloak>
                <select id="categoryId" ng-model="ctrl.entry.category.id" size="1" required>
                   <option ng-repeat="(key, value) in ctrl.metadata.categories" value="{{key}}">{{value}}</option>
                </select>
            </td>
        </tr>

        <tr>
            <td class="entryEditFormLabel">
                <label for="tags"><fmt:message key="entryEdit.tags" /></label>
            </td>
            <td>
                <input id="tags" type="text" cssClass="entryEditTags" ng-model="ctrl.entry.tagsAsString"
                    size="70" maxlength="255" tabindex="3" style="width:30%">
            </td>
        </tr>

        <tr ng-cloak>
            <td class="entryEditFormLabel">
                <label for="title"><fmt:message key="entryEdit.editFormat" /></label>
            </td>
            <td>
                <span style="font-weight:bold">{{ctrl.metadata.editFormatDescriptions[ctrl.entry.editFormat]}}</span>
            </td>
        </tr>

    </table>

    <%-- ================================================================== --%>
    <%-- Weblog editor --%>

    <p class="toplabel">

    <div id="accordion">
        <h3>
            <fmt:message key="entryEdit.content" />
            <span style="font-weight:normal;float:right;">
                <a href="#" onClick="onClickAddImage();"><fmt:message key="entryEdit.insertMediaFile" /></a>
            </span>
        </h3>
        <div>

            <span ng-show="ctrl.entry.editFormat == 'RICHTEXT'">
                <div id="toolbar_quill" class="toolbar">
                    <span class="ql-format-group">
                        <select title="Font" class="ql-font">
                            <option value="sans-serif" selected="">Sans Serif</option>
                            <option value="serif">Serif</option>
                            <option value="monospace">Monospace</option>
                        </select>
                        <select title="Size" class="ql-size">
                            <option value="10px">Small</option>
                            <option value="13px" selected="">Normal</option>
                            <option value="18px">Large</option>
                            <option value="32px">Huge</option>
                        </select>
                    </span>
                    <span class="ql-format-group">
                        <span title="Bold" class="ql-format-button ql-bold"></span>
                        <span class="ql-format-separator"></span>
                        <span title="Italic" class="ql-format-button ql-italic"></span>
                        <span class="ql-format-separator"></span>
                        <span title="Underline" class="ql-format-button ql-underline"></span>
                        <span class="ql-format-separator"></span>
                        <span title="Strikethrough" class="ql-format-button ql-strike"></span>
                    </span>
                    <span class="ql-format-group">
                        <span title="List" class="ql-format-button ql-list"></span>
                        <span class="ql-format-separator"></span>
                        <span title="Bullet" class="ql-format-button ql-bullet"></span>
                        <span class="ql-format-separator"></span>
                        <select title="Text Alignment" class="ql-align">
                            <option value="left" label="Left" selected=""></option>
                            <option value="center" label="Center"></option>
                            <option value="right" label="Right"></option>
                            <option value="justify" label="Justify"></option>
                        </select>
                    </span>
                    <span class="ql-format-group">
                        <span title="Link" class="ql-format-button ql-link"></span>
                    </span>
                </div>
                <div id="editor_quill"></div>
            </span>
            <span ng-show="ctrl.entry.editFormat != 'RICHTEXT'">
                <textarea id="edit_content" cols="75" rows="25" style="width:100%" ng-model="ctrl.entry.text" tabindex="5"></textarea>
            </span>
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


    <%-- ********************************************************************* --%>
    <%-- Lightbox for popping up image chooser --%>

    <div id="mediafile_edit_lightbox" title="<fmt:message key='entryEdit.insertMediaFile'/>" style="display:none">
        <iframe id="mediaFileChooser"
                style="visibility:inherit"
                height="100%"
                width="100%"
                frameborder="no"
                scrolling="auto">
        </iframe>
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
            <input type="button" value="<fmt:message key='entryEdit.deleteEntry'/>" delete-entry-dialog="confirm-delete"/>
        </span>
    </div>
</div>

<div id="confirm-delete" title="<fmt:message key='entryEdit.deleteEntry'/>" style="display:none">
   <p><span class="ui-icon ui-icon-alert" style="float:left; margin:0 7px 20px 0;"></span><fmt:message key="entryEdit.confirmDelete"/></p>
</div>
