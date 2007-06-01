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
   <s:text name="pageForm.subtitle" >
       <s:param value="bean.name" />
       <s:param value="actionWeblog.handle" />
   </s:text>
</p>

<s:if test="template.required">
    <p class="pagetip"><s:text name="pageForm.tip.required" /></p>
</s:if>
<s:else>
    <p class="pagetip"><s:text name="pageForm.tip" /></p>
</s:else>
                
<s:form action="templateEdit!save" id="template">
    <s:hidden name="weblog" />
    <s:hidden name="bean.id"/>
    
    <%-- ================================================================== --%>
    <%-- Name, link and desription: disabled when page is a required page --%>
    
    <table cellspacing="5">
        <tr>
            <td class="label"><s:text name="pageForm.name" />&nbsp;</td>
            <td class="field">
                <s:if test="template.required">
                    <s:textfield name="bean.name" size="50" readonly="true" cssStyle="background: #e5e5e5" />
                </s:if>
                <s:else>
                    <s:textfield name="bean.name" size="50"/>
                </s:else>
            </td>
            <td class="description"></td>
        </tr>
        
        <script type="text/javascript">
        <!--
        var weblogURL = '<s:property value="actionWeblog.absoluteURL" />';
        var originalLink = '<s:property value="bean.link" />';
        
        // Update page URL when user changes link
        function updatePageURLDisplay() {
            var previewSpan = document.getElementById('linkPreview');
            var n1 = previewSpan.firstChild;
            var n2 = document.createTextNode(document.getElementById('template_bean_link').value);
            if (n1 == null) {
                previewSpan.appendChild(n2);
            } else {
                previewSpan.replaceChild(n2, n1);
            }           
        }
        // Don't launch page if user has changed link, it'll be a 404
        function launchPage() {
            if (originalLink != document.getElementById('template_bean_link').value) {
                window.alert("Link changed, not launching page");
            } else {
                window.open(weblogURL + '/page/' + originalLink, '_blank');
            }
        }
        -->
        </script>
        
        <s:if test="!template.required && template.custom">
            <tr>
                <td class="label" valign="top"><s:text name="pageForm.link" />&nbsp;</td>
                <td class="field">
                    <s:textfield name="bean.link" size="50" onkeyup="updatePageURLDisplay()" />
                    <br />
                    <s:property value="actionWeblog.absoluteURL" />/page/<span id="linkPreview" style="color:red"><s:property value="bean.link" /></span>
                    <s:if test="template.link != null">[<a id="launchLink" onClick="launchPage()"><s:text name="pageForm.launch" /></a>]</s:if>
                </td>
                <td class="description"></td>
            </tr>
        </s:if>
        
        <tr>
            <td class="label" valign="top" style="padding-top: 4px"><s:text name="pageForm.description" />&nbsp;</td>
            <td class="field">
                <s:if test="template.required">
                    <s:textarea name="bean.description" cols="50" rows="2" readonly="true" cssStyle="background: #e5e5e5" />
                </s:if>
                <s:else>
                    <s:textarea name="bean.description" cols="50" rows="2" />
                </s:else>
            </td>
            <td class="description"></td>
        </tr>
        
    </table>
    
    <%-- ================================================================== --%>
    <%-- Template editing area w/resize buttons --%>
    
    <br />
    <s:textarea name="bean.contents" cols="80" rows="30" cssStyle="width:100%" />
    
    <script type="text/javascript"><!--
        if (getCookie("editorSize1") != null) {
            document.getElementById('template_bean_contents').rows = getCookie("editorSize1");
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
                <s:submit key="pageForm.save" />
                <input type="button" value="<s:text name="application.done"/>" onclick="window.location='<s:url action="templates"><s:param name="weblog" value="%{weblog}"/></s:url>'" />
            </td>
            <td align="right">
                <!-- Add buttons to make this textarea taller or shorter -->
                <input type="button" name="taller" value=" &darr; " 
                       onclick="changeSize1(document.getElementById('template_bean_contents'), 5)" />
                <input type="button" name="shorter" value=" &uarr; " 
                       onclick="changeSize1(document.getElementById('template_bean_contents'), -5)" />
            </td>
        </tr>
    </table>
   
    
    <%-- ================================================================== --%>
    <%-- Advanced settings inside a control toggle --%>
    
    <s:if test="template.custom">
        <br />
        <div id="advancedControlToggle" class="controlToggle">
            <span id="iadvancedControl">+</span>
            <a class="controlToggle" onclick="javascript:toggleControl('advancedControlToggle','advancedControl')">
            <s:text name="pageForm.advancedSettings" /></a>
        </div>
        
        <div id="advancedControl" class="advancedControl" style="display:none">
            
            <table cellspacing="6">
                <tr>
                    <td class="label" valign="top"><s:text name="pageForm.outputContentType" />&nbsp;</td>
                    <td class="field">
                        <script type="text/javascript"><!--
                        function showContentTypeField() {
                            if (document.getElementById('template_bean_autoContentType1').checked) {
                                document.getElementById('template_bean_manualContentType').readOnly = true;
                                document.getElementById('template_bean_manualContentType').style.background = '#e5e5e5';
                            } else {
                                document.getElementById('template_bean_manualContentType').readOnly = false;
                                document.getElementById('template_bean_manualContentType').style.background = '#ffffff';
                            }
                        }
                        // --></script>
                        <s:if test="bean.autoContentType">
                            <input type="radio" name="bean.autoContentType" value="true" checked="true" onchange="showContentTypeField()" id="template_bean_autoContentType1"/> 
                            <s:text name="pageForm.useAutoContentType" /><br />
                            
                            <input type="radio" name="bean.autoContentType" value="false" onchange="showContentTypeField()" id="template_bean_autoContentType2"/>
                            <s:text name="pageForm.useManualContentType" />
                            <s:textfield name="bean.manualContentType" />
                        </s:if>
                        <s:else>
                            <input type="radio" name="bean.autoContentType" value="true" onchange="showContentTypeField()" id="template_bean_autoContentType1"/> 
                            <s:text name="pageForm.useAutoContentType" /><br />
                            
                            <input type="radio" name="bean.autoContentType" value="false" checked="true" onchange="showContentTypeField()" id="template_bean_autoContentType2"/>
                            <s:text name="pageForm.useManualContentType" />
                            <s:textfield name="bean.manualContentType" />
                        </s:else>
                        
                        <br />
                        <br />
                        
                        <script type="text/javascript"><!--
                            showContentTypeField();
                        // --></script> 
                        
                    </td>
                    <td class="description"></td>
                </tr>
                
                <tr>
                    <td class="label"><s:text name="pageForm.navbar" />&nbsp;</td>
                    <td class="field"><s:checkbox name="bean.navbar" /> 
                        <s:text name="pageForm.navbar.tip" />
                    </td>
                    <td class="description"></td>
                </tr>
                
                <tr>
                    <td class="label"><s:text name="pageForm.hidden" />&nbsp;</td>
                    <td class="field"><s:checkbox name="bean.hidden" />
                        <s:text name="pageForm.hidden.tip" />
                    </td>
                    <td class="description"></td>                            
                </tr>
                
                <tr>
                    <td class="label"><s:text name="pageForm.templateLanguage" />&nbsp;</td>
                    <td class="field">
                        <s:select name="bean.templateLanguage" list="templateLanguages" size="1" />
                    </td>
                    <td class="description"></td>
                </tr>
                
            </table>
            
        </div>
    </s:if>
    
</s:form>
