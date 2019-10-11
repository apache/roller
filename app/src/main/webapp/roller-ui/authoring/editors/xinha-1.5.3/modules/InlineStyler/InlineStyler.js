Xinha.InlineStyler = function(element, editor, dialog, doc)
{
  this.element = element;
  this.editor = editor;
  this.dialog = dialog;
  this.doc = doc ? doc : document;
  this.inputs = {
    styles : {},
    aux : {}
  }
  this.styles = {};
  this.auxData = {}; //units and such
}

Xinha.InlineStyler.getLength = function(value)
{
  var len = parseInt(value);
  if (isNaN(len)) 
  {
    len = "";
  }
  return len;
};

// Applies the style found in "params" to the given element.
Xinha.InlineStyler.prototype.applyStyle = function(params, _ifMatchRe, _exceptMatchRe)
{
  var element = this.element;
  var style = element.style;

  for (var i in params) 
  {
    if (typeof params[i] == 'function') 
      continue;
    if (params[i] != null)
      var val = params[i].value || params[i];
    
    if(_ifMatchRe && !i.match(_ifMatchRe))        continue;
    if(_exceptMatchRe && i.match(_exceptMatchRe)) continue;
    
    switch (i)
    {
    case "backgroundImage":
      if (/\S/.test(val)) 
      {
        style.backgroundImage = "url(" + val + ")";
      }
      else 
      {
        style.backgroundImage = "none";
      }
      break;
    case "borderCollapse":
      style.borderCollapse = params[i] == "on" ? "collapse" : "separate";
      break;
    case "width":
      if (/\S/.test(val)) 
      {
        style.width = val + this.inputs.aux["widthUnit"].value;
      }
      else 
      {
        style.width = "";
      }
      break;
    case "height":
      if (/\S/.test(val)) 
      {
        style.height = val + this.inputs.aux["heightUnit"].value;
      }
      else 
      {
        style.height = "";
      }
      break;
    case "textAlign":
      if (val == "char") 
      {
        var ch = this.inputs.aux["textAlignChar"].value;
        if (ch == '"') 
        {
          ch = '\\"';
        }
        style.textAlign = '"' + ch + '"';
      }
      else 
        if (val == "-") 
      {
        style.textAlign = "";
      }
      else 
      {
        style.textAlign = val;
      }
      break;
    case "verticalAlign":
      element.vAlign = "";
      if (val == "-") 
      {
        style.verticalAlign = "";
        
      }
      else 
      {
        style.verticalAlign = val;
      }
      break;
    case "float":
      if (Xinha.is_ie) {
        style.styleFloat = val;
      }
      else {
        style.cssFloat = val;
      }
      break;
    case "borderWidth":
      style[i] = val ? val + "px" : '0px';
      break;
    default:      
      style[i] = val;
      break;
      // 		    case "f_st_margin":
      // 			style.margin = val + "px";
      // 			break;
      // 		    case "f_st_padding":
      // 			style.padding = val + "px";
      // 			break;
    }
  }
};

Xinha.InlineStyler.prototype.applyStyleExceptMatch = function(params, exceptMatchRe)
{
  return this.applyStyle(params, null, exceptMatchRe);
};

Xinha.InlineStyler.prototype.applyStyleIfMatch    = function(params, ifMatchRe)
{
  return this.applyStyle(params, ifMatchRe);
};

