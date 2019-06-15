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

    <s:textfield name="bean.name" maxlength="255" label="%{getText('generic.name')}"/>

    <s:textarea name="bean.description"  rows="3" label="%{getText('generic.description')}"/>

    <s:textarea name="bean.copyrightText" rows="3" label="%{getText('mediaFileAdd.copyright')}"/>

    <s:textfield name="bean.tagsAsString" maxlength="255" label="%{getText('mediaFileAdd.tags')}"/>

    <s:select name="bean.directoryId" list="allDirectories"
              listKey="id" listValue="name" label="%{getText('mediaFileAdd.directory')}"/>

    <s:checkbox name="bean.sharedForGallery"
                label="%{getText('mediaFileAdd.includeGallery')}"
                tooltip="%{getText('mediaFileEdit.includeGalleryHelp')}"/>

    <div class="panel panel-default">
        <div class="panel-heading">
            <h4 class="panel-title">
                <s:text name="mediaFileAdd.fileLocation"/>
            </h4>
        </div>
        <div class="panel-body">
            <s:file id="fileControl0" name="uploadedFiles" size="30" />
            <s:file id="fileControl1" name="uploadedFiles" size="30" />
            <s:file id="fileControl2" name="uploadedFiles" size="30" />
            <s:file id="fileControl3" name="uploadedFiles" size="30" />
            <s:file id="fileControl4" name="uploadedFiles" size="30" />
        </div>
    </div>

    <s:submit id="uploadButton" cssClass="btn btn-default"
              value="%{getText('mediaFileAdd.upload')}" action="mediaFileAdd!save"/>
    <s:submit cssClass="btn" value="%{getText('generic.cancel')}" action="mediaFileAdd!cancel"/>

</s:form>


<%-- ================================================================== --%>

<script>

    $(document).ready(function () {

        $("input[type='file']").change(function () {

            var name = '';
            var count = 0;
            var fileControls = $("input[type='file']");

            for (var i = 0; i < fileControls.length; i++) {
                if (jQuery.trim(fileControls.get(i).value).length > 0) {
                    count++;
                    name = fileControls.get(i).value;
                }
            }

            var entryBean = $("#entry_bean_name");
            if (count === 1) {
                entryBean.get(0).disabled = false;
                entryBean.get(0).value = getFileName(name);

            } else if (count > 1) {
                entryBean.css("font-style", "italic");
                entryBean.css("color", "grey");
                entryBean.get(0).value = "<s:text name="mediaFileAdd.multipleNames"  />";
                entryBean.get(0).disabled = true;
            }

            if (count > 0) {
                $("#uploadButton:first").attr("disabled", false)
            }
        });

        $("#uploadButton:first").attr("disabled", true)
    });

    function getFileName(fullName) {
        var backslashIndex = fullName.lastIndexOf('/');
        var fwdslashIndex = fullName.lastIndexOf('\\');
        var fileName;
        if (backslashIndex >= 0) {
            fileName = fullName.substring(backslashIndex + 1);
        } else if (fwdslashIndex >= 0) {
            fileName = fullName.substring(fwdslashIndex + 1);
        }
        else {
            fileName = fullName;
        }
        return fileName;
    }

</script>