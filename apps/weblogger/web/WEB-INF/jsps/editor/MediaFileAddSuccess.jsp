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

<p class="subtitle">
   Add Media Files
</p>

<s:form id="entry" action="addMedia!submit" onsubmit="editorCleanup()">
    <s:hidden name="weblog" />

<div style="border:1px solid; width=80px;background-color: #cfc;margin-bottom:5px;">
<ul>
<li> <s:property value="bean.name" /> uploaded successfully</li>
</ul>
</div>

<s:url id="mediaFileURL" value="/roller-ui/rendering/media-resources/%{bean.id}">
</s:url>

<s:if test="contentTypeImage">
<div style="margin-bottom:10px;"><img border="0" src='<s:property value="%{mediaFileURL}" />' width="150px" alt="image"/></div>
</s:if>

<s:a href="%{mediaFileURL}">
<s:property value="%{mediaFileURL}" />
</s:a>

<div style="margin-top:20px;text-decoration:underline">
<a href="#">Create a blog post out of <s:property value="bean.name" /></a><br/>

<s:url id="mediaFileAddURL" action="mediaFileAdd">
    <s:param name="weblog" value="%{actionWeblog.handle}" />
</s:url>

<s:a href="%{mediaFileAddURL}">Add another media file</s:a>
</div>










    <%-- ================================================================== --%>
    <%-- Weblog edit or preview --%>






    <%-- ================================================================== --%>
    <%-- plugin chooser --%>




    <%-- ================================================================== --%>
    <%-- advanced settings  --%>


    <%-- ================================================================== --%>
    <%-- the button box --%>


   </s:form>
