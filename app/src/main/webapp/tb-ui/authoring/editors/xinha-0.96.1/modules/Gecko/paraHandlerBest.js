/* This compressed file is part of Xinha. For uncompressed sources, forum, and bug reports, go to xinha.org */
/* The URL of the most recent version of this file is http://svn.xinha.org/trunk/modules/Gecko/paraHandlerBest.js */
EnterParagraphs._pluginInfo={name:"EnterParagraphs",version:"1.0",developer:"Adam Wright",developer_url:"http://www.hipikat.org/",sponsor:"The University of Western Australia",sponsor_url:"http://www.uwa.edu.au/",license:"htmlArea"};
EnterParagraphs.prototype._whiteSpace=/^\s*$/;
EnterParagraphs.prototype._pExclusions=/^(address|blockquote|body|dd|div|dl|dt|fieldset|form|h1|h2|h3|h4|h5|h6|hr|li|noscript|ol|p|pre|table|ul)$/i;
EnterParagraphs.prototype._pContainers=/^(body|del|div|fieldset|form|ins|map|noscript|object|td|th)$/i;
EnterParagraphs.prototype._pBreak=/^(address|pre|blockquote)$/i;
EnterParagraphs.prototype._permEmpty=/^(area|base|basefont|br|col|frame|hr|img|input|isindex|link|meta|param)$/i;
EnterParagraphs.prototype._elemSolid=/^(applet|br|button|hr|img|input|table)$/i;
EnterParagraphs.prototype._pifySibling=/^(address|blockquote|del|div|dl|fieldset|form|h1|h2|h3|h4|h5|h6|hr|ins|map|noscript|object|ol|p|pre|table|ul|)$/i;
EnterParagraphs.prototype._pifyForced=/^(ul|ol|dl|table)$/i;
EnterParagraphs.prototype._pifyParent=/^(dd|dt|li|td|th|tr)$/i;
function EnterParagraphs(_1){
this.editor=_1;
if(Xinha.is_gecko){
this.onKeyPress=this.__onKeyPress;
}
};
EnterParagraphs.prototype.name="EnterParagraphs";
EnterParagraphs.prototype.insertAdjacentElement=function(_2,_3,el){
if(_3=="BeforeBegin"){
_2.parentNode.insertBefore(el,_2);
}else{
if(_3=="AfterEnd"){
_2.nextSibling?_2.parentNode.insertBefore(el,_2.nextSibling):_2.parentNode.appendChild(el);
}else{
if(_3=="AfterBegin"&&_2.firstChild){
_2.insertBefore(el,_2.firstChild);
}else{
if(_3=="BeforeEnd"||_3=="AfterBegin"){
_2.appendChild(el);
}
}
}
}
};
EnterParagraphs.prototype.forEachNodeUnder=function(_5,_6,_7,_8){
var _9,_a;
if(_5.nodeType==11&&_5.firstChild){
_9=_5.firstChild;
_a=_5.lastChild;
}else{
_9=_a=_5;
}
while(_a.lastChild){
_a=_a.lastChild;
}
return this.forEachNode(_9,_a,_6,_7,_8);
};
EnterParagraphs.prototype.forEachNode=function(_b,_c,_d,_e,_f){
var _10=function(_11,_12){
return (_12=="ltr"?_11.nextSibling:_11.previousSibling);
};
var _13=function(_14,_15){
return (_15=="ltr"?_14.firstChild:_14.lastChild);
};
var _16,_17,_18;
var _19=_f;
var _1a=false;
while(_16!=_e=="ltr"?_c:_b){
if(!_16){
_16=_e=="ltr"?_b:_c;
}else{
if(_13(_16,_e)){
_16=_13(_16,_e);
}else{
if(_10(_16,_e)){
_16=_10(_16,_e);
}else{
_17=_16;
while(!_10(_17,_e)&&_17!=(_e=="ltr"?_c:_b)){
_17=_17.parentNode;
}
_16=(_10(_17,_e)?_10(_17,_e):_17);
}
}
}
_1a=(_16==(_e=="ltr"?_c:_b));
switch(_d){
case "cullids":
_18=this._fenCullIds(_16,_19);
break;
case "find_fill":
_18=this._fenEmptySet(_16,_19,_d,_1a);
break;
case "find_cursorpoint":
_18=this._fenEmptySet(_16,_19,_d,_1a);
break;
}
if(_18[0]){
return _18[1];
}
if(_1a){
break;
}
if(_18[1]){
_19=_18[1];
}
}
return false;
};
EnterParagraphs.prototype._fenEmptySet=function(_1b,_1c,_1d,_1e){
if(!_1c&&!_1b.firstChild){
_1c=_1b;
}
if((_1b.nodeType==1&&this._elemSolid.test(_1b.nodeName))||(_1b.nodeType==3&&!this._whiteSpace.test(_1b.nodeValue))||(_1b.nodeType!=1&&_1b.nodeType!=3)){
switch(_1d){
case "find_fill":
return new Array(true,false);
break;
case "find_cursorpoint":
return new Array(true,_1b);
break;
}
}
if(_1e){
return new Array(true,_1c);
}
return new Array(false,_1c);
};
EnterParagraphs.prototype._fenCullIds=function(_1f,_20,_21){
if(_20.id){
_21[_20.id]?_20.id="":_21[_20.id]=true;
}
return new Array(false,_21);
};
EnterParagraphs.prototype.processSide=function(rng,_23){
var _24=function(_25,_26){
return (_26=="left"?_25.previousSibling:_25.nextSibling);
};
var _27=_23=="left"?rng.startContainer:rng.endContainer;
var _28=_23=="left"?rng.startOffset:rng.endOffset;
var _29,_2a=_27;
while(_2a.nodeType==1&&!this._permEmpty.test(_2a.nodeName)){
_2a=(_28?_2a.lastChild:_2a.firstChild);
}
while(_29=_29?(_24(_29,_23)?_24(_29,_23):_29.parentNode):_2a){
if(_24(_29,_23)){
if(this._pExclusions.test(_24(_29,_23).nodeName)){
return this.processRng(rng,_23,_29,_24(_29,_23),(_23=="left"?"AfterEnd":"BeforeBegin"),true,false);
}
}else{
if(this._pContainers.test(_29.parentNode.nodeName)){
return this.processRng(rng,_23,_29,_29.parentNode,(_23=="left"?"AfterBegin":"BeforeEnd"),true,false);
}else{
if(this._pExclusions.test(_29.parentNode.nodeName)){
if(this._pBreak.test(_29.parentNode.nodeName)){
return this.processRng(rng,_23,_29,_29.parentNode,(_23=="left"?"AfterBegin":"BeforeEnd"),false,(_23=="left"?true:false));
}else{
return this.processRng(rng,_23,(_29=_29.parentNode),(_24(_29,_23)?_24(_29,_23):_29.parentNode),(_24(_29,_23)?(_23=="left"?"AfterEnd":"BeforeBegin"):(_23=="left"?"AfterBegin":"BeforeEnd")),false,false);
}
}
}
}
}
};
EnterParagraphs.prototype.processRng=function(rng,_2c,_2d,_2e,_2f,_30,_31){
var _32=_2c=="left"?rng.startContainer:rng.endContainer;
var _33=_2c=="left"?rng.startOffset:rng.endOffset;
var _34=this.editor;
var _35=_34._doc.createRange();
_35.selectNode(_2d);
if(_2c=="left"){
_35.setEnd(_32,_33);
rng.setStart(_35.startContainer,_35.startOffset);
}else{
if(_2c=="right"){
_35.setStart(_32,_33);
rng.setEnd(_35.endContainer,_35.endOffset);
}
}
var cnt=_35.cloneContents();
this.forEachNodeUnder(cnt,"cullids","ltr",this.takenIds,false,false);
var _37,_38,_39;
_37=_2c=="left"?(_35.endContainer.nodeType==3?true:false):(_35.startContainer.nodeType==3?false:true);
_38=_37?_35.startOffset:_35.endOffset;
_37=_37?_35.startContainer:_35.endContainer;
if(this._pifyParent.test(_37.nodeName)&&_37.parentNode.childNodes.item(0)==_37){
while(!this._pifySibling.test(_37.nodeName)){
_37=_37.parentNode;
}
}
if(cnt.nodeType==11&&!cnt.firstChild){
if(_37.nodeName!="BODY"||(_37.nodeName=="BODY"&&_38!=0)){
cnt.appendChild(_34._doc.createElement(_37.nodeName));
}
}
_39=this.forEachNodeUnder(cnt,"find_fill","ltr",false);
if(_39&&this._pifySibling.test(_37.nodeName)&&((_38==0)||(_38==1&&this._pifyForced.test(_37.nodeName)))){
_2d=_34._doc.createElement("p");
_2d.innerHTML="&nbsp;";
if((_2c=="left")&&_37.previousSibling){
return new Array(_37.previousSibling,"AfterEnd",_2d);
}else{
if((_2c=="right")&&_37.nextSibling){
return new Array(_37.nextSibling,"BeforeBegin",_2d);
}else{
return new Array(_37.parentNode,(_2c=="left"?"AfterBegin":"BeforeEnd"),_2d);
}
}
}
if(_39){
if(_39.nodeType==3){
_39=_34._doc.createDocumentFragment();
}
if((_39.nodeType==1&&!this._elemSolid.test())||_39.nodeType==11){
var _3a=_34._doc.createElement("p");
_3a.innerHTML="&nbsp;";
_39.appendChild(_3a);
}else{
var _3a=_34._doc.createElement("p");
_3a.innerHTML="&nbsp;";
_39.parentNode.insertBefore(parentNode,_39);
}
}
if(_39){
_2d=_39;
}else{
_2d=(_30||(cnt.nodeType==11&&!cnt.firstChild))?_34._doc.createElement("p"):_34._doc.createDocumentFragment();
_2d.appendChild(cnt);
}
if(_31){
_2d.appendChild(_34._doc.createElement("br"));
}
return new Array(_2e,_2f,_2d);
};
EnterParagraphs.prototype.isNormalListItem=function(rng){
var _3c,_3d;
_3c=rng.startContainer;
if((typeof _3c.nodeName!="undefined")&&(_3c.nodeName.toLowerCase()=="li")){
_3d=_3c;
}else{
if((typeof _3c.parentNode!="undefined")&&(typeof _3c.parentNode.nodeName!="undefined")&&(_3c.parentNode.nodeName.toLowerCase()=="li")){
_3d=_3c.parentNode;
}else{
return false;
}
}
if(!_3d.previousSibling){
if(rng.startOffset==0){
return false;
}
}
return true;
};
EnterParagraphs.prototype.__onKeyPress=function(ev){
if(ev.keyCode==13&&!ev.shiftKey&&this.editor._iframe.contentWindow.getSelection){
return this.handleEnter(ev);
}
};
EnterParagraphs.prototype.handleEnter=function(ev){
var _40;
var sel=this.editor.getSelection();
var rng=this.editor.createRange(sel);
if(this.isNormalListItem(rng)){
return true;
}
this.takenIds=new Object();
var _43=this.processSide(rng,"left");
var _44=this.processSide(rng,"right");
_40=_44[2];
sel.removeAllRanges();
rng.deleteContents();
var _45=this.forEachNodeUnder(_40,"find_cursorpoint","ltr",false,true);
if(!_45){
alert("INTERNAL ERROR - could not find place to put cursor after ENTER");
}
if(_43){
this.insertAdjacentElement(_43[0],_43[1],_43[2]);
}
if(_44&&_44.nodeType!=1){
this.insertAdjacentElement(_44[0],_44[1],_44[2]);
}
if((_45)&&(this._permEmpty.test(_45.nodeName))){
var _46=0;
while(_45.parentNode.childNodes.item(_46)!=_45){
_46++;
}
sel.collapse(_45.parentNode,_46);
}else{
try{
sel.collapse(_45,0);
if(_45.nodeType==3){
_45=_45.parentNode;
}
this.editor.scrollToElement(_45);
}
catch(e){
}
}
this.editor.updateToolbar();
Xinha._stopEvent(ev);
return true;
};

