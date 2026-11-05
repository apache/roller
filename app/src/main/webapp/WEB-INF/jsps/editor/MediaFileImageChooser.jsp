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

<%-- The entry editor's media chooser, shown in a dialog frame. A page of one folder's files,
     newest first; choosing one calls the editor's onSelectMediaFile() with values from data-*
     attributes (MediaFileCard.jsp, chooser mode). --%>

<div class="media-chooser">

    <s:form id="mediaFileChooserForm" name="mediaFileChooserForm" action="mediaFileImageChooser"
            method="get" theme="simple" cssClass="media-chooser-search" role="search">
        <s:hidden name="weblog"/>
        <div>
            <label class="form-label" for="chooserFolder"><s:text name="mediaFileView.viewFolder"/></label>
            <s:select id="chooserFolder" name="directoryId" list="allDirectories" listKey="id" listValue="name"
                      cssClass="form-select"/>
        </div>
        <div class="media-chooser-name">
            <label class="form-label" for="chooserName"><s:text name="mediaFileView.nameStartsWith"/></label>
            <s:textfield id="chooserName" name="bean.name" maxlength="255" cssClass="form-control"/>
        </div>
        <div>
            <button type="submit" class="btn btn-primary"><s:text name="generic.search"/></button>
        </div>
    </s:form>

    <p class="pagetip"><s:text name="mediaFileImageChooser.pageTip"/></p>

    <s:if test="childFiles.size() > 0">
        <s:include value="/WEB-INF/jsps/editor/Pager.jsp"/>
        <s:set var="chooserMode" value="true"/>
        <ul class="media-grid">
            <s:iterator var="mediaFile" value="childFiles">
                <s:include value="/WEB-INF/jsps/editor/MediaFileCard.jsp"/>
            </s:iterator>
        </ul>
        <s:include value="/WEB-INF/jsps/editor/Pager.jsp"/>
    </s:if>
    <s:else>
        <div class="media-empty">
            <span class="bi bi-images" aria-hidden="true"></span>
            <p><s:if test="bean.search"><s:text name="mediaFileView.noResults"/></s:if>
               <s:else><s:text name="mediaFileView.noFiles"/></s:else></p>
        </div>
    </s:else>

</div>

<script>
    // Values come from data-* attributes and are bound via delegated listeners.
    $(document).on('click', '.mediafile-select-target', function () {
        window.parent.onSelectMediaFile($(this).attr('data-mediafile-name'),
                $(this).attr('data-mediafile-url'),
                $(this).attr('data-mediafile-is-image'),
                $(this).attr('data-mediafile-alt'));
    });

    $('#chooserFolder').on('change', function () {
        $('#chooserName').val('');
        this.form.submit();
    });

    // A thumbnail that fails to load shows the file type icon instead.
    document.addEventListener('error', function (e) {
        var img = e.target;
        if (img.tagName === 'IMG' && img.closest('.media-card-preview')) {
            var icon = document.createElement('span');
            icon.className = 'bi ' + (img.getAttribute('data-fallback-icon') || 'bi-file-earmark');
            icon.setAttribute('aria-hidden', 'true');
            img.replaceWith(icon);
        }
    }, true);
</script>
