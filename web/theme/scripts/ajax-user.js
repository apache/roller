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
var userURL = "<%= request.getContextPath() %>" + "/editor/userdata?length=50";

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
