/*
* Licensed to the Apache Software Foundation (ASF) under one or more
*  contributor license agreements.  The ASF licenses this file to You
* under the Apache License, Version 2.0 (the "License"); you may not
* use this file except in compliance with the License.
* You may obtain a copy of the License at
*
*     http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.  For additional information regarding
* copyright in this work, please see the NOTICE file in the top level
* directory of this distribution.
*/
// Used in: InviteMember.jsp, UserAdmin.jsp

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
var isBusy = false;
var userURL = "<%= request.getContextPath() %>" + "/roller-ui/authoring/userdata?length=50";

function onUserNameFocus(enabled) {
    if (!init) {
        init = true;
        u = userURL;
        if (enabled != null) u = u + "&enabled=" + enabled;
        sendUserRequest(u);
    }
}
function onUserNameChange(enabled) {
    u = userURL;
    if (enabled != null) u = u + "&enabled=" + enabled;
    userName = document.getElementById("userName");
    if (userName.value.length > 0) u = u + "&startsWith=" + userName.value;
    sendUserRequest(u);
}
function onUserSelected() {
    userList = document.getElementById("userList");
    user = userList.options[userList.options.selectedIndex];
    userName = document.getElementById("userName");
    userName.value = user.value;
}
function sendUserRequest(url) {
    if (isBusy) return;
    isBusy = true;
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

    }
    isBusy = false;
}
