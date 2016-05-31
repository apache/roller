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
<script src="//ajax.googleapis.com/ajax/libs/angularjs/1.5.5/angular.min.js"></script>

<div class="sidebarFade">
    <div class="menu-tr">
        <div class="menu-tl">
            
            <div class="sidebarInner">
                <h3><s:text name="templates.addNewPage" /></h3>
                <hr size="1" noshade="noshade" />
                
                <s:form action="templates!add">
                    <sec:csrfInput/>
					<s:hidden name="weblog" />
                    
                    <table cellpadding="0" cellspacing="6" ng-app="roleDescriptionModule"
                        ng-controller="roleDescriptionController">
                        <tr>
                            <td><s:text name="generic.name"/></td>
                            <td><s:textfield name="newTmplName" /></td>
                        </tr>
                        
                        <tr>
                            <td><s:text name="templates.role"/></td>
                            <td>
                                <s:select ng-model="selectedRole"
                                ng-init="changedValue(selectedRole)" ng-change="changedValue(selectedRole)"
                                  name="newTmplAction" size="1" list="availableRoles"
                                listKey="left" listValue="right"/>
                            </td>
                        </tr>

                        <tr>
                            <td colspan="2" class="field">
                                <p>{{ description }}</p>
                            </td>
                        </tr>

                        <tr>
                            <td></td>
                            <td><s:submit value="%{getText('templates.add')}" /></td>
                        </tr>

                    </table>
                    
                </s:form>
                <br />
                
            </div>
        </div>
    </div>
</div>	

<script>
    document.forms[0].elements[0].focus();

    angular.module('roleDescriptionModule', [])
        .controller('roleDescriptionController', ['$scope', function($scope) {
           $scope.selectedRole = 'STYLESHEET';

           $scope.changedValue=function(item){
              $.ajax({ url: "<s:property value='siteURL' />/tb-ui/authoring/rest/templateDescriptions/" + $scope.selectedRole
                  , async:false,
                  success: function(data) { $scope.description = data; }
              });
           }
    }]);


</script>
