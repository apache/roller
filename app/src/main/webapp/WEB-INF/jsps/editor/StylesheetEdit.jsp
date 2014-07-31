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
<link rel="stylesheet" media="all" href='<s:url value="/roller-ui/jquery-ui-1.11.0/jquery-ui.min.css"/>' />

<script>
	function revertStylesheet() {
		if (window.confirm('<s:text name="stylesheetEdit.confirmRevert"/>')) {
			document.stylesheetEdit.action = "<s:url action='stylesheetEdit!revert' />";
			document.stylesheetEdit.submit();
		}
	};
	<s:if test="%{customStylesheet}">
		function deleteStylesheet() {
			if (window.confirm('<s:text name="stylesheetEdit.confirmDelete"/>')) {
				document.stylesheetEdit.action = "<s:url action='stylesheetEdit!delete' />";
				document.stylesheetEdit.submit();
			}
		};
	</s:if>
</script>

<p class="subtitle"><s:text name="stylesheetEdit.subtitle" /></p>

<p class="pagetip">
    <s:text name="stylesheetEdit.tip" />
    <s:if test="!customTheme">
        <s:text name="stylesheetEdit.revertTip" />
        <s:if test="customStylesheet">
            <br /><br /><s:text name="stylesheetEdit.revertTip1" />
        </s:if>
    </s:if>
</p>

<s:form action="stylesheetEdit!save">
	<s:hidden name="salt" />
    <s:hidden name="weblog" />

    <%-- ================================================================== --%>
    <%-- Tabs for each of the two content areas: Standard and Mobile --%>

    <div id="template-code-tabs">
    <ul>
        <li class="selected"><a href="#tabStandard"><em>Standard</em></a></li>
        <s:if test="contentsMobile != null">
            <li><a href="#tabMobile"><em>Mobile</em></a></li>
        </s:if>
    </ul>
    <div>
        <div id="tabStandard">
            <s:textarea name="contentsStandard" cols="80" rows="30" cssStyle="width:100%" />
        </div>
        <s:if test="contentsMobile != null">
            <div id="tabMobile">
                <s:textarea name="contentsMobile" cols="80" rows="30" cssStyle="width:100%" />
            </div>
        </s:if>
    </div>
    </div>
    
    <%-- ================================================================== --%>
    <%-- Save, Close and Resize text area buttons--%>

    <script>
        if (getCookie("templateEditorRows") != null) {
            document.getElementById('stylesheetEdit_contentsMobile').rows = getCookie("templateEditorRows");
            document.getElementById('stylesheetEdit_contentsStandard').rows = getCookie("templateEditorRows");
        } else {
            document.getElementById('stylesheetEdit_contentsMobile').rows = 20;
            document.getElementById('stylesheetEdit_contentsStandard').rows = 20;
        }
    </script>
    <table style="width:100%">
        <tr>
            <td>
                <s:submit value="%{getText('generic.save')}" />&nbsp;&nbsp;
                <s:if test="!customTheme">
                    <s:submit value="%{getText('stylesheetEdit.revert')}" onclick="revertStylesheet();return false;" />
                </s:if>
                <%-- Only delete if we have no custom templates ie website.customStylesheetPath=null --%>
                <s:if test="customStylesheet">
                    <s:submit value="%{getText('stylesheetEdit.delete')}" onclick="deleteStylesheet();return false;" />
                </s:if>
            </td>
            <td align="right">
                <!-- Add buttons to make this textarea taller or shorter -->
                <input type="button" name="taller" value=" &darr; " onclick="changeSize1(5)" />
                <input type="button" name="shorter" value=" &uarr; " onclick="changeSize1(-5)" />
            </td>
        </tr>
    </table>
    
</s:form>

<script>
function changeSize1(num) {
    var standardElem = document.getElementById('stylesheetEdit_contentsStandard');
    var mobileElem = document.getElementById('stylesheetEdit_contentsMobile');
    a = standardElem.rows + num;
    if (a > 0) {
        standardElem.rows = a;
        mobileElem.rows = a;
    }
    var expires = new Date();
    expires.setTime(expires.getTime() + 24 * 90 * 60 * 60 * 1000); // sets it for approx 90 days.
    setCookie("templateEditorRows", standardElem.rows, expires);
}
</script>

<script src="<s:url value='/roller-ui/scripts/jquery-2.1.1.min.js'></s:url>"></script>
<script src="<s:url value='/roller-ui/jquery-ui-1.11.0/jquery-ui.min.js'></s:url>"></script>

<script>
    $(function() {
        $( "#template-code-tabs" ).tabs();
    });
</script>
