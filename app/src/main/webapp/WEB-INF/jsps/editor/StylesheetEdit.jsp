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
<link rel="stylesheet" media="all" href='<s:url value="/roller-ui/jquery-ui-1.11.0/jquery-ui.min.css"/>' />

<p class="subtitle"><s:text name="stylesheetEdit.subtitle" /></p>

<s:if test="template == null">
    <div class="notification">
        <s:if test="sharedStylesheetDeleted">
            <%-- clicking the stylesheet tab will recreate your custom override stylesheet ... --%>
            <s:text name="stylesheetEdit.canRecreateStylesheetOverride" />
        </s:if>
        <s:else>
            <%-- shared theme does not offer a stylesheet ... --%>
            <s:text name="stylesheetEdit.noOverridableStylesheetAvailable" />
        </s:else>
    </div>
</s:if>
<s:else>
    <p class="pagetip">
        <s:text name="stylesheetEdit.tip" />
        <s:if test="!customTheme">
            <s:text name="stylesheetEdit.revertTip" />
            <s:if test="sharedThemeCustomStylesheet">
                <br /><br /><s:text name="stylesheetEdit.revertTip1" />
            </s:if>
        </s:if>
    </p>

    <s:form action="stylesheetEdit!save">
        <s:hidden name="salt" />
        <s:hidden name="weblog" />

        <%-- ================================================================== --%>
        <%-- Tabs for each of the two content areas: Standard and Mobile --%>

        <div id="template-code-tabs">
        <ul>
            <li class="selected"><a href="#tabStandard"><em>Standard</em></a></li>
            <s:if test="contentsMobile != null">
                <li><a href="#tabMobile"><em>Mobile</em></a></li>
            </s:if>
        </ul>
        <div>
            <div id="tabStandard">
                <s:textarea name="contentsStandard" cols="80" rows="30" cssStyle="width:100%" />
            </div>
            <s:if test="contentsMobile != null">
                <div id="tabMobile">
                    <s:textarea name="contentsMobile" cols="80" rows="30" cssStyle="width:100%" />
                </div>
            </s:if>
        </div>
        </div>

        <%-- ================================================================== --%>
        <%-- Save, Close and Resize text area buttons--%>

        <table style="width:100%">
            <tr>
                <td>
                    <s:submit value="%{getText('generic.save')}" />&nbsp;&nbsp;
                    <s:if test="!customTheme">
                        <s:submit value="%{getText('stylesheetEdit.revert')}" onclick="revertStylesheet();return false;" />
                    </s:if>
                    <%-- Only delete if we have no custom templates ie website.customStylesheetPath=null --%>
                    <s:if test="sharedThemeCustomStylesheet">
                        <s:submit value="%{getText('stylesheetEdit.delete')}" onclick="deleteStylesheet();return false;" />
                    </s:if>
                </td>
            </tr>
        </table>

    </s:form>

    <script src="<s:url value='/roller-ui/scripts/jquery-2.1.1.min.js'></s:url>"></script>
    <script src="<s:url value='/roller-ui/jquery-ui-1.11.0/jquery-ui.min.js'></s:url>"></script>

    <script>
        function revertStylesheet() {
            if (window.confirm('<s:text name="stylesheetEdit.confirmRevert"/>')) {
                document.stylesheetEdit.action = "<s:url action='stylesheetEdit!revert' />";
                document.stylesheetEdit.submit();
            }
        };
        <s:if test="%{sharedThemeCustomStylesheet}">
            function deleteStylesheet() {
                if (window.confirm('<s:text name="stylesheetEdit.confirmDelete"/>')) {
                    document.stylesheetEdit.action = "<s:url action='stylesheetEdit!delete' />";
                    document.stylesheetEdit.submit();
                }
            };
        </s:if>
        $(function() {
            $( "#template-code-tabs" ).tabs();
        });
    </script>
</s:else>
