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


<p class="subtitle"> <s:text name="mediaFileAdd.title"/> </p>
<p class="pagetip"> <s:text name="mediaFileAdd.pageTip"/> </p>

<s:form id="entry" action="mediaFileAdd!save"
        method="POST" enctype="multipart/form-data" theme="bootstrap" cssClass="form-horizontal">
    <s:hidden name="salt"/>
    <s:hidden name="weblog"/>
    <s:hidden name="directoryName"/>

    <s:textarea name="bean.description"  rows="3" label="%{getText('generic.description')}"/>

    <s:textarea name="bean.copyrightText" rows="3" label="%{getText('mediaFileAdd.copyright')}"/>

    <s:textfield name="bean.tagsAsString" maxlength="255" label="%{getText('mediaFileAdd.tags')}"/>

    <s:select name="bean.directoryId" list="allDirectories"
              listKey="id" listValue="name" label="%{getText('mediaFileAdd.directory')}"/>

    <s:checkbox name="bean.sharedForGallery"
                label="%{getText('mediaFileAdd.includeGallery')}"
                tooltip="%{getText('mediaFileEdit.includeGalleryHelp')}"/>

    <div class="card">
        <div class="card-header">
            <h4 class="card-title">
                <s:text name="mediaFileAdd.fileLocation"/>
            </h4>
        </div>
        <div class="card-body">
            <s:file id="fileControl0" name="uploadedFiles" size="30" cssClass="form-control mb-2" />
            <s:file id="fileControl1" name="uploadedFiles" size="30" cssClass="form-control mb-2" />
            <s:file id="fileControl2" name="uploadedFiles" size="30" cssClass="form-control mb-2" />
            <s:file id="fileControl3" name="uploadedFiles" size="30" cssClass="form-control mb-2" />
            <s:file id="fileControl4" name="uploadedFiles" size="30" cssClass="form-control mb-2" />
        </div>
    </div>

    <div class="row mb-3">
        <div class="col-sm-9 offset-sm-3">
            <s:submit id="uploadButton" cssClass="btn btn-primary" theme="simple"
                      value="%{getText('mediaFileAdd.upload')}" action="mediaFileAdd!save"/>
            <s:url var="mediaFileViewURL" escapeAmp="false" action="mediaFileView">
                <s:param name="weblog" value="%{actionWeblog.handle}"/>
                <s:param name="directoryName" value="%{directoryName}"/>
            </s:url>
            <a class="btn btn-outline-secondary" href="<s:property value='%{mediaFileViewURL}'/>">
                <s:text name="generic.cancel"/></a>
        </div>
    </div>

</s:form>


<%-- ================================================================== --%>

<script>

    // Upload is enabled once a file is chosen. Each file keeps its own name.
    $(document).ready(function () {
        var uploadButton = $("#uploadButton");
        uploadButton.attr("disabled", true);
        $("input[type='file']").change(function () {
            var chosen = $("input[type='file']").filter(function () {
                return this.value.length > 0;
            }).length;
            uploadButton.attr("disabled", chosen === 0);
        });
    });

</script>
