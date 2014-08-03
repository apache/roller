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

<s:url var="mediaFileURL" value="/roller-ui/rendering/media-resources/%{bean.id}" />

<script>
function addImage() {
    var filePointer = "<img src='<s:property value="%{mediaFileURL}" />' alt='<s:property value="bean.name" />' width='<width>' height='<height>' style='<style>' />";
    filePointer = filePointer.replace('<width>', document.imageDimForm.imageWidth.value);
    filePointer = filePointer.replace('<height>', document.imageDimForm.imageHeight.value);
    var styleDescription = '';

    var selectedAlignment;
    for (i = 0; i < document.imageDimForm.imageAlignment.length; i ++) {
        if (document.imageDimForm.imageAlignment[i].checked) {
            selectedAlignment = document.imageDimForm.imageAlignment[i].value
         }
    }

    if (selectedAlignment == 'Left') {
        styleDescription='display:inline;float:left;';
    }
    else if (selectedAlignment == 'Right') {
        styleDescription='display:inline;float:right;';
    }
    else if (selectedAlignment == 'Center') {
        styleDescription = 'display:block;margin-left:auto;margin-right:auto;';
    }

    var filePointer = filePointer.replace('<style>', styleDescription);
    parent.onClose(filePointer);
}
</script>

<p class="subtitle">
    Choose layout for image
</p>

<form name="imageDimForm" method="POST" onsubmit="addImage()">

    <div id="imageAlign" style="border:1px solid #000000;width:120px;height:100px;margin:5px;">
        <img border="0" src='<s:property value="%{mediaFileURL}" />' width="120px" height="100px" />
    </div>

    <table class="entryEditTable" cellpadding="0" cellspacing="0" width="100%">

        <tr>
            <td class="entryEditFormLabel">
                <label for="status">Link URL:</label>
            </td>
            <td>
               <s:a href="%{mediaFileURL}"><s:property value="%{mediaFileURL}" /></s:a>
            </td>
       </tr>

       <tr>
            <td class="entryEditFormLabel">
                <label for="status">Alignment:</label>
            </td>
            <td>
                <input type="radio" name="imageAlignment" value="None" checked> None
                <input type="radio" name="imageAlignment" value="Left"> Left
                <input type="radio" name="imageAlignment" value="Center"> Center
                <input type="radio" name="imageAlignment" value="Right"> Right
            </td>
       </tr>

       <tr>
            <td class="entryEditFormLabel">
                <label for="status">Size:</label>
            </td>
            <td>
               <input type="text" name="imageWidth" size="10" maxlength="15" tabindex="1" />
               <input type="text" name="imageHeight" size="10" maxlength="15" tabindex="1" />
               <div style="float:left;margin-right:60px;">Width</div> <div>Height</div>
            </td>
       </tr>


    </table>
    <br />
    <div class="control">
       <input type="submit" value="Insert" name="submit" />
       <input type="button" value="Skip" name="Skip" />
    </div>

</form>
