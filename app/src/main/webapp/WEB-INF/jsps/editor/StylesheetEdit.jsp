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

<p class="subtitle"><s:text name="stylesheetEdit.subtitle" /></p>


<s:if test="template != null">

    <s:text name="stylesheetEdit.youCanCustomize" />

    <s:form action="stylesheetEdit!save" theme="bootstrap" cssClass="form-vertical">
        <s:hidden name="salt" />
        <s:hidden name="weblog" />

        <%-- Tabs for each of the two content areas: Standard and Mobile --%>
        <ul id="template-code-tabs" class="nav nav-tabs" role="tablist" style="margin-bottom: 1em">

            <li role="presentation" class="active">
                <a href="#tabStandard" aria-controls="home" role="tab" data-toggle="tab">
                    <em><s:text name="stylesheetEdit.standard"/></em>
                </a>
            </li>

            <s:if test="contentsMobile != null">
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
                <s:textarea name="contentsStandard" cols="80" rows="30" cssStyle="width:100%" />
            </div>

            <s:if test="contentsMobile != null">
                <div role="tabpanel" class="tab-pane" id="tabMobile">
                    <s:textarea name="contentsMobile" cols="80" rows="30" cssStyle="width:100%" />
                </div>
            </s:if>

        </div>

        <%-- Save, Close and Resize text area buttons--%>
        <s:submit value="%{getText('generic.save')}" cssClass="btn btn-success" />

        <s:if test="!customTheme">
            <s:submit value="%{getText('stylesheetEdit.revert')}" cssClass="btn"
                onclick="revertStylesheet();return false;"
                      tooltip="%{getText('stylesheetEdit.revertTip')}" />
        </s:if>

        <%-- Only delete if we have no custom templates ie website.customStylesheetPath=null --%>
        <s:if test="sharedThemeStylesheet">
            <s:submit value="%{getText('stylesheetEdit.delete')}"  cssClass="btn btn-danger"
                onclick="deleteStylesheet();return false;"
                      tooltip="%{getText('stylesheetEdit.deleteTip')}" />
        </s:if>

    </s:form>

</s:if>
<s:elseif test="sharedTheme">

    <s:if test="sharedThemeStylesheet">

        <s:text name="stylesheetEdit.sharedThemeWithStylesheet" />

        <s:form action="stylesheetEdit!copyStylesheet" theme="bootstrap" cssClass="form-vertical">
            <s:hidden name="salt" />
            <s:hidden name="weblog" />
            <s:submit value="%{getText('stylesheetEdit.copyStylesheet')}" cssClass="btn btn-success"
                tooltip="%{getText('stylesheetEdit.createStylesheetTip')}" />
        </s:form>

    </s:if>
    <s:else>
        <p><s:text name="stylesheetEdit.sharedThemeNoStylesheetSupport" /></p>
    </s:else>

</s:elseif>
<s:else>
    <s:text name="stylesheetEdit.customThemeNoStylesheet" />
</s:else>


<script type="text/javascript">

    function revertStylesheet() {
        if (window.confirm('<s:text name="stylesheetEdit.confirmRevert"/>')) {
            document.stylesheetEdit.action = "<s:url action='stylesheetEdit!revert' />";
            document.stylesheetEdit.submit();
        }
    };
    <s:if test="%{sharedThemeStylesheet}">
        function deleteStylesheet() {
            if (window.confirm('<s:text name="stylesheetEdit.confirmDelete"/>')) {
                document.stylesheetEdit.action = "<s:url action='stylesheetEdit!delete' />";
                document.stylesheetEdit.submit();
            }
        };
    </s:if>

</script>

