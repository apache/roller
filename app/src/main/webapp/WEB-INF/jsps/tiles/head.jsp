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
<%-- 
This default stuff goes in the HTML head element of each page
You can override it with your own file via WEB-INF/tiles-def.xml
--%>

<%@ include file="/WEB-INF/jsps/taglibs-struts2.jsp" %>

<%-- salt issued with this response, read by refreshSalt() after an AJAX post --%>
<meta name="roller-salt" content="<s:property value="salt"/>" />

<script src="<s:url value='/webjars/jquery/4.1.0/jquery.min.js' />"></script>

<%-- jquery-ui webjar is 1.14.2+1 in pom.xml, but its resources are served
     under the plus-free path: the +N is a webjar build suffix, not part of
     the URL. Keeping the +1 here 404s and breaks the date picker/autocomplete. --%>
<script src="<s:url value='/webjars/jquery-ui/1.14.2/jquery-ui.min.js' />"></script>
<link href="<s:url value='/webjars/jquery-ui/1.14.2/jquery-ui.css' />" rel="stylesheet" />
<script src="<s:url value='/roller-ui/scripts/date-fields.js' />"></script>

<script src="<s:url value='/webjars/jquery-validation/1.22.1/dist/jquery.validate.min.js' />"></script>

<link href="<s:url value='/webjars/bootstrap/5.3.8/css/bootstrap.min.css' />" rel="stylesheet" />
<link href="<s:url value='/webjars/bootstrap-icons/1.13.2/font/bootstrap-icons.css' />" rel="stylesheet" />
<script src="<s:url value='/webjars/bootstrap/5.3.8/js/bootstrap.bundle.min.js' />"></script>

<script src="<s:url value='/webjars/clipboard.js/2.0.11/clipboard.min.js' />"></script>

<%-- restores the three jQuery APIs Summernote 0.9.1 still uses; remove with a jQuery 4 ready Summernote --%>
<script src="<s:url value='/roller-ui/scripts/jquery4-summernote-compat.js' />"></script>
<script src="<s:url value='/webjars/summernote/0.9.1/dist/summernote-bs5.min.js' />"></script>
<link href="<s:url value='/webjars/summernote/0.9.1/dist/summernote-bs5.min.css' />" rel="stylesheet" />

<link rel="stylesheet" media="all" href='<s:url value="/roller-ui/styles/roller.css"/>' />

<script src="<s:url value="/theme/scripts/roller.js"/>"></script>

