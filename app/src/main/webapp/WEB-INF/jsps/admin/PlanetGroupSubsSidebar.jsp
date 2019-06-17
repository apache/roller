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
<%-- add new planet feed subscription --%>

<s:if test="!createNew">

    <h3><s:text name="mainPage.actions"/></h3>
    <hr size="1" noshade="noshade"/>

    <s:text name="planetGroupSubs.addFeed"/>

    <s:form action="planetGroupSubs!saveSubscription"
            theme="bootstrap" cssClass="form-horizontal" style="margin-top:1em">
        <s:hidden name="salt"/>
        <s:hidden name="group.handle"/>

        <s:textfield name="subUrl" size="40" maxlength="255" label="%{getText('planetSubscription.feedUrl')}"
            onchange="validateUrl()" onkeyup="validateUrl()" />

        <p id="feedback-area" style="clear:right; width:100%"></p>

        <s:submit value="%{getText('generic.save')}" cssClass="btn btn-default" />

    </s:form>

    <script>

        function validateUrl() {
            var feedbackArea = $("#feedback-area");
            var url = $("#planetGroupSubs_subUrl").val();
            var saveButton = $('#planetGroupSubs_0');

            if (url && url.trim() !== '') {
                if (!isValidUrl(url)) {
                    saveButton.attr("disabled", true);
                    feedbackArea.html('<s:text name="planetGroupSubs.badFeedURL" />');
                    feedbackArea.css("color", "red");
                    return;
                }
            }

            feedbackArea.html('');
            saveButton.attr("disabled", false);
        }

        $( document ).ready(function() {
            var saveButton = $('#planetGroupSubs_0');
            saveButton.attr("disabled", true);
        });
    </script>

</s:if>

