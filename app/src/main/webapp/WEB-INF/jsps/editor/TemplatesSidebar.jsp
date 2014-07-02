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
                <h3><s:text name="pagesForm.addNewPage" /></h3>
                <hr size="1" noshade="noshade" />
                
                <s:form action="templates!add">
					<s:hidden name="salt" />
					<s:hidden name="weblog" />
                    
                    <table cellpadding="0" cellspacing="6">
                        <tr>
                            <td><s:text name="generic.name"/></td>
                            <td><s:textfield name="newTmplName" /></td>
                        </tr>
                        
                        <s:if test="!availableActions.isEmpty" >
                            <tr>
                                <td><s:text name="pagesForm.action"/></td>
                                <td>
                                    <s:select name="newTmplAction" size="1" list="availableActions" />
                                </td>
                            </tr>
                        </s:if>
                        
                        <tr>
                            <td></td>
                            <td><s:submit value="%{getText('pagesForm.add')}" /></td>
                        </tr>
                    </table>
                    
                </s:form>
                <br />
                
            </div>
        </div>
    </div>
</div>	
