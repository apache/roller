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


<p class="subtitle"><s:text name="mediaFileSuccess.subtitle"/></p>
<p class="pagetip"><s:text name="mediaFileSuccess.pageTip"/></p>

<s:form id="entry" theme="bootstrap" cssClass="form-horizontal">
    <s:hidden name="salt"/>
    <s:hidden name="weblog"/>
    <s:hidden name="bean.enclosureURL" id="enclosureURL"/>

    <s:if test="newImages.size() > 0">
        <h4><s:text name="mediaFileSuccess.selectImagesTitle"/></h4>
        <p><s:text name="mediaFileSuccess.selectImages"/></p>

        <%-- select images via checkboxes --%>

        <s:iterator value="newImages" var="newImage">

            <div class="panel panel-default">
                <div class="panel-body">

                    <div class="row">

                        <div class="col-md-1">
                            <input type="checkbox" name="selectedImages" value="<s:property value="#newImage.id"/>"/>
                        </div>

                        <div class="col-md-2">
                            <img align="center" class="mediaFileImage"
                                 src='<s:property value="%{#newImage.thumbnailURL}" />' alt="thumbnail"/>
                        </div>

                        <div class="col-md-9">
                            <p>
                                <b><s:text name="mediaFileSuccess.name"/></b>
                                <s:property value="%{#newImage.name}"/>
                            </p>

                            <p>
                                <b><s:text name="mediaFileSuccess.type"/></b>
                                <s:property value="%{#newImage.contentType}"/>
                            </p>

                            <p>
                                <b><s:text name="mediaFileSuccess.size"/></b>
                                <s:property value="%{#newImage.length}"/> <s:text name="mediaFileSuccess.bytes"/>,
                                <s:property value="%{#newImage.width}"/> x
                                <s:property value="%{#newImage.height}"/> <s:text name="mediaFileSuccess.pixels"/>
                            </p>

                            <p>
                                <b><s:text name="mediaFileSuccess.link"/></b>
                                <s:property value="%{#newImage.permalink}"/>
                            </p>
                        </div>

                    </div>

                </div>
            </div>

        </s:iterator>

    </s:if>

    <s:if test="newFiles.size() > 0">

        <%-- select enclosure file via radio boxes --%>

        <h4><s:text name="mediaFileSuccess.selectEnclosureTitle"/></h4>
        <p><s:text name="mediaFileSuccess.selectEnclosure"/></p>

        <s:iterator value="newFiles" var="newFile">
            <div class="panel panel-default">
                <div class="panel-body">

                    <div class="row">

                        <div class="col-md-1">
                            <input type="radio" name="enclosure"
                                   onchange="setEnclosure('<s:property value="%{#newFile.permalink}"/>')"/>
                        </div>

                        <div class="col-md-11">
                            <p>
                                <b><s:text name="mediaFileSuccess.name"/></b>
                                <s:property value="%{#newFile.name}"/>
                            </p>

                            <p>
                                <b><s:text name="mediaFileSuccess.type"/></b>
                                <s:property value="%{#newFile.contentType}"/>,&nbsp;

                                <b><s:text name="mediaFileSuccess.size"/></b>
                                <s:property value="%{#newFile.length}"/> <s:text name="mediaFileSuccess.bytes"/>,
                                <s:property value="%{#newFile.width}"/> x
                                <s:property value="%{#newFile.height}"/> <s:text name="mediaFileSuccess.pixels"/>
                            </p>

                            <p>
                                <b><s:text name="mediaFileSuccess.link"/></b>
                                <s:property value="%{#newFile.permalink}"/>
                            </p>
                        </div>

                    </div>

                </div>
            </div>
        </s:iterator>

        <div class="panel panel-default">
            <div class="panel-body">
                <div class="row">

                    <div class="col-md-1">
                        <input type="radio" name="enclosure" onchange="setEnclosure('')" />
                    </div>

                    <div class="col-md-10">
                        <s:text name="mediaFileSuccess.noEnclosure" />
                    </div>

                </div>
            </div>
        </div>

    </s:if>

    <%-- buttons for create new weblog, cancel and upload more --%>

    <div>
        <s:url var="mediaFileAddURL" action="mediaFileAdd">
            <s:param name="weblog" value="%{actionWeblog.handle}"/>
            <s:param name="directoryName" value="%{directoryName}"/>
        </s:url>

        <s:url var="mediaFileViewURL" action="mediaFileView">
            <s:param name="weblog" value="%{actionWeblog.handle}"/>
            <s:param name="directoryId" value="%{bean.directoryId}"/>
        </s:url>

        <s:submit cssClass="btn btn-success" id="submit" value="%{getText('mediaFileSuccess.createPost')}"
                  action="entryAddWithMediaFile"/>

        <button class="btn btn-default" onclick='window.load("<s:property value='%{mediaFileAddURL}'/>")'>
            <s:text name="mediaFileSuccess.uploadMore"/>
        </button>

        <button class="btn" onclick='window.load("<s:property value='%{mediaFileViewURL}'/>")'>
            <s:text name="generic.cancel"/>
        </button>
    </div>

</s:form>


<%-- ================================================================================= --%>

<script>

    var submitButton = $("#submit");

    $(document).ready(function () {
        $("#submit").attr("disabled", true);

        $("input[type='checkbox']").change(function () {
            if ($("#enclosureURL").get(0).getAttribute("value") !== '') {
                $("#submit").attr("disabled", false);
                return;
            }
            submitButton.attr("disabled", !isImageChecked());
        });
    });

    function isImageChecked() {
        var boxes = $("input[type='checkbox']");
        for (var i = 0; i < boxes.length; i++) {
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
        submitButton.attr("disabled", url === '');
    }

</script>
