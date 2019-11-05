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
<script src='<c:url value="/tb-ui/scripts/jquery-2.2.3.min.js" />'></script>
<script src='<c:url value="/tb-ui/jquery-ui-1.11.4/jquery-ui.min.js"/>'></script>
<script src="//ajax.googleapis.com/ajax/libs/angularjs/1.7.0/angular.min.js"></script>
<script src="//ajax.googleapis.com/ajax/libs/angularjs/1.7.0/angular-sanitize.min.js"></script>

<script>
    var contextPath = "${pageContext.request.contextPath}";
    var weblogId = "<c:out value='${actionWeblog.id}'/>";
    var entryId = "<c:out value='${param.entryId}'/>";
    var nowShowingTmpl = "<fmt:message key='comments.nowShowing'/>";
    var commentHeaderTmpl = "<fmt:message key='comments.commentHeader'/>";
    var entryTitleTmpl = "<fmt:message key='comments.entry.subtitle'/>";
</script>

<script src="<c:url value='/tb-ui/scripts/commonangular.js'/>"></script>
<script src="<c:url value='/tb-ui/scripts/comments.js'/>"></script>

<c:choose>
    <c:when test="${param.entryId == null}">
        <input type="hidden" id="refreshURL" value="<c:url value='/tb-ui/app/authoring/comments'/>?weblogId=<c:out value='${param.weblogId}'/>"/>
    </c:when>
    <c:otherwise>
        <input type="hidden" id="refreshURL" value="<c:url value='/tb-ui/app/authoring/comments'/>?weblogId=<c:out value='${param.weblogId}'/>&entryId=<c:out value='${param.entryId}'/>"/>
    </c:otherwise>
</c:choose>

<div id="errorMessageDiv" class="alert alert-danger" role="alert" ng-show="ctrl.errorObj.errors" ng-cloak>
    <button type="button" class="close" data-ng-click="ctrl.errorObj.errors = null" aria-label="Close">
       <span aria-hidden="true">&times;</span>
    </button>
    <ul>
       <li ng-repeat="item in ctrl.errorObj.errors">{{item.message}}</li>
    </ul>
</div>

<p class="subtitle" ng-show="ctrl.entryTitleMsg != ''">
    <span>
        <span ng-bind-html="ctrl.entryTitleMsg"></span>
    </span>
</p>

{{commentArr = ctrl.commentData.comments;""}}

    <p class="pagetip">
        <fmt:message key="comments.tip" />
    </p>
    <div class="sidebarFade">
        <div class="menu-tr">
            <div class="menu-tl">
                <div class="sidebarInner">

                    <h3><fmt:message key="comments.sidebarTitle" /></h3>
                    <hr size="1" noshade="noshade" />

                    <p><fmt:message key="comments.sidebarDescription" /></p>

                    <div class="sideformrow">
                        <label for="searchText" class="sideformrow"><fmt:message key="comments.searchString" />:</label>
                        <input id="searchText" type="text" ng-model="ctrl.searchParams.searchText" size="30"/>
                    </div>
                    <br />
                    <br />

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
                            <fmt:message key="comments.pendingStatus" />:
                        </label>
                        <div>
                            <select id="status" ng-model="ctrl.searchParams.status" size="1" required>
                                <option ng-repeat="(key, value) in ctrl.lookupFields" value="{{key}}">{{value}}</option>
                            </select>
                        </div>
                    </div>
                    <br><br>
                    <input ng-click="ctrl.loadComments()" type="button" value="<fmt:message key='entries.button.query'/>" />
                    <br>
                </div>
            </div>
        </div>
    </div>

<div ng-if="commentArr.length == 0">
    <fmt:message key="comments.noCommentsFound" />
</div>