Xinha.InlineStyler.prototype.createStyleLayoutFieldset = function()
{
  var self = this;
  var editor = this.editor;
  var doc = this.doc;
  var el = this.element;
  var fieldset = doc.createElement("fieldset");
  var legend = doc.createElement("legend");
  fieldset.appendChild(legend);
  legend.innerHTML = Xinha._lc("Layout", "InlineStyler");
  var table = doc.createElement("table");
  fieldset.appendChild(table);
  table.style.width = "100%";
  var tbody = doc.createElement("tbody");
  table.appendChild(tbody);
  
  var tagname = el.tagName.toLowerCase();
  var tr, td, input, select, option, options, i;
  
  if (tagname != "td" && tagname != "tr" && tagname != "th") 
  {
    tr = doc.createElement("tr");
    tbody.appendChild(tr);
    td = doc.createElement("td");
    td.className = "label";
    tr.appendChild(td);
    td.innerHTML = Xinha._lc("Float", "InlineStyler") + ":";
    td = doc.createElement("td");
    tr.appendChild(td);
    select = doc.createElement("select");
    select.name = this.dialog.createId("float");
    td.appendChild(select);
    this.inputs.styles['float'] = select;
    
    options = ["None", "Left", "Right"];
    /* For lc_parse_strings.php
   
      Xinha._lc("None", "InlineStyler");
      Xinha._lc("Left", "InlineStyler");
      Xinha._lc("Right", "InlineStyler");
      
    */
    for (var i = 0; i < options.length; ++i) 
    {
      var Val = options[i];
      var val = options[i].toLowerCase();
      option = doc.createElement("option");
      option.innerHTML = Xinha._lc(Val, "InlineStyler");
      option.value = val;
      if (Xinha.is_ie) {
        option.selected = (("" + el.style.styleFloat).toLowerCase() == val);
      }
      else {
        option.selected = (("" + el.style.cssFloat).toLowerCase() == val);
      }
      select.appendChild(option);
    }
  }
  
  tr = doc.createElement("tr");
  tbody.appendChild(tr);
  td = doc.createElement("td");
  td.className = "label";
  tr.appendChild(td);
  td.innerHTML = Xinha._lc("Width", "InlineStyler") + ":";
  td = doc.createElement("td");
  tr.appendChild(td);
  input = doc.createElement("input");
  input.name = this.dialog.createId("width");
  input.type = "text";
  input.value = Xinha.InlineStyler.getLength(el.style.width);
  input.size = "5";
  this.inputs.styles['width'] = input;
  input.style.marginRight = "0.5em";
  td.appendChild(input);
  select = doc.createElement("select");
  select.name = this.dialog.createId("widthUnit");
  this.inputs.aux['widthUnit'] = select;
  option = doc.createElement("option");
  option.innerHTML = Xinha._lc("percent", "InlineStyler");
  option.value = "%";
  option.selected = /%/.test(el.style.width);
  select.appendChild(option);
  option = doc.createElement("option");
  option.innerHTML = Xinha._lc("pixels", "InlineStyler");
  option.value = "px";
  option.selected = /px/.test(el.style.width);
  select.appendChild(option);
  td.appendChild(select);
  
  select.style.marginRight = "0.5em";
  td.appendChild(doc.createTextNode(Xinha._lc("Text align", "InlineStyler") + ":"));
  select = doc.createElement("select");
  select.name = this.dialog.createId("textAlign");
  select.style.marginLeft = select.style.marginRight = "0.5em";
  td.appendChild(select);
  this.inputs.styles['textAlign'] = select;
  options = ["Left", "Center", "Right", "Justify", "-"];
  if (tagname == "td") 
  {
    options.push("Char");
  }
  
  /* For lc_parse_strings.php
  
    Xinha._lc("Left", "InlineStyler");
    Xinha._lc("Center", "InlineStyler");
    Xinha._lc("Right", "InlineStyler");
    Xinha._lc("Justify", "InlineStyler");
    Xinha._lc("-", "InlineStyler");
    Xinha._lc("Char", "InlineStyler");
    
  */
  
  input = doc.createElement("input");
  this.inputs.aux['textAlignChar'] = input;
  input.name= this.dialog.createId("textAlignChar");
  input.size = "1";
  input.style.fontFamily = "monospace";
  td.appendChild(input);
  
  for (var i = 0; i < options.length; ++i) 
  {
    var Val = options[i];
    var val = Val.toLowerCase();
    option = doc.createElement("option");
    option.value = val;
    option.innerHTML = Xinha._lc(Val, "InlineStyler");
    option.selected = ((el.style.textAlign.toLowerCase() == val) || (el.style.textAlign == "" && Val == "-"));
    select.appendChild(option);
  }
  var textAlignCharInput = input;
  function setCharVisibility(value)
  {
    textAlignCharInput.style.visibility = value ? "visible" : "hidden";
    if (value) 
    {
      textAlignCharInput.focus();
      textAlignCharInput.select();
    }
  }
  select.onchange = function()
  {
    setCharVisibility(this.value == "char");
  };
  setCharVisibility(select.value == "char");
  
  tr = doc.createElement("tr");
  tbody.appendChild(tr);
  td = doc.createElement("td");
  td.className = "label";
  tr.appendChild(td);
  td.innerHTML = Xinha._lc("Height", "InlineStyler") + ":";
  td = doc.createElement("td");
  tr.appendChild(td);
  input = doc.createElement("input");
  input.name = this.dialog.createId("height");
  input.type = "text";
  input.value = Xinha.InlineStyler.getLength(el.style.height);
  input.size = "5";
  this.inputs.styles['height'] = input;
  input.style.marginRight = "0.5em";
  td.appendChild(input);
  select = doc.createElement("select");
  select.name = this.dialog.createId("heightUnit");
  this.inputs.aux['heightUnit'] = select;
  option = doc.createElement("option");
  option.innerHTML = Xinha._lc("percent", "InlineStyler");
  option.value = "%";
  option.selected = /%/.test(el.style.height);
  select.appendChild(option);
  option = doc.createElement("option");
  option.innerHTML = Xinha._lc("pixels", "InlineStyler");
  option.value = "px";
  option.selected = /px/.test(el.style.height);
  select.appendChild(option);
  td.appendChild(select);
  
  select.style.marginRight = "0.5em";
  td.appendChild(doc.createTextNode(Xinha._lc("Vertical align", "InlineStyler") + ":"));
  select = doc.createElement("select");
  select.name = this.dialog.createId("verticalAlign");
  this.inputs.styles['verticalAlign'] = select;
  select.style.marginLeft = "0.5em";
  td.appendChild(select);
  options = ["Top", "Middle", "Bottom", "Baseline", "-"];
  /* For lc_parse_strings.php
   
   Xinha._lc("Top", "InlineStyler");
   Xinha._lc("Middle", "InlineStyler");
   Xinha._lc("Bottom", "InlineStyler");
   Xinha._lc("Baseline", "InlineStyler");
   Xinha._lc("-", "InlineStyler");
   
   */
  for (var i = 0; i < options.length; ++i) 
  {
    var Val = options[i];
    var val = Val.toLowerCase();
    option = doc.createElement("option");
    option.value = val;
    option.innerHTML = Xinha._lc(Val, "InlineStyler");
    option.selected = ((el.style.verticalAlign.toLowerCase() == val) || (el.style.verticalAlign == "" && Val == "-"));
    select.appendChild(option);
  }
  
  return fieldset;
};

