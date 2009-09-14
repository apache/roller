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

<div class="sidebarFade">
    <div class="menu-tr">
        <div class="menu-tl">
            <div class="sidebarInner">

                <b><s:text name="mediaFileSidebar.actions" /></b>
                
                <s:url id="mediaFileAddURL" action="mediaFileAdd">
                    <s:param name="weblog" value="%{actionWeblog.handle}" />
                </s:url>

                <s:url id="mediaFileSearchURL" action="mediaFileSearch">
                    <s:param name="weblog" value="%{actionWeblog.handle}" />
                </s:url>

                <s:url id="mediaFileViewURL" action="mediaFileView">
                    <s:param name="weblog" value="%{actionWeblog.handle}" />
                </s:url>

                <hr size="1" noshade="noshade" />

                <a href='<s:property value="%{mediaFileViewURL}" />'
                    <s:if test="actionName.equals('mediaFileView')">style='font-weight:bold;'</s:if> >
                    <s:text name="mediaFileSidebar.view" /></a>

                <hr size="1" noshade="noshade" />

                <a href='<s:property value="%{mediaFileAddURL}" />'
                    <s:if test="actionName.equals('mediaFileAdd')">style='font-weight:bold;'</s:if> >
                    <s:text name="mediaFileSidebar.add" /></a>

                <hr size="1" noshade="noshade" />
                
                <a href='<s:property value="%{mediaFileSearchURL}" />'
                    <s:if test="actionName.equals('mediaFileSearch')">style='font-weight:bold;'</s:if> >
                    <s:text name="mediaFileSidebar.search" /></a>

                <hr size="1" noshade="noshade" />
                <br />
                <br />

            </div>
        </div>
    </div>
</div>
