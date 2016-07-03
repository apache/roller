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
<%@ include file="/WEB-INF/jsps/taglibs-struts2.jsp" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt"  prefix="fmt" %>
<%@ taglib uri="http://sargue.net/jsptags/time" prefix="javatime" %>
<link rel="stylesheet" media="all" href='<s:url value="/tb-ui/jquery-ui-1.11.4/jquery-ui.min.css"/>' />
<script src='<s:url value="/tb-ui/scripts/jquery-2.2.3.min.js" />'></script>
<script src="//ajax.googleapis.com/ajax/libs/angularjs/1.5.5/angular.min.js"></script>
<script src='<s:url value="/tb-ui/jquery-ui-1.11.4/jquery-ui.min.js"/>'></script>
<script>
var contextPath = "${pageContext.request.contextPath}";
var msg= {
    deleteLabel: '<s:text name="generic.delete"/>',
    cancelLabel: '<s:text name="generic.cancel"/>'
};
</script>

<script src="<s:url value='/tb-ui/scripts/commonjquery.js'/>"></script>
<script src="<s:url value='/tb-ui/scripts/templates.js'/>"></script>

<div id="templates-list" ng-app="TemplatesApp" ng-controller="TemplatesController as ctrl">

    <p class="subtitle">
       <s:text name="templates.subtitle" >
           <s:param value="actionWeblog.handle" />
       </s:text>
    </p>
    <p class="pagetip">
       <s:text name="templates.tip" />
    </p>

    <div id="errorMessageDiv" class="errors" style="display:none">
      <b>{{ctrl.errorObj.errorMessage}}</b>
      <ul>
         <li ng-repeat="em in ctrl.errorObj.errors">{{em}}</li>
      </ul>
    </div>

    <div id="successMessageDiv" class="messages" style="display:none">
      <s:if test="weblogId != null">
        <p><s:text name="generic.changes.saved"/></p>
      </s:if>
    </div>

    <input id="refreshURL" type="hidden" value="<s:url action='templates'/>?weblogId=<s:property value='%{#parameters.weblogId}'/>"/>
    <input type="hidden" id="actionWeblogId" value="<s:property value='%{#parameters.weblogId}'/>"/>

    <div>

    <fmt:message key="generic.date.toStringFormat" var="dateFormat"/>
    <table class="rollertable">

      <thead>
        <tr>
          <th width="4%"><input type="checkbox" onclick="toggleFunction(this.checked,'idSelections');"
            title="<s:text name="templates.selectAllLabel"/>"/></th>
          <th width="17%"><s:text name="generic.name"/></th>
          <th width="20%"><s:text name="templates.path"/></th>
          <th width="34%"><s:text name="templates.role"/></th>
          <th width="8%"><s:text name="templates.source"/></th>
          <th width="13%"><s:text name="generic.lastModified"/></th>
          <th width="4%"><s:text name="generic.view"/></th>
        </tr>
      </thead>
      <tbody>
        <tr ng-repeat="tpl in ctrl.weblogTemplateData.templates" ng-class-even="'altrow'">
            <td class="center" style="vertical-align:middle">
              <span ng-if="tpl.derivation != 'Default'">
                  <input type="checkbox" name="idSelections" value="{{tpl.id}}" />
              </span>
            </td>

            <td style="vertical-align:middle">
                <s:url var="edit" action="templateEdit">
                    <s:param name="weblogId" value="%{actionWeblog.id}" />
                </s:url>
                <span ng-if="tpl.derivation != 'Default'">
                    <s:a href="%{edit}&bean.id={{tpl.id}}">{{tpl.name}}</s:a>
                </span>
                <span ng-if="tpl.derivation == 'Default'">
                    <s:a href="%{edit}&bean.name={{tpl.name}}">{{tpl.name}}</s:a>
                </span>
            </td>

            <td style="vertical-align:middle">
                <span ng-if="tpl.role.accessibleViaUrl == true">
                  {{tpl.relativePath}}
                </span>
            </td>

            <td style="vertical-align:middle">
              <span ng-if="tpl.role.singleton == true || tpl.description == null || tpl.description == ''">
                {{tpl.role.readableName}}
              </span>
              <span ng-if="tpl.role.singleton != true && tpl.description != null && tpl.description != ''">
                {{tpl.role.readableName}}: {{tpl.description}}
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

            <td align="center" style="vertical-align:middle">
                <span ng-if="tpl.role.accessibleViaUrl && tpl.relativePath != null && tpl.relativePath != ''">
                    <a target="_blank" href="<s:property value='actionWeblog.absoluteURL'/>page/{{tpl.relativePath}}">
                      <img src='<s:url value="/images/world_go.png"/>' border="0" alt="icon"/>
                    </a>
                </span>
            </td>
        </tr>
      </tbody>
    </table>

    <div class="control">
    	<input id="delete-link" type="button" value="<s:text name='templates.deleteselected'/>" />

      <span style="float:right">
          <s:form>
            <sec:csrfInput/>
            <s:hidden name="weblogId" value="%{actionWeblog.id}" />
            <s:submit id="switch-theme-button" action="themeEdit" namespace="/tb-ui/authoring" value="%{getText('templates.switchTheme')}" />
          </s:form>
      </span>
    </div>

    <form name="myform">
      <table cellpadding="0" cellspacing="6">
          <caption><s:text name="templates.addNewPage" /></caption>
          <tr>
              <td><s:text name="generic.name"/></td>
              <td><input type="text" ng-model="ctrl.newTemplateName" maxlength="40" required/></td>
          </tr>
          <tr>
              <td><s:text name="templates.role"/></td>
              <td>
                  <select ng-model="ctrl.selectedRole" size="1" required>
                    <option ng-repeat="option in ctrl.weblogTemplateData.availableTemplateRoles" value="{{option.name}}">{{option.readableName}}</option>
                  </select>
              </td>
          </tr>
          <tr>
              <td colspan="2" class="field">
                  <p>{{ description }}</p>
              </td>
          </tr>
          <tr>
              <td><input ng-click="myform.$valid && ctrl.addTemplate()" type="button" value="<s:text name='templates.add'/>" required></td>
          </tr>
      </table>
    </form>
  </div>

</div>

<br/>

<div id="confirm-delete" title="<s:text name='generic.confirm'/>" style="display:none">
   <p><span class="ui-icon ui-icon-alert" style="float:left; margin:0 7px 20px 0;"></span><s:text name="templateRemoves.youSure" />
	<br/>
	<br/>
	<span class="warning">
		<s:text name="templateRemoves.youSureWarning" />
	</span>
  </p>
</div>
