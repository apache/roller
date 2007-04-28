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
   <s:text name="pagesForm.subtitle" >
       <s:param value="actionWeblog.handle" />
   </s:text>
</p>  
<p class="pagetip">
   <s:text name="pagesForm.tip" />
</p>

<s:if test="actionWeblog.editorTheme != 'custom'">
    <p><s:text name="pagesForm.themesReminder"><s:param value="actionWeblog.editorTheme"/></s:text></p>
</s:if>

<%-- table of templates via ajax --%>
<div class="tmplsHead">
    <table cellpadding="0">
        <tr>
            <td><h2>Your Templates</h2> <img src="<s:url value="/images/help.png"/>"/></td>
            <td align="right">
                <s:url id="addTmpl" action="templateAdd">
                    <s:param name="weblog" value="actionWeblog.handle"/>
                </s:url>
                <img src="<s:url value="/images/add.png"/>"/> <s:a theme="ajax" href="%{addTmpl}" targets="addTmplForm">Add Template</s:a>
            </td>
        </tr>
    </table>
</div>
<script type="text/javascript" language="javascript">
dojo.event.topic.subscribe("/refreshTmpls", function(nothing) {
  // i don't need to do anything :/
});
</script>
<s:url id="listTmpls" action="templates" method="list">
    <s:param name="weblog" value="actionWeblog.handle"/>
</s:url>
<s:div theme="ajax" href="%{listTmpls}" listenTopics="/refreshTmpls" cssClass="tmplsList" />


<div id="addTmplForm"></div>
