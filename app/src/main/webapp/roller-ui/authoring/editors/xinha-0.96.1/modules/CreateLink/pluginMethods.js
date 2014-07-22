/* This compressed file is part of Xinha. For uncompressed sources, forum, and bug reports, go to xinha.org */
/* The URL of the most recent version of this file is http://svn.xinha.org/trunk/modules/CreateLink/pluginMethods.js */
CreateLink.prototype.show=function(a){
if(!this.dialog){
this.prepareDialog();
}
var _2=this.editor;
this.a=a;
if(!a&&this.editor.selectionEmpty(this.editor.getSelection())){
alert(this._lc("You need to select some text before creating a link"));
return false;
}
var _3={f_href:"",f_title:"",f_target:"",f_other_target:""};
if(a&&a.tagName.toLowerCase()=="a"){
_3.f_href=this.editor.fixRelativeLinks(a.getAttribute("href"));
_3.f_title=a.title;
if(a.target){
if(!/_self|_top|_blank/.test(a.target)){
_3.f_target="_other";
_3.f_other_target=a.target;
}else{
_3.f_target=a.target;
_3.f_other_target="";
}
}
}
this.dialog.show(_3);
};
CreateLink.prototype.apply=function(){
var _4=this.dialog.hide();
var a=this.a;
var _6=this.editor;
var _7={href:"",target:"",title:""};
if(_4.f_href){
_7.href=_4.f_href;
_7.title=_4.f_title;
if(_4.f_target.value){
if(_4.f_target.value=="other"){
_7.target=_4.f_other_target;
}else{
_7.target=_4.f_target.value;
}
}
}
if(_4.f_target.value){
if(_4.f_target.value!="_other"){
_7.target=_4.f_target.value;
}else{
_7.target=_4.f_other_target;
}
}
if(a&&a.tagName.toLowerCase()=="a"){
if(!_7.href){
if(confirm(this._lc("Are you sure you wish to remove this link?"))){
var p=a.parentNode;
while(a.hasChildNodes()){
p.insertBefore(a.removeChild(a.childNodes[0]),a);
}
p.removeChild(a);
_6.updateToolbar();
return;
}
}else{
for(var i in _7){
a.setAttribute(i,_7[i]);
}
if(Xinha.is_ie){
if(/mailto:([^?<>]*)(\?[^<]*)?$/i.test(a.innerHTML)){
a.innerHTML=RegExp.$1;
}
}
}
}else{
if(!_7.href){
return true;
}
var _a=Xinha.uniq("http://www.example.com/Link");
_6._doc.execCommand("createlink",false,_a);
var _b=_6._doc.getElementsByTagName("a");
for(var i=0;i<_b.length;i++){
var _c=_b[i];
if(_c.href==_a){
if(!a){
a=_c;
}
for(var j in _7){
_c.setAttribute(j,_7[j]);
}
}
}
}
_6.selectNodeContents(a);
_6.updateToolbar();
};
CreateLink.prototype._getSelectedAnchor=function(){
var _e=this.editor.getSelection();
var _f=this.editor.createRange(_e);
var a=this.editor.activeElement(_e);
if(a!=null&&a.tagName.toLowerCase()=="a"){
return a;
}else{
a=this.editor._getFirstAncestor(_e,"a");
if(a!=null){
return a;
}
}
return null;
};

