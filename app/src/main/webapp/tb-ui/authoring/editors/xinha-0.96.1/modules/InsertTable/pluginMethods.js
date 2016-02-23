/* This compressed file is part of Xinha. For uncompressed sources, forum, and bug reports, go to xinha.org */
/* The URL of the most recent version of this file is http://svn.xinha.org/trunk/modules/InsertTable/pluginMethods.js */
InsertTable.prototype.show=function(_1){
if(!this.dialog){
this.prepareDialog();
}
var _2=this.editor;
var _3={"caption":"","rows":"2","cols":"4","width":"100","unit":"%","fixed":"","align":"","border":"1","border_style":"dotted","border_color":"#000000","border_collapse":"on","spacing":"","padding":"5"};
this.borderColorPicker.setColor("#000000");
this.dialog.show(_3);
this.dialog.onresize();
};
InsertTable.prototype.apply=function(){
var _4=this.editor;
var _5=_4._doc;
var _6=this.dialog.getValues();
if(!_6.rows||!_6.cols){
if(!_6.rows){
this.dialog.getElementById("rows_alert").style.display="";
}
if(!_6.cols){
this.dialog.getElementById("columns_alert").style.display="";
}
return;
}
this.dialog.hide();
var _7=_5.createElement("table");
for(var _8 in _6){
var _9=_6[_8];
if(!_9){
continue;
}
switch(_8){
case "width":
_7.style.width=_9+_6.unit.value;
break;
case "align":
_7.align=_9.value;
break;
case "border":
_7.style.border=_9+"px "+_6.border_style.value+" "+_6.border_color;
break;
case "border_collapse":
_7.style.borderCollapse=(_9=="on")?"collapse":"";
break;
case "spacing":
_7.cellSpacing=parseInt(_9,10);
break;
case "padding":
_7.cellPadding=parseInt(_9,10);
break;
}
}
if(_6.caption){
var _a=_7.createCaption();
_a.appendChild(_5.createTextNode(_6.caption));
}
var _b=0;
if(_6.fixed){
_b=Math.floor(100/parseInt(_6.cols,10));
}
var _c=_5.createElement("tbody");
_7.appendChild(_c);
for(var i=0;i<_6.rows;++i){
var tr=_5.createElement("tr");
_c.appendChild(tr);
for(var j=0;j<_6.cols;++j){
var td=_5.createElement("td");
if(_b&&i===0){
td.style.width=_b+"%";
}
if(_6.border){
td.style.border=_6.border+"px "+_6.border_style.value+" "+_6.border_color;
}
tr.appendChild(td);
td.appendChild(_5.createTextNode(" "));
}
}
_4.insertNodeAtSelection(_7);
};

