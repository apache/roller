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


<% request.setAttribute("version",
      org.apache.roller.weblogger.business.WebloggerFactory.getWeblogger().getVersion()); %>
<% request.setAttribute("revision",
      org.apache.roller.weblogger.business.WebloggerFactory.getWeblogger().getRevision()); %>

<img src='<c:url value="/images/tinyfeather.png"/>' alt="ASF logo" style="vertical-align:middle" />
<s:text name="footer.productName" >
    <s:param value="#request.version" />
    <s:param value="#request.revision" />
</s:text>
      
