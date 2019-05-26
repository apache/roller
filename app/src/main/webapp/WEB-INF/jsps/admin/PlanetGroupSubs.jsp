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

        
<%-- ================================================================== --%>
<%-- add/edit custom planet group form --%>


<%-- title for default planet group --%>
<s:if test="groupHandle == 'all'" >
    <p class="subtitle"><s:text name="planetGroupSubs.default.subtitle" /></p>
    <p><s:text name="planetGroupSubs.default.desc" /></p>
</s:if>

<%-- title for a custom planet group --%>
<s:else>
    <s:if test="createNew">
        <p class="subtitle">
            <s:text name="planetGroupSubs.custom.subtitle.new" />
        </p>
    </s:if>
    <s:else>
        <p class="subtitle">
            <s:text name="planetGroupSubs.custom.subtitle" >
                <s:param value="groupHandle" />
            </s:text>
        </p>
    </s:else>
    <p><s:text name="planetGroupSubs.custom.desc" /></p>
</s:else>


<%-- only show edit form for custom group --%>
<s:if test="groupHandle != 'all'">

    <div class="panel panel-default">
        <div class="panel-heading">
            <p><s:text name="planetGroupSubs.properties"/></p>
        </div>
        <div class="panel-body">
            <s:if test="createNew">
                <s:text name="planetGroupSubs.creatingNewGroup" />
            </s:if>
            <s:else>
                <s:text name="planetGroupSubs.editingExistingGroup" />
            </s:else>

            <s:form action="planetGroupSubs!saveGroup" theme="bootstrap" cssClass="form-horizontal" style="margin-top:1em">
                <s:hidden name="salt"/>
                <s:hidden name="group.id"/>

                <s:textfield name="group.title" size="40" maxlength="255"
                             onchange="validate()" onkeyup="validate()"
                             label="%{getText('planetGroups.title')}"
                             tooltip="%{getText('planetGroups.tip.title')}"/>

                <s:textfield name="group.handle" size="40" maxlength="255"
                             onchange="validate()" onkeyup="validate()"
                             label="%{getText('planetGroups.handle')}"
                             tooltip="%{getText('planetGroups.tip.handle')}"/>


                <div class="form-group ">
                    <label class="col-sm-3 control-label"></label>
                    <div class="col-sm-9 controls">
                        <s:submit value="%{getText('generic.save')}" cssClass="btn btn-default"/>
                        <s:if test="createNew">
                            <input type="button" class="btn"
                                   value='<s:text name="generic.cancel" />'
                                   onclick="window.location='<s:url action="planetGroups"/>'"/>
                        </s:if>
                    </div>
                </div>

            </s:form>

        </div>
    </div>

</s:if>


<%-- ================================================================== --%>
<%-- table of planet group's subscription  --%>

<s:if test="!createNew">

    <h3><s:text name="planetGroupSubs.subscriptions"/></h3>
    <s:text name="planetGroupSubs.subscriptionDesc" />

    <s:if test="%{subscriptions.isEmpty()}">
        <s:if test="groupHandle == 'all'">
            <s:text name="planetGroupSubs.noneDefinedDefault" />
        </s:if>
        <s:else>
            <s:text name="planetGroupSubs.noneDefinedCustom" />
        </s:else>
    </s:if>
    <s:else>

        <table class="table">
            <tr>
                <th width="30%"> <s:text name="planetGroupSubs.column.title"/> </th>
                <th width="55%"> <s:text name="planetGroupSubs.column.feedUrl"/> </th>
                <th width="15%"> <s:text name="generic.delete"/> </th>
            </tr>

            <s:iterator var="sub" value="subscriptions">
                <tr>
                    <td class="rollertable"><s:property value="#sub.title"/></td>
                    <td><s:set var="feedURL" value="#sub.feedURL"/> ${fn:substring(feedURL, 0, 100)} </td>
                    <td>
                        <a href="javascript: void(0);" onclick="confirmDelete('<s:property value="feedURL"/>')">
                            <span class="glyphicon glyphicon-remove" aria-hidden="true"> </span>
                            <s:text name="generic.delete"/>
                        </a>
                    </td>
                </tr>
            </s:iterator>
        </table>

        <%-- planet subscription delete logic --%>

        <s:form action="planetGroupSubs!deleteSubscription" id="deleteForm">
            <s:hidden name="salt"/>
            <s:hidden name="group.handle"/>
            <input type="hidden" name="subUrl"/>
        </s:form>

    </s:else>

</s:if>


<%-- ================================================================== --%>

<script>

    function confirmDelete(subUrl) {
        if (window.confirm('<s:text name="planetGroupSubs.delete.confirm" />')) {
            var form = $("#deleteForm");
            form.find('input[name="subUrl"]').val(subUrl);
            form.find('input[name="groupHandle"]').val('<s:property value="groupHandle" />');
            form.submit();
        }
    }

</script>