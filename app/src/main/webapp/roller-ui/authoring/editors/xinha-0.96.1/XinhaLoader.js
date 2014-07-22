/* This compressed file is part of Xinha. For uncompressed sources, forum, and bug reports, go to xinha.org */
/* The URL of the most recent version of this file is http://svn.xinha.org/trunk/XinhaLoader.js */
var Xinha={};
if(!window._editor_url){
(function(){
var _1=document.getElementsByTagName("script");
var _2=_1[_1.length-1];
var _3=_2.src.split("?");
_3=_3.length==2?_3[1].split("&"):"";
for(var _4=0;_4<_3.length;++_4){
var _5=_3[_4].split("=");
if(_5.length==2){
switch(_5[0]){
case "lang":
case "icons":
case "skin":
case "url":
window["_editor_"+_5[0]]=_5[1];
break;
}
}
}
if(_2.innerHTML.replace(/\s+/,"")){
eval(_2.innerHTML);
}
_editor_lang=window._editor_lang||"en";
_editor_url=window._editor_url||_2.src.split("?")[0].split("/").slice(0,-1).join("/");
})();
}
_editor_url=_editor_url.replace(/\x2f*$/,"/");
Xinha.agt=navigator.userAgent.toLowerCase();
Xinha.is_ie=((Xinha.agt.indexOf("msie")!=-1)&&(Xinha.agt.indexOf("opera")==-1));
Xinha.ie_version=parseFloat(Xinha.agt.substring(Xinha.agt.indexOf("msie")+5));
Xinha.is_opera=(Xinha.agt.indexOf("opera")!=-1);
Xinha.is_khtml=(Xinha.agt.indexOf("khtml")!=-1);
Xinha.is_webkit=(Xinha.agt.indexOf("applewebkit")!=-1);
Xinha.is_safari=(Xinha.agt.indexOf("safari")!=-1);
Xinha.opera_version=navigator.appVersion.substring(0,navigator.appVersion.indexOf(" "))*1;
Xinha.is_mac=(Xinha.agt.indexOf("mac")!=-1);
Xinha.is_mac_ie=(Xinha.is_ie&&Xinha.is_mac);
Xinha.is_win_ie=(Xinha.is_ie&&!Xinha.is_mac);
Xinha.is_gecko=(navigator.product=="Gecko"&&!Xinha.is_safari);
Xinha.isRunLocally=document.URL.toLowerCase().search(/^file:/)!=-1;
Xinha.is_designMode=(typeof document.designMode!="undefined"&&!Xinha.is_ie);
Xinha.isSupportedBrowser=Xinha.is_gecko||(Xinha.is_opera&&Xinha.opera_version>=9.1)||Xinha.ie_version>=5.5||Xinha.is_safari;
Xinha.loadPlugins=function(_6,_7){
if(!Xinha.isSupportedBrowser){
return;
}
Xinha.loadStyle(typeof _editor_css=="string"?_editor_css:"Xinha.css","XinhaCoreDesign");
Xinha.createLoadingMessages(xinha_editors);
var _8=Xinha.loadingMessages;
Xinha._loadback(_editor_url+"XinhaCore.js",function(){
Xinha.removeLoadingMessages(xinha_editors);
Xinha.createLoadingMessages(xinha_editors);
_7();
});
return false;
};
Xinha._loadback=function(_9,_a,_b,_c){
var T=!Xinha.is_ie?"onload":"onreadystatechange";
var S=document.createElement("script");
S.type="text/javascript";
S.src=_9;
if(_a){
S[T]=function(){
if(Xinha.is_ie&&(!(/loaded|complete/.test(window.event.srcElement.readyState)))){
return;
}
_a.call(_b?_b:this,_c);
S[T]=null;
};
}
document.getElementsByTagName("head")[0].appendChild(S);
};
Xinha.getElementTopLeft=function(_f){
var _10=0;
var _11=0;
if(_f.offsetParent){
_10=_f.offsetLeft;
_11=_f.offsetTop;
while(_f=_f.offsetParent){
_10+=_f.offsetLeft;
_11+=_f.offsetTop;
}
}
return {top:_11,left:_10};
};
Xinha.findPosX=function(obj){
var _13=0;
if(obj.offsetParent){
return Xinha.getElementTopLeft(obj).left;
}else{
if(obj.x){
_13+=obj.x;
}
}
return _13;
};
Xinha.findPosY=function(obj){
var _15=0;
if(obj.offsetParent){
return Xinha.getElementTopLeft(obj).top;
}else{
if(obj.y){
_15+=obj.y;
}
}
return _15;
};
Xinha.createLoadingMessages=function(_16){
if(Xinha.loadingMessages||!Xinha.isSupportedBrowser){
return;
}
Xinha.loadingMessages=[];
for(var i=0;i<_16.length;i++){
if(!document.getElementById(_16[i])){
continue;
}
Xinha.loadingMessages.push(Xinha.createLoadingMessage(document.getElementById(_16[i])));
}
};
Xinha.createLoadingMessage=function(_18,_19){
if(document.getElementById("loading_"+_18.id)||!Xinha.isSupportedBrowser){
return;
}
var _1a=document.createElement("div");
_1a.id="loading_"+_18.id;
_1a.className="loading";
_1a.style.left=(Xinha.findPosX(_18)+_18.offsetWidth/2)-106+"px";
_1a.style.top=(Xinha.findPosY(_18)+_18.offsetHeight/2)-50+"px";
var _1b=document.createElement("div");
_1b.className="loading_main";
_1b.id="loading_main_"+_18.id;
_1b.appendChild(document.createTextNode(Xinha._lc("Loading in progress. Please wait!")));
var _1c=document.createElement("div");
_1c.className="loading_sub";
_1c.id="loading_sub_"+_18.id;
_19=_19?_19:Xinha._lc("Loading Core");
_1c.appendChild(document.createTextNode(_19));
_1a.appendChild(_1b);
_1a.appendChild(_1c);
document.body.appendChild(_1a);
return _1c;
};
Xinha.loadStyle=function(_1d,id){
var url=_editor_url||"";
url+=_1d;
var _20=document.getElementsByTagName("head")[0];
var _21=document.createElement("link");
_21.rel="stylesheet";
_21.href=url;
if(id){
_21.id=id;
}
_20.appendChild(_21);
};
Xinha._lc=function(_22){
return _22;
};
Xinha._addEvent=function(el,_24,_25){
if(document.addEventListener){
el.addEventListener(_24,_25,true);
}else{
el.attachEvent("on"+_24,_25);
}
};
Xinha.addOnloadHandler=function(_26){
var _27=function(){
if(arguments.callee.done){
return;
}
arguments.callee.done=true;
if(Xinha.onloadTimer){
clearInterval(Xinha.onloadTimer);
}
_26.call();
};
if(Xinha.is_ie){
document.attachEvent("onreadystatechange",function(){
if(document.readyState==="complete"){
document.detachEvent("onreadystatechange",arguments.callee);
_27();
}
});
if(document.documentElement.doScroll&&typeof window.frameElement==="undefined"){
(function(){
if(arguments.callee.done){
return;
}
try{
document.documentElement.doScroll("left");
}
catch(error){
setTimeout(arguments.callee,0);
return;
}
_27();
})();
}
}else{
if(/WebKit/i.test(navigator.userAgent)){
Xinha.onloadTimer=setInterval(function(){
if(/loaded|complete/.test(document.readyState)){
_27();
}
},10);
}else{
document.addEventListener("DOMContentLoaded",_27,false);
}
}
};

