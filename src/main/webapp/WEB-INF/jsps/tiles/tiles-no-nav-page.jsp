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
<%@ include file="/WEB-INF/jsps/tightblog-taglibs.jsp" %>
<!doctype html>
<html>
    <head>
      <meta charset="utf-8">
      <meta name="viewport" content="width=device-width, initial-scale=1.0">
      <link rel="shortcut icon" href="${pageContext.request.contextPath}/favicon.ico">
      <title><fmt:message key="${pageTitleKey}"/></title>
      <tiles:insertAttribute name="head" />
    </head>
    <body>
        
        <div id="banner">
            <div class="bannerStatusBox">
                <table class="bannerStatusBox" cellpadding="0" cellspacing="0">
                    <tr>
                        <td class="bannerLeft">
                            <fmt:message key="product.name.version">
                                <fmt:param value="${tightblogVersion}" />:
                            </fmt:message>
                        </td>
                        <td class="bannerRight">
                            <fmt:message><tiles:insertAttribute name='titleRight'/></fmt:message>
                        </td>
                    </tr>
                </table>
            </div>
        </div>
        
        <div id="content">
            <div id="nosidebar_maincontent_wrap">
                <div id="maincontent">
                    <tiles:insertAttribute name="content" />
                </div>
            </div>
        </div>

        <div id="footer">
            <tiles:insertAttribute name="footer" ignore="true" />
        </div>
    </body>
</html>
