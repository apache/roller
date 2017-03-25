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

<p class="subtitle"><s:text name="configForm.subtitle"/></p>
<p><s:text name="configForm.prompt"/></p>


<s:form action="globalConfig!save" theme="bootstrap" cssClass="form-horizontal">

    <s:hidden name="salt"/>

    <s:iterator id="dg" value="globalConfigDef.displayGroups">

        <h2><s:text name="%{#dg.key}"/></h2>

        <s:iterator id="pd" value="#dg.propertyDefs">

            <%-- special condition for comment plugins --%>
            <s:if test="#pd.name == 'users.comments.plugins'">
                <s:checkboxlist label="%{getText(#pd.key)}" name="commentPlugins"
                                list="pluginsList" listKey="id" listValue="name"/>
            </s:if>

            <%-- special condition for front page blog --%>
            <s:elseif test="#pd.name == 'site.frontpage.weblog.handle'">
                <s:select name="%{#pd.name}" label="%{getText(#pd.key)}"
                          list="weblogs" listKey="handle" listValue="name"/>
            </s:elseif>

            <%-- "string" type means use a simple textbox --%>
            <s:elseif test="#pd.type == 'string'">
                <s:textfield name="%{#pd.name}" label="%{getText(#pd.key)}" size="35"
                             value="%{properties[#pd.name].value}"/>
            </s:elseif>

            <%-- "text" type means use a full textarea --%>
            <s:elseif test="#pd.type == 'text'">
                <s:textarea name="%{#pd.name}" label="%{getText(#pd.key)}" rows="#pd.rows" cols="#pd.cols"
                            value="%{properties[#pd.name].value}"/>
            </s:elseif>

            <%-- "boolean" type means use a checkbox --%>
            <s:elseif test="#pd.type == 'boolean'">
                <s:if test="properties[#pd.name].value == 'true'">
                    <s:checkbox name="%{#pd.name}" label="%{getText(#pd.key)}" checked="checked" />
                </s:if>
                <s:if test="properties[#pd.name].value == 'false'">
                    <s:checkbox name="%{#pd.name}" label="%{getText(#pd.key)}" />
                </s:if>
            </s:elseif>

            <s:elseif test="#pd.type == 'integer'">
                <div class="form-group ">
                    <label class="col-sm-3 control-label"
                        for='globalConfig_<s:property value="#pd.nameWithUnderbars" />'>
                        <s:text name="%{#pd.key}"/>
                    </label>
                    <div class="col-sm-9 controls">
                        <input type="number" name='<s:property value="#pd.name" />'
                            size="35" value="30" id='globalConfig_<s:property value="#pd.nameWithUnderbars" />'
                            class="form-control integer" onkeyup="formChanged()" />
                    </div>
                </div>
            </s:elseif>

            <s:elseif test="#pd.type == 'float'">
                <div class="form-group ">
                    <label class="col-sm-3 control-label"
                        for='globalConfig_<s:property value="#pd.nameWithUnderbars" />'>
                        <s:text name="%{#pd.key}"/>
                    </label>
                    <div class="col-sm-9 controls">
                        <input type="number" name='<s:property value="#pd.name" />'
                            size="35" value="30" id='globalConfig_<s:property value="#pd.nameWithUnderbars" />'
                            class="form-control float" onkeyup="formChanged()" />
                    </div>
                </div>
            </s:elseif>

            <%-- if it's something we don't understand then use textbox --%>
            <s:else>
                <s:textfield name="%{#pd.name}" label="%{getText(#pd.key)}" size="35"
                             value="%{properties[#pd.name].value}" />
            </s:else>

        </s:iterator>
    </s:iterator>

    <input id="saveButton" class="btn" type="submit" value="<s:text name="generic.save"/>"/>

</s:form>


<script type="text/javascript">

    function formChanged() {

        var saveBookmarkButton = $('#saveButton:first');

        var error = false;

        $("input").each( function() {

            var isInteger = $(this).hasClass("integer");

            if ( $(this).attr("type") == "number") {

                if ( isNaN( this.valueAsNumber )) {
                    $(this).css("background", "#FBB")
                    error = true;

                } else if ( isInteger && !Number.isInteger( this.valueAsNumber ) ) {
                    $(this).css("background", "#FBB")
                    error = true;

                } else {
                    $(this).css("background", "white")
                }
            }

        });

        saveBookmarkButton.attr("disabled", error );
    }

</script>

