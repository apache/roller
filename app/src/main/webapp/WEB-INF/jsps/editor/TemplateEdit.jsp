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
<link rel="stylesheet" media="all" href='<s:url value="/tb-ui/jquery-ui-1.11.4/jquery-ui.min.css"/>' />
<script src='<s:url value="/tb-ui/scripts/jquery-2.2.3.min.js" />'></script>
<script src='<s:url value="/tb-ui/jquery-ui-1.11.4/jquery-ui.min.js"/>'></script>


<script>
  $(function() {
    $(".delete-link").click(function(e) {
      e.preventDefault();
      $('#confirm-delete').dialog('open');
    });

    $("#confirm-delete").dialog({
      autoOpen: false,
      resizable: true,
      height:200,
      modal: true,
      buttons: {
        "<s:text name='generic.delete'/>": function() {
          document.templateEdit.action = "<s:url action='templateEdit!delete' />";
          document.templateEdit.submit();
          $( this ).dialog( "close" );
        },
        Cancel: function() {
          $( this ).dialog( "close" );
        }
      }
    });

    $( "#template-code-tabs" ).tabs();
  });
</script>

<p class="subtitle">
   <s:text name="templateEdit.subtitle" >
       <s:param value="bean.name" />
       <s:param value="actionWeblog.handle" />
   </s:text>
</p>

<s:if test="template.required">
    <p class="pagetip"><s:text name="templateEdit.tip.required" /></p>
</s:if>
<s:else>
    <p class="pagetip"><s:text name="templateEdit.tip" /></p>
</s:else>
                
<s:form action="templateEdit!save">
    <sec:csrfInput/>
    <s:hidden name="weblogId" />
    <s:hidden name="bean.id"/>
    <s:hidden name="bean.derivation"/>

    <%-- ================================================================== --%>
    <%-- Name, link and description: disabled when page is a required page --%>
    
    <table cellspacing="5">
        <tr>
            <td class="label"><s:text name="generic.name" />&nbsp;</td>
            <td class="field">
                <s:if test="nameChangeable">
                    <s:textfield name="bean.name" size="50" maxlength="255"/>
                </s:if>
                <s:else>
                    <s:textfield name="bean.name" size="50" readonly="true" cssStyle="background: #e5e5e5" />
                </s:else>
            </td>
        </tr>
        
        <tr>
            <td class="label"><s:text name="templateEdit.role" />&nbsp;</td>
            <td class="field">
                 <s:textfield name="bean.role" size="50" readonly="true" cssStyle="background: #e5e5e5" />
            </td>
        </tr>
        
       <s:if test="bean.role.accessibleViaUrl">
            <tr>
                <td class="label" valign="top"><s:text name="templateEdit.path" />&nbsp;</td>
                <td class="field">
                    <s:textfield name="bean.relativePath" size="50" maxlength="255" onkeyup="updatePageURLDisplay()" />
                    <br/>
                    <s:property value="actionWeblog.absoluteURL" />page/<span id="linkPreview" style="color:red"><s:property value="bean.relativePath" /></span>
                    <s:if test="template.relativePath != null">
                        [<a id="launchLink" onClick="launchPage()"><s:text name="templateEdit.launch" /></a>]
                    </s:if>
                </td>
            </tr>
        </s:if>

        <s:if test="!template.role.singleton">
            <tr>
                <td class="label" valign="top" style="padding-top: 4px">
                    <s:text name="generic.description"/>&nbsp;</td>
                <td class="field">
                    <s:textarea name="bean.description" cols="50" rows="2" maxlength="255"/>
                </td>
            </tr>
        </s:if>

        <tr>
            <td class="label"><s:text name="templateEdit.templateLanguage" />&nbsp;</td>
            <td class="field">
                <s:select name="bean.templateLanguage" list="templateLanguages" size="1" />
            </td>
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
                <input type="button" value='<s:text name="generic.cancel"/>'
                    onclick="window.location='<s:url action="templates"><s:param name="weblog" value="%{weblog}"/></s:url>'" />
                <s:if test="template != null && template.id != null">
                    <s:submit class="delete-link" value="%{getText('templateEdit.delete')}"/>
                </s:if>
            </td>
        </tr>
    </table>

</s:form>

<div id="confirm-delete" title="<s:text name='generic.confirm'/>" style="display:none">
   <p><span class="ui-icon ui-icon-alert" style="float:left; margin:0 7px 20px 0;"></span><s:text name="templateEdit.confirmDelete"/></p>
</div>

<script>
var weblogURL = '<s:property value="actionWeblog.absoluteURL" />';
var originalLink = '<s:property value="bean.relativePath" />';
var type = '<s:property value="bean.type" /> ' ;

// Update page URL when user changes link
function updatePageURLDisplay() {
    var previewSpan = document.getElementById('linkPreview');
    var n1 = previewSpan.firstChild;
    var n2 = document.createTextNode(document.getElementById('templateEdit_bean_relativePath').value);
    if (n1 == null) {
        previewSpan.appendChild(n2);
    } else {
        previewSpan.replaceChild(n2, n1);
    }           
}
// Don't launch page if user has changed link, it'll be a 404
function launchPage() {
    if (originalLink != document.getElementById('templateEdit_bean_relativePath').value) {
        window.alert("Link changed, please save before launching");
    } else {
        window.open(weblogURL + 'page/' + originalLink, '_blank');
    }
}
</script>

 <script src="<s:url value='/tb-ui/scripts/jquery-2.2.3.min.js'></s:url>"></script>
 <script src="<s:url value='/tb-ui/jquery-ui-1.11.4/jquery-ui.min.js'></s:url>"></script>

 <script>
     $(function() {
         $( "#template-code-tabs" ).tabs();
     });
 </script>
