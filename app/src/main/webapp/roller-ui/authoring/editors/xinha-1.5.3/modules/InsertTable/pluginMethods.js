InsertTable.prototype.show = function(image)
{
  if (!this.dialog) this.prepareDialog();

  var editor = this.editor;

  var values = 
  {
    "caption"          : '',
    "rows"             : '2',
    "cols"             : '4',
    "width"            : '100',
    "unit"             : '%',
    "fixed"            : '',
    "align"            : '',
    "border"           : '',
    "border_style"     : '',
    "border_color"     : '',
    "border_collapse"  : 'on',
    "spacing"          : '',
    "padding"          : '5'
  }
  // update the color of the picker manually
  this.borderColorPicker.setColor('#000000');
  // now calling the show method of the Xinha.Dialog object to set the values and show the actual dialog
  this.dialog.show(values);
  this.dialog.onresize();
};

InsertTable.prototype.apply = function()
{
  var editor = this.editor;
  var doc = editor._doc;
  var param = this.dialog.getValues();
  
  if (!param.rows || !param.cols)
  {
    if (!param.rows)
    {
      this.dialog.getElementById("rows_alert").style.display = '';
    }
    if (!param.cols)
    {
      this.dialog.getElementById("columns_alert").style.display = '';
    }
    return;
  }
  // selection is only restored on dialog.hide()
  this.dialog.hide();
  // create the table element
  var table = doc.createElement("table");
  // assign the given arguments
  
  for ( var field in param )
  {
    var value = param[field];
    if ( !value )
    {
      continue;
    }
    switch (field)
    {
      case "width":
      table.style.width = value + param.unit.value;
      break;
      case "align":
      table.align = value.value;
      break;
      case "border":
      table.style.border = value + 'px ' + param.border_style.value + ' ' + param.border_color;
      break;
      case "border_collapse":
      table.style.borderCollapse = (value == 'on') ? 'collapse' : '' ;
      break;
      case "spacing":
      table.cellSpacing = parseInt(value, 10);
      break;
      case "padding":
      table.cellPadding = parseInt(value, 10);
      break;
    }
  }
  if (param.caption)
  {
    var caption = table.createCaption();
    caption.appendChild(doc.createTextNode(param.caption));
   }
  var cellwidth = 0;
  if ( param.fixed )
  {
    cellwidth = Math.floor(100 / parseInt(param.cols, 10));
  }
  var tbody = doc.createElement("tbody");
  table.appendChild(tbody);
  for ( var i = 0; i < param.rows; ++i )
  {
    var tr = doc.createElement("tr");
    tbody.appendChild(tr);
    for ( var j = 0; j < param.cols; ++j )
    {
      var td = doc.createElement("td");
      // @todo : check if this line doesnt stop us to use pixel width in cells
      if (cellwidth && i===0)
      {
        td.style.width = cellwidth + "%";
      }
      if (param.border)
      {
        td.style.border = param.border + 'px ' + param.border_style.value + ' ' + param.border_color;
      }
      tr.appendChild(td);
      // Browsers like to see something inside the cell (&nbsp;).
      td.appendChild(doc.createTextNode('\u00a0'));
    }
  }
  // insert the table
  editor.insertNodeAtSelection(table);
};