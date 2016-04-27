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

  Source file modified from the original ASF source; all changes made
  are also under Apache License.
--%>
<%@ include file="/WEB-INF/jsps/taglibs-struts2.jsp" %>
<link rel="stylesheet" media="all" href='<s:url value="/tb-ui/jquery-ui-1.11.0/jquery-ui.min.css"/>' />
<script src="<s:url value="/tb-ui/scripts/jquery-2.1.1.min.js" />"></script>
<script src='<s:url value="/tb-ui/jquery-ui-1.11.0/jquery-ui.min.js"/>'></script>

<script>
  $(function() {
    $(".delete-link").click(function(e) {
      e.preventDefault();
      $('#confirm-delete').data('sub',  $(this).attr("data-feedURL")).dialog('open');
    });

    $("#confirm-delete").dialog({
      autoOpen: false,
      resizable: false,
      height:170,
      modal: true,
      buttons: {
        "<s:text name='generic.delete'/>": function() {
          document.location.href='<s:url action="planetSubscriptions!delete" />?planetHandle=<s:property value="planetHandle"/>&subUrl='+encodeURIComponent($(this).data('sub'));
          $( this ).dialog( "close" );
        },
        Cancel: function() {
          $( this ).dialog( "close" );
        }
      }
    });
  });
</script>
        
      
<p class="subtitle">
    <s:text name="planetSubscriptions.subtitle.add" >
        <s:param value="planetHandle" />
    </s:text>
</p>
<p><s:text name="planetSubscriptions.prompt.add" /></p>

<s:form action="planetSubscriptions!save">
	<s:hidden name="salt" />
    <s:hidden name="planetHandle" />
    
    <div class="formrow">
        <label for="feedURL" class="formrow" /><s:text name="planetSubscription.feedUrl" /></label>
        <s:textfield name="subUrl" size="40" maxlength="255" onBlur="this.value=this.value.trim()"/>
        &nbsp;<s:submit value="%{getText('generic.save')}" />
    </div>
</s:form>

<br style="clear:left" />

<h2>
    <s:text name="planetSubscriptions.existingTitle" />
    <s:if test="planetHandle != 'all'" >
        &nbsp;[planet: <s:property value="planetHandle" />]
    </s:if>
</h2>

<table class="rollertable">
    <tr class="rHeaderTr">
        <th class="rollertable" width="30%">
            <s:text name="planetSubscriptions.column.title" />
        </th>
        <th class="rollertable" width="60%">
            <s:text name="planetSubscriptions.column.feedUrl" />
        </th>
        <th class="rollertable" width="10%">
            <s:text name="generic.delete" />
        </th>
    </tr>
    <s:iterator id="sub" value="subscriptions" status="rowstatus">
        <s:if test="#rowstatus.odd == true">
            <tr class="rollertable_odd">
        </s:if>
        <s:else>
            <tr class="rollertable_even">
        </s:else>
        
        <td class="rollertable">
            <s:property value="#sub.title" />
        </td>
        
        <td class="rollertable">
            <str:truncateNicely lower="70" upper="100" ><s:property value="#sub.feedURL" /></str:truncateNicely>
        </td>
        
        <td class="rollertable">
            <img src='<s:url value="/images/delete.png"/>' />
            <a class="delete-link" data-feedURL="<s:property value='#sub.feedURL'/>">
                <s:text name="generic.delete"/>
            </a>
        </td>       
        
        </tr>
    </s:iterator>
</table>

<div id="confirm-delete" title="<s:text name='generic.confirm'/>" style="display:none">
   <p><span class="ui-icon ui-icon-alert" style="float:left; margin:0 7px 20px 0;"></span><s:text name="planetSubscriptions.delete.confirm"/></p>
</div>
