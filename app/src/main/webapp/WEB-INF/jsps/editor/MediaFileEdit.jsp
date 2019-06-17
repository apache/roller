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

<p class="pagetip">
    <s:text name="mediaFileEdit.pagetip"/>
</p>

<s:form id="entry" action="mediaFileEdit!save" method="POST" enctype="multipart/form-data"
        theme="bootstrap" class="form-horizontal">

    <s:hidden name="salt"/>
    <s:hidden name="weblog"/>
    <s:hidden name="mediaFileId" id="mediaFileId"/>
    <s:hidden name="bean.permalink"/>

    <s:if test="bean.isImage">
        <div class="form-group">
            <label class="control-label col-sm-3">Thumbnail</label>
            <div class="controls col-sm-9">
                <a href='<s:property value="bean.permalink" />' target="_blank">
                    <img alt="thumbnail" src='<s:property value="bean.thumbnailURL" />'
                         title='<s:text name="mediaFileEdit.clickToView" />'/>
                </a>
            </div>
        </div>
    </s:if>

    <%-- ================================================================== --%>
    <%-- Title, category, dates and other metadata --%>

    <s:textfield name="bean.name" size="35" maxlength="100" tabindex="1"
                 label="%{getText('generic.name')}"/>

    <div class="form-group">
        <label class="control-label col-sm-3"><s:text name="mediaFileEdit.fileInfo"/></label>

        <div class="controls col-sm-9">

            <s:text name="mediaFileEdit.fileTypeSize">
                <s:param value="bean.contentType"/>
                <s:param value="bean.length"/>
            </s:text>

            <s:if test="bean.isImage">
                <s:text name="mediaFileEdit.fileDimensions">
                    <s:param value="bean.width"/>
                    <s:param value="bean.height"/>
                </s:text>
            </s:if>

        </div>
    </div>

    <div class="form-group">
        <label class="control-label col-sm-3">URL</label>

        <div class="controls col-sm-9">

            <input type="text" id="clip_text" size="80"
                   value='<s:property value="bean.permalink" />' readonly />

            <s:url var="linkIconURL" value="/roller-ui/images/clippy.svg"/>
            <button class="clipbutton" data-clipboard-target="#clip_text" type="button">
                <img src='<s:property value="%{linkIconURL}" />' alt="Copy to clipboard" style="width:0.9em; height:0.9em">
            </button>

        </div>
    </div>

    <s:textarea name="bean.description" cols="50" rows="2" tabindex="2"
                label="%{getText('generic.description')}"/>

    <s:textfield name="bean.tagsAsString" size="30" maxlength="100" tabindex="3"
                 label="%{getText('mediaFileEdit.tags')}"/>

    <s:textfield name="bean.copyrightText" size="30" maxlength="100" tabindex="4"
                 label="%{getText('mediaFileEdit.copyright')}"/>

    <s:select name="bean.directoryId" list="allDirectories" listKey="id" listValue="name"
              tabindex="5" label="%{getText('mediaFileEdit.directory')}"/>

    <s:checkbox name="bean.sharedForGallery" tabindex="6"
                label="%{getText('mediaFileEdit.includeGalleryHelp')}"/>

    <!-- original path from base URL of ctx/resources/ -->
    <s:if test="getBooleanProp('mediafile.originalPathEdit.enabled')">
        <div id="originalPathdiv" class="miscControl">
            <s:textfield name="bean.originalPath" id="originalPath" size="30"
                         maxlength="100" tabindex="3"/>
        </div>
    </s:if>


    <input type="submit" tabindex="7" class="btn btn-success"
           value="<s:text name="generic.save" />" name="submit"/>
    <input type="button" tabindex="8" class="btn"
           value="<s:text name="generic.cancel" />" onClick="window.parent.onEditCancelled();"/>

</s:form>


<script>
    $(document).ready(function () {
        new ClipboardJS('.clipbutton');
    });
</script>
