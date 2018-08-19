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

<p class="subtitle"><s:text name="createWebsite.prompt"/></p>

<br/>

<s:form action="createWeblog!save" theme="bootstrap" cssClass="form-horizontal">

    <s:hidden name="salt"/>

    <s:textfield label="%{getText('generic.name')}"
                 tooltip="%{getText('createWebsite.tip.name')}" onkeyup="formChanged()"
                 name="bean.name" size="30" maxlength="30"/>

    <s:textfield label="%{getText('createWebsite.handle')}"
                 tooltip="%{getText('createWebsite.tip.handle')}"
                 name="bean.handle" size="30" maxlength="30"
                 onkeyup="handlePreview(this)"/>

    <div class="form-group">
        <label class="col-sm-3"></label>
        <div class="col-sm-9 controls">
            <s:text name="createWebsite.weblogUrl" />:&nbsp;
            <s:property value="absoluteSiteURL" />/<span id="handlePreview" style="color:red">
            <s:if test="bean.handle != null">
                <s:property value="bean.handle"/>
            </s:if>
            <s:else>handle</s:else></span>
            <br>
        </div>
    </div>

    <s:textfield label="%{getText('createWebsite.emailAddress')}"
                 tooltip="%{getText('createWebsite.tip.emailAddress')}" onkeyup="formChanged()"
                 name="bean.emailAddress" size="40" maxlength="50"/>

    <s:select label="%{getText('createWebsite.locale')}"
              tooltip="%{getText('createWebsite.tip.locale')}"
              name="bean.locale" size="1" list="localesList" listValue="displayName"/>

    <s:select label="%{getText('createWebsite.timeZone')}"
              tooltip="%{getText('createWebsite.tip.timeZone')}"
              name="bean.timeZone" size="1" list="timeZonesList"/>

    <div class="form-group" ng-app="themeSelectModule" ng-controller="themeController">
        <label class="col-sm-3 control-label" for="createWeblog_bean_timeZone">Timezone</label>
        <div class="col-sm-9 controls">
            <select id="themeSelector" name="bean.theme" size="1"
                    ng-model="selectedTheme"
                    ng-options="theme as theme.name for theme in themes track by theme.id">
            </select>
            <br>
            <br>
            <p>{{ selectedTheme.description }}</p>
            <img src="<s:property value='siteURL'/>{{ selectedTheme.previewPath }}"/>
        </div>
    </div>

    <s:submit cssClass="btn btn-default"
              value="%{getText('createWebsite.button.save')}"/>

    <input class="btn" type="button" value="<s:text name="generic.cancel"/>"
           onclick="window.location='<s:url action="menu"/>'"/>

</s:form>

<script>

    document.forms[0].elements[0].focus();

    angular.module('themeSelectModule', [])
        .controller('themeController', ['$scope', function ($scope) {
            $.ajax({
                url: "<s:property value='siteURL' />/roller-ui/authoring/themedata", async: false,
                success: function (data) {
                    $scope.themes = data;
                }
            });
            $scope.selectedTheme = $scope.themes[0];
        }]);


    var saveButton;

    $( document ).ready(function() {
        saveButton = $("#createWeblog_0");
        formChanged();
    });

    function formChanged() {
        var valid = false;

        var name   = $("#createWeblog_bean_name:first").val();
        var handle = $("#createWeblog_bean_handle:first").val();
        var email  = $("#createWeblog_bean_emailAddress:first").val();

        if (    name      && name.trim().length > 0
                && handle && handle.trim().length > 0
                && email  && email.trim().length > 0   && validateEmail(email) ) {
            valid = true;

        } else {
            valid = false;
        }

        if ( valid ) {
            saveButton.attr("disabled", false);
            saveButton.removeClass("btn-danger");
        } else {
            saveButton.attr("disabled", true);
            saveButton.addClass("btn-danger");
        }

    }

    function validateEmail(email) {
        var re = /^(([^<>()\[\]\\.,;:\s@"]+(\.[^<>()\[\]\\.,;:\s@"]+)*)|(".+"))@((\[[0-9]{1,3}\.[0-9]{1,3}\.[0-9]{1,3}\.[0-9]{1,3}])|(([a-zA-Z\-0-9]+\.)+[a-zA-Z]{2,}))$/;
        return re.test(email);
    }

    function handlePreview(handle) {
        previewSpan = document.getElementById("handlePreview");
        var n1 = previewSpan.childNodes[0];
        var n2 = document.createTextNode(handle.value);
        if (handle.value == null) {
            previewSpan.appendChild(n2);
        } else {
            previewSpan.replaceChild(n2, n1);
        }
        formChanged();
    }

    formChanged();

</script>


</script>

