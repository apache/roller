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

<%-- One media file in the library grid or list, or in the editor's media chooser.
     Expects #mediaFile on the value stack. In the library (libraryQuery on the action) the
     checkbox (shown over the preview on hover) selects the file and the preview and name open its details page, which links
     back to this library page. In the chooser (#chooserMode set) the preview and name insert
     the file into the entry, with its description as the alt text. --%>

<s:if test="#chooserMode">
    <s:set var="altText" value="(#mediaFile.description != null && !#mediaFile.description.trim().isEmpty()) ? #mediaFile.description.trim() : #mediaFile.name"/>
<li class="media-card">
    <button type="button" class="media-card-preview mediafile-select-target" tabindex="-1" aria-hidden="true"
            data-mediafile-name="<s:property value='#mediaFile.name'/>"
            data-mediafile-url="<s:property value='#mediaFile.permalink'/>"
            data-mediafile-alt="<s:property value='#altText'/>"
            data-mediafile-is-image="<s:property value='#mediaFile.imageFile'/>">
        <s:if test="#mediaFile.imageFile">
            <img src="<s:property value='#mediaFile.thumbnailURL'/>" alt="" loading="lazy"
                 data-fallback-icon="<s:property value='typeIcon(#mediaFile.contentType)'/>"/>
        </s:if>
        <s:else>
            <span class="bi <s:property value='typeIcon(#mediaFile.contentType)'/>" aria-hidden="true"></span>
        </s:else>
    </button>
    <div class="media-card-body">
        <div class="media-card-text">
            <button type="button" class="media-card-name mediafile-select-target"
                    data-mediafile-name="<s:property value='#mediaFile.name'/>"
                    data-mediafile-url="<s:property value='#mediaFile.permalink'/>"
                    data-mediafile-alt="<s:property value='#altText'/>"
                    data-mediafile-is-image="<s:property value='#mediaFile.imageFile'/>"
                    aria-label="<s:text name='mediaFileImageChooser.insert' escapeHtml='true'><s:param value='#mediaFile.name'/></s:text>"
                    title="<s:property value='#mediaFile.name'/>"><s:property value="#mediaFile.name"/></button>
            <span class="media-card-meta">
                <span class="media-card-type"><s:property value="formatType(#mediaFile.contentType, #mediaFile.name)"/></span>
                <span class="media-card-size"><s:property value="formatSize(#mediaFile.length)"/></span>
            </span>
        </div>
    </div>
</li>
</s:if>
<s:else>
<s:url var="detailsURL" escapeAmp="false" action="mediaFileEdit">
    <s:param name="weblog" value="%{actionWeblog.handle}"/>
    <s:param name="mediaFileId" value="%{#mediaFile.id}"/>
    <s:param name="returnQuery" value="%{libraryQuery}"/>
</s:url>

<li class="media-card">

    <%-- over the preview in the grid; the first column in the list --%>
    <input type="checkbox" class="form-check-input media-select" name="selectedMediaFiles"
           id="select-<s:property value='#mediaFile.id'/>"
           value="<s:property value='#mediaFile.id'/>"
           aria-label="<s:text name='mediaFileView.selectFile' escapeHtml='true'><s:param value='#mediaFile.name'/></s:text>"/>

    <a class="media-card-preview" href="<s:property value='%{detailsURL}'/>" tabindex="-1" aria-hidden="true">
        <s:if test="#mediaFile.imageFile">
            <img src="<s:property value='#mediaFile.thumbnailURL'/>" alt="" loading="lazy"
                 data-fallback-icon="<s:property value='typeIcon(#mediaFile.contentType)'/>"/>
        </s:if>
        <s:else>
            <span class="bi <s:property value='typeIcon(#mediaFile.contentType)'/>" aria-hidden="true"></span>
        </s:else>
    </a>

    <div class="media-card-body">
        <div class="media-card-text">
            <a class="media-card-name" href="<s:property value='%{detailsURL}'/>"
               title="<s:property value='#mediaFile.name'/>"><s:property value="#mediaFile.name"/></a>
            <span class="media-card-meta">
                <span class="media-card-type"><s:property value="formatType(#mediaFile.contentType, #mediaFile.name)"/></span>
                <span class="media-card-size"><s:property value="formatSize(#mediaFile.length)"/></span>
                <span class="media-card-date"><s:date name="#mediaFile.dateUploaded" format="yyyy-MM-dd"/></span>
            </span>
        </div>
    </div>

</li>
</s:else>
