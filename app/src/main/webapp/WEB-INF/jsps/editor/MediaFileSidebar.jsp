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

<div class="sidebarFade">
    <div class="menu-tr">
        <div class="menu-tl">
            <div class="sidebarInner">

                <h3><s:text name="mediaFileSidebar.actions"/></h3>

                <div style="clear:right">
                    <span class="glyphicon glyphicon-picture"></span>
                    <s:url var="mediaFileAddURL" action="mediaFileAdd">
                        <s:param name="weblog" value="%{actionWeblog.handle}"/>
                        <s:param name="directoryName" value="%{directoryName}"/>
                    </s:url>
                    <a href='<s:property escapeHtml="false" value="%{mediaFileAddURL}" />'
                            <s:if test="actionName.equals('mediaFileAdd')"> style='font-weight:bold;'</s:if> >
                        <s:text name="mediaFileSidebar.add"/>
                    </a>
                </div>

                <s:if test="!pager">
                    <%-- Only show Create New Directory control when NOT showing search results --%>

                    <div style="clear:right; margin-top: 1em">

                        <span class="glyphicon glyphicon-folder-open"></span>
                        <s:text name="mediaFileView.addDirectory"/> <br />

                        <label for="newDirectoryName">
                            <s:text name="mediaFileView.directoryName"/>
                        </label>
                        <input type="text" id="newDirectoryName" name="newDirectoryName" size="8" maxlength="25"/>

                        <input type="button" id="newDirectoryButton" class="btn btn-primary" style="clear:left"
                               value='<s:text name="mediaFileView.create" />' onclick="onCreateDirectory()"/>

                    </div>
                </s:if>

                <hr size="1" noshade="noshade"/>

                <h3><s:text name="mediaFileView.search"/></h3>

                <s:form id="mediaFileSearchForm" name="mediaFileSearchForm"
                        action="mediaFileView!search" theme="bootstrap" cssClass="form-vertical">
                    <s:hidden name="salt"/>
                    <s:hidden name="weblog"/>
                    <input type="hidden" name="mediaFileId" value=""/>

                    <s:textfield id="beanName" name="bean.name" size="20" maxlength="255"
                                 label="%{getText('generic.name')}"/>

                    <s:select id="beanType" name="bean.type"
                              list="fileTypes" listKey="key" listValue="value"
                              label="%{getText('mediaFileView.type')}"/>

                    <s:select name="bean.sizeFilterType" id="sizeFilterTypeCombo"
                              list="sizeFilterTypes" listKey="key" listValue="value"
                              label="%{getText('mediaFileView.size')}"/>

                    <s:textfield id="beanSize" name="bean.size" size="3" maxlength="10"/>

                    <s:select name="bean.sizeUnit" list="sizeUnits" listKey="key" listValue="value"/>

                    <s:textfield id="beanTags" name="bean.tags" size="20" maxlength="50"
                                 label="%{getText('mediaFileView.tags')}"/>

                    <s:submit id="searchButton" cssClass="btn btn-primary"
                              value="%{getText('mediaFileView.search')}" cssStyle="margin:5px 0;"/>

                    <s:if test="pager">
                        <input id="resetButton" style="margin:5px 0;" type="button" class="btn"
                               name="reset" value='<s:text name="mediaFileView.reset" />'/>
                    </s:if>

                </s:form>

            </div>
        </div>
    </div>
</div>


<script>

    function onCreateDirectory() {
        document.mediaFileViewForm.newDirectoryName.value = $("#newDirectoryName").get(0).value;
        document.mediaFileViewForm.action = '<s:url action="mediaFileView!createNewDirectory" />';
        document.mediaFileViewForm.submit();
    }

    $("#newDirectoryButton").ready(function () {
        $("#newDirectoryName").bind("keyup", maintainDirectoryButtonState);
        $("#newDirectoryButton").attr("disabled", true);
    });

    function maintainDirectoryButtonState(e) {
        if (jQuery.trim($("#newDirectoryName").get(0).value).length === 0) {
            $("#newDirectoryButton").attr("disabled", true);
        } else {
            $("#newDirectoryButton").attr("disabled", false);
        }
    }

    $("#searchButton").ready(function () {

        maintainSearchButtonState();
        $("input").bind("keyup", maintainSearchButtonState);
        $("select").bind("change", maintainSearchButtonState);

        $("#resetButton").bind("click", function () {
            <s:url var="mediaFileViewURL" action="mediaFileView">
            <s:param name="weblog" value="%{actionWeblog.handle}" />
            </s:url>
            window.location = '<s:property value="%{mediaFileViewURL}" />';
        });
    });

    function maintainSearchButtonState(e) {
        var beanSize = $("#beanSize").get(0).value;
        var beanType = $("#beanType").get(0).value;

        if (jQuery.trim($("#beanName").get(0).value).length === 0
            && jQuery.trim($("#beanTags").get(0).value).length === 0
            && (jQuery.trim(beanSize).length === 0 || beanSize === 0)
            && (beanType.length === 0 || beanType === "mediaFileView.any")) {
            $("#searchButton").attr("disabled", true);
        } else {
            $("#searchButton").attr("disabled", false);
        }
    }

</script>
