/* This compressed file is part of Xinha. For uncompressed sources, forum, and bug reports, go to xinha.org */
/* The URL of the most recent version of this file is http://svn.xinha.org/trunk/modules/InsertImage/insert_image.js */
InsertImage._pluginInfo={name:"InsertImage",origin:"Xinha Core",version:"$LastChangedRevision: 1239 $".replace(/^[^:]*:\s*(.*)\s*\$$/,"$1"),developer:"The Xinha Core Developer Team",developer_url:"$HeadURL: http://svn.xinha.org/tags/0.96.1/modules/InsertImage/insert_image.js $".replace(/^[^:]*:\s*(.*)\s*\$$/,"$1"),sponsor:"",sponsor_url:"",license:"htmlArea"};
function InsertImage(_1){
this.editor=_1;
var _2=_1.config;
var _3=this;
if(typeof _1._insertImage=="undefined"){
_1._insertImage=function(){
_3.show();
};
}
};
InsertImage.prototype._lc=function(_4){
return Xinha._lc(_4,"Xinha");
};
InsertImage.prototype.onGenerateOnce=function(){
InsertImage.loadAssets();
};
InsertImage.loadAssets=function(){
var _5=InsertImage;
if(_5.loading){
return;
}
_5.loading=true;
Xinha._getback(_editor_url+"modules/InsertImage/dialog.html",function(_6){
_5.html=_6;
_5.dialogReady=true;
});
Xinha._getback(_editor_url+"modules/InsertImage/pluginMethods.js",function(_7){
eval(_7);
_5.methodsReady=true;
});
};
InsertImage.prototype.onUpdateToolbar=function(){
if(!(InsertImage.dialogReady&&InsertImage.methodsReady)){
this.editor._toolbarObjects.insertimage.state("enabled",false);
}else{
this.onUpdateToolbar=null;
}
};
InsertImage.prototype.prepareDialog=function(){
var _8=this;
var _9=this.editor;
var _a=this.dialog=new Xinha.Dialog(_9,InsertImage.html,"Xinha",{width:410});
_a.getElementById("ok").onclick=function(){
_8.apply();
};
_a.getElementById("cancel").onclick=function(){
_8.dialog.hide();
};
_a.getElementById("preview").onclick=function(){
var _b=_a.getElementById("f_url");
var _c=_b.value;
if(!_c){
alert(_a._lc("You must enter the URL"));
_b.focus();
return false;
}
_a.getElementById("ipreview").src=_c;
return false;
};
this.dialog.onresize=function(){
var _d=parseInt(this.height,10)-this.getElementById("h1").offsetHeight-this.getElementById("buttons").offsetHeight-this.getElementById("inputs").offsetHeight-parseInt(this.rootElem.style.paddingBottom,10);
this.getElementById("ipreview").style.height=((_d>0)?_d:0)+"px";
this.getElementById("ipreview").style.width=this.width-2+"px";
};
this.dialogReady=true;
};

