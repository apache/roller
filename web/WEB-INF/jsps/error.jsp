<!--
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
-->
<%@ include file="/taglibs.jsp" %>
<%@ page isErrorPage="true" %>
<tiles:insert page="/WEB-INF/jsps/tiles/tiles-simplepage.jsp">
   <tiles:put name="banner"       value="/WEB-INF/jsps/tiles/banner.jsp" />
   <tiles:put name="bannerStatus" value="/WEB-INF/jsps/tiles/bannerStatus.jsp" />
   <tiles:put name="head"         value="/WEB-INF/jsps/tiles/head.jsp" />
   <tiles:put name="styles"       value="/WEB-INF/jsps/tiles/empty.jsp" />
   <tiles:put name="messages"     value="/WEB-INF/jsps/tiles/messages.jsp" />
   <tiles:put name="content"      value="/WEB-INF/jsps/errorBody.jsp" />
   <tiles:put name="footer"       value="/WEB-INF/jsps/tiles/footer.jsp" />
</tiles:insert>