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

<p class="subtitle">
    <s:text name="pageForm.subtitle">
        <s:param value="bean.name"/>
        <s:param value="actionWeblog.handle"/>
    </s:text>
</p>

<s:if test="template.required">
    <p class="pagetip"><s:text name="pageForm.tip.required"/></p>
</s:if>
<s:else>
    <p class="pagetip"><s:text name="pageForm.tip"/></p>
</s:else>

<s:form action="templateEdit!save" id="template" theme="bootstrap" cssClass="form-vertical">
    <s:hidden name="salt"/>
    <s:hidden name="weblog"/>
    <s:hidden name="bean.id"/>
    <s:hidden name="bean.type"/>

    <%-- ================================================================== --%>
    <%-- Name, link and description: disabled when page is a required page --%>

    <s:if test="template.required || bean.mobile">
        <%-- Cannot edit name of a reqired template --%>
        <s:textfield name="bean.name"
                     label="%{getText('generic.name')}" size="50" readonly="true" cssStyle="background: #e5e5e5"/>
    </s:if>
    <s:else>
        <s:textfield name="bean.name"
                     label="%{getText('generic.name')}" size="50"/>
    </s:else>

    <s:textfield name="bean.action" label="%{getText('pageForm.action')}" size="50"
                 readonly="true" cssStyle="background: #e5e5e5"/>

    <s:if test="!template.required && template.custom">

        <%-- allow setting the path for a custom template --%>
        <s:textfield name="bean.link"
                     label="%{getText('pageForm.link')}" size="50" onkeyup="updatePageURLDisplay()"/>

        <%-- show preview of the full URL that will result from that path --%>

        <div id="no_link" class="alert-danger" style="display: none; margin-top:3em; margin-bottom:2em; padding: 1em">
            <s:text name="pageForm.noUrl"/>
        </div>

        <div id="good_link" class="alert-success"
             style="display: none; margin-top:3em; margin-bottom:2em; padding: 1em">
            <s:text name="pageForm.resultingUrlWillBe"/>
            <s:property value="actionWeblog.absoluteURL"/>page/
            <span id="linkPreview" style="color:red"><s:property value="bean.link"/></span>
            <s:if test="template.link != null">
                [<a id="launchLink" onClick="launchPage()"><s:text name="pageForm.launch"/></a>]
            </s:if>
        </div>

    </s:if>

    <s:if test="template.required">
        <%-- Required templates have a description--%>
        <s:textarea name="bean.description" label="%{getText('generic.description')}"
                    cols="50" rows="2" readonly="true" cssStyle="background: #e5e5e5"/>
    </s:if>
    <s:else>
        <s:textarea name="bean.description" label="%{getText('generic.description')}" cols="50" rows="2"/>
    </s:else>

    <%-- ================================================================== --%>

    <%-- Tabs for each of the two content areas: Standard and Mobile --%>
    <ul id="template-code-tabs" class="nav nav-tabs" role="tablist" style="margin-bottom: 1em">

        <li role="presentation" class="active">
            <a href="#tabStandard" aria-controls="home" role="tab" data-toggle="tab">
                <em><s:text name="stylesheetEdit.standard"/></em>
            </a>
        </li>

        <s:if test="bean.contentsMobile != null">
            <li role="presentation">
                <a href="#tabMobile" aria-controls="home" role="tab" data-toggle="tab">
                    <em><s:text name="stylesheetEdit.mobile"/></em>
                </a>
            </li>
        </s:if>

    </ul>

    <%-- Tab content for each of the two content areas: Standard and Mobile --%>
    <div class="tab-content">

        <div role="tabpanel" class="tab-pane active" id="tabStandard">
            <s:textarea name="bean.contentsStandard" cols="80" rows="30" cssStyle="width:100%"/>
        </div>

        <s:if test="bean.contentsMobile != null">
            <div role="tabpanel" class="tab-pane" id="tabMobile">
                <s:textarea name="bean.contentsMobile" cols="80" rows="30" cssStyle="width:100%"/>
            </div>
        </s:if>

    </div>

    <%-- ================================================================== --%>
    <%-- Save, Close and Resize text area buttons--%>

    <s:submit value="%{getText('generic.save')}" cssClass="btn btn-default"/>
    <input type="button" value='<s:text name="generic.done"/>' class="button btn"
           onclick="window.location='<s:url action="templates"><s:param name="weblog" value="%{weblog}"/></s:url>'"/>

    <%-- ================================================================== --%>
    <%-- Advanced settings inside a control toggle --%>

    <s:if test="template.custom">

        <div class="panel-group" id="accordion" style="margin-top:2em">

        <div class="panel panel-default" id="panel-plugins">

            <div class="panel-heading">

                <h4 class="panel-title">
                    <a class="collapsed" data-toggle="collapse" data-target="#collapseAdvanced" href="#">
                        <s:text name="pageForm.advancedSettings"/>
                    </a>
                </h4>

            </div>

            <div id="collapseAdvanced" class="panel-collapse collapse">
                <div class="panel-body">

                    <s:select name="bean.templateLanguage" list="templateLanguages" size="1"
                              label="%{getText('pageForm.templateLanguage')}"/>

                    <s:checkbox name="bean.hidden"
                                label="%{getText('pageForm.hidden')}" tooltip="%{getText('pageForm.hidden.tip')}"/>

                    <s:checkbox name="bean.navbar"
                                label="%{getText('pageForm.navbar')}" tooltip="%{getText('pageForm.navbar.tip')}"/>

                    <s:checkbox name="bean.autoContentType"
                                label="%{getText('pageForm.useAutoContentType')}"/>

                    <div id="manual-content-type-control-group" style="display:none">
                        <s:textfield name="bean.manualContentType"
                                     label="%{getText('pageForm.useManualContentType')}"/>
                    </div>

                </div>
            </div>
        </div>

    </s:if>

</s:form>


<script type="text/javascript">

    var weblogURL = '<s:property value="actionWeblog.absoluteURL" />';
    var originalLink = '<s:property value="bean.link" />';
    var type = '<s:property value="bean.type" />';

    $(document).ready(function () {

        $("#template-code-tabs").tabs();

        showContentTypeField();
        $("#template_bean_autoContentType").click(function(e) {
            showContentTypeField();
        });
    });

    // Update page URL when user changes link
    function updatePageURLDisplay() {
        var link = $("#template_bean_link").val();
        if (link !== "") {
            $("#no_link").hide();
            $("#good_link").show();
            $("#linkPreview").html(link);
        } else {
            $("#good_link").hide();
            $("#no_link").show();
        }
    }

    // Don't launch page if user has changed link, it'll be a 404
    function launchPage() {
        if (originalLink != document.getElementById('template_bean_link').value) {
            window.alert("Link changed, not launching page");
        } else {
            window.open(weblogURL + 'page/' + originalLink + '?type=' + type, '_blank');
        }
    }

    function showContentTypeField() {
        var checked = $("#template_bean_autoContentType").prop("checked");
        if ( checked ) {
            $("#manual-content-type-control-group").hide();
        } else {
            $("#manual-content-type-control-group").show();
        }
    }

</script>
