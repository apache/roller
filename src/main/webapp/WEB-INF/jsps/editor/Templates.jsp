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

<script src='<c:url value="/tb-ui/scripts/jquery-2.2.3.min.js" />'></script>
<script src="//ajax.googleapis.com/ajax/libs/angularjs/1.7.0/angular.min.js"></script>

<script>
    var contextPath = "${pageContext.request.contextPath}";
    var actionWeblogId = "<c:out value='${param.weblogId}'/>";
</script>

<script src="<c:url value='/tb-ui/scripts/commonangular.js'/>"></script>
<script src="<c:url value='/tb-ui/scripts/templates.js'/>"></script>

<div>

    <div id="successMessageDiv" class="alert alert-success" role="alert" ng-show="ctrl.showSuccessMessage" ng-cloak>
        <fmt:message key="generic.changes.saved"/>
        <button type="button" class="close" data-ng-click="ctrl.showSuccessMessage = false" aria-label="Close">
           <span aria-hidden="true">&times;</span>
        </button>
    </div>

    <div id="errorMessageDiv" class="alert alert-danger" role="alert" ng-show="ctrl.errorObj.errorMessage" ng-cloak>
        <b>{{ctrl.errorObj.errorMessage}}</b>
        <button type="button" class="close" data-ng-click="ctrl.errorObj.errorMessage = null" aria-label="Close">
           <span aria-hidden="true">&times;</span>
        </button>
        <ul ng-if="ctrl.errorObj.errors">
           <li ng-repeat="item in ctrl.errorObj.errors">{{item}}</li>
        </ul>
    </div>

    <p class="pagetip">
       <fmt:message key="templates.tip" />
    </p>

    <p>
        <fmt:message key="themeEdit.yourCurrentTheme" />:
        <b><c:out value="${actionWeblog.theme}"/></b>
    </p>

    <input id="refreshURL" type="hidden" value="<c:url value='/tb-ui/app/authoring/templates'/>?weblogId=<c:out value='${param.weblogId}'/>"/>

    <div>

    <fmt:message key="generic.date.toStringFormat" var="dateFormat"/>

    <table class="table table-sm table-bordered table-striped">
        <thead class="thead-light">
        <tr>
          <th width="4%"><input type="checkbox" ng-model="ctrl.checkAll"
                ng-change="ctrl.toggleCheckboxes(ctrl.checkAll)"
            title="<fmt:message key='templates.selectAllLabel'/>"/></th>
          <th width="17%"><fmt:message key="generic.name"/></th>
          <th width="16%"><fmt:message key="templates.role"/></th>
          <th width="38%"><fmt:message key="templates.description"/></th>
          <th width="8%"><fmt:message key="templates.source"/></th>
          <th width="13%"><fmt:message key="generic.lastModified"/></th>
          <th width="4%"><fmt:message key="generic.view"/></th>
        </tr>
      </thead>
      <tbody ng-cloak>
        <tr ng-repeat="tpl in ctrl.weblogTemplateData.templates">
            <td class="center" style="vertical-align:middle">
              <span ng-if="tpl.derivation != 'Default'">
                  <input type="checkbox" name="idSelections" ng-model="tpl.selected" value="{{tpl.id}}" />
              </span>
            </td>

            <td style="vertical-align:middle">
                <c:url var="edit" value="/tb-ui/app/authoring/templateEdit">
                    <c:param name="weblogId" value="${actionWeblog.id}" />
                </c:url>
                <span ng-if="tpl.derivation != 'Default'">
                    <a ng-href="${edit}&templateId={{tpl.id}}">{{tpl.name}}</a>
                </span>
                <span ng-if="tpl.derivation == 'Default'">
                    <a ng-href="${edit}&templateName={{tpl.name}}">{{tpl.name}}</a>
                </span>
            </td>

            <td style="vertical-align:middle">
               {{tpl.role.readableName}}
            </td>

            <td style="vertical-align:middle">
              <span ng-if="tpl.role.singleton != true && tpl.description != null && tpl.description != ''">
                 {{tpl.description}}
              </span>
            </td>

            <td style="vertical-align:middle">
              {{tpl.derivation}}
            </td>

            <td>
              <span ng-if="tpl.lastModified != null">
                {{tpl.lastModified | date:'short' }}
              </span>
            </td>

            <td class="buttontd">
                <span ng-if="tpl.role.accessibleViaUrl">
                    <a target="_blank" href="<c:out value='${actionWeblogURL}'/>page/{{tpl.name}}">
                      <img src='<c:url value="/images/world_go.png"/>' border="0" alt="icon"/>
                    </a>
                </span>
            </td>
        </tr>
      </tbody>
    </table>

    <div class="control">
      <span style="padding-left:7px">
    	<button ng-disabled="!ctrl.templatesSelected()" data-toggle="modal" data-target="#deleteTemplatesModal">
    	    <fmt:message key='generic.deleteSelected'/>
    	</button>
      </span>

      <c:url var="templateEditUrl" value="/tb-ui/app/authoring/themeEdit">
          <c:param name="weblogId" value="${weblogId}" />
      </c:url>

      <span style="float:right">
          <form action="${templateEditUrl}">
            <sec:csrfInput/>
            <input type="hidden" name="weblogId" value="<c:out value='${actionWeblog.id}'/>"/>
            <input type="submit" value="<fmt:message key='templates.switchTheme'/>">
          </form>
      </span>
    </div>

    <form name="myform">
      <table cellpadding="0" cellspacing="6">
          <caption style="text-align:left"><fmt:message key="templates.addNewPage" /></caption>
          <tr>
              <td><fmt:message key="generic.name"/></td>
              <td><input type="text" ng-model="ctrl.newTemplateName" maxlength="40" required/></td>
          </tr>
          <tr>
              <td><fmt:message key="templates.role"/></td>
              <td>
                  <select ng-model="ctrl.selectedRole" size="1" required>
                    <option ng-repeat="(key, value) in ctrl.weblogTemplateData.availableTemplateRoles" value="{{key}}">{{value}}</option>
                  </select>
              </td>
          </tr>
          <tr>
              <td colspan="2" class="field">
                  <p>{{ctrl.weblogTemplateData.templateRoleDescriptions[ctrl.selectedRole]}}</p>
              </td>
          </tr>
          <tr>
              <td><input ng-click="myform.$valid && ctrl.addTemplate()" type="button" value="<fmt:message key='templates.add'/>" required></td>
          </tr>
      </table>
    </form>
  </div>

</div>

<br/>

<!-- Delete templates modal -->
<div class="modal fade" id="deleteTemplatesModal" tabindex="-1" role="dialog" aria-labelledby="deleteTemplatesTitle" aria-hidden="true">
  <div class="modal-dialog modal-dialog-centered" role="document">
    <div class="modal-content">
      <div class="modal-header">
        <h5 class="modal-title" id="deleteTemplatesTitle"><fmt:message key="templates.confirmDelete"/></h5>
      </div>
      <div class="modal-body">
        <span id="deleteTemplatesMsg"><fmt:message key="templates.deleteWarning" /></span>
      </div>
      <div class="modal-footer">
        <button type="button" class="btn btn-secondary" data-dismiss="modal"><fmt:message key='generic.cancel'/></button>
        <button type="button" class="btn btn-danger" ng-click="ctrl.deleteTemplates()"><fmt:message key='generic.delete'/></button>
      </div>
    </div>
  </div>
</div>