// Returns an HTML element containing the style attributes for the given
// element.  This can be easily embedded into any dialog; the functionality is
// also provided.
Xinha.InlineStyler.prototype.createStyleFieldset = function()
{
  var editor = this.editor;
  var doc = this.doc;
  var el = this.element;
  
  var fieldset = doc.createElement("fieldset");
  var legend = doc.createElement("legend");
  fieldset.appendChild(legend);
  legend.innerHTML = Xinha._lc("CSS Style", "InlineStyler");
  var table = doc.createElement("table");
  fieldset.appendChild(table);
  table.style.width = "100%";
  var tbody = doc.createElement("tbody");
  table.appendChild(tbody);
  
  var tr, td, input, select, option, options, i;
  
  tr = doc.createElement("tr");
  tbody.appendChild(tr);
  td = doc.createElement("td");
  tr.appendChild(td);
  td.className = "label";
  td.innerHTML = Xinha._lc("Background", "InlineStyler") + ":";
  td = doc.createElement("td");
  tr.appendChild(td);

  input = doc.createElement("input");
  input.name = this.dialog.createId("backgroundColor");
  input.value = Xinha._colorToRgb( el.style.backgroundColor );
  input.type = "hidden";
  this.inputs.styles['backgroundColor'] = input;
  input.style.marginRight = "0.5em";
  td.appendChild(input);
  new Xinha.colorPicker.InputBinding(input)
  
  td.appendChild(doc.createTextNode(" " + Xinha._lc("Image URL", "InlineStyler") + ": "));
  input = doc.createElement("input");
  input.name = this.dialog.createId("backgroundImage");
  input.type = "text";
  this.inputs.styles['backgroundImage'] = input;
  if (el.style.backgroundImage.match(/url\(\s*(.*?)\s*\)/))
    input.value = RegExp.$1;
  // input.style.width = "100%";
  td.appendChild(input);
  
  tr = doc.createElement("tr");
  tbody.appendChild(tr);
  td = doc.createElement("td");
  tr.appendChild(td);
  td.className = "label";
  td.innerHTML = Xinha._lc("FG Color", "InlineStyler") + ":";
  td = doc.createElement("td");
  tr.appendChild(td);
  input = doc.createElement("input");
  input.name = this.dialog.createId("color");
  input.value = Xinha._colorToRgb( el.style.color );
  input.type = "hidden";
  this.inputs.styles['color'] = input;
  input.style.marginRight = "0.5em";
  td.appendChild(input);
  new Xinha.colorPicker.InputBinding(input)
  
  // for better alignment we include an invisible field.
  input = doc.createElement("input");
  input.style.visibility = "hidden";
  input.type = "text";
  td.appendChild(input);
  
  tr = doc.createElement("tr");
  tbody.appendChild(tr);
  td = doc.createElement("td");
  tr.appendChild(td);
  td.className = "label";
  td.innerHTML = Xinha._lc("Border", "InlineStyler") + ":";
  td = doc.createElement("td");
  tr.appendChild(td);
  input = doc.createElement("input");
  var borderColourInput = input;
  input.name = this.dialog.createId("borderColor");
  input.value = Xinha._colorToRgb( el.style.borderColor );
  input.type = "hidden";
  this.inputs.styles['borderColor'] = input;
  input.style.marginRight = "0.5em";
  td.appendChild(input);
  new Xinha.colorPicker.InputBinding(input)
  
  select = doc.createElement("select");
  var borderSelect = select;  
  select.name = this.dialog.createId("borderStyle");
  var borderFields = [];
  td.appendChild(select);
  this.inputs.styles['borderStyle'] = select;
  options = ["none", "dotted", "dashed", "solid", "double", "groove", "ridge", "inset", "outset"];
  var currentBorderStyle = el.style.borderStyle;
  // Gecko reports "solid solid solid solid" for "border-style: solid".
  // That is, "top right bottom left" -- we only consider the first
  // value.
  if (currentBorderStyle.match(/([^\s]*)\s/)) currentBorderStyle = RegExp.$1;
  for (var i=0;i<options.length;i++) {
    var val = options[i];
    option = doc.createElement("option");
    option.value = val;
    option.innerHTML = val;
    if (val == currentBorderStyle) option.selected = true;
    select.appendChild(option);
  }
  select.style.marginRight = "0.5em";
  function setBorderFieldsStatus(value)
  {
    for (var i = 0; i < borderFields.length; ++i) 
    {
      var el = borderFields[i];
      el.style.visibility = value ? "hidden" : "visible";
      if (!value && (el.tagName.toLowerCase() == "input")) 
      {
        el.focus();
        el.select();
      }
    }
  }
  select.onchange = function()
  {
    setBorderFieldsStatus(this.value == "none");
  };
  

  
  input = doc.createElement("input");
  var borderWidthInput = input
  input.name = this.dialog.createId("borderWidth");
  borderFields.push(input);
  input.type = "text";
  this.inputs.styles['borderWidth'] = input;
  input.value = Xinha.InlineStyler.getLength(el.style.borderWidth);
  input.size = "5";
  td.appendChild(input);
  input.style.marginRight = "0.5em";
  var span = doc.createElement("span");
  span.innerHTML = Xinha._lc("pixels", "InlineStyler");
  td.appendChild(span);
  borderFields.push(span);
  
  setBorderFieldsStatus(select.value == "none");
  
  // if somebody changes the border colour, and the border Style is not set, set it
  // because otherwise they might not do that and get confused
  borderColourInput.oncolorpicked = function(){
    if(borderSelect.selectedIndex == 0)
    {
      borderSelect.selectedIndex = 3;
      borderSelect.onchange();
    } 
    
    if(!borderWidthInput.value.length) 
    {
      borderWidthInput.value = 1;
    }
  };
  
  if (el.tagName.toLowerCase() == "table") 
  {
    // the border-collapse style is only for tables
    tr = doc.createElement("tr");
    tbody.appendChild(tr);
    td = doc.createElement("td");
    td.className = "label";
    tr.appendChild(td);
    input = doc.createElement("input");
    input.name = this.dialog.createId("borderCollapse");
    input.type = "checkbox";
    input.value = "on";
    this.inputs.styles['borderCollapse'] = input;
    input.id = "f_st_borderCollapse";
    var val = (/collapse/i.test(el.style.borderCollapse));
    input.checked = val ? 1 : 0;
    td.appendChild(input);
    
    td = doc.createElement("td");
    tr.appendChild(td);
    var label = doc.createElement("label");
    label.htmlFor = "f_st_borderCollapse";
    label.innerHTML = Xinha._lc("Collapsed borders", "InlineStyler");
    td.appendChild(label);
  }
  
  // 	tr = doc.createElement("tr");
  // 	tbody.appendChild(tr);
  // 	td = doc.createElement("td");
  // 	td.className = "label";
  // 	tr.appendChild(td);
  // 	td.innerHTML = Xinha._lc("Margin", "InlineStyler") + ":";
  // 	td = doc.createElement("td");
  // 	tr.appendChild(td);
  // 	input = doc.createElement("input");
  // 	input.type = "text";
  // 	input.size = "5";
  // 	input.name = "f_st_margin";
  // 	td.appendChild(input);
  // 	input.style.marginRight = "0.5em";
  // 	td.appendChild(doc.createTextNode(Xinha._lc("Padding", "InlineStyler") + ":"));
  
  // 	input = doc.createElement("input");
  // 	input.type = "text";
  // 	input.size = "5";
  // 	input.name = "f_st_padding";
  // 	td.appendChild(input);
  // 	input.style.marginLeft = "0.5em";
  // 	input.style.marginRight = "0.5em";
  // 	td.appendChild(doc.createTextNode(Xinha._lc("pixels", "InlineStyler")));
  
  return fieldset;
};
