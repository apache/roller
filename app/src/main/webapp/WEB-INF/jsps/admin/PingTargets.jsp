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
                <a href="#" onclick="showAddEditModal('<s:property value="#pingTarget.id"/>',
                        '<s:property value="#pingTarget.name" />',
                        '<s:property value="#pingTarget.pingUrl" />'
                        )">
                    <span class="glyphicon glyphicon-edit" aria-hidden="true"> </span>
                </a>
            </td>

            <td class="rollertable" align="center">
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
    <a href="#" onclick="showAddEditModal()">
        <span class="glyphicon glyphicon-plus-sign" aria-hidden="true"> </span>
        <s:text name="pingTarget.addTarget"/>
    </a>
</div>


<%-- ================================================================================================ --%>

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
                    <button type="button" class="btn" data-dismiss="modal">
                        <s:text name="generic.cancel"/>
                    </button>
                </div>

            </s:form>

        </div>

    </div>

</div>


<%-- ================================================================================================ --%>

<%-- add/edit link form: a modal --%>

<div id="addedit-pingtarget-modal" class="modal fade addedit-pingtarget-modal" tabindex="-1" role="dialog">

    <div class="modal-dialog modal-lg">

        <div class="modal-content">

            <div class="modal-header">

                <s:if test="actionName == 'commonPingTargetEdit'">
                    <s:set var="subtitleKey">pingTargetEdit.subtitle</s:set>
                </s:if>
                <s:else>
                    <s:set var="subtitleKey">pingTargetAdd.subtitle</s:set>
                </s:else>

                <div class="modal-title">
                    <h3> <s:text name="%{#subtitleKey}"> </s:text> </h3>
                </div>

            </div> <%-- modal header --%>

            <div class="modal-body">

                <s:form id="pingTargetEditForm" theme="bootstrap" cssClass="form-horizontal">
                    <s:hidden name="bean.id"/>
                    <s:hidden name="salt"/>
                    <s:hidden name="actionName"/>

                    <s:textfield name="bean.name" size="30" maxlength="30" style="width:50%"
                                 onchange="validate()" onkeyup="validate()"
                                 label="%{getText('generic.name')}" />

                    <s:textfield name="bean.pingUrl" size="100" maxlength="255" style="width:50%"
                                 onchange="validate()" onkeyup="validate()"
                                 label="%{getText('pingTarget.pingUrl')}" />
                </s:form>

            </div> <%-- modal body --%>

            <div class="modal-footer">

                <p id="feedback-area-edit"></p>

                <button type="button" id="save_ping_target" onclick="savePingTarget()" class="btn btn-success">
                    <s:text name="generic.save"/>
                </button>

                <button type="button" class="btn" data-dismiss="modal">
                    <s:text name="generic.cancel"/>
                </button>

            </div> <%-- modal footer --%>

        </div> <%-- modal content --%>

    </div> <%-- modal dialog --%>

</div> <%-- modal --%>


<%-- page reload mechanism --%>
<s:form action="commonPingTargets!execute">
    <s:hidden name="salt"/>
    <s:hidden name="weblog"/>
</s:form>


<%-- ================================================================================================ --%>

<script>

    function showDeleteModal( removeId ) {
        $('#removeId').val(removeId);
        $('#delete-ping-target-modal').modal({show: true});
    }

    function showAddEditModal(pingTargetId, name, url) {
        if ( pingTargetId ) {
            $('#pingTargetEditForm_actionName:first').val("commonPingTargetEdit");
            $('#pingTargetEditForm_bean_id:first').val(pingTargetId);
            $('#pingTargetEditForm_bean_name:first').val(name);
            $('#pingTargetEditForm_bean_pingUrl:first').val(url);
        } else {
            $('#pingTargetEditForm_actionName:first').val("commonPingTargetAdd");
            $('#pingTargetEditForm_bean_name:first').val("");
            $('#pingTargetEditForm_bean_pingUrl:first').val("");
        }
        $('#addedit-pingtarget-modal').modal({show: true});
    }

    function validate() {
        var savePingTargetButton = $('#save-button:first');
        var name = $('#pingTargetEditForm_bean_name:first').val().trim();
        var url = $('#pingTargetEditForm_bean_pingUrl:first').val().trim();
        if ( name.length > 0 && url.length > 0 && isValidUrl(url) ) {
            savePingTargetButton.attr("disabled", false);
        } else {
            savePingTargetButton.attr("disabled", true);
        }
    }

    function isValidUrl(url) {
        if (/^(http|https|ftp):\/\/[a-z0-9]+([\-\.]{1}[a-z0-9]+)*\.[a-z]{2,5}(:[0-9]{1,5})?(\/.*)?$/i.test(url)) {
            return true;
        } else {
            return false;
        }
    }

    $( document ).ready(function() {
        var savePingTargetButton = $('#save-button:first');
        savePingTargetButton.attr("disabled", true);
    });

    function viewChanged() {
        var form = $("#commonPingTargets")[0];
        form.submit();
    }

    function savePingTarget() {

        var feedbackAreaEdit = $("#feedback-area-edit");

        var actionName = $('#pingTargetEditForm_actionName:first').val();

        // post ping target via AJAX
        $.ajax({
            method: 'post',
            url: actionName + ".rol#save",
            data: $("#pingTargetEditForm").serialize(),
            context: document.body

        }).done(function (data) {

            // kludge: scrape response status from HTML returned by Struts
            var alertEnd = data.indexOf("ALERT_END");
            var notUnique = data.indexOf("<s:text name='pingTarget.nameNotUnique' />");
            if (notUnique > 0 && notUnique < alertEnd) {
                feedbackAreaEdit.css("color", "red");
                feedbackAreaEdit.html('<s:text name="pingTarget.nameNotUnique" />');

            } else {
                feedbackAreaEdit.css("color", "green");
                feedbackAreaEdit.html('<s:text name="generic.success" />');
                $('#addedit-pingtarget-modal').modal("hide");

                // cause page to be reloaded so that edit appears
                viewChanged();
            }

        }).error(function (data) {
            feedbackAreaEdit.html('<s:text name="generic.error.check.logs" />');
            feedbackAreaEdit.css("color", "red");
        });
    }

</script>

