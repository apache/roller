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

<p class="subtitle"><s:text name="stylesheetEdit.subtitle" /></p>

<p class="pagetip">
    <s:text name="stylesheetEdit.tip" />
    <s:if test="!customTheme"><s:text name="stylesheetEdit.revertTip" /></s:if>
    <s:if test="$(type == null)"><s:param name="type">standard</s:param></s:if>
</p>
                
<s:form action="stylesheetEdit!save">
    <s:hidden name="weblog" />
    <s:hidden name="type"/>
    <s:set name="type" value="type"/>

     <table class="menuTabTable" cellspacing="0" >
     <tr>
          <s:if test="%{#type=='standard'}">
        <td class="menuTabSelected">
    </s:if>
    <s:else>
        <td class="menuTabUnselected">
    </s:else>

          <div class="menu-tr">
           <s:url id="styleEdit" action="stylesheetEdit">
               <s:param name="weblog" value="actionWeblog.handle" />
               <s:param name="type">standard</s:param>
           </s:url>
	       <div class="menu-tl">&nbsp;&nbsp;<s:a href="%{styleEdit}">Standard</s:a>&nbsp;&nbsp; </div>
	    </div></td>

          <td class="menuTabSeparator"></td>
        <s:if test="%{#type == 'mobile'}">
        <td class="menuTabSelected">
    </s:if>
    <s:else>
        <td class="menuTabUnselected">
    </s:else>
        <div class="menu-tr">

           <s:url id="styleEdit" action="stylesheetEdit">
                 <s:param name="weblog" value="actionWeblog.handle" />
                 <s:param name="type">mobile</s:param>
           </s:url>
	       <div class="menu-tl">&nbsp;&nbsp;<s:a href="%{styleEdit}">Mobile</s:a>&nbsp;&nbsp; </div>
	    </div></td>

     </tr>
        </table>

    <%-- ================================================================== --%>
    <%-- Template editing area w/resize buttons --%>
    <s:textarea name="contents" cols="80" rows="30" cssStyle="width:100%" />
    
    <script type="text/javascript"><!--
        if (getCookie("editorSize1") != null) {
            document.getElementById('stylesheetEdit_contents').rows = getCookie("editorSize1");
        }
        function changeSize(e, num) {
            a = e.rows + num;
            if (a > 0) e.rows = a;
            var expires = new Date();
            expires.setTime(expires.getTime() + 24 * 90 * 60 * 60 * 1000); // sets it for approx 90 days.
            setCookie("editorSize",e.rows,expires);
        }
        function changeSize1(e, num) {
            a = e.rows + num;
            if (a > 0) e.rows = a;
            var expires = new Date();
            expires.setTime(expires.getTime() + 24 * 90 * 60 * 60 * 1000); // sets it for approx 90 days.
            setCookie("editorSize1",e.rows,expires);
        }
    // --></script>
    <table style="width:100%">
        <tr>
            <td>
                <s:submit value="%{getText('stylesheetEdit.save')}" />&nbsp;&nbsp;
                <s:if test="!customTheme">
                    <s:submit value="%{getText('stylesheetEdit.revert')}" action="stylesheetEdit!revert" />
                </s:if>
            </td>
            <td align="right">
                <!-- Add buttons to make this textarea taller or shorter -->
                <input type="button" name="taller" value=" &darr; " 
                       onclick="changeSize1(document.getElementById('stylesheetEdit_contents'), 5)" />
                <input type="button" name="shorter" value=" &uarr; " 
                       onclick="changeSize1(document.getElementById('stylesheetEdit_contents'), -5)" />
            </td>
        </tr>
    </table>
    
</s:form>
