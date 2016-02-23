/* This compressed file is part of Xinha. For uncompressed sources, forum, and bug reports, go to xinha.org */
/* The URL of the most recent version of this file is http://svn.xinha.org/trunk/modules/InsertTable/insert_table.js */
InsertTable._pluginInfo={name:"InsertTable",origin:"Xinha Core",version:"$LastChangedRevision: 1085 $".replace(/^[^:]*:\s*(.*)\s*\$$/,"$1"),developer:"The Xinha Core Developer Team",developer_url:"$HeadURL: http://svn.xinha.org/tags/0.96.1/modules/InsertTable/insert_table.js $".replace(/^[^:]*:\s*(.*)\s*\$$/,"$1"),sponsor:"",sponsor_url:"",license:"htmlArea"};
function InsertTable(_1){
this.editor=_1;
var _2=_1.config;
var _3=this;
_1.config.btnList.inserttable[3]=function(){
_3.show();
};
};
InsertTable.prototype._lc=function(_4){
return Xinha._lc(_4,"Xinha");
};
InsertTable.prototype.onGenerateOnce=function(){
InsertTable.loadAssets();
};
InsertTable.loadAssets=function(){
var _5=InsertTable;
if(_5.loading){
return;
}
_5.loading=true;
Xinha._getback(_editor_url+"modules/InsertTable/dialog.html",function(_6){
_5.html=_6;
_5.dialogReady=true;
});
Xinha._getback(_editor_url+"modules/InsertTable/pluginMethods.js",function(_7){
eval(_7);
_5.methodsReady=true;
});
};
InsertTable.prototype.onUpdateToolbar=function(){
if(!(InsertTable.dialogReady&&InsertTable.methodsReady)){
this.editor._toolbarObjects.inserttable.state("enabled",false);
}else{
this.onUpdateToolbar=null;
}
};
InsertTable.prototype.prepareDialog=function(){
var _8=this;
var _9=this.editor;
var _a=this.dialog=new Xinha.Dialog(_9,InsertTable.html,"Xinha",{width:400});
_a.getElementById("ok").onclick=function(){
_8.apply();
};
_a.getElementById("cancel").onclick=function(){
_8.dialog.hide();
};
this.borderColorPicker=new Xinha.colorPicker.InputBinding(_a.getElementById("border_color"));
this.dialog.onresize=function(){
this.getElementById("layout_fieldset").style.width=(this.width/2)+50+"px";
this.getElementById("spacing_fieldset").style.width=(this.width/2)-120+"px";
};
this.dialogReady=true;
};

