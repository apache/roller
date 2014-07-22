/* This compressed file is part of Xinha. For uncompressed sources, forum, and bug reports, go to xinha.org */
/* The URL of the most recent version of this file is http://svn.xinha.org/trunk/modules/WebKit/WebKit.js */
WebKit._pluginInfo={name:"WebKit",origin:"Xinha Core",version:"$LastChangedRevision: 1146 $".replace(/^[^:]*:\s*(.*)\s*\$$/,"$1"),developer:"The Xinha Core Developer Team",developer_url:"$HeadURL: http://svn.xinha.org/tags/0.96.1/modules/WebKit/WebKit.js $".replace(/^[^:]*:\s*(.*)\s*\$$/,"$1"),sponsor:"",sponsor_url:"",license:"htmlArea"};
function WebKit(_1){
this.editor=_1;
_1.WebKit=this;
};
WebKit.prototype.onKeyPress=function(ev){
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
case 13:
if(ev.shiftKey){
}
break;
case 27:
if(_3._unLink){
_3._unLink();
Xinha._stopEvent(ev);
}
break;
case 8:
case 46:
if(!ev.shiftKey&&this.handleBackspace()){
Xinha._stopEvent(ev);
}
break;
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
WebKit.prototype.handleBackspace=function(){
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
WebKit.prototype.inwardHtml=function(_25){
return _25;
};
WebKit.prototype.outwardHtml=function(_26){
return _26;
};
WebKit.prototype.onExecCommand=function(_27,UI,_29){
this.editor._doc.execCommand("styleWithCSS",false,false);
switch(_27){
case "paste":
alert(Xinha._lc("The Paste button does not work in the Safari browser for security reasons. Press CTRL-V on your keyboard to paste directly."));
return true;
break;
case "removeformat":
var _2a=this.editor;
var sel=_2a.getSelection();
var _2c=_2a.saveSelection(sel);
var _2d=_2a.createRange(sel);
var els=_2a._doc.getElementsByTagName("*");
els=Xinha.collectionToArray(els);
var _2f=(_2d.startContainer.nodeType==1)?_2d.startContainer:_2d.startContainer.parentNode;
var i,el,_32,_33,_34,r2=_2a._doc.createRange();
function clean(el){
if(el.nodeType!=1){
return;
}
el.removeAttribute("style");
for(var j=0;j<el.childNodes.length;j++){
clean(el.childNodes[j]);
}
if((el.tagName.toLowerCase()=="span"&&!el.attributes.length)||el.tagName.toLowerCase()=="font"){
r2.selectNodeContents(el);
_33=r2.extractContents();
while(_33.firstChild){
_34=_33.removeChild(_33.firstChild);
el.parentNode.insertBefore(_34,el);
}
el.parentNode.removeChild(el);
}
};
if(sel.isCollapsed){
els=_2a._doc.body.childNodes;
for(i=0;i<els.length;i++){
el=els[i];
if(el.nodeType!=1){
continue;
}
if(el.tagName.toLowerCase()=="span"){
_32=_2a.convertNode(el,"div");
el.parentNode.replaceChild(_32,el);
el=_32;
}
clean(el);
}
}else{
for(i=0;i<els.length;i++){
el=els[i];
if(_2d.isPointInRange(el,0)||(els[i]==_2f&&_2d.startOffset==0)){
clean(el);
}
}
}
r2.detach();
_2a.restoreSelection(_2c);
return true;
break;
}
return false;
};
WebKit.prototype.onMouseDown=function(ev){
if(ev.target.tagName.toLowerCase()=="hr"||ev.target.tagName.toLowerCase()=="img"){
this.editor.selectNodeContents(ev.target);
}
};
Xinha.prototype.insertNodeAtSelection=function(_39){
var sel=this.getSelection();
var _3b=this.createRange(sel);
sel.removeAllRanges();
_3b.deleteContents();
var _3c=_3b.startContainer;
var pos=_3b.startOffset;
var _3e=_39;
switch(_3c.nodeType){
case 3:
if(_39.nodeType==3){
_3c.insertData(pos,_39.data);
_3b=this.createRange();
_3b.setEnd(_3c,pos+_39.length);
_3b.setStart(_3c,pos+_39.length);
sel.addRange(_3b);
}else{
_3c=_3c.splitText(pos);
if(_39.nodeType==11){
_3e=_3e.firstChild;
}
_3c.parentNode.insertBefore(_39,_3c);
this.selectNodeContents(_3e);
this.updateToolbar();
}
break;
case 1:
if(_39.nodeType==11){
_3e=_3e.firstChild;
}
_3c.insertBefore(_39,_3c.childNodes[pos]);
this.selectNodeContents(_3e);
this.updateToolbar();
break;
}
};
Xinha.prototype.getParentElement=function(sel){
if(typeof sel=="undefined"){
sel=this.getSelection();
}
var _40=this.createRange(sel);
try{
var p=_40.commonAncestorContainer;
if(!_40.collapsed&&_40.startContainer==_40.endContainer&&_40.startOffset-_40.endOffset<=1&&_40.startContainer.hasChildNodes()){
p=_40.startContainer.childNodes[_40.startOffset];
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
Xinha.prototype.restoreSelection=function(_44){
var sel=this.getSelection();
sel.removeAllRanges();
sel.addRange(_44);
};
Xinha.prototype.selectNodeContents=function(_46,_47){
this.focusEditor();
this.forceRedraw();
var _48;
var _49=typeof _47=="undefined"?true:false;
var sel=this.getSelection();
_48=this._doc.createRange();
if(_49&&_46.tagName&&_46.tagName.toLowerCase().match(/table|img|input|textarea|select/)){
_48.selectNode(_46);
}else{
_48.selectNodeContents(_46);
}
sel.removeAllRanges();
sel.addRange(_48);
if(typeof _47!="undefined"){
if(_47){
sel.collapse(_48.startContainer,_48.startOffset);
}else{
sel.collapse(_48.endContainer,_48.endOffset);
}
}
};
Xinha.prototype.insertHTML=function(_4b){
var sel=this.getSelection();
var _4d=this.createRange(sel);
this.focusEditor();
var _4e=this._doc.createDocumentFragment();
var div=this._doc.createElement("div");
div.innerHTML=_4b;
while(div.firstChild){
_4e.appendChild(div.firstChild);
}
var _50=this.insertNodeAtSelection(_4e);
};
Xinha.prototype.getSelectedHTML=function(){
var sel=this.getSelection();
if(sel.isCollapsed){
return "";
}
var _52=this.createRange(sel);
if(_52){
return Xinha.getHTML(_52.cloneContents(),false,this);
}else{
return "";
}
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
Xinha.prototype.isKeyEvent=function(_54){
return _54.type=="keydown";
};
Xinha.prototype.getKey=function(_55){
var key=String.fromCharCode(parseInt(_55.keyIdentifier.replace(/^U\+/,""),16));
if(_55.shiftKey){
return key;
}else{
return key.toLowerCase();
}
};
Xinha.getOuterHTML=function(_57){
return (new XMLSerializer()).serializeToString(_57);
};
Xinha.cc=String.fromCharCode(8286);
Xinha.prototype.setCC=function(_58){
var cc=Xinha.cc;
try{
if(_58=="textarea"){
var ta=this._textArea;
var _5b=ta.selectionStart;
var _5c=ta.value.substring(0,_5b);
var _5d=ta.value.substring(_5b,ta.value.length);
if(_5d.match(/^[^<]*>/)){
var _5e=_5d.indexOf(">")+1;
ta.value=_5c+_5d.substring(0,_5e)+cc+_5d.substring(_5e,_5d.length);
}else{
ta.value=_5c+cc+_5d;
}
ta.value=ta.value.replace(new RegExp("(&[^"+cc+";]*?)("+cc+")([^"+cc+"]*?;)"),"$1$3$2");
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
Xinha.prototype.findCC=function(_60){
var cc=Xinha.cc;
if(_60=="textarea"){
var ta=this._textArea;
var pos=ta.value.indexOf(cc);
if(pos==-1){
return;
}
var end=pos+cc.length;
var _65=ta.value.substring(0,pos);
var _66=ta.value.substring(end,ta.value.length);
ta.value=_65;
ta.scrollTop=ta.scrollHeight;
var _67=ta.scrollTop;
ta.value+=_66;
ta.setSelectionRange(pos,pos);
ta.focus();
ta.scrollTop=_67;
}else{
var _68=this;
try{
var doc=this._doc;
doc.body.innerHTML=doc.body.innerHTML.replace(new RegExp(cc),"<span id=\"XinhaEditingPostion\"></span>");
var _6a=doc.getElementById("XinhaEditingPostion");
this.selectNodeContents(_6a);
this.scrollToElement(_6a);
_6a.parentNode.removeChild(_6a);
this._iframe.contentWindow.focus();
}
catch(e){
}
}
};
Xinha.prototype._standardToggleBorders=Xinha.prototype._toggleBorders;
Xinha.prototype._toggleBorders=function(){
var _6b=this._standardToggleBorders();
var _6c=this._doc.getElementsByTagName("TABLE");
for(var i=0;i<_6c.length;i++){
_6c[i].style.display="none";
_6c[i].style.display="table";
}
return _6b;
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

