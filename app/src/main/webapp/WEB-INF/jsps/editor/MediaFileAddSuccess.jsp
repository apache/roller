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

<style>
    table.mediaFileTable {
        margin-left: 2em;
        width: 90%;
    }
    img.mediaFileImage {
        margin-right: 1em;
    }
    span.label {
        font-weight: bold;
    }
</style>

<script>
    $(document).ready(function() {
        $("#submit").attr("disabled", true);

        $("input[type='checkbox']").change(function() {
            if ($("#enclosureURL").get(0).getAttribute("value") != '') {
                $("#submit").attr("disabled", false);
                return;
            }
            $("#submit").attr("disabled", isImageChecked() ? false : true);
        });
    });
    function isImageChecked() {
        var boxes = $("input[type='checkbox']");
        for (var i=0; i<boxes.length; i++) {
            if (boxes.get(i).checked) {
                return true;
            }
        }
        return false;
    }
    function setEnclosure(url) {
        $("#enclosureURL").get(0).value = url;
        if (isImageChecked()) {
            $("#submit").attr("disabled", false);
            return;
        }
        $("#submit").attr("disabled", url == '' ? true : false);
    }
</script>


<p class="subtitle">
    <s:text name="mediaFileSuccess.subtitle" />
</p>
<p class="pagetip">
    <s:text name="mediaFileSuccess.pageTip" />
</p>

<s:form id="entry">
	<s:hidden name="salt" />
    <s:hidden name="weblog" />
    <s:hidden name="bean.enclosureURL" id="enclosureURL" />

    <s:if test="newImages.size() > 0">
        <p><s:text name="mediaFileSuccess.selectImages" /></p>

        <%-- checkboxed list of images uploaded --%>
        <table class="mediaFileTable">
            <s:iterator value="newImages" id="newImage">
            <tr>
                <td width="5%">
                    <%-- checkbox for file --%>
                    <input type="checkbox"
                           name="selectedImages"
                           value="<s:property value="#newImage.id"/>"/>
                </td>

                <td width="15%">
                    <img align="center" class="mediaFileImage"
                         src='<s:property value="%{#newImage.thumbnailURL}" />' alt="thumbnail" />
                </td>

                <td width="80%">
                    <%-- description of file --%>
                    <span class="label"><s:text name="mediaFileSuccess.name" /></span>
                    <s:property value="%{#newImage.name}" /><br />

                    <span class="label"><s:text name="mediaFileSuccess.type" /></span>
                    <s:property value="%{#newImage.contentType}" /><br />

                    <span class="label"><s:text name="mediaFileSuccess.link" /></span>
                    <s:property value="%{#newImage.permalink}" /><br />

                    <span class="label"><s:text name="mediaFileSuccess.size" /></span>
                    <s:property value="%{#newImage.length}" /> <s:text name="mediaFileSuccess.bytes" />,
                    <s:property value="%{#newImage.width}" /> x
                    <s:property value="%{#newImage.height}" /> <s:text name="mediaFileSuccess.pixels" />

                </td>
            </tr>
            </s:iterator>
        </table>

    </s:if>

    <s:if test="newFiles.size() > 0">
        <p><s:text name="mediaFileSuccess.selectEnclosure" /></p>

        <%-- checkboxed list of other files uploaded --%>
        <table class="mediaFileTable">
            <s:iterator value="newFiles" id="newFile">
            <tr>
                <td width="20%">
                    <%-- radio button for file --%>
                    <input type="radio" name="enclosure"
                       onchange="setEnclosure('<s:property value="%{#newFile.permalink}" />')" />
                </td>
                <td width="80%">
                    <%-- description of file --%>
                    <s:property value="%{#newFile.name}" />
                </td>
            </tr>
            </s:iterator>
            <tr>
                <td>
                    <input type="radio" name="enclosure" onchange="setEnclosure('')" />
                </td>
                <td>
                    <s:text name="mediaFileSuccess.noEnclosure" />
                </td>
            </tr>
        </table>

    </s:if>


    <div style="margin-top:20px"">

        <p><s:text name="mediaFileSuccess.createPostPrompt" /></p>
        <s:submit id="submit" value="%{getText('mediaFileSuccess.createPost')}" action="entryAddWithMediaFile"/>
        <br/>
        <br/>
        <br/>

        <s:text name="mediaFileSuccess.noThanks" />
        <ul>
            <li>
                <s:url var="mediaFileAddURL" action="mediaFileAdd">
                    <s:param name="weblog" value="%{actionWeblog.handle}" />
                    <s:param name="directoryName" value="%{directoryName}" />
                </s:url>
                <s:a href="%{mediaFileAddURL}">
                    <s:text name="mediaFileSuccess.addAnother" />
                </s:a>
            </li>

            <li>
                <s:url var="mediaFileViewURL" action="mediaFileView">
                    <s:param name="weblog" value="%{actionWeblog.handle}" />
                    <s:param name="directoryId" value="%{bean.directoryId}" />
                </s:url>
                <s:a href="%{mediaFileViewURL}">
                    <s:text name="mediaFileSuccess.mediaFileView" />
                </s:a>
            </li>
        </ul>

    </div>

</s:form>
