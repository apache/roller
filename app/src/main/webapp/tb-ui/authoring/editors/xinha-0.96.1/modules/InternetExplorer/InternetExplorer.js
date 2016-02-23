/* This compressed file is part of Xinha. For uncompressed sources, forum, and bug reports, go to xinha.org */
/* The URL of the most recent version of this file is http://svn.xinha.org/trunk/modules/InternetExplorer/InternetExplorer.js */
InternetExplorer._pluginInfo={name:"Internet Explorer",origin:"Xinha Core",version:"$LastChangedRevision: 1260 $".replace(/^[^:]*:\s*(.*)\s*\$$/,"$1"),developer:"The Xinha Core Developer Team",developer_url:"$HeadURL: http://svn.xinha.org/tags/0.96.1/modules/InternetExplorer/InternetExplorer.js $".replace(/^[^:]*:\s*(.*)\s*\$$/,"$1"),sponsor:"",sponsor_url:"",license:"htmlArea"};
function InternetExplorer(_1){
this.editor=_1;
_1.InternetExplorer=this;
};
InternetExplorer.prototype.onKeyPress=function(ev){
if(this.editor.isShortCut(ev)){
switch(this.editor.getKey(ev).toLowerCase()){
case "n":
this.editor.execCommand("formatblock",false,"<p>");
Xinha._stopEvent(ev);
return true;
break;
case "1":
case "2":
case "3":
case "4":
case "5":
case "6":
this.editor.execCommand("formatblock",false,"<h"+this.editor.getKey(ev).toLowerCase()+">");
Xinha._stopEvent(ev);
return true;
break;
}
}
switch(ev.keyCode){
case 8:
case 46:
if(this.handleBackspace()){
Xinha._stopEvent(ev);
return true;
}
break;
case 9:
Xinha._stopEvent(ev);
return true;
}
return false;
};
InternetExplorer.prototype.handleBackspace=function(){
var _3=this.editor;
var _4=_3.getSelection();
if(_4.type=="Control"){
var _5=_3.activeElement(_4);
Xinha.removeFromParent(_5);
return true;
}
var _6=_3.createRange(_4);
var r2=_6.duplicate();
r2.moveStart("character",-1);
var a=r2.parentElement();
if(a!=_6.parentElement()&&(/^a$/i.test(a.tagName))){
r2.collapse(true);
r2.moveEnd("character",1);
r2.pasteHTML("");
r2.select();
return true;
}
};
InternetExplorer.prototype.inwardHtml=function(_9){
_9=_9.replace(/<(\/?)del(\s|>|\/)/ig,"<$1strike$2");
_9=_9.replace(/(<script|<!--)/i,"&nbsp;$1");
_9=_9.replace(/<span[^>]+id="__InsertSpan_Workaround_[a-z]+".*?>([\s\S]*?)<\/span>/i,"$1");
return _9;
};
InternetExplorer.prototype.outwardHtml=function(_a){
_a=_a.replace(/&nbsp;(\s*)(<script|<!--)/i,"$1$2");
_a=_a.replace(/<span[^>]+id="__InsertSpan_Workaround_[a-z]+".*?>([\s\S]*?)<\/span>/i,"$1");
return _a;
};
InternetExplorer.prototype.onExecCommand=function(_b,UI,_d){
switch(_b){
case "saveas":
var _e=null;
var _f=this.editor;
var _10=document.createElement("iframe");
_10.src="about:blank";
_10.style.display="none";
document.body.appendChild(_10);
try{
if(_10.contentDocument){
_e=_10.contentDocument;
}else{
_e=_10.contentWindow.document;
}
}
catch(ex){
}
_e.open("text/html","replace");
var _11="";
if(_f.config.browserQuirksMode===false){
var _12="<!DOCTYPE HTML PUBLIC \"-//W3C//DTD HTML 4.01 Transitional//EN\" \"http://www.w3.org/TR/html4/loose.dtd\">";
}else{
if(_f.config.browserQuirksMode===true){
var _12="";
}else{
var _12=Xinha.getDoctype(document);
}
}
if(!_f.config.fullPage){
_11+=_12+"\n";
_11+="<html>\n";
_11+="<head>\n";
_11+="<meta http-equiv=\"Content-Type\" content=\"text/html; charset="+_f.config.charSet+"\">\n";
if(typeof _f.config.baseHref!="undefined"&&_f.config.baseHref!==null){
_11+="<base href=\""+_f.config.baseHref+"\"/>\n";
}
if(typeof _f.config.pageStyleSheets!=="undefined"){
for(var i=0;i<_f.config.pageStyleSheets.length;i++){
if(_f.config.pageStyleSheets[i].length>0){
_11+="<link rel=\"stylesheet\" type=\"text/css\" href=\""+_f.config.pageStyleSheets[i]+"\">";
}
}
}
if(_f.config.pageStyle){
_11+="<style type=\"text/css\">\n"+_f.config.pageStyle+"\n</style>";
}
_11+="</head>\n";
_11+="<body>\n";
_11+=_f.getEditorContent();
_11+="</body>\n";
_11+="</html>";
}else{
_11=_f.getEditorContent();
if(_11.match(Xinha.RE_doctype)){
_f.setDoctype(RegExp.$1);
}
}
_e.write(_11);
_e.close();
_e.execCommand(_b,UI,_d);
document.body.removeChild(_10);
return true;
break;
case "removeformat":
var _f=this.editor;
var sel=_f.getSelection();
var _15=_f.saveSelection(sel);
var i,el,els;
function clean(el){
if(el.nodeType!=1){
return;
}
el.removeAttribute("style");
for(var j=0;j<el.childNodes.length;j++){
clean(el.childNodes[j]);
}
if((el.tagName.toLowerCase()=="span"&&!el.attributes.length)||el.tagName.toLowerCase()=="font"){
el.outerHTML=el.innerHTML;
}
};
if(_f.selectionEmpty(sel)){
els=_f._doc.body.childNodes;
for(i=0;i<els.length;i++){
el=els[i];
if(el.nodeType!=1){
continue;
}
if(el.tagName.toLowerCase()=="span"){
newNode=_f.convertNode(el,"div");
el.parentNode.replaceChild(newNode,el);
el=newNode;
}
clean(el);
}
}
_f._doc.execCommand(_b,UI,_d);
_f.restoreSelection(_15);
return true;
break;
}
return false;
};
Xinha.prototype.insertNodeAtSelection=function(_1a){
this.insertHTML(_1a.outerHTML);
};
Xinha.prototype.getParentElement=function(sel){
if(typeof sel=="undefined"){
sel=this.getSelection();
}
var _1c=this.createRange(sel);
switch(sel.type){
case "Text":
var _1d=_1c.parentElement();
while(true){
var _1e=_1c.duplicate();
_1e.moveToElementText(_1d);
if(_1e.inRange(_1c)){
break;
}
if((_1d.nodeType!=1)||(_1d.tagName.toLowerCase()=="body")){
break;
}
_1d=_1d.parentElement;
}
return _1d;
case "None":
try{
return _1c.parentElement();
}
catch(e){
return this._doc.body;
}
case "Control":
return _1c.item(0);
default:
return this._doc.body;
}
};
Xinha.prototype.activeElement=function(sel){
if((sel===null)||this.selectionEmpty(sel)){
return null;
}
if(sel.type.toLowerCase()=="control"){
return sel.createRange().item(0);
}else{
var _20=sel.createRange();
var _21=this.getParentElement(sel);
if(_21.innerHTML==_20.htmlText){
return _21;
}
return null;
}
};
Xinha.prototype.selectionEmpty=function(sel){
if(!sel){
return true;
}
return this.createRange(sel).htmlText==="";
};
Xinha.prototype.saveSelection=function(sel){
return this.createRange(sel?sel:this.getSelection());
};
Xinha.prototype.restoreSelection=function(_24){
if(!_24){
return;
}
var _25=null;
if(_24.parentElement){
_25=_24.parentElement();
}else{
_25=_24.item(0);
}
var _26=this.createRange(this.getSelection());
var _27=null;
if(_26.parentElement){
_27=_26.parentElement();
}else{
_27=_26.item(0);
}
var _28=function(el){
for(var _2a=el;_2a;_2a=_2a.parentNode){
if(_2a.tagName.toLowerCase()=="html"){
return _2a.parentNode;
}
}
return null;
};
if(_24.parentElement&&_28(_25)==_28(_27)){
if(_26.isEqual(_24)){
return;
}
}
try{
_24.select();
}
catch(e){
}
_26=this.createRange(this.getSelection());
if(_26.parentElement){
_27=_26.parentElement();
}else{
_27=_26.item(0);
}
if(_27!=_25){
var _2b=this.config.selectWorkaround||"VisibleCue";
switch(_2b){
case "SimulateClick":
case "InsertSpan":
var _2c=_28(_25);
var _2d=function(_2e){
var _2f="";
for(var _30=0;_30<26;++_30){
_2f+=String.fromCharCode("a".charCodeAt(0)+_30);
}
var _31="";
for(var _30=0;_30<_2e;++_30){
_31+=_2f.substr(Math.floor(Math.random()*_2f.length+1),1);
}
return _31;
};
var _32=1;
var _33="__InsertSpan_Workaround_"+_2d(_32);
while(_2c.getElementById(_33)){
_32+=1;
_33="__InsertSpan_Workaround_"+_2d(_32);
}
_24.pasteHTML("<span id=\""+_33+"\"></span>");
var _34=_2c.getElementById(_33);
_24.moveToElementText(_34);
_24.select();
break;
case "JustificationHack":
var _35=String.fromCharCode(1);
_24.pasteHTML(_35);
_24.findText(_35,-1);
_24.select();
_24.execCommand("JustifyNone");
_24.pasteHTML("");
break;
case "VisibleCue":
default:
var _35=String.fromCharCode(1);
_24.pasteHTML(_35);
_24.findText(_35,-1);
_24.select();
}
}
};
Xinha.prototype.selectNodeContents=function(_36,_37){
this.focusEditor();
this.forceRedraw();
var _38;
var _39=typeof _37=="undefined"?true:false;
if(_39&&_36.tagName&&_36.tagName.toLowerCase().match(/table|img|input|select|textarea/)){
_38=this._doc.body.createControlRange();
_38.add(_36);
}else{
_38=this._doc.body.createTextRange();
if(3==_36.nodeType){
if(_36.parentNode){
_38.moveToElementText(_36.parentNode);
}else{
_38.moveToElementText(this._doc.body);
}
var _3a=this._doc.body.createTextRange();
var _3b=0;
var _3c=_36.previousSibling;
for(;_3c&&(1!=_3c.nodeType);_3c=_3c.previousSibling){
if(3==_3c.nodeType){
_3b+=_3c.nodeValue.length-_3c.nodeValue.split("\r").length-1;
}
}
if(_3c&&(1==_3c.nodeType)){
_3a.moveToElementText(_3c);
_38.setEndPoint("StartToEnd",_3a);
}
if(_3b){
_38.moveStart("character",_3b);
}
_3b=0;
_3c=_36.nextSibling;
for(;_3c&&(1!=_3c.nodeType);_3c=_3c.nextSibling){
if(3==_3c.nodeType){
_3b+=_3c.nodeValue.length-_3c.nodeValue.split("\r").length-1;
if(!_3c.nextSibling){
_3b+=1;
}
}
}
if(_3c&&(1==_3c.nodeType)){
_3a.moveToElementText(_3c);
_38.setEndPoint("EndToStart",_3a);
}
if(_3b){
_38.moveEnd("character",-_3b);
}
if(!_36.nextSibling){
_38.moveEnd("character",-1);
}
}else{
_38.moveToElementText(_36);
}
}
if(typeof _37!="undefined"){
_38.collapse(_37);
if(!_37){
_38.moveStart("character",-1);
_38.moveEnd("character",-1);
}
}
_38.select();
};
Xinha.prototype.insertHTML=function(_3d){
this.focusEditor();
var sel=this.getSelection();
var _3f=this.createRange(sel);
_3f.pasteHTML(_3d);
};
Xinha.prototype.getSelectedHTML=function(){
var sel=this.getSelection();
if(this.selectionEmpty(sel)){
return "";
}
var _41=this.createRange(sel);
if(_41.htmlText){
return _41.htmlText;
}else{
if(_41.length>=1){
return _41.item(0).outerHTML;
}
}
return "";
};
Xinha.prototype.getSelection=function(){
return this._doc.selection;
};
Xinha.prototype.createRange=function(sel){
if(!sel){
sel=this.getSelection();
}
return sel.createRange();
};
Xinha.prototype.isKeyEvent=function(_43){
return _43.type=="keydown";
};
Xinha.prototype.getKey=function(_44){
return String.fromCharCode(_44.keyCode);
};
Xinha.getOuterHTML=function(_45){
return _45.outerHTML;
};
Xinha.cc=String.fromCharCode(8201);
Xinha.prototype.setCC=function(_46){
var cc=Xinha.cc;
if(_46=="textarea"){
var ta=this._textArea;
var pos=document.selection.createRange();
pos.collapse();
pos.text=cc;
var _4a=ta.value.indexOf(cc);
var _4b=ta.value.substring(0,_4a);
var _4c=ta.value.substring(_4a+cc.length,ta.value.length);
if(_4c.match(/^[^<]*>/)){
var _4d=_4c.indexOf(">")+1;
ta.value=_4b+_4c.substring(0,_4d)+cc+_4c.substring(_4d,_4c.length);
}else{
ta.value=_4b+cc+_4c;
}
ta.value=ta.value.replace(new RegExp("(&[^"+cc+"]*?)("+cc+")([^"+cc+"]*?;)"),"$1$3$2");
ta.value=ta.value.replace(new RegExp("(<script[^>]*>[^"+cc+"]*?)("+cc+")([^"+cc+"]*?</script>)"),"$1$3$2");
ta.value=ta.value.replace(new RegExp("^([^"+cc+"]*)("+cc+")([^"+cc+"]*<body[^>]*>)(.*?)"),"$1$3$2$4");
}else{
var sel=this.getSelection();
var r=sel.createRange();
if(sel.type=="Control"){
var _50=r.item(0);
_50.outerHTML+=cc;
}else{
r.collapse();
r.text=cc;
}
}
};
Xinha.prototype.findCC=function(_51){
var _52=(_51=="textarea")?this._textArea:this._doc.body;
range=_52.createTextRange();
if(range.findText(escape(Xinha.cc))){
range.select();
range.text="";
range.select();
}
if(range.findText(Xinha.cc)){
range.select();
range.text="";
range.select();
}
if(_51=="textarea"){
this._textArea.focus();
}
};
Xinha.getDoctype=function(doc){
return (doc.compatMode=="CSS1Compat"&&Xinha.ie_version<8)?"<!DOCTYPE HTML PUBLIC \"-//W3C//DTD HTML 4.01 Transitional//EN\" \"http://www.w3.org/TR/html4/loose.dtd\">":"";
};

