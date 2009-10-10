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
<script type="text/javascript" src='<s:url value="/roller-ui/scripts/jquery-1.3.1.min.js" />'></script>

<s:if test="bean.isImage">
    <div class="mediaFileThumbnail">
        <img align="right" alt="thumbnail" src='<s:property value="bean.thumbnailURL" />' />
    </div>
</s:if>

<p class="subtitle">
    <s:text name="mediaFileEdit.subtitle">
        <s:param value="bean.name" />
    </s:text>
</p>

<p class="pagetip">
    <s:text name="mediaFileEdit.pagetip"  />
</p>

<s:form id="entry" action="mediaFileEdit!save" method="POST">
    <s:hidden name="weblog" />
    <s:hidden name="mediaFileId" id="mediaFileId" />
    <s:hidden name="bean.permalink" />


    <%-- ================================================================== --%>
    <%-- Title, category, dates and other metadata --%>

    <table class="entryEditTable" cellpadding="0" cellspacing="0" width="100%">

        <tr>
            <td class="entryEditFormLabel">
                <label for="status"><s:text name="mediaFileEdit.name" /></label>
            </td>
            <td>
                <s:textfield name="bean.name" size="30" maxlength="100" tabindex="1" />
            </td>
       </tr>

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
                <label for="status"><s:text name="mediaFileEdit.description" /></label>
            </td>
            <td>
                <s:textarea name="bean.description" cols="50" rows="2" tabindex="2"/>
            </td>
       </tr>

       <tr>
            <td class="entryEditFormLabel">
                <label for="tags"><s:text name="mediaFileEdit.tags" /></label>
            </td>
            <td>
                <s:textfield name="bean.tags" size="30" maxlength="100" tabindex="3" />
            </td>
       </tr>

       <tr>
            <td class="entryEditFormLabel">
                <label for="copyright"><s:text name="mediaFileEdit.copyright" /></label>
            </td>
            <td>
                <s:textfield name="bean.copyrightText" size="30" maxlength="100" tabindex="4"/>
            </td>
       </tr>

       <tr>
            <td class="entryEditFormLabel">
                <label for="directoryId"><s:text name="mediaFileEdit.directory" /></label>
            </td>
            <td>
                <s:select name="bean.directoryId" list="allDirectories"
                    listKey="id" listValue="path" tabindex="5" />
            </td>
       </tr>

       <tr>
            <td class="entryEditFormLabel">
                <label for="status"><s:text name="mediaFileEdit.includeGallery" /></label>
            </td>
            <td>
                <s:checkbox name="bean.sharedForGallery" tabindex="6" />
                <s:text name="mediaFileEdit.includeGalleryHelp" />
            </td>
       </tr>

    </table>

    <div class="control">
       <input type="submit" tabindex="7"
              value="<s:text name="mediaFileEdit.save" />" name="submit" />
       <input type="button" tabindex="8"
              value="<s:text name="mediaFileEdit.cancel" />" onClick="javascript:window.parent.onClose();" />
    </div>

</s:form>


<%-- Create Weblog Entry and Create Podcast Entry links --%>
<br />

<p>
<a href='#' onclick='javascript:window.parent.onCreateWeblogPost($("#mediaFileId").get(0).value)'>
    <s:text name="mediaFileEdit.createWeblogPost" />
</a><br />
<s:text name="mediaFileEdit.createWeblogPostTip" />
</p>

<p>
<a href='#' onclick='javascript:window.parent.onCreatePodcastPost($("#entry_bean_permalink").get(0).value)'>
    <s:text name="mediaFileEdit.createPodcastPost" />
</a><br />
<s:text name="mediaFileEdit.createPodcastPostTip" /><br />
</p>

