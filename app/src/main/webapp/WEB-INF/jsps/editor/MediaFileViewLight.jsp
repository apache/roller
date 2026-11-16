<%--
  Licensed to the Apache Software Foundation (ASF) under one
  or more contributor license agreements.  See the NOTICE file
  distributed with this work for additional information
  regarding copyright ownership.  The ASF licenses this file
  to you under the Apache License, Version 2.0 (the
  "License"); you may not use this file except in compliance
  with the License.  You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

  Unless required by applicable law or agreed to in writing,
  software distributed under the License is distributed on an
  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
  KIND, either express or implied.  See the License for the
  specific language governing permissions and limitations
  under the License.
--%>
<%@ include file="/WEB-INF/jsps/taglibs-struts2.jsp" %>
{
"Result":
[
    <s:iterator var="directory" value="childDirectories" status="dirStatus">
{"label":"<s:property value="#directory.name" />","key":"<s:property value="#directory.id" />","type":"dir"}<s:if test="(!#dirStatus.last) || (#dirStatus.last && childFiles.size() > 0)">,</s:if>
    </s:iterator>
    <s:iterator var="mediaFile" value="childFiles" status="fileStatus">
{"label":"<s:property value="#mediaFile.name" />","key":"<s:property value="#mediaFile.id" />","type":"file"}<s:if test="!#fileStatus.last">,</s:if>
    </s:iterator>
]
}
