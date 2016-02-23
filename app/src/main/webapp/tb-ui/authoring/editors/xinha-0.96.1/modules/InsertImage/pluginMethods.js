/* This compressed file is part of Xinha. For uncompressed sources, forum, and bug reports, go to xinha.org */
/* The URL of the most recent version of this file is http://svn.xinha.org/trunk/modules/InsertImage/pluginMethods.js */
InsertImage.prototype.show=function(_1){
if(!this.dialog){
this.prepareDialog();
}
var _2=this.editor;
if(typeof _1=="undefined"){
_1=_2.getParentElement();
if(_1&&_1.tagName.toLowerCase()!="img"){
_1=null;
}
}
if(_1){
function getSpecifiedAttribute(_3,_4){
var a=_3.attributes;
for(var i=0;i<a.length;i++){
if(a[i].nodeName==_4&&a[i].specified){
return a[i].value;
}
}
return "";
};
outparam={f_url:_2.stripBaseURL(_1.getAttribute("src",2)),f_alt:_1.alt,f_border:_1.border,f_align:_1.align,f_vert:getSpecifiedAttribute(_1,"vspace"),f_horiz:getSpecifiedAttribute(_1,"hspace"),f_width:_1.width,f_height:_1.height};
}else{
outparam={f_url:"",f_alt:"",f_border:"",f_align:"",f_vert:"",f_horiz:"",f_width:"",f_height:""};
}
this.image=_1;
this.dialog.show(outparam);
};
InsertImage.prototype.apply=function(){
var _7=this.dialog.hide();
if(!_7.f_url){
return;
}
var _8=this.editor;
var _9=this.image;
if(!_9){
if(Xinha.is_ie){
var _a=_8.getSelection();
var _b=_8.createRange(_a);
_8._doc.execCommand("insertimage",false,_7.f_url);
_9=_b.parentElement();
if(_9.tagName.toLowerCase()!="img"){
_9=_9.previousSibling;
}
}else{
_9=document.createElement("img");
_9.src=_7.f_url;
_8.insertNodeAtSelection(_9);
if(!_9.tagName){
_9=_b.startContainer.firstChild;
}
}
}else{
_9.src=_7.f_url;
}
for(var _c in _7){
var _d=_7[_c];
switch(_c){
case "f_alt":
if(_d){
_9.alt=_d;
}else{
_9.removeAttribute("alt");
}
break;
case "f_border":
if(_d){
_9.border=parseInt(_d||"0");
}else{
_9.removeAttribute("border");
}
break;
case "f_align":
if(_d.value){
_9.align=_d.value;
}else{
_9.removeAttribute("align");
}
break;
case "f_vert":
if(_d!=""){
_9.vspace=parseInt(_d||"0");
}else{
_9.removeAttribute("vspace");
}
break;
case "f_horiz":
if(_d!=""){
_9.hspace=parseInt(_d||"0");
}else{
_9.removeAttribute("hspace");
}
break;
case "f_width":
if(_d){
_9.width=parseInt(_d||"0");
}else{
_9.removeAttribute("width");
}
break;
case "f_height":
if(_d){
_9.height=parseInt(_d||"0");
}else{
_9.removeAttribute("height");
}
break;
}
}
};

