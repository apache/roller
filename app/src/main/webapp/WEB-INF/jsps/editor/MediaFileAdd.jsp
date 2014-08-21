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
<script src="<s:url value="/roller-ui/scripts/jquery-2.1.1.min.js" />"></script>


<script>
    $(document).ready(function() {
        $("input[type='file']").change(function() {
            var name = '';
            var count = 0;
            var fileControls = $("input[type='file']");
            for (var i=0; i<fileControls.size(); i++) {
                if (jQuery.trim(fileControls.get(i).value).length > 0) {
                    count++;
                    name = fileControls.get(i).value;
                }
            }
            if (count == 1) {
                $("#entry_bean_name").get(0).disabled = false;
                $("#entry_bean_name").get(0).value = name;
            } else if (count > 1) {
                $("#entry_bean_name").css("font-style","italic");
                $("#entry_bean_name").css("color","grey");
                $("#entry_bean_name").get(0).value = "<s:text name="mediaFileAdd.multipleNames"  />";
                $("#entry_bean_name").get(0).disabled = true;
            }
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

<p class="subtitle">
    <s:text name="mediaFileAdd.title"  />
</p>
<p class="pagetip">
    <s:text name="mediaFileAdd.pageTip"  />
</p>

<s:form id="entry" action="mediaFileAdd!save" method="POST" enctype="multipart/form-data">
	<s:hidden name="salt" />
    <s:hidden name="weblog" />
    <s:hidden name="directoryName" />

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
                <label for="status"><s:text name="generic.description" /></label>
            </td>
            <td>
                <s:textarea name="bean.description" cols="50" rows="5" style="width:30%"/>
            </td>
       </tr>

       <tr>
            <td class="entryEditFormLabel">
                <label for="status"><s:text name="mediaFileAdd.copyright" /></label>
            </td>
            <td>
                <s:textarea name="bean.copyrightText" cols="50" rows="3" style="width:30%"/>
            </td>
       </tr>

       <tr>
            <td class="entryEditFormLabel">
                <label for="status"><s:text name="mediaFileAdd.tags" /></label>
            </td>
            <td>
                <s:textfield name="bean.tagsAsString" size="50" maxlength="255" style="width:30%"/>
            </td>
       </tr>

       <tr>
            <td class="entryEditFormLabel">
                <label for="status"><s:text name="mediaFileAdd.directory" /></label>
            </td>
            <td>
                <s:select name="bean.directoryId" list="allDirectories" listKey="id" listValue="name" />
            </td>
       </tr>

       <tr>
            <td class="entryEditFormLabel">
                <label for="status"><s:text name="mediaFileAdd.includeGallery" /></label>
            </td>
            <td>
                <s:checkbox name="bean.sharedForGallery" />
                <s:text name="mediaFileEdit.includeGalleryHelp" />
            </td>
       </tr>

       <tr>
           <td>
                <br />
                <br />
                <br />
           </td>
           <td>
           </td>
       </tr>

        <tr>
            <td class="entryEditFormLabel">
                <label for="title"><s:text name="mediaFileAdd.fileLocation" /></label>
            </td>
            <td>
                <div id="fileControl0div" class="miscControl">
                    <s:file id="fileControl0" name="uploadedFiles" size="30" />
                    <br />
                </div>

                <div id="fileControl1div" class="miscControl">
                    <s:file id="fileControl1" name="uploadedFiles" size="30" />
                    <br />
                </div>

                <div id="fileControl2div" class="miscControl">
                    <s:file id="fileControl2" name="uploadedFiles" size="30" />
                    <br />
                </div>

                <div id="fileControl3div" class="miscControl">
                    <s:file id="fileControl3" name="uploadedFiles" size="30" />
                    <br />
                </div>

                <div id="fileControl4div" class="miscControl">
                    <s:file id="fileControl4" name="uploadedFiles" size="30" />
                    <br />
                </div>
            </td>
        </tr>

    </table>

    <br />
    <div class="control">
       <s:submit value="%{getText('mediaFileAdd.upload')}" action="mediaFileAdd!save" />
       <s:submit value="%{getText('generic.cancel')}" action="mediaFileAdd!cancel" />
    </div>

</s:form>
