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

<p class="subtitle"><s:text name="planetGroups.subtitle"/></p>


<%-- ================================================================== --%>
<%-- table of custom planet groups (excluding the default group) --%>

<s:if test="%{!groups.isEmpty()}">

    <table class="table">

        <tr>
            <th width="50%"> <s:text name="planetGroups.column.title"/> </th>
            <th width="20%"> <s:text name="planetGroups.column.handle"/> </th>
            <th width="15%"> <s:text name="generic.edit"/> </th>
            <th width="15%"> <s:text name="generic.delete"/> </th>
        </tr>

        <s:iterator var="group" value="groups">
            <tr>
                <td> <s:property value="#group.title"/> </td>
                <td> <s:property value="#group.handle"/> </td>

                <td>
                    <s:url var="groupUrl" action="planetGroupSubs">
                        <s:param name="group.id" value="#group.id"/>
                    </s:url>
                    <s:a href="%{groupUrl}">
                        <span class="glyphicon glyphicon-edit" aria-hidden="true"></span>
                        <s:text name='generic.edit'/>
                    </s:a>
                </td>

                <td>
                    <a href="javascript: void(0);" onclick="confirmDelete('<s:property value="#group.handle"/>')">
                        <span class="glyphicon glyphicon-remove" aria-hidden="true"> </span>
                        <s:text name="generic.delete"/>
                    </a>
                </td>

            </tr>
        </s:iterator>

    </table>

    <%-- planet group delete logic --%>

    <s:form action="planetGroups!delete" id="deleteForm">
        <input type="hidden" name="salt" value='<s:property value="salt" />' />
        <input type="hidden" name="group.handle"/>
    </s:form>

    <script>
        function confirmDelete(groupHandle) {
            if (window.confirm('<s:text name="planetGroups.delete.confirm" />')) {
                var form = $("#deleteForm");
                form.find('input[name="group.handle"]').val(groupHandle);
                form.submit();
            }
        }
    </script>

</s:if>
<s:else>
    <s:text name="planetGroups.noneDefined"/>
</s:else>

