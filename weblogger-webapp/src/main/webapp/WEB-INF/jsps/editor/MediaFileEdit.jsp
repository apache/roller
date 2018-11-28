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
<%@ page import="org.apache.roller.weblogger.config.WebloggerConfig" %>

<script type="text/javascript" src='<s:url value="/roller-ui/scripts/jquery-1.4.2.min.js" />'></script>
<script type="text/javascript" src='<s:url value="/custom-ui/ZeroClipboard.js" />'></script>

<%-- 
<!-- Can't distribute ZeroClipboard with Roller, LGPL violates ASF policy -->
<!-- (1/2) For ZeroClipboard you would add this: -->
<style type="text/css">
#d_clip_button {
    text-align:center;
    border:1px solid black;
    background-color:#ccc;
    margin:5px; padding:5px;
}
#d_clip_button.hover { background-color:#eee; }
#d_clip_button.active { background-color:#aaa; }
</style>

<script type="text/javascript">
$("#d_clip_button").ready(function() {
    // assuming ZeroClipboard is at context/custom-ui path
    ZeroClipboard.setMoviePath( '<s:url value="/custom-ui/ZeroClipboard.swf" />' );
    var clip = new ZeroClipboard.Client();
    clip.setText(''); // will be set later on mouseDownv
    clip.setHandCursor( true );
    clip.setCSSEffects( true );
    clip.addEventListener( 'mouseDown', function(client) {
        // set text to copy here
        clip.setText( document.getElementById('clip_text').value );
        } );
    clip.addEventListener( 'complete', function(client, text) {
        alert("Copied link to the Clipboard.");
    } );
    clip.glue( 'd_clip_button' );
});
</script>
--%>

<s:if test="bean.isImage">
    <div class="mediaFileThumbnail">
        <a href='<s:property value="bean.permalink" />' target="_blank">
            <img align="right" alt="thumbnail" src='<s:property value="bean.thumbnailURL" />'
                 title='<s:text name="mediaFileEdit.clickToView" />' />
        </a>
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

<s:form id="entry" action="mediaFileEdit!save" method="POST" enctype="multipart/form-data">
	<s:hidden name="salt" />
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
                <s:textfield name="bean.name" size="40" maxlength="100" tabindex="1" />
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
                <label for="status"><s:text name="mediaFileEdit.permalink" /></label>
            </td>
            <td>
                <a href='<s:text name="bean.permalink" />' target="_blank"
                   title='<s:text name="mediaFileEdit.linkTitle" />'>
                   <s:url id="linkIconURL" value="/images/link.png"></s:url>
                   <img border="0" src='<s:property value="%{linkIconURL}" />'
                       style="padding:2px 2px;" alt="link" />
                </a>
                <input type="text" id="clip_text" size="50" value='<s:text name="bean.permalink" />' readonly />

                <%-- 
                <!-- (2/2) For ZeroClipboard you would add this: -->
                <div id="d_clip_button"><s:text name="mediaFileEdit.copyToClipboard" /></div>
                --%>
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

        <tr>
            <td class="entryEditFormLabel">
                <label for="title"><s:text name="mediaFileEdit.updateFileContents" /></label>
            </td>
            <td>
                <div id="fileControldiv" class="miscControl">
                    <s:file id="fileControl" name="uploadedFile" size="30" />
                    <br />
                </div>
            </td>
        </tr>

        <!-- orginal path from base URL of ctx/resources/ -->
        <% if (WebloggerConfig.getBooleanProperty("mediafile.originalPathEdit.enabled")) { %>
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
        <% } %>

    </table>

    <div class="control">
       <input type="submit" tabindex="7"
              value="<s:text name="mediaFileEdit.save" />" name="submit" />
       <input type="button" tabindex="8"
              value="<s:text name="mediaFileEdit.cancel" />" onClick="javascript:window.parent.onEditCancelled();" />
    </div>

</s:form>


