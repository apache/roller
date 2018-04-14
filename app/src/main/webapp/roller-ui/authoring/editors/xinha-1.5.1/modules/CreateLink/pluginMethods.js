
CreateLink.prototype.show = function(a)
{
  if (!this.dialog)
  {
    this.prepareDialog();
  } 
	var editor = this.editor;
	this.a = a;
	if(!a && this.editor.selectionEmpty(this.editor.getSelection()))
	{
		alert(this._lc("You need to select some text before creating a link"));
		return false;
	}

	var inputs =
	{
		f_href   : '',
		f_title  : '',
		f_target : '',
		f_other_target : ''
	};

	if(a && a.tagName.toLowerCase() == 'a')
	{
		inputs.f_href   = this.editor.fixRelativeLinks(a.getAttribute('href'));
		inputs.f_title  = a.title;
		if (a.target)
		{
			if (!/_self|_top|_blank/.test(a.target))
			{
				inputs.f_target = '_other';
				inputs.f_other_target = a.target;
			}
			else
			{
				inputs.f_target = a.target;
				inputs.f_other_target = '';
			}
		}
	}

	// now calling the show method of the Xinha.Dialog object to set the values and show the actual dialog
	this.dialog.show(inputs);
};

// and finally ... take some action
CreateLink.prototype.apply = function()
{

	var values = this.dialog.hide();
	var a = this.a;
	var editor = this.editor;

	var atr =
	{
		href: '',
		target:'',
		title:''
	};

	if(values.f_href)
	{
		atr.href = values.f_href;
		atr.title = values.f_title;
		if (values.f_target.value)
		{
			if (values.f_target.value == 'other') atr.target = values.f_other_target;
			else atr.target = values.f_target.value;
		}
	}
	if (values.f_target.value)
	{
		if (values.f_target.value != '_other')
		{
			atr.target = values.f_target.value;
		}
		else
		{
			atr.target = values.f_other_target;
		}
	}
	
	if(a && a.tagName.toLowerCase() == 'a')
	{
		if(!atr.href)
		{
			if(confirm(this._lc('Are you sure you wish to remove this link?')))
			{
				var p = a.parentNode;
				while(a.hasChildNodes())
				{
					p.insertBefore(a.removeChild(a.childNodes[0]), a);
				}
				p.removeChild(a);
				editor.updateToolbar();
				return;
			}
		}
		else
		{
			// Update the link
			for(var i in atr)
			{
				a.setAttribute(i, atr[i]);
			}

			// If we change a mailto link in IE for some hitherto unknown
			// reason it sets the innerHTML of the link to be the
			// href of the link.  Stupid IE.
			if(Xinha.is_ie)
			{
				if(/mailto:([^?<>]*)(\?[^<]*)?$/i.test(a.innerHTML))
				{
					a.innerHTML = RegExp.$1;
				}
			}
		}
	}
	else
	{
		if(!atr.href) return true;

		// Insert a link, we let the browser do this, we figure it knows best
		var tmp = Xinha.uniq('http://www.example.com/Link');
		editor._doc.execCommand('createlink', false, tmp);

		// Fix them up
		var anchors = editor._doc.getElementsByTagName('a');
		for(var i = 0; i < anchors.length; i++)
		{
			var anchor = anchors[i];
			if(anchor.href == tmp)
			{
				// Found one.
				if (!a) a = anchor;
				for(var j in atr)
				{
					anchor.setAttribute(j, atr[j]);
				}
			}
		}
	}
	editor.selectNodeContents(a);
	editor.updateToolbar();
};
CreateLink.prototype._getSelectedAnchor = function()
{
  var sel  = this.editor.getSelection();
  var rng  = this.editor.createRange(sel);
  var a    = this.editor.activeElement(sel);
  if(a != null && a.tagName.toLowerCase() == 'a')
  {
    return a;
  }
  else
  {
    a = this.editor._getFirstAncestor(sel, 'a');
    if(a != null)
    {
      return a;
    }
  }
  return null;
};