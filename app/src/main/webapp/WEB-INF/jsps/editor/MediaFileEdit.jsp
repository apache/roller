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

  Source file modified from the original ASF source; all changes made
  are also under Apache License.
--%>
<%@ include file="/WEB-INF/jsps/taglibs-struts2.jsp" %>
<%@ page import="org.apache.roller.weblogger.config.WebloggerConfig" %>
<script src="<s:url value="/roller-ui/scripts/jquery-2.1.1.min.js" />"></script>

<script>
    $(document).ready(function() {
        $("input[type='file']").change(function() {
            var name = '';
            var fileControls = $("input[type='file']");
            for (var i=0; i<fileControls.size(); i++) {
                if (jQuery.trim(fileControls.get(i).value).length > 0) {
                    name = fileControls.get(i).value;
                }
            }
            $("#entry_bean_name").get(0).disabled = false;
            $("#entry_bean_name").get(0).value = name;
        });
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

<s:if test="actionName == 'mediaFileEdit'">
    <s:set var="subtitleKey">mediaFileEdit.subtitle</s:set>
    <s:set var="mainAction">mediaFileEdit</s:set>
    <s:set var="pageTip">mediaFileEdit.pagetip</s:set>
    <s:if test="bean.isImage">
        <div class="mediaFileThumbnail">
            <a href='<s:property value="bean.permalink" />' target="_blank">
                <img align="right" alt="thumbnail" src='<s:property value="bean.thumbnailURL" />'
                     title='<s:text name="mediaFileEdit.clickToView" />' />
            </a>
        </div>
    </s:if>
</s:if>
<s:else>
    <s:set var="subtitleKey">mediaFileAdd.title</s:set>
    <s:set var="mainAction">mediaFileAdd</s:set>
    <s:set var="pageTip">mediaFileAdd.pageTip</s:set>
</s:else>


<p class="subtitle">
    <s:text name="%{#subtitleKey}">
        <s:param value="bean.name" />
    </s:text>
</p>

<p class="pagetip">
    <s:text name="%{#pageTip}"/>
</p>

<s:form id="entry" action="%{#mainAction}!save" method="POST" enctype="multipart/form-data">
	<s:hidden name="salt" />
    <s:hidden name="weblog" />
    <s:hidden name="directoryName" />
    <s:hidden name="mediaFileId" id="mediaFileId" />
    <s:hidden name="bean.permalink" />

    <%-- ================================================================== --%>
    <%-- Title, category, dates and other metadata --%>

    <table class="entryEditTable" cellpadding="0" cellspacing="0" width="100%">

        <tr>
            <td class="entryEditFormLabel">
                <label for="status"><s:text name="generic.name" /></label>
            </td>
            <td>
                <s:textfield name="bean.name" size="50" maxlength="255" style="width:30%"/>
            </td>
        </tr>

        <tr>
            <td class="entryEditFormLabel">
                <label for="status"><s:text name="mediaFileAdd.altText"/><tags:help key="mediaFileAdd.altText.tooltip"/></label>
            </td>
            <td>
                <s:textfield name="bean.altText" size="50" maxlength="255" style="width:30%"/>
            </td>
        </tr>

        <tr>
            <td class="entryEditFormLabel">
                <label for="status"><s:text name="mediaFileAdd.titleText"/><tags:help key="mediaFileAdd.titleText.tooltip"/></label>
            </td>
            <td>
                <s:textfield name="bean.titleText" size="50" maxlength="255" style="width:30%"/>
            </td>
        </tr>

        <tr>
            <td class="entryEditFormLabel">
                <label for="status"><s:text name="mediaFileAdd.anchor"/><tags:help key="mediaFileAdd.anchor.tooltip"/></label>
            </td>
            <td>
                <s:textfield name="bean.anchor" size="50" maxlength="255" style="width:30%"/>
            </td>
        </tr>

       <tr>
            <td class="entryEditFormLabel">
                <label for="status"><s:text name="generic.notes"/></label>
            </td>
            <td>
                <s:textarea name="bean.notes" cols="50" rows="5" maxlength="255" style="width:30%"/>
            </td>
       </tr>
<s:if test="actionName == 'mediaFileEdit'">
       <tr>
            <td class="entryEditFormLabel">
                <label for="fileInfo"><s:text name="mediaFileEdit.fileInfo" /></label>
            </td>
            <td>
                <s:text name="mediaFileEdit.fileTypeSize">
                    <s:param value="bean.contentType" />
                    <s:param value="bean.length" />
                </s:text>
                <s:if test="bean.isImage">
                    <s:text name="mediaFileEdit.fileDimensions">
                        <s:param value="bean.width" />
                        <s:param value="bean.height" />
                    </s:text>
                </s:if>
            </td>
       </tr>

       <tr>
            <td class="entryEditFormLabel">
                <label for="status"><s:text name="mediaFileEdit.permalink" /></label>
            </td>
            <td>
                <a href='<s:text name="bean.permalink" />' target="_blank"
                   title='<s:text name="mediaFileEdit.linkTitle" />'>
                   <s:url var="linkIconURL" value="/images/link.png"></s:url>
                   <img border="0" src='<s:property value="%{linkIconURL}" />'
                       style="padding:2px 2px;" alt="link" />
                </a>
                <input type="text" id="clip_text" size="50" style="width:90%" value='<s:text name="bean.permalink" />' readonly />
            </td>
       </tr>
</s:if>
       <tr>
            <td class="entryEditFormLabel">
                <label for="directoryId"><s:text name="%{#mainAction}.directory" /></label>
            </td>
            <td>
                <s:select name="bean.directoryId" list="allDirectories"
                    listKey="id" listValue="name" />
            </td>
       </tr>

        <tr>
            <td class="entryEditFormLabel">
                <label for="title"><s:text name="%{#mainAction}.fileLocation" /></label>
            </td>
            <td>
                <div id="fileControldiv" class="miscControl">
                    <s:file id="fileControl" name="uploadedFile" size="30" />
                </div>
            </td>
        </tr>

        <!-- original path from base URL of ctx/resources/ -->
        <s:if test="actionName == 'mediaFileEdit' && getBooleanProp('mediafile.originalPathEdit.enabled')">
        <tr>
            <td class="originalPathLabel">
                <label for="originalPath"><s:text name="mediaFileEdit.originalPath" /></label>
            </td>
            <td>
                <div id="fileControldiv" class="miscControl">
                    <s:textfield name="bean.originalPath" size="30" maxlength="100" tabindex="3" />
                    <br />
                </div>
            </td>
        </tr>
        </s:if>

    </table>

    <br />
    <div class="control">
        <s:if test="actionName == 'mediaFileEdit'">
           <input type="submit" value="<s:text name="generic.save" />" name="submit" />
           <input type="button" value="<s:text name="generic.cancel" />" onClick="javascript:window.parent.onEditCancelled();" />
        </s:if>
        <s:else>
           <s:submit value="%{getText('mediaFileAdd.upload')}" action="mediaFileAdd!save" />
           <s:submit value="%{getText('generic.cancel')}" action="mediaFileAdd!cancel" />
        </s:else>
    </div>

</s:form>
