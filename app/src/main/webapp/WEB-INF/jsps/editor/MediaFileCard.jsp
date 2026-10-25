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

<%-- One media file in the library grid or list. Expects #mediaFile on the value stack.
     The checkbox selects the file; the preview and name open its details. --%>

<li class="media-card">

    <button type="button" class="media-card-preview mediafile-edit-target"
            data-mediafile-id="<s:property value='#mediaFile.id'/>"
            data-mediafile-name="<s:property value='#mediaFile.name'/>"
            aria-label="<s:text name='mediaFileView.openDetails' escapeHtml='true'><s:param value='#mediaFile.name'/></s:text>">
        <s:if test="#mediaFile.imageFile">
            <img src="<s:property value='#mediaFile.thumbnailURL'/>" alt="" loading="lazy"
                 data-fallback-icon="<s:property value='typeIcon(#mediaFile.contentType)'/>"/>
        </s:if>
        <s:else>
            <span class="bi <s:property value='typeIcon(#mediaFile.contentType)'/>" aria-hidden="true"></span>
        </s:else>
    </button>

    <div class="media-card-body">
        <input type="checkbox" class="form-check-input media-select" name="selectedMediaFiles"
               id="select-<s:property value='#mediaFile.id'/>"
               value="<s:property value='#mediaFile.id'/>"
               aria-label="<s:text name='mediaFileView.selectFile' escapeHtml='true'><s:param value='#mediaFile.name'/></s:text>"/>
        <div class="media-card-text">
            <button type="button" class="media-card-name mediafile-edit-target"
                    data-mediafile-id="<s:property value='#mediaFile.id'/>"
                    data-mediafile-name="<s:property value='#mediaFile.name'/>"
                    title="<s:property value='#mediaFile.name'/>"><s:property value="#mediaFile.name"/></button>
            <span class="media-card-meta">
                <span class="media-card-type"><s:property value="formatType(#mediaFile.contentType, #mediaFile.name)"/></span>
                <span class="media-card-size"><s:property value="formatSize(#mediaFile.length)"/></span>
                <span class="media-card-date"><s:date name="#mediaFile.dateUploaded" format="yyyy-MM-dd"/></span>
            </span>
        </div>
    </div>

</li>