<div ng-if="commentArr.length > 0">

    <%-- ============================================================= --%>
    <%-- Number of comments and date message --%>
    <%-- ============================================================= --%>

        <div class="tablenav">

        <div style="float:left" ng-bind-html="ctrl.nowShowingMsg"></div>

        <span ng-if="commentArr.length > 0">
            <div style="float:right;">
                {{commentArr[0].postTime | date:'short'}}
                ---
                {{commentArr[commentArr.length - 1].postTime | date:'short'}}
            </div>
        </span>
        <br><br>


        <%-- ============================================================= --%>
        <%-- Next / previous links --%>
        <%-- ============================================================= --%>

        <span ng-if="ctrl.pageNum > 0 || ctrl.commentData.hasMore" ng-cloak>
            <center>
                &laquo;
                <input type="button" value="<fmt:message key='weblogEntryQuery.prev'/>"
                    ng-disabled="ctrl.pageNum <= 0" ng-click="ctrl.previousPage()">
                |
                <input type="button" value="<fmt:message key='weblogEntryQuery.next'/>"
                    ng-disabled="!ctrl.commentData.hasMore" ng-click="ctrl.nextPage()">
                &raquo;
            </center>
        </span>

        </div>


        <table class="table table-sm table-bordered table-hover" width="100%">

        <%-- ======================================================== --%>
        <%-- Comment table header --%>

        <thead class="thead-light">
            <tr>
                <th width="8%"><fmt:message key="comments.showhide" /></th>
                <th width="8%" ><fmt:message key="generic.delete" /></th>
                <th><fmt:message key="comments.columnComment" /> -
                    <span class="pendingCommentBox">&nbsp;&nbsp;&nbsp;&nbsp;</span>
                    <fmt:message key="comments.pending" />&nbsp;&nbsp;
                    <span class="spamCommentBox">&nbsp;&nbsp;&nbsp;&nbsp;</span>
                    <fmt:message key="comments.spam" />&nbsp;&nbsp;
                </th>
            </tr>
        </thead>

        <%-- ========================================================= --%>
        <%-- Loop through comments --%>
        <%-- ========================================================= --%>

        <tbody>
            <tr ng-repeat="comment in commentArr">
                <td>
                    <input ng-if="comment.status == 'SPAM' || comment.status == 'DISAPPROVED' || comment.status == 'PENDING'"
                        type="button" value="<fmt:message key='comments.approve'/>" ng-click="ctrl.approveComment(comment)"/>
                    <input ng-if="comment.status == 'APPROVED'"
                        type="button" value="<fmt:message key='comments.hide'/>" ng-click="ctrl.hideComment(comment)"/>
                </td>
                <td>
                    <input type="button" value="<fmt:message key='generic.delete'/>" ng-click="ctrl.deleteComment(comment)"/>
                </td>

                <td ng-class="{SPAM : 'spamcomment', PENDING : 'pendingcomment', DISAPPROVED : 'pendingcomment'}[comment.status]">

                    <%-- comment details table in table --%>
                    <table class="innertable" >
                        <tr>
                            <td class="viewbody">
                            <div class="viewdetails bot">
                                <div class="details">
                                    <fmt:message key="comments.entryTitled" />:&nbsp;
                                    <a ng-href='{{comment.weblogEntry.permalink}}' target="_blank">{{comment.weblogEntry.title}}</a>
                                </div>
                                <div class="details">
                                    <fmt:message key="comments.commentBy" />:&nbsp;
                                    <span ng-bind-html="ctrl.getCommentHeader(comment)"></span>
                                </div>
                                <span ng-if="comment.url">
                                    <div class="details">
                                        <fmt:message key="comments.commentByURL" />:&nbsp;
                                        <a ng-href='{{comment.url}}'>
                                        {{comment.url | limitTo:60}}{{comment.url.length > 60 ? '...' : ''}}
                                        </a>
                                    </div>
                                </span>
                                <div class="details">
                                    <fmt:message key="comments.postTime" />: {{comment.postTime | date:'short'}}
                                </div>
                            </div>
                            <div class="viewdetails bot">
                                 <div class="details bot">
                                      <textarea style='width:100%' rows='10' ng-model="comment.content" ng-readonly="!comment.editable"></textarea>
                                 </div>
                                 <div class="details" ng-show="!comment.editable">
                                      <a ng-click="ctrl.editComment(comment)"><fmt:message key="generic.edit"/></a>
                                 </div>
                                 <div class="details" ng-show="comment.editable">
                                      <a ng-click="ctrl.saveComment(comment)"><fmt:message key="generic.save"/></a> &nbsp;|&nbsp;
                                      <a ng-click="ctrl.editCommentCancel(comment)"><fmt:message key="generic.cancel"/></a>
                                 </div>
                            </div>
                        </tr>
                    </table>
                </td>
            </tr>
        </tbody>
    </table>
    <br>
</div>
