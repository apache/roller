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
<%-- 
This default stuff goes in the HTML head element of each page
You can override it with your own file via WEB-INF/tiles-def.xml
--%>

<%@ include file="/WEB-INF/jsps/taglibs-struts2.jsp" %>

<script src="<s:url value='/webjars/jquery/3.7.1/jquery.min.js' />"></script>

<%-- jquery-ui webjar is 1.14.2+1 in pom.xml, but its resources are served
     under the plus-free path: the +N is a webjar build suffix, not part of
     the URL. Keeping the +1 here 404s and breaks the date picker/autocomplete. --%>
<script src="<s:url value='/webjars/jquery-ui/1.14.2/jquery-ui.min.js' />"></script>
<link href="<s:url value='/webjars/jquery-ui/1.14.2/jquery-ui.css' />" rel="stylesheet" />

<script src="<s:url value='/webjars/jquery-validation/1.21.0/jquery.validate.min.js' />"></script>

<link href="<s:url value='/webjars/bootstrap/3.4.1/css/bootstrap.min.css' />" rel="stylesheet" />
<link href="<s:url value='/webjars/bootstrap/3.4.1/css/bootstrap-theme.min.css' />" rel="stylesheet" />
<script src="<s:url value='/webjars/bootstrap/3.4.1/js/bootstrap.min.js' />"></script>

<script src="<s:url value='/webjars/clipboard.js/2.0.11/clipboard.min.js' />"></script>

<script src="<s:url value='/webjars/summernote/0.8.12/dist/summernote.min.js' />"></script>
<link href="<s:url value='/webjars/summernote/0.8.12/dist/summernote.css' />" rel="stylesheet" />

<link rel="stylesheet" media="all" href='<s:url value="/roller-ui/styles/roller.css"/>' />

<script src="<s:url value="/theme/scripts/roller.js"/>"></script>

