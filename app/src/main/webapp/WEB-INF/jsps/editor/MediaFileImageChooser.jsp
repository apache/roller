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

<style>

</style>

<script>
    function onSelectDirectory(id) {
        window.location = "?directoryId=" + id + "&weblog=" + '<s:property value="actionWeblog.handle" />';
    }
</script>


<%-- ********************************************************************* --%>

<%-- Subtitle and folder path --%>

<s:if test="childFiles || allDirectories">

    <s:form id="mediaFileChooserForm" name="mediaFileChooserForm" action="mediaFileImageChooser"
            theme="bootstrap" cssClass="form-vertical">
        <s:hidden name="salt"/>
        <s:hidden name="weblog"/>
        <input type="hidden" name="mediaFileId" value=""/>

        <p class="pagetip"><s:text name="mediaFileImageChooser.pageTip"/></p>

        <%-- ***************************************************************** --%>
        <%-- Maybe show media directory selector --%>

        <s:if test="!allDirectories.isEmpty">
            <s:select name="directoryId" emptyOption="true" label="%{getText('mediaFileView.viewFolder')}"
                      list="allDirectories" listKey="id" listValue="name" onchange="onView()"/>
        </s:if>

        <%-- ***************************************************************** --%>
        <%-- Media files grid --%>

        <div id="imageGrid" class="panel panel-default">
            <div class="panel-body">

                <ul>

                    <s:if test="childFiles.size() == 0">
                        <p style="text-align: center"><s:text name="mediaFileView.noFiles"/></p>
                    </s:if>

                    <s:if test="childFiles.size() > 0">

                        <s:iterator var="mediaFile" value="childFiles">

                            <s:url var="mediaFileURL" includeContext="false" value="%{#mediaFile.permalink}"/>
                            <s:url var="mediaFileThumbnailURL" value="%{#mediaFile.thumbnailURL}"/>

                            <li class="align-images"
                                onmouseover="highlight(this, true)" onmouseout="highlight(this, false)">

                                <div class="mediaObject"
                                     onclick="onSelectMediaFile('<s:property value="#mediaFile.name"/>',
                                             '<s:property value="%{mediaFileURL}"/>',
                                             '<s:property value="#mediaFile.isImageFile()"/>')">

                                    <s:if test="#mediaFile.imageFile">
                                        <img border="0" src='<s:property value="%{mediaFileThumbnailURL}" />'
                                             width='<s:property value="#mediaFile.thumbnailWidth"/>'
                                             height='<s:property value="#mediaFile.thumbnailHeight"/>'
                                             alt='<s:property value="#mediaFile.name" />'/>
                                    </s:if>

                                    <s:else>
                                        <span class="glyphicon glyphicon-file"></span>
                                    </s:else>

                                </div>

                                <div class="mediaObjectInfo">
                                    <str:truncateNicely upper="60">
                                        <s:property value="#mediaFile.name"/>
                                    </str:truncateNicely>
                                </div>

                            </li>

                        </s:iterator>
                    </s:if>

                </ul>
            </div>
        </div>

        <div style="clear:left;"></div>

    </s:form>

</s:if>


<script>

    function onSelectMediaFile(name, url, isImage) {
        window.parent.onSelectMediaFile(name, url, isImage);
    }

    function highlight(el, flag) {
        if (flag) {
            $(el).addClass("highlight");
        } else {
            $(el).removeClass("highlight");
        }
    }

    function onView() {
        document.mediaFileChooserForm.submit();
    }

</script>
