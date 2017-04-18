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

<script src="<c:url value='/tb-ui/scripts/jquery-2.2.3.min.js'/>"></script>
<script src="//ajax.googleapis.com/ajax/libs/angularjs/1.6.1/angular.min.js"></script>

<script>
    var contextPath = "${pageContext.request.contextPath}";
    var actionWeblogId = "<c:out value='${param.weblogId}'/>";
</script>

<script src="<c:url value='/tb-ui/scripts/commonangular.js'/>"></script>
<script src="<c:url value='/tb-ui/scripts/mediafilechooser.js'/>"></script>

<input type="hidden" id="refreshURL" value="<c:url value='/tb-ui/app/authoring/mediaFileChooser'/>?weblogId=<c:out value='${param.weblogId}'/>"/>

<%-- Drop-down box to choose media directory --%>
<select ng-model="ctrl.selectedDirectory" size="1" required>
   <option ng-repeat="item in ctrl.directories" value="{{item.id}}">{{item.name}}</option>
</select>

<input type="button" ng-click="ctrl.loadImages()" style="margin:4px" value='<fmt:message key="generic.view" />'/>

<p class="pagetip">
    <fmt:message key="mediaFileChooser.pageTip" />
</p>

<%-- Media file contents for selected folder --%>
<div width="720px" height="500px">
    <ul>
        <li ng-repeat="item in ctrl.images" class="align-images"
                ng-class="{mediaFileHighlight : hovering}" ng-mouseenter="hovering=true" ng-mouseleave="hovering=false">
            <div class="mediaObject" ng-click="ctrl.chooseFile(item)">
                <img ng-if="item.imageFile" border="0" ng-src='{{item.thumbnailURL}}' />
            </div>

            <div class="mediaObjectInfo">
                <label>{{item.name}}</label>
            </div>
        </li>
    </ul>
</div>
