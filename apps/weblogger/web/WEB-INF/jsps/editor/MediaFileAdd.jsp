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

<script type="text/javascript">
<!--
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
-->
</script>

<p class="subtitle">
    <s:text name="mediaFileAdd.title"  />
</p>
<p class="pagetip">
    <s:text name="mediaFileAdd.pageTip"  />
</p>

<s:form id="entry" action="mediaFileAdd!save" onsubmit="editorCleanup()" method="POST" enctype="multipart/form-data">
    <s:hidden name="weblog" />

    <%-- ================================================================== --%>
    <%-- Title, category, dates and other metadata --%>

    <table class="entryEditTable" cellpadding="0" cellspacing="0" width="100%">

        <tr>
            <td class="entryEditFormLabel">
                <label for="title"><s:text name="mediaFileAdd.fileLocation" /></label>
            </td>
            <td>
                 <s:file name="uploadedFile" size="30" onchange="this.form['bean.name'].value=getFileName(this.value)" />
                 <br />
                 <br />
                 <br />
            </td>
        </tr>

        <tr>
            <td class="entryEditFormLabel">
                <label for="status"><s:text name="mediaFileAdd.name" /></label>
            </td>
            <td>
                <s:textfield name="bean.name" size="50" maxlength="255" />
            </td>
       </tr>

       <tr>
            <td class="entryEditFormLabel">
                <label for="status"><s:text name="mediaFileAdd.description" /></label>
            </td>
            <td>
                <s:textarea name="bean.description" cols="50" rows="5" />
            </td>
       </tr>

       <tr>
            <td class="entryEditFormLabel">
                <label for="status"><s:text name="mediaFileAdd.copyright" /></label>
            </td>
            <td>
                <s:textarea name="bean.copyrightText" cols="50" rows="3" />
            </td>
       </tr>

       <tr>
            <td class="entryEditFormLabel">
                <label for="status"><s:text name="mediaFileAdd.tags" /></label>
            </td>
            <td>
                <s:textfield name="bean.tags" size="50" maxlength="255" />
            </td>
       </tr>

       <tr>
            <td class="entryEditFormLabel">
                <label for="status"><s:text name="mediaFileAdd.directory" /></label>
            </td>
            <td>
                <s:select name="bean.directoryId" list="allDirectories" listKey="id" listValue="path" />
            </td>
       </tr>

       <tr>
            <td class="entryEditFormLabel">
                <label for="status"><s:text name="mediaFileAdd.includeGallery" /></label>
            </td>
            <td>
                <s:checkbox name="bean.sharedForGallery" />
            </td>
       </tr>

    </table>

    <br />
    <div class="control">
       <input type="submit" value='<s:text name="mediaFileAdd.upload" />' name="upload" />
    </div>

</s:form>
