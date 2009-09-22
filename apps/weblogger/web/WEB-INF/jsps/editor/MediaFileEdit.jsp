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

<p class="subtitle">
    <s:text name="mediaFile.edit.title"  />
</p>

<s:form id="entry" action="mediaFileEdit!save" method="POST">
    <s:hidden name="weblog" />
    <s:hidden name="mediaFileId" />

    <%-- ================================================================== --%>
    <%-- Title, category, dates and other metadata --%>

    <table class="entryEditTable" cellpadding="0" cellspacing="0" width="100%">

        <tr>
            <td class="entryEditFormLabel">
                <label for="status"><s:text name="mediaFileEdit.name" /></label>
            </td>
            <td>
                <s:textfield name="bean.name" size="50" maxlength="255" tabindex="1" />
            </td>
       </tr>

        <tr>
            <td class="entryEditFormLabel">
                <label for="status"><s:text name="mediaFileEdit.description" /></label>
            </td>
            <td>
                <s:textarea name="bean.description" cols="50" rows="5" tabindex="3"/>
            </td>
       </tr>

       <tr>
            <td class="entryEditFormLabel">
                <label for="copyright"><s:text name="mediaFileEdit.copyright" /></label>
            </td>
            <td>
                <s:textarea name="bean.copyrightText" cols="50" rows="3" tabindex="4"/>
            </td>
       </tr>

       <tr>
            <td class="entryEditFormLabel">
                <label for="tags"><s:text name="mediaFileEdit.tags" /></label>
            </td>
            <td>
                <s:textfield name="bean.tags" size="50" maxlength="255" tabindex="5" />
            </td>
       </tr>

       <tr>
            <td class="entryEditFormLabel">
                <label for="directoryId"><s:text name="mediaFileEdit.directory" /></label>
            </td>
            <td>
                <s:select name="bean.directoryId" list="allDirectories" listKey="id" listValue="path" />
            </td>
       </tr>

       <tr>
            <td class="entryEditFormLabel">
                <label for="status"><s:text name="mediaFileEdit.includeGallery" /></label>
            </td>
            <td>
                <s:checkbox name="bean.sharedForGallery" />
            </td>
       </tr>

    </table>

    <br>
    <div class="control">
       <input type="submit" value="<s:text name="mediaFileEdit.save" />" name="submit" />
       <input type="button" value="<s:text name="mediaFileEdit.cancel" />" onClick="javascript:window.parent.onClose();" />
    </div>

</s:form>
