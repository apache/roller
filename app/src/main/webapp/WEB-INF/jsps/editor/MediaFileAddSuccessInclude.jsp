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

<s:if test="contentTypeImage">
   <s:include value="/WEB-INF/jsps/editor/MediaFileImageDimension.jsp" />
</s:if>
<s:else>
<script>
    <s:url var="mediaFileURL" value="/roller-ui/rendering/media-resources/%{bean.id}" />
    var filePointer = "<a href='<s:property value="%{mediaFileURL}" />'><s:property value="bean.name" /></a>";
    parent.onClose(filePointer);
</script>
</s:else>
