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

                <br />
                <b><s:text name="mediaFileSidebar.actions" /></b>
                <br />
                <br />

                <img src='<s:url value="/images/image_add.png"/>' border="0"alt="icon" />
                <s:url var="mediaFileAddURL" action="mediaFileAdd">
                    <s:param name="weblog" value="%{actionWeblog.handle}" />
                    <s:param name="directoryName" value="%{directoryName}" />
                </s:url>
                <a href='<s:property escape="false" value="%{mediaFileAddURL}" />'
                    <s:if test="actionName.equals('mediaFileAdd')">style='font-weight:bold;'</s:if> >
                    <s:text name="mediaFileSidebar.add" />
                </a>

              <s:if test="!pager">

                <%-- Only show Create New Directory control when NOT showing search results --%>
                <br /><br />
                <div>
                    <img src='<s:url value="/images/folder_add.png"/>' border="0"alt="icon" />
                    <s:text name="mediaFileView.addDirectory" /><br />
                    <div style="padding-left:2em; padding-top:1em">
                        <s:text name="mediaFileView.directoryName" />
                        <input type="text" id="newDirectoryName" name="newDirectoryName" size="10" maxlength="25" />
                        <input type="button" id="newDirectoryButton"
                            value='<s:text name="mediaFileView.create" />' onclick="onCreateDirectory()" />
                    </div>
                </div>
              </s:if>

                <br />
                <hr size="1" noshade="noshade" />
                <br />

                <b><s:text name="mediaFileView.search" /></b>
                <br />
                <br />

                <s:form id="mediaFileSearchForm" name="mediaFileSearchForm"
                        action="mediaFileView!search">
					<s:hidden name="salt" />
                    <s:hidden name="weblog" />

                    <input type="hidden" name="mediaFileId" value="" />
                    <table class="mediaFileSearchTable" cellpadding="0" cellspacing="3" width="100%">

                        <tr>
                            <td>
                                <label for="name"><s:text name="generic.name" /></label>
                            </td>
                            <td>
                                <s:textfield id="beanName" name="bean.name" size="20" maxlength="255" />
                            </td>
                        </tr>

                        <tr>
                            <td>
                                <label for="type"><s:text name="mediaFileView.type" /></label>
                            </td>
                            <td>
                                <s:select id="beanType" name="bean.type"
                                    list="fileTypes" listKey="key" listValue="value" />
                            </td>
                        </tr>

                        <tr>
                            <td>
                                <label for="size"><s:text name="mediaFileView.size" /></label>
                            </td>
                            <td width="80%">
                                <s:select name="bean.sizeFilterType" id="sizeFilterTypeCombo"
                                    list="sizeFilterTypes" listKey="key" listValue="value" />
                                <s:textfield id="beanSize" name="bean.size"
                                    size="3" maxlength="10" />
                                <s:select name="bean.sizeUnit"
                                    list="sizeUnits" listKey="key" listValue="value" />
                            </td>
                        </tr>

                        <tr>
                            <td width="10%">
                                <label for="tags"><s:text name="mediaFileView.tags" /></label>
                            </td>
                            <td>
                                <s:textfield id="beanTags" name="bean.tags"
                                    size="20" maxlength="50" />
                            </td>
                        </tr>

                        <tr>
                            <td>
                                <s:submit id="searchButton" value="%{getText('mediaFileView.search')}" cssStyle="margin:5px 0px;"/>
                            </td>
                            <td>
                                <s:if test="pager">
                                    <input id="resetButton" style="margin:5px 0px;" type="button"
                                           name="reset" value='<s:text name="mediaFileView.reset" />' />
                                </s:if>
                                &nbsp;
                            </td>
                            <td>&nbsp;</td>
                            <td>&nbsp;</td>
                        </tr>
                    </table>

                </s:form>

            </div>
        </div>
    </div>
</div>



<script>

function onCreateDirectory() {
    document.mediaFileViewForm.newDirectoryName.value = $("#newDirectoryName").get(0).value;
    document.mediaFileViewForm.action='<s:url action="mediaFileView!createNewDirectory" />';
    document.mediaFileViewForm.submit();
}

$("#newDirectoryButton").ready(function () {
    $("#newDirectoryName").bind("keyup", maintainDirectoryButtonState);
    $("#newDirectoryButton").attr("disabled", true);
});

function maintainDirectoryButtonState(e) {
    if ( jQuery.trim($("#newDirectoryName").get(0).value).length == 0) {
        $("#newDirectoryButton").attr("disabled", true);
    } else {
        $("#newDirectoryButton").attr("disabled", false);
    }
}

$("#searchButton").ready(function () {

    maintainSearchButtonState();
    $("input").bind("keyup", maintainSearchButtonState);
    $("select").bind("change", maintainSearchButtonState);

    $("#resetButton").bind("click", function() {
        <s:url var="mediaFileViewURL" action="mediaFileView">
            <s:param name="weblog" value="%{actionWeblog.handle}" />
        </s:url>
        window.location = '<s:property value="%{mediaFileViewURL}" />';
    });
});

function maintainSearchButtonState(e) {
    if ( jQuery.trim($("#beanName").get(0).value).length == 0
     &&  jQuery.trim($("#beanTags").get(0).value).length == 0
     && (jQuery.trim($("#beanSize").get(0).value).length == 0 || $("#beanSize").get(0).value == 0)
     && ($("#beanType").get(0).value.length == 0 || $("#beanType").get(0).value == "mediaFileView.any")) {
        $("#searchButton").attr("disabled", true);
    } else {
        $("#searchButton").attr("disabled", false);
    }
}

</script>
