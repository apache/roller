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
    <s:text name="pagesForm.subtitle">
        <s:param value="actionWeblog.handle"/>
    </s:text>
</p>
<p class="pagetip">
    <s:text name="pagesForm.tip"/>
</p>

<s:if test="actionWeblog.editorTheme != 'custom'">
    <p><s:text name="pagesForm.themesReminder"><s:param value="actionWeblog.editorTheme"/></s:text></p>
</s:if>

<s:form action="templates!remove" theme="bootstrap" cssClass="form-horizontal">
    <s:hidden name="salt"/>
    <s:hidden name="weblog" value="%{actionWeblog.handle}"/>
    <s:hidden name="removeId" id="removeId"/>

    <table class="table table-striped"> <%-- of weblog templates --%>

        <s:if test="!templates.isEmpty">

            <tr>
                <th width="30%"><s:text name="generic.name"/></th>
                <th width="10"><s:text name="pagesForm.action"/></th>
                <th width="55%"><s:text name="generic.description"/></th>
                <th width="10"><s:text name="pagesForm.remove"/></th>
            </tr>

            <s:iterator var="p" value="templates" status="rowstatus">
                <tr>

                    <td style="vertical-align:middle">
                        <s:if test="! #p.hidden">
                            <img src='<s:url value="/images/page_white.png"/>' border="0" alt="icon"/>
                        </s:if>
                        <s:else>
                            <img src='<s:url value="/images/page_white_gear.png"/>' border="0" alt="icon"/>
                        </s:else>
                        <s:url var="edit" action="templateEdit">
                            <s:param name="weblog" value="actionWeblog.handle"/>
                            <s:param name="bean.id" value="#p.id"/>
                        </s:url>
                        <s:a href="%{edit}"><s:property value="#p.name"/></s:a>
                    </td>

                    <td style="vertical-align:middle"><s:property value="#p.action.readableName"/></td>

                    <td style="vertical-align:middle"><s:property value="#p.description"/></td>

                    <td class="center" style="vertical-align:middle">
                        <s:if test="!#p.required || !customTheme">
                            <s:url var="removeUrl" action="templateRemove">
                                <s:param name="weblog" value="actionWeblog.handle"/>
                                <s:param name="removeId" value="#p.id"/>
                            </s:url>
                            <a href="#" onclick=
                                    "confirmTemplateDelete('<s:property value="#p.id"/>', '<s:property value="#p.name"/>' )">
                                <span class="glyphicon glyphicon-trash"></span>
                            </a>

                        </s:if>
                        <s:else>
                            <span class="glyphicon glyphicon-lock"></span>
                        </s:else>
                    </td>

                </tr>
            </s:iterator>

        </s:if>
        <s:else>
            <tr class="rollertable_odd">
                <td style="vertical-align:middle" colspan="5">
                    <s:text name="pageForm.notemplates"/>
                </td>
            </tr>
        </s:else>
    </table>

</s:form>


<script>
    function confirmTemplateDelete(templateId, templateName) {
        $('#removeId').val(templateId);
        if (window.confirm('<s:text name="pageRemove.confirm"/>: \'' + templateName + '\'?')) {
            document.getElementById("templates").submit();
        }
    }
</script>
