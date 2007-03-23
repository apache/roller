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
<%@ include file="/WEB-INF/jsps/taglibs.jsp" %>

<roller:StatusMessage/>

<p class="subtitle">
   <fmt:message key="pageForm.subtitle" >
       <fmt:param value="${model.page.name}" />
       <fmt:param value="${model.website.handle}" />
   </fmt:message>
</p>  
<c:choose>
    <c:when test="${model.page.required}">
        <p class="pagetip"><fmt:message key="pageForm.tip.required" /></p>
    </c:when>
    <c:otherwise>
        <p class="pagetip"><fmt:message key="pageForm.tip" /></p>
    </c:otherwise>
</c:choose>
                
<html:form action="/roller-ui/authoring/page" method="post">
    <html:hidden property="id"/>
    <html:hidden property="decoratorName" />
    <html:hidden property="required" />
    <input type="hidden" name="method" value="update" />
    <input type="hidden" name="weblog" value='<c:out value="${model.website.handle}" />' />
    
    <%-- ================================================================== --%>
    <%-- Name, link and desription: disabled when page is a required page --%>
    
    <table cellspacing="5">
        <tr>
            <td class="label"><fmt:message key="pageForm.name" />&nbsp;</td>
            <td class="field">
                <c:choose>
                    <c:when test="${model.page.required}">
                        <html:text style="background: #e5e5e5" property="name" size="50" readonly="true"/>
                    </c:when>
                    <c:otherwise>
                        <html:text property="name" size="50"/>
                    </c:otherwise>
                </c:choose>
            </td>
            <td class="description"></td>
        </tr>
        
        <script type="text/javascript">
        <!--
        var weblogURL = '<c:out value="${model.website.absoluteURL}" />';
        var originalLink = '<c:out value="${model.page.link}" />';
        
        // Update page URL when user changes link
        function updatePageURLDisplay() {
            var previewSpan = document.getElementById("handlePreview");
            var n1 = previewSpan.firstChild;
            var n2 = document.createTextNode(document.weblogTemplateFormEx.link.value);
            if (n1 == null) {
                previewSpan.appendChild(n2);
            } else {
                previewSpan.replaceChild(n2, n1);
            }           
        }
        // Don't launch page if user has changed link, it'll be a 404
        function launchPage() {
            if (originalLink != document.weblogTemplateFormEx.link.value) {
                window.alert("Link changed, not launching page");
            } else {
                window.open(weblogURL + '/page/' + originalLink, '_blank');
            }
        }
        // Only highlight launch link user hasn't changed link
        function highlightLaunchLink() {
            if (originalLink == document.weblogTemplateFormEx.link.value) {
                document.getElementById('launchLink').style.textDecoration = 'underline';
            }
        }
        function unhighlightLaunchLink() {
                document.getElementById('launchLink').style.textDecoration = 'none';
        }
        -->
        </script>

        <tr>
            <td class="label" valign="top"><fmt:message key="pageForm.link" />&nbsp;</td>
            <td class="field">
                <c:choose>
                    <c:when test="${model.page.required}">
                        <html:text style="background: #e5e5e5" property="link" size="50" readonly="true"/>
                    </c:when>
                    <c:otherwise>
                        <html:text property="link" size="50" onkeyup="updatePageURLDisplay()" />
                    </c:otherwise>
                </c:choose>
                <br />
                <c:out value="${model.website.absoluteURL}" />/page/<span id="handlePreview" style="color:red"><c:out value="${model.page.link}" /></span>
                [<span id="launchLink" class="fakelink" 
                    onClick="launchPage()" onMouseOver="highlightLaunchLink()" onMouseOut="unhighlightLaunchLink()"><fmt:message key="pageForm.launch" /></span>]
            </td>
            <td class="description"></td>
        </tr>
        
        <tr>
            <td class="label" valign="top" style="padding-top: 4px"><fmt:message key="pageForm.description" />&nbsp;</td>
            <td class="field">
                <c:choose>
                    <c:when test="${model.page.required}">
                        <html:textarea style="background: #e5e5e5" property="description" cols="50" rows="2" readonly="true"/>
                    </c:when>
                    <c:otherwise>
                        <html:textarea property="description" cols="50" rows="2" />
                    </c:otherwise>
                </c:choose>
            </td>
            <td class="description"></td>
        </tr>
        
    </table>
    
    <%-- ================================================================== --%>
    <%-- Template editing area w/resize buttons --%>
    
    <br />
    <html:textarea property="contents" cols="80" rows="30" style="width: 100%" />
    
    <script type="text/javascript"><!--
        if (getCookie("editorSize1") != null) {
            document.weblogTemplateFormEx.contents.rows = getCookie("editorSize1");
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
    <table style="width:100%"><tr><td align="right">
        <!-- Add buttons to make this textarea taller or shorter -->
        <input type="button" name="taller" value=" &darr; " 
            onclick="changeSize1(document.weblogTemplateFormEx.contents, 5)" />
        <input type="button" name="shorter" value=" &uarr; " 
            onclick="changeSize1(document.weblogTemplateFormEx.contents, -5)" />
    </td></tr></table>
   
    
    <%-- ================================================================== --%>
    <%-- Advanced settings inside a control toggle --%>
        
    <br />
    <div id="advancedControlToggle" class="controlToggle">
        <span id="iadvancedControl">+</span>
        <a class="controlToggle" onclick="javascript:toggleControl('advancedControlToggle','advancedControl')">
        <fmt:message key="pageForm.advancedSettings" /></a>
    </div>
    <div id="advancedControl" class="advancedControl" style="display:none">
        
        <table cellspacing="0">
            
            <tr>
                <script type="text/javascript"><!--
                    function showContentTypeField() {
                        if (document.weblogTemplateFormEx.autoContentType[0].checked) {
                            document.weblogTemplateFormEx.manualContentType.readOnly = true;
                            document.weblogTemplateFormEx.manualContentType.style.background = '#e5e5e5';
                        } else {
                            document.weblogTemplateFormEx.manualContentType.readOnly = false;
                            document.weblogTemplateFormEx.manualContentType.style.background = '#ffffff';
                        }
                    }
                // --></script> 
                <td class="field">                
                    <tr>
                        <td class="label" valign="top"><fmt:message key="pageForm.outputContentType" />&nbsp;</td>
                        <td class="field">
                                                        
                            <html:radio property="autoContentType" value="true" onchange="showContentTypeField()" /> 
                            <fmt:message key="pageForm.useAutoContentType" /><br />
                            
                            <html:radio property="autoContentType" value="false" onchange="showContentTypeField()" />
                            <fmt:message key="pageForm.useManualContentType" />
                            <html:text property="manualContentType" />   
                            
                            <br />
                            <br />
                                               
                        </td>
                        <td class="description"></td>
                    </tr>
                </td>
                <td class="description"></td>
                <script type="text/javascript"><!--
                    showContentTypeField();
                // --></script> 
            </tr>
            
            <tr>
                <td class="field">                
                    <c:choose>
                        <c:when test="${model.page.required}">
                            <html:hidden property="navbar" />
                        </c:when>
                        <c:otherwise>
                            <tr>
                                <td class="label"><fmt:message key="pageForm.navbar" />&nbsp;</td>
                                <td class="field"><html:checkbox property="navbar" /> 
                                    <fmt:message key="pageForm.navbar.tip" />
                                </td>
                                <td class="description"></td>
                            </tr>
                        </c:otherwise>
                    </c:choose>
                </td>
                <td class="description"></td>
            </tr>
            
            <td class="field">                
                <c:choose>
                    <c:when test="${model.page.required}">
                        <html:hidden property="hidden" />
                    </c:when>
                    <c:otherwise>
                        <tr>
                            <td class="label"><fmt:message key="pageForm.hidden" />&nbsp;</td>
                            <td class="field"><html:checkbox property="hidden" />
                                <fmt:message key="pageForm.hidden.tip" />
                            </td>
                            <td class="description"></td>                            
                        </tr>
                    </c:otherwise>
                </c:choose>
                <br />
                <br />

            </td>
            <td class="description"></td> 
            </tr>
                        
            <tr>
                <td class="field">                
                    <c:choose>
                        <c:when test="${model.page.required || !model.rollerSession.globalAdminUser}">
                            <html:hidden property="templateLanguage" />
                        </c:when>
                        <c:otherwise>
                            <tr>
                                <td class="label"><fmt:message key="pageForm.templateLanguage" />&nbsp;</td>
                                <td class="field">
                                    <html:select property="templateLanguage" size="1" >
                                        <html:optionsCollection name="model" property="languages" value="value" label="label"  />
                                    </html:select>
                                </td>
                                <td class="description"></td>
                            </tr>
                        </c:otherwise>
                    </c:choose>
                </td>
                <td class="description"></td>
            </tr>
            
            <tr>
                <td class="field">
                            <tr>
                                <td class="label"><fmt:message key="pageForm.action" />&nbsp;</td>
                                <td class="field">
                                    <html:text property="action" size="30" />
                                </td>
                                <td class="description"></td>
                            </tr>
                </td>
                <td class="description"></td>
            </tr>
                        
        </table>
        <br />
        
    </div>
    
    <script type="text/javascript">
    <!--
    function cancel() {
        document.weblogTemplateFormEx.method.value="cancel"; 
        document.weblogTemplateFormEx.submit();
    }
    -->
    </script>
    <br />
    <input type="submit" value='<fmt:message key="pageForm.save" />' /></input>
    <input type="button" value='<fmt:message key="application.done" />' onclick="cancel()" /></input>
    
</html:form>

<%--
Added by Matt Raible since the focus javascript generated by Struts 
doesn't seem to work for forms with duplicate named elements.
--%>
<script type="text/javascript">
<!--
    document.forms[0].elements[0].focus();
// -->
</script>



