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
                 tooltip="%{getText('createWebsite.tip.email')}" onkeyup="formChanged()"
                 name="bean.emailAddress" size="40" maxlength="50"/>

    <s:select label="%{getText('createWebsite.locale')}"
              tooltip="%{getText('createWebsite.tip.locale')}"
              name="bean.locale" size="1" list="localesList" listValue="displayName"/>

    <s:select label="%{getText('createWebsite.timezone')}"
              tooltip="%{getText('createWebsite.tip.timezone')}"
              name="bean.timeZone" size="1" list="timeZonesList"/>

    <div class="form-group" ng-app="themeSelectModule" ng-controller="themeController">
        <label class="col-sm-3 control-label" for="createWeblog_bean_timeZone">
            <s:text name="createWebsite.theme" />
        </label>
        <div class="col-sm-9 controls">
            <s:select name="bean.theme" size="1" list="themes" listKey="id" listValue="name"
                      onchange="previewImage(this[selectedIndex].value)"/>
            <p id="themedescription"></p>
            <p><img id="themeThumbnail" src="" class="img-responsive img-thumbnail" style="max-width: 30em" /></p>

        </div>
    </div>

    <s:submit cssClass="btn btn-default"
              value="%{getText('createWebsite.button.save')}"/>

    <input class="btn" type="button" value="<s:text name="generic.cancel"/>"
           onclick="window.location='<s:url action="menu"/>'"/>

</s:form>

<%-- ============================================================================== --%>

<script>

    document.forms[0].elements[0].focus();

    var saveButton;

    $( document ).ready(function() {

        saveButton = $("#createWeblog_0");

        <s:if test="bean.theme == null">
        previewImage('<s:property value="themes[0].id"/>');
        </s:if>
        <s:else>
        previewImage('<s:property value="bean.theme"/>');
        </s:else>

        formChanged()
    });

    function formChanged() {
        var valid = false;

        var name   = $("#createWeblog_bean_name:first").val();
        var handle = $("#createWeblog_bean_handle:first").val();
        var email  = $("#createWeblog_bean_emailAddress:first").val();

        valid = !!(name && name.trim().length > 0
            && handle && handle.trim().length > 0
            && email && email.trim().length > 0 && validateEmail(email));

        if ( valid ) {
            saveButton.attr("disabled", false);
        } else {
            saveButton.attr("disabled", true);
        }
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
    }

    function previewImage(themeId) {
        $.ajax({ url: "<s:property value='siteURL' />/roller-ui/authoring/themedata",
            data: {theme:themeId}, success: function(data) {
                $('#themedescription').html(data.description);
                $('#themeThumbnail').attr('src','<s:property value="siteURL" />' + data.previewPath);
            }
        });
        formChanged();
    }

</script>

