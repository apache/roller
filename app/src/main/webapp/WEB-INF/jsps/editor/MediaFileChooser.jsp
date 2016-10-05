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

  Source file modified from the original ASF source; all changes made
  are also under Apache License.
--%>
<%@ include file="/WEB-INF/jsps/taglibs-struts2.jsp" %>

<script src="<s:url value="/tb-ui/scripts/jquery-2.2.3.min.js" />"></script>
<script src="<s:url value='/tb-ui/scripts/jquery-2.2.3.min.js'/>"></script>
<script src="https://cdnjs.cloudflare.com/ajax/libs/jsviews/0.9.75/jsviews.min.js"></script>
<script>
var contextPath = "${pageContext.request.contextPath}";
</script>
<script src="<s:url value='/tb-ui/scripts/commonjquery.js'/>"></script>
<script src="<s:url value='/tb-ui/scripts/mediafilechooser.js'/>"></script>

<input type="hidden" id="recordId" value="<s:property value='%{#parameters.weblogId}'/>"/>
<input type="hidden" id="refreshURL" value="<s:url action='mediaFileChooser'/>?weblogId=<s:property value='%{#parameters.weblogId}'/>"/>

<%-- Drop-down box to choose media directory --%>
<select id="mediachooser-select-directory"></select>
<input id="select-item" type="button" style="margin:4px" value='<s:text name="generic.view" />'/>

<p class="pagetip">
    <s:text name="mediaFileChooser.pageTip" />
</p>

<%-- Media file contents for selected folder --%>
<div width="720px" height="500px">
    <ul id="formBody">
        <script id="formTemplate" type="text/x-jsrender">
            <li class="align-images" onmouseover="highlight(this, true)" onmouseout="highlight(this, false)">
                <div class="mediaObject" onclick=
                "window.parent.onSelectMediaFile('{{:name}}', '{{:permalink}}', '{{:altText}}', '{{:titleText}}', '{{:anchor}}', {{:imageFile}})">
                    {{if imageFile}}
                        <img border="0" src='{{:thumbnailURL}}' />
                    {{/if}}
                </div>

                <div class="mediaObjectInfo">
                    <label>{{:name}}</label>
                </div>
            </li>
        </script>
    </ul>
</div>
