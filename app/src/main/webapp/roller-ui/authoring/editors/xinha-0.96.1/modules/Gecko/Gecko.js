/* This compressed file is part of Xinha. For uncompressed sources, forum, and bug reports, go to xinha.org */
/* The URL of the most recent version of this file is http://svn.xinha.org/trunk/modules/Gecko/Gecko.js */
Gecko._pluginInfo={name:"Gecko",origin:"Xinha Core",version:"$LastChangedRevision: 1084 $".replace(/^[^:]*:\s*(.*)\s*\$$/,"$1"),developer:"The Xinha Core Developer Team",developer_url:"$HeadURL: http://svn.xinha.org/tags/0.96.1/modules/Gecko/Gecko.js $".replace(/^[^:]*:\s*(.*)\s*\$$/,"$1"),sponsor:"",sponsor_url:"",license:"htmlArea"};
function Gecko(_1){
this.editor=_1;
_1.Gecko=this;
};
Gecko.prototype.onKeyPress=function(ev){
var _3=this.editor;
var s=_3.getSelection();
if(_3.isShortCut(ev)){
switch(_3.getKey(ev).toLowerCase()){
case "z":
if(_3._unLink&&_3._unlinkOnUndo){
Xinha._stopEvent(ev);
_3._unLink();
_3.updateToolbar();
return true;
}
break;
case "a":
sel=_3.getSelection();
sel.removeAllRanges();
range=_3.createRange();
range.selectNodeContents(_3._doc.body);
sel.addRange(range);
Xinha._stopEvent(ev);
return true;
break;
case "v":
if(!_3.config.htmlareaPaste){
return true;
}
break;
}
}
switch(_3.getKey(ev)){
case " ":
var _5=function(_6,_7){
var _8=_6.nextSibling;
if(typeof _7=="string"){
_7=_3._doc.createElement(_7);
}
var a=_6.parentNode.insertBefore(_7,_8);
Xinha.removeFromParent(_6);
a.appendChild(_6);
_8.data=" "+_8.data;
s.collapse(_8,1);
_3._unLink=function(){
var t=a.firstChild;
a.removeChild(t);
a.parentNode.insertBefore(t,a);
Xinha.removeFromParent(a);
_3._unLink=null;
_3._unlinkOnUndo=false;
};
_3._unlinkOnUndo=true;
return a;
};
if(_3.config.convertUrlsToLinks&&s&&s.isCollapsed&&s.anchorNode.nodeType==3&&s.anchorNode.data.length>3&&s.anchorNode.data.indexOf(".")>=0){
var _b=s.anchorNode.data.substring(0,s.anchorOffset).search(/\S{4,}$/);
if(_b==-1){
break;
}
if(_3._getFirstAncestor(s,"a")){
break;
}
var _c=s.anchorNode.data.substring(0,s.anchorOffset).replace(/^.*?(\S*)$/,"$1");
var _d=_c.match(Xinha.RE_email);
if(_d){
var _e=s.anchorNode;
var _f=_e.splitText(s.anchorOffset);
var _10=_e.splitText(_b);
_5(_10,"a").href="mailto:"+_d[0];
break;
}
RE_date=/([0-9]+\.)+/;
RE_ip=/(?:(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\.){3}(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)/;
var _11=_c.match(Xinha.RE_url);
if(_11){
if(RE_date.test(_c)){
break;
}
var _12=s.anchorNode;
var _13=_12.splitText(s.anchorOffset);
var _14=_12.splitText(_b);
_5(_14,"a").href=(_11[1]?_11[1]:"http://")+_11[2];
break;
}
}
break;
}
switch(ev.keyCode){
case 27:
if(_3._unLink){
_3._unLink();
Xinha._stopEvent(ev);
}
break;
break;
case 8:
case 46:
if(!ev.shiftKey&&this.handleBackspace()){
Xinha._stopEvent(ev);
}
default:
_3._unlinkOnUndo=false;
if(s.anchorNode&&s.anchorNode.nodeType==3){
var a=_3._getFirstAncestor(s,"a");
if(!a){
break;
}
if(!a._updateAnchTimeout){
if(s.anchorNode.data.match(Xinha.RE_email)&&a.href.match("mailto:"+s.anchorNode.data.trim())){
var _16=s.anchorNode;
var _17=function(){
a.href="mailto:"+_16.data.trim();
a._updateAnchTimeout=setTimeout(_17,250);
};
a._updateAnchTimeout=setTimeout(_17,1000);
break;
}
var m=s.anchorNode.data.match(Xinha.RE_url);
if(m&&a.href.match(new RegExp("http(s)?://"+Xinha.escapeStringForRegExp(s.anchorNode.data.trim())))){
var _19=s.anchorNode;
var _1a=function(){
m=_19.data.match(Xinha.RE_url);
if(m){
a.href=(m[1]?m[1]:"http://")+m[2];
}
a._updateAnchTimeout=setTimeout(_1a,250);
};
a._updateAnchTimeout=setTimeout(_1a,1000);
}
}
}
break;
}
return false;
};
Gecko.prototype.handleBackspace=function(){
var _1b=this.editor;
setTimeout(function(){
var sel=_1b.getSelection();
var _1d=_1b.createRange(sel);
var SC=_1d.startContainer;
var SO=_1d.startOffset;
var EC=_1d.endContainer;
var EO=_1d.endOffset;
var _22=SC.nextSibling;
if(SC.nodeType==3){
SC=SC.parentNode;
}
if(!(/\S/.test(SC.tagName))){
var p=document.createElement("p");
while(SC.firstChild){
p.appendChild(SC.firstChild);
}
SC.parentNode.insertBefore(p,SC);
Xinha.removeFromParent(SC);
var r=_1d.cloneRange();
r.setStartBefore(_22);
r.setEndAfter(_22);
r.extractContents();
sel.removeAllRanges();
sel.addRange(r);
}
},10);
};
Gecko.prototype.inwardHtml=function(_25){
_25=_25.replace(/<(\/?)strong(\s|>|\/)/ig,"<$1b$2");
_25=_25.replace(/<(\/?)em(\s|>|\/)/ig,"<$1i$2");
_25=_25.replace(/<(\/?)del(\s|>|\/)/ig,"<$1strike$2");
return _25;
};
Gecko.prototype.outwardHtml=function(_26){
_26=_26.replace(/<script[\s]*src[\s]*=[\s]*['"]chrome:\/\/.*?["']>[\s]*<\/script>/ig,"");
return _26;
};
Gecko.prototype.onExecCommand=function(_27,UI,_29){
try{
this.editor._doc.execCommand("useCSS",false,true);
this.editor._doc.execCommand("styleWithCSS",false,false);
}
catch(ex){
}
switch(_27){
case "paste":
alert(Xinha._lc("The Paste button does not work in Mozilla based web browsers (technical security reasons). Press CTRL-V on your keyboard to paste directly."));
return true;
break;
case "removeformat":
var _2a=this.editor;
var sel=_2a.getSelection();
var _2c=_2a.saveSelection(sel);
var _2d=_2a.createRange(sel);
var els=_2a._doc.body.getElementsByTagName("*");
var _2f=(_2d.startContainer.nodeType==1)?_2d.startContainer:_2d.startContainer.parentNode;
var i,el;
if(sel.isCollapsed){
_2d.selectNodeContents(_2a._doc.body);
}
for(i=0;i<els.length;i++){
el=els[i];
if(_2d.isPointInRange(el,0)||(els[i]==_2f&&_2d.startOffset==0)){
el.removeAttribute("style");
}
}
this.editor._doc.execCommand(_27,UI,_29);
_2a.restoreSelection(_2c);
return true;
break;
}
return false;
};
Gecko.prototype.onMouseDown=function(ev){
if(ev.target.tagName.toLowerCase()=="hr"){
var sel=this.editor.getSelection();
var _34=this.editor.createRange(sel);
_34.selectNode(ev.target);
}
};
Xinha.prototype.insertNodeAtSelection=function(_35){
if(_35.ownerDocument!=this._doc){
try{
_35=this._doc.adoptNode(_35);
}
catch(e){
}
}
var sel=this.getSelection();
var _37=this.createRange(sel);
sel.removeAllRanges();
_37.deleteContents();
var _38=_37.startContainer;
var pos=_37.startOffset;
var _3a=_35;
switch(_38.nodeType){
case 3:
if(_35.nodeType==3){
_38.insertData(pos,_35.data);
_37=this.createRange();
_37.setEnd(_38,pos+_35.length);
_37.setStart(_38,pos+_35.length);
sel.addRange(_37);
}else{
_38=_38.splitText(pos);
if(_35.nodeType==11){
_3a=_3a.firstChild;
}
_38.parentNode.insertBefore(_35,_38);
this.selectNodeContents(_3a);
this.updateToolbar();
}
break;
case 1:
if(_35.nodeType==11){
_3a=_3a.firstChild;
}
_38.insertBefore(_35,_38.childNodes[pos]);
this.selectNodeContents(_3a);
this.updateToolbar();
break;
}
};
Xinha.prototype.getParentElement=function(sel){
if(typeof sel=="undefined"){
sel=this.getSelection();
}
var _3c=this.createRange(sel);
try{
var p=_3c.commonAncestorContainer;
if(!_3c.collapsed&&_3c.startContainer==_3c.endContainer&&_3c.startOffset-_3c.endOffset<=1&&_3c.startContainer.hasChildNodes()){
p=_3c.startContainer.childNodes[_3c.startOffset];
}
while(p.nodeType==3){
p=p.parentNode;
}
return p;
}
catch(ex){
return null;
}
};
Xinha.prototype.activeElement=function(sel){
if((sel===null)||this.selectionEmpty(sel)){
return null;
}
if(!sel.isCollapsed){
if(sel.anchorNode.childNodes.length>sel.anchorOffset&&sel.anchorNode.childNodes[sel.anchorOffset].nodeType==1){
return sel.anchorNode.childNodes[sel.anchorOffset];
}else{
if(sel.anchorNode.nodeType==1){
return sel.anchorNode;
}else{
return null;
}
}
}
return null;
};
Xinha.prototype.selectionEmpty=function(sel){
if(!sel){
return true;
}
if(typeof sel.isCollapsed!="undefined"){
return sel.isCollapsed;
}
return true;
};
Xinha.prototype.saveSelection=function(){
return this.createRange(this.getSelection()).cloneRange();
};
Xinha.prototype.restoreSelection=function(_40){
try{
var sel=this.getSelection();
sel.removeAllRanges();
sel.addRange(_40);
}
catch(e){
}
};
Xinha.prototype.selectNodeContents=function(_42,_43){
this.focusEditor();
this.forceRedraw();
var _44;
var _45=typeof _43=="undefined"?true:false;
var sel=this.getSelection();
_44=this._doc.createRange();
if(!_42){
sel.removeAllRanges();
return;
}
if(_45&&_42.tagName&&_42.tagName.toLowerCase().match(/table|img|input|textarea|select/)){
_44.selectNode(_42);
}else{
_44.selectNodeContents(_42);
}
sel.removeAllRanges();
sel.addRange(_44);
if(typeof _43!="undefined"){
if(_43){
sel.collapse(_44.startContainer,_44.startOffset);
}else{
sel.collapse(_44.endContainer,_44.endOffset);
}
}
};
Xinha.prototype.insertHTML=function(_47){
var sel=this.getSelection();
var _49=this.createRange(sel);
this.focusEditor();
var _4a=this._doc.createDocumentFragment();
var div=this._doc.createElement("div");
div.innerHTML=_47;
while(div.firstChild){
_4a.appendChild(div.firstChild);
}
var _4c=this.insertNodeAtSelection(_4a);
};
Xinha.prototype.getSelectedHTML=function(){
var sel=this.getSelection();
if(sel.isCollapsed){
return "";
}
var _4e=this.createRange(sel);
return Xinha.getHTML(_4e.cloneContents(),false,this);
};
Xinha.prototype.getSelection=function(){
return this._iframe.contentWindow.getSelection();
};
Xinha.prototype.createRange=function(sel){
this.activateEditor();
if(typeof sel!="undefined"){
try{
return sel.getRangeAt(0);
}
catch(ex){
return this._doc.createRange();
}
}else{
return this._doc.createRange();
}
};
Xinha.prototype.isKeyEvent=function(_50){
return _50.type=="keypress";
};
Xinha.prototype.getKey=function(_51){
return String.fromCharCode(_51.charCode);
};
Xinha.getOuterHTML=function(_52){
return (new XMLSerializer()).serializeToString(_52);
};
Xinha.cc=String.fromCharCode(8286);
Xinha.prototype.setCC=function(_53){
var cc=Xinha.cc;
try{
if(_53=="textarea"){
var ta=this._textArea;
var _56=ta.selectionStart;
var _57=ta.value.substring(0,_56);
var _58=ta.value.substring(_56,ta.value.length);
if(_58.match(/^[^<]*>/)){
var _59=_58.indexOf(">")+1;
ta.value=_57+_58.substring(0,_59)+cc+_58.substring(_59,_58.length);
}else{
ta.value=_57+cc+_58;
}
ta.value=ta.value.replace(new RegExp("(&[^"+cc+"]*?)("+cc+")([^"+cc+"]*?;)"),"$1$3$2");
ta.value=ta.value.replace(new RegExp("(<script[^>]*>[^"+cc+"]*?)("+cc+")([^"+cc+"]*?</script>)"),"$1$3$2");
ta.value=ta.value.replace(new RegExp("^([^"+cc+"]*)("+cc+")([^"+cc+"]*<body[^>]*>)(.*?)"),"$1$3$2$4");
}else{
var sel=this.getSelection();
sel.getRangeAt(0).insertNode(this._doc.createTextNode(cc));
}
}
catch(e){
}
};
Xinha.prototype.findCC=function(_5b){
if(_5b=="textarea"){
var ta=this._textArea;
var pos=ta.value.indexOf(Xinha.cc);
if(pos==-1){
return;
}
var end=pos+Xinha.cc.length;
var _5f=ta.value.substring(0,pos);
var _60=ta.value.substring(end,ta.value.length);
ta.value=_5f;
ta.scrollTop=ta.scrollHeight;
var _61=ta.scrollTop;
ta.value+=_60;
ta.setSelectionRange(pos,pos);
ta.focus();
ta.scrollTop=_61;
}else{
try{
var doc=this._doc;
doc.body.innerHTML=doc.body.innerHTML.replace(new RegExp(Xinha.cc),"<span id=\"XinhaEditingPostion\"></span>");
var _63=doc.getElementById("XinhaEditingPostion");
this.selectNodeContents(_63);
this.scrollToElement(_63);
_63.parentNode.removeChild(_63);
this._iframe.contentWindow.focus();
}
catch(e){
}
}
};
Xinha.prototype._standardToggleBorders=Xinha.prototype._toggleBorders;
Xinha.prototype._toggleBorders=function(){
var _64=this._standardToggleBorders();
var _65=this._doc.getElementsByTagName("TABLE");
for(var i=0;i<_65.length;i++){
_65[i].style.display="none";
_65[i].style.display="table";
}
return _64;
};
Xinha.getDoctype=function(doc){
var d="";
if(doc.doctype){
d+="<!DOCTYPE "+doc.doctype.name+" PUBLIC ";
d+=doc.doctype.publicId?"\""+doc.doctype.publicId+"\"":"";
d+=doc.doctype.systemId?" \""+doc.doctype.systemId+"\"":"";
d+=">";
}
return d;
};

