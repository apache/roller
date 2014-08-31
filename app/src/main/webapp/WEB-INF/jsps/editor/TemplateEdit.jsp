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
	<s:hidden name="salt" />
    <s:hidden name="weblog" />
    <s:hidden name="bean.id"/>
    <s:hidden name ="bean.type"/>
    
    <%-- ================================================================== --%>
    <%-- Name, link and description: disabled when page is a required page --%>
    
    <table cellspacing="5">
        <tr>
            <td class="label"><s:text name="generic.name" />&nbsp;</td>
            <td class="field">
                <s:if test="template.required || bean.mobile">
                    <s:textfield name="bean.name" size="50" readonly="true" cssStyle="background: #e5e5e5" />
                </s:if>
                <s:else>
                    <s:textfield name="bean.name" size="50"/>
                </s:else>
            </td>
            <td class="description"></td>
        </tr>
        
        <tr>
            <td class="label"><s:text name="pageForm.action" />&nbsp;</td>
            <td class="field">
                 <s:textfield name="bean.action" size="50" readonly="true" cssStyle="background: #e5e5e5" />
            </td>
            <td class="description"></td>
        </tr>
        
       <s:if test="!template.required && template.custom">
            <tr>
                <td class="label" valign="top"><s:text name="pageForm.link" />&nbsp;</td>
                <td class="field">
                    <s:textfield name="bean.link" size="50" onkeyup="updatePageURLDisplay()" />
                    <br />
                    <s:property value="actionWeblog.absoluteURL" />page/<span id="linkPreview" style="color:red"><s:property value="bean.link" /></span>
                    <s:if test="template.link != null">
                        [<a id="launchLink" onClick="launchPage()"><s:text name="pageForm.launch" /></a>]
                    </s:if>
                </td>
                <td class="description"></td>
            </tr>
        </s:if>
        <tr>
            <td class="label" valign="top" style="padding-top: 4px">
                <s:text name="generic.description" />&nbsp;</td>
            <td class="field">
                <s:if test="template.required">
                    <s:textarea name="bean.description" cols="50" rows="2" 
                        readonly="true" cssStyle="background: #e5e5e5" />
                </s:if>
                <s:else>
                    <s:textarea name="bean.description" cols="50" rows="2" />
                </s:else>
            </td>
            <td class="description"></td>
        </tr>
        
    </table>

    <%-- ================================================================== --%>
    <%-- Tabs for each of the two content areas: Standard and Mobile --%>

    <div id="template-code-tabs">
    <ul>
        <li class="selected"><a href="#tabStandard"><em>Standard</em></a></li>
        <s:if test="bean.contentsMobile != null">
            <li><a href="#tabMobile"><em>Mobile</em></a></li>
        </s:if>
    </ul>
    <div>
        <div id="tabStandard">
            <s:textarea name="bean.contentsStandard" cols="80" rows="30" cssStyle="width:100%" />
        </div>
        <s:if test="bean.contentsMobile != null">
            <div id="tabMobile">
                <s:textarea name="bean.contentsMobile" cols="80" rows="30" cssStyle="width:100%" />
            </div>
        </s:if>
    </div>
    </div>

    <%-- ================================================================== --%>
    <%-- Save, Close and Resize text area buttons--%>

    <table style="width:100%">
        <tr>
            <td>
                <s:submit value="%{getText('generic.save')}" />
                <input type="button" value='<s:text name="generic.done"/>'
                    onclick="window.location='<s:url action="templates"><s:param name="weblog" value="%{weblog}"/></s:url>'" />
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
                        <script><!--
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
                            <input type="radio" name="bean.autoContentType" 
                                   value="true" checked="true" 
                                   onchange="showContentTypeField()" 
                                   id="template_bean_autoContentType1"/> 
                            <s:text name="pageForm.useAutoContentType" /><br />
                            
                            <input type="radio" name="bean.autoContentType" 
                                   value="false" 
                                   onchange="showContentTypeField()" 
                                   id="template_bean_autoContentType2"/>
                            <s:text name="pageForm.useManualContentType" />
                            <s:textfield name="bean.manualContentType" />
                        </s:if>
                        <s:else>
                            <input type="radio" name="bean.autoContentType" 
                                   value="true" 
                                   onchange="showContentTypeField()" 
                                   id="template_bean_autoContentType1"/> 
                            <s:text name="pageForm.useAutoContentType" /><br />
                            
                            <input type="radio" name="bean.autoContentType" 
                                   value="false" 
                                   checked="true" 
                                   onchange="showContentTypeField()" 
                                   id="template_bean_autoContentType2"/>
                            <s:text name="pageForm.useManualContentType" />
                            <s:textfield name="bean.manualContentType" />
                        </s:else>
                        
                        <br />
                        <br />
                        
                        <script><!--
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


<script>
var weblogURL = '<s:property value="actionWeblog.absoluteURL" />';
var originalLink = '<s:property value="bean.link" />';
var type = '<s:property value="bean.type" /> ' ;

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
        window.open(weblogURL + 'page/' + originalLink+'?type='+type, '_blank');
    }
}
//Get cookie to determine state of control
if (getCookie('control_advancedControl') != null) {
    if(getCookie('control_advancedControl') == 'true'){
        toggle('advancedControl');
        togglePlusMinus('iadvancedControl');
    }
}
</script>

 <script src="<s:url value='/roller-ui/scripts/jquery-2.1.1.min.js'></s:url>"></script>
 <script src="<s:url value='/roller-ui/jquery-ui-1.11.0/jquery-ui.min.js'></s:url>"></script>

 <script>
     $(function() {
         $( "#template-code-tabs" ).tabs();
     });
 </script>
