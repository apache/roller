/** makes the columns of a given table resizable with the mouse
 * @author Raimund Meyer
 * @param id {String} the id of the table
 */
function dragTableCols (id)
{
	this.table = document.getElementById(id);
	var ths = this.table.rows[0].cells;
	this.ths = ths;
	var self = this;
	var c;
	for (var j = 0; j < this.table.rows.length; j++) 
	{
		for (var i = 0; i < this.table.rows[j].cells.length; i++) 
		{
			c = this.table.rows[j].cells[i];
			c._i = i;
			dragTableCols.addEvent(c, 'mousemove', function(e)
			{
				self.cellMouseMove(e);
			});
			dragTableCols.addEvent(c, 'mouseover', function(e)
			{
				e = e ? e : window.event;
				var t = e.target  || e.srcElement;
				t._pos = dragTableCols.getElementTopLeft(t);
			});
		}
	}
	function deactivate ()
	{
		 self.drag = false; self._col = null; document.body.style.cursor = '';
	}
	dragTableCols.addEvent(document.body, 'mousemove',function (e) { self.bodyMouseMove(e); });
	dragTableCols.addEvent(document.body, 'mouseup',deactivate);
	

}
dragTableCols.prototype.cellMouseMove = function (e)
{
	e = e ? e : window.event;
	var t = e.target || e.srcElement;
	
	if ( typeof dragTableCols == 'undefined' ) return;// sometimes happens, no idea why
	if (!t._pos) 
	{
		t._pos = dragTableCols.getElementTopLeft(t);
		return;
	}
	
	if (t.tagName.toLowerCase() != 'td' && t.tagName.toLowerCase() != 'th') return;
	var self = this;
	function activate (e)
	{
		e = e ? e : window.event;
		self.drag = true;
		self.startX = t._pos.left + t.offsetWidth;
		self._col = t;
		var offsetWidth = t.offsetWidth;
		self.startWidth = (t.width) ? parseInt(t.width, 10) : t.offsetWidth;
		t.style.width = self.startWidth + 'px';
		self.offset = t.offsetWidth - offsetWidth; //padding + border; 
		t.style.width = self.startWidth - self.offset+ 'px';
	}
	// activate right side
	if (t._pos.left + t.offsetWidth - dragTableCols.getPageX (e)  < 5 && t != t.parentNode.cells[t.parentNode.cells.length -1] )
	{
		t.style.cursor = 'e-resize';

		dragTableCols.addEvent(t,'mousedown', activate);
	}
	else 
	{
		t.style.cursor = '';
		dragTableCols.removeEvent(t,'mousedown', activate);
	}
}
dragTableCols.prototype.bodyMouseMove = function (e)
{
	if (!this.drag) return true;
	e = e ? e : window.event;
	var mouseX = dragTableCols.getPageX (e);
	var delta = mouseX - this.startX;
	document.body.style.cursor = (delta < 0) ? 'e-resize' : 'w-resize';
	var newWidth = this.startWidth + delta - this.offset;
	this._col.style.width =  ((newWidth > 10 ) ? newWidth : 10 ) + 'px';
	return true;
}

dragTableCols.addEvent = function (obj, evType, fn)
{
    if (obj.addEventListener) { obj.addEventListener(evType, fn, true); }
    else if (obj.attachEvent) { obj.attachEvent("on"+evType, fn);}
    else {  return false; }
}
dragTableCols.removeEvent = function (obj, evType, fn)
{
    if (obj.addEventListener) { obj.removeEventListener(evType, fn, true); }
    else if (obj.detachEvent) { obj.detachEvent("on"+evType, fn); }
    else {  return false; }
}
dragTableCols.getElementTopLeft = function(element) 
{
  var curleft = 0;
  var curtop = 0;
  if (element.offsetParent) 
  {
    curleft = element.offsetLeft
    curtop = element.offsetTop
    while (element = element.offsetParent) 
    {
      curleft += element.offsetLeft
      curtop += element.offsetTop
    }
  }
  return { top:curtop, left:curleft };
}
dragTableCols.getPageX = function (e)
{
	if ( e.pageX ) return e.pageX;
	else if (document.documentElement && document.documentElement.scrollTop)
	// Explorer 6 Strict
	{
		return document.documentElement.scrollLeft + e.clientX;

	}
	else if (document.body) // all other Explorers
	{
		return document.body.scrollLeft + e.clientX;
	}
}