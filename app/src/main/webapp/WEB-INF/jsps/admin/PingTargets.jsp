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

<p class="subtitle"><s:text name="commonPingTargets.subtitle"/></p>

<p><s:text name="commonPingTargets.explanation"/></p>

<table class="rollertable table table-striped">

    <%-- Headings --%>
    <tr class="rollertable">
        <th class="rollertable" width="20%%"><s:text name="generic.name"/></th>
        <th class="rollertable" width="55%"><s:text name="pingTarget.pingUrl"/></th>
        <th class="rollertable" width="15%" colspan="2"><s:text name="pingTarget.autoEnabled"/></th>
        <th class="rollertable" width="5%"><s:text name="generic.edit"/></th>
        <th class="rollertable" width="5%"><s:text name="pingTarget.remove"/></th>
    </tr>

    <%-- Listing of current common targets --%>
    <s:iterator var="pingTarget" value="pingTargets" status="rowstatus">

        <tr class="rollertable_odd">

            <td class="rollertable"><s:property value="#pingTarget.name"/></td>

            <td class="rollertable"><s:property value="#pingTarget.pingUrl"/></td>

            <td class="rollertable" align="center">
                <s:if test="#pingTarget.autoEnabled">
                    <span style="color: #00aa00; font-weight: bold;"><s:text name="pingTarget.enabled"/></span>&nbsp;
                </s:if>
                <s:else>
                    <span style="color: #aaaaaa; font-weight: bold;"><s:text name="pingTarget.disabled"/></span>&nbsp;
                </s:else>
            </td>

            <td class="rollertable" align="center">
                <s:if test="#pingTarget.autoEnabled">
                    <s:url var="disablePing" action="commonPingTargets!disable">
                        <s:param name="pingTargetId" value="#pingTarget.id"/>
                    </s:url>
                    <s:a href="%{disablePing}">
                        <s:text name="pingTarget.disable"/>
                    </s:a>
                </s:if>
                <s:else>
                    <s:url var="enablePing" action="commonPingTargets!enable">
                        <s:param name="pingTargetId" value="#pingTarget.id"/>
                    </s:url>
                    <s:a href="%{enablePing}">
                        <s:text name="pingTarget.enable"/></s:a>
                </s:else>
            </td>

            <td class="rollertable" align="center">
                <s:url var="editPing" action="commonPingTargetEdit">
                    <s:param name="bean.id" value="#pingTarget.id"/>
                </s:url>
                <s:a href="%{editPing}">
                    <span class="glyphicon glyphicon-edit" aria-hidden="true"> </span>
                </s:a>
            </td>

            <td class="rollertable" align="center">
                <s:url var="removePing" action="commonPingTargets!deleteConfirm">
                    <s:param name="pingTargetId" value="#pingTarget.id"/>
                </s:url>
                <a href="#" onclick="showDeleteModal('<s:property value="#pingTarget.id"/>')">
                    <span class="glyphicon glyphicon-trash" aria-hidden="true"> </span>
                </a>
            </td>

        </tr>
    </s:iterator>

</table>

<div style="padding: 4px; font-weight: bold;">
    <s:url var="addPing" action="commonPingTargetAdd">
        <s:param name="weblog" value="actionWeblog.handle"/>
    </s:url>
    <s:a href="%{addPing}">
        <span class="glyphicon glyphicon-plus-sign" aria-hidden="true"> </span>
        <s:text name="pingTarget.addTarget"/>
    </s:a>
</div>


<div id="delete-ping-target-modal" class="modal fade ping-target-modal" tabindex="-1" role="dialog">

    <div class="modal-dialog modal-lg">

        <div class="modal-content">

            <s:form theme="bootstrap" cssClass="form-horizontal">
                <s:hidden name="salt"/>
                <s:hidden id="removeId" name="pingTargetId"/>

                <div class="modal-header">
                    <div class="modal-title">
                        <h3><s:text name="pingTarget.confirmRemoveTitle"/></h3>
                    </div>
                </div>

                <div class="modal-body">
                    <s:text name="pingTarget.confirmCommonRemove"/>
                </div>

                <div class="modal-footer">
                    <s:submit cssClass="btn btn-danger"
                        value="%{getText('generic.yes')}" action="commonPingTargets!delete"/>
                    <s:submit cssClass="btn btn-default"
                        value="%{getText('generic.cancel')}" action="commonPingTargets"/>
                </div>

            </s:form>

        </div>

    </div>

</div>


<script>
    function showDeleteModal( removeId ) {
        $('#removeId').val(removeId);
        $('#delete-ping-target-modal').modal({show: true});
    }
</script>

