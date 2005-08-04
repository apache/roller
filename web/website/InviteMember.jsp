<%@ include file="/taglibs.jsp" %><%@ include file="/theme/header.jsp" %>
<script type="text/javascript">
// <!--
function createRequestObject() {
    var ro;
    var browser = navigator.appName;
    if (browser == "Microsoft Internet Explorer") {
        ro = new ActiveXObject("Microsoft.XMLHTTP");
    } else {
        ro = new XMLHttpRequest();
    }
    return ro;
}
var http = createRequestObject();
var init = false;
var userURL = "<%= request.getContextPath() %>" + "/userdata";

function onUserNameFocus() {
    if (!init) {
        init = true;
        sendUserRequest(userURL);
    }
}
function onUserNameChange() {
    userName = document.getElementById("userName");
    if (userName.value.length > 0) {
        sendUserRequest(userURL + "?startsWith=" + userName.value);
    } else {
        sendUserRequest(userURL);
    }
}
function onUserSelected() {
    userList = document.getElementById("userList");
    user = userList.options[userList.options.selectedIndex];
    userName = document.getElementById("userName");
    userName.value = user.value;
}
function sendUserRequest(url) {
    http.open('get', url);
    http.onreadystatechange = handleUserResponse;
    http.send(null);
}
function handleUserResponse() {
    if (http.readyState == 4) {
        userList = document.getElementById("userList");
        for (i = userList.options.length; i >= 0; i--) {
            userList.options[i] = null;
        }   
        //userList.onchange = null;
        data = http.responseText;  
        //alert(data);
        if (data.indexOf("\n") != -1) {
            lines = data.split('\n');
            for (i = 0; i < lines.length; i++) {
                if (lines[i].indexOf(',') != -1) {
                   userArray = lines[i].split(',');
                   userList.options[userList.length] = 
                      new Option(userArray[0] + " (" + userArray[1] + ")", userArray[0]);
                }
            }
        }  
        //userList.onchange = onUserSelected();
    }
}
// -->
</script> 

<h1><fmt:message key="inviteMember.title" /></h1>

<p><fmt:message key="inviteMember.description" /></p>

<html:form action="/editor/inviteMember" method="post" focus="userName">

    <div class="formrow">
       <label for="userName" class="formrow" />
           <fmt:message key="inviteMember.userName" /></label>
       <div>
           <input name="userName" id="userName" size="30" maxlength="30" 
               onfocus="onUserNameFocus()" onkeyup="onUserNameChange()" /><br />
       </div>
    </div>    
    
    <div class="formrow">
       <label class="formrow" />&nbsp;</label>
       <div>
           <select id="userList" size="10" onchange="onUserSelected()" style="width:300px"></select>
       </div>
    </div>    
    
     <div class="formrow">
       <label for="userName" class="formrow" />
           <fmt:message key="inviteMember.permissions" /></label>
       <input type="radio" name="permissionsMask" value="3"  />
       <fmt:message key="inviteMember.administrator" />
       <input type="radio" name="permissionsMask" value="1" checked />
       <fmt:message key="inviteMember.author" />
       <input type="radio" name="permissionsMask" value="0" />
       <fmt:message key="inviteMember.limited" />
    </div>
                  
    <br />      
    <div class="control">
       <input type="submit" value='<fmt:message key="inviteMember.button.save" />'></input>
    </div>
    
    <div class="helptext">
        <img src="../images/TipOfTheDay16.gif" alt="info-icon" align="bottom" />
        <fmt:message key="memberPermissions.permissionHelp" />
    </div> 

</html:form>

<%@ include file="/theme/footer.jsp" %>


