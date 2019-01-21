/**
Uploader Implementation Not Requiring Flash
===============================================================================

This class implements Upload functionality into the FileManager using 
either HTML5 Uploads via XMLHTTPRequest if the browser supports that, 
or a hidden file input and submitting to an iframe otherwise.

HTML5 Uploads can handle uploading multiple files with progress indications.

Fallback uploads present a standard file field to facilitate picking a single
file and an upload button to upload it (posts to a hidden iframe).

Tested In
----------

Working Fully With HTML5 Multiple Uploads

  * Linux Chrome 65.0.3325.18
  * Linux Firefox 58 (see note for Firefox users below)
  * Linux Opera 50
  * Linux Chromium 63.0.3239.132
  
  * Windows IE 11
  
  * Mac OSX Safari 9.1.1 (11601.6.17)

Working Fallback Single Upload At A Time

  * Windows IE 8 - Falls back to iframe upload
  * Windows IE 9 - Falls back to iframe upload


FIREFOX USERS NOTICE
-------------------------------------------------------------------------------

The Firefox file selection dialog can be HORRIBLY broken, unusable, this is not 
a bug in MFM, it's firefox.  If you start firefox with the MOZ_USE_XINPUT2=1 
environment variable, it fixes it!

@author James Sleeman <james@gogo.co.nz>
@license MIT-style License

*/

FileManager.implement({

	options: {
		resizeImages: true,
		upload: true,
		uploadAuthData: {}             // deprecated; use FileManager.propagateData instead!
	},

	hooks: {
		show: {
			upload: function() {
				this.startUpload();
			}
		},

		cleanup: {
			upload: function() {
				this.hideUpload();
			}
		}
	},

	onDialogOpenWhenUpload: function() {

	},

	onDialogCloseWhenUpload: function() {

	},

  /** Create the file input field and inject it into the given form element
   */
  
	make_file_input: function(form_el)
	{
		var fileinput = (new Element('input')).set({
			type: 'file',
			name: 'Filedata',
			id: 'filemanager_upload_Filedata',
      multiple: 'multiple'
		});
    
    // fileinput.style.visibility = 'hidden';
    
		if (form_el.getElement('input[type=file]'))
		{
			fileinput.replaces(form_el.getElement('input[type=file]'));
		}
		else
		{
			form_el.adopt(fileinput);
		}
		return form_el;
	},

  /** Cleanup after ourselves when the filemanager window is closed
   * 
   *  @TODO This may not be entirely necessary now, leaving it anyway.
   */
  
	hideUpload: function()
	{
		if (!this.options.upload || !this.upload) return;

		if (this.upload.uploadButton.label)
		{
			this.upload.uploadButton.label.fade(0).get('tween').chain(function() {
				this.element.dispose().destroy();
			});
			this.upload.uploadButton.label = null;
		}
		if (this.upload.uploadButton)
		{
			this.upload.uploadButton.fade(0).get('tween').chain(function() {
				this.element.dispose().destroy();
			});
			this.upload.uploadButton = null;
		}
		if (this.upload.form)
		{
			this.upload.inputs = null;

			this.upload.form.dispose().destroy();
			this.upload.form = null;
		}
		this.menu.setStyle('height', '');

		if (this.upload.resizer)
		{
			this.upload.resizer.dispose().destroy();
			this.upload.resizer = null;
		}
		
    // discard old iframe, if it exists:
    if (this.upload.dummyframe)
    {
      // remove from the menu (dispose) and trash it (destroy)
      this.upload.dummyframe.dispose().destroy();
      this.upload.dummyframe = null;
    }
	},

  /** Setup out upload interface.
   * 
   *  Creates the upload button, the container for the form field, calls out to create the form field
   *  the area for the upload list, and the resizing checkbox.
   *
   */
  
	startUpload: function()
	{
		if (!this.options.upload) {
			return;
		}

		var self = this;

		this.upload = {
			inputs: {},
			resizer: null,
      dummyframe: null,
      dummyframe_active: false,     // prevent premature firing of the load event (hello, MSIE!) to cause us serious trouble in there
      can_support_xhr:   typeof ((new Element('input')).set({
        type: 'file',
        name: 'Filedata',
        multiple: 'multiple'
      })).files == 'undefined' ? false : true,
      
			form: (new Element('form'))
				// .set('action', tx_cfg.url)
				.set('method', 'post')
				.set('enctype', 'multipart/form-data')
        .set('target', 'dummyframe')
				.setStyles({
					'float': 'left',
					'padding-left': '3px',
					'display': 'block'
			}),

			uploadButton: this.addMenuButton('upload').inject(this.menu, 'bottom').addEvents({				
				mouseenter: function() {
					this.addClass('hover');
				},
				mouseleave: function() {
					this.removeClass('hover');
					this.blur();
				},
				mousedown: function() {
					this.focus();
				}
			}),

      list: new Element('ul', {'class': 'filemanager-uploader-list'}),
      uploader: new Element('div', {opacity: 0, 'class': 'filemanager-uploader-area'}).adopt(
        new Element('h2', {text: this.language.upload}),
        new Element('div', {'class': 'filemanager-uploader'})
      )
		};
    this.upload.uploader.getElement('div').adopt(this.upload.list);

    if(this.upload.can_support_xhr)
    {
      this.upload.form.setStyle('visibility', 'hidden');
      this.upload.uploadButton.addEvent('click',  function(e) {
          e.stop();

          self.upload.form.getElement('input[type=file]').removeEvents('change');
          self.upload.form.getElement('input[type=file]').addEvent('change', self.doUpload.bind(self));
          
          self.upload.form.getElement('input[type=file]').click();
          
      }); 
    }
    else
    {
      this.upload.uploadButton.addEvent('click',  function(e) {
          e.stop()
          self.doUploadFallback();
      }); 
    }
    
		if (this.options.resizeImages)
		{
			this.upload.resizer = new Element('div', {'class': 'checkbox'});
			var check = (function()
			{
				this.toggleClass('checkboxChecked');
			}).bind(this.upload.resizer);
			check();
			this.upload.uploadButton.label = new Element('label', { 'class': 'filemanager-resize' }).adopt(
				this.upload.resizer,
				new Element('span', {text: this.language.resizeImages})
			).addEvent('click', check).inject(this.menu);
		}

		this.make_file_input(self.upload.form);

		self.upload.form.inject(this.menu, 'top');
		//this.menu.setStyle('height', '60px');

  },

  
  /** Change handler for the form field, actually do the uploads.
   * 
   *  Note that if you don't select a different file in the form field, no change, so no re-upload
   *  unless you actually pick a different file (as well or instead).
   * 
   */
  
  doUpload: function()
  {
    if(this.upload.form.getElement('input[type=file]').files.length == 0) return;

    // Notice here that propagateData is not passed into mkServerRequestURL
    //   this is how the rest of the system works too so it can't really be changed
    //   because mkServerRequestURL is passed into FileManager.Request normally
    //   and the propagateData is added there.
                      
    var tx_cfg = this.options.mkServerRequestURL(this, 'upload', Object.merge({},
        /*this.options.propagateData, */
        (this.options.uploadAuthData || {}), {
          directory: (this.CurrentDir ? this.CurrentDir.path : '/'),
          filter: this.options.filter,
          resize: this.options.resizeImages ? this.upload.resizer.hasClass('checkboxChecked') : false,
          reportContentType: 'text/plain'
        }));
    
    var files = this.upload.form.getElement('input[type=file]').files;
    var fieldName = this.upload.form.getElement('input[type=file]').name;
    var i     = 0;
    var self = this;
    
    // Construct the entries in the upload list for each file
    var fileUIs = [ ];
    for(var i = 0; i < files.length; i++)
    {
      fileUIs[i] = new FileManager.UploadListEntry(files[i], self);
            
      // @TODO Validate client side here
      // fileUIs[i].invalidate("Testing");
      
    }
    
    i = 0;
    
    // Show the upload list of files
    this.show_our_info_sections(false);
    this.info.adopt(this.upload.uploader.setStyle('display', 'block'));
    this.upload.uploader.fade(1);
    
    // Display spinner
    self.browserLoader.fade(1);
    
    // When the list becomes empty (all files uploaded) remove it and update
    //  the selected file to the first valid one, that is, the first one that
    //  uploaded OK.
    var hideList = function()
    {
      if(self.upload.uploader.getElements('li').length)
      {
        hideList.delay(1000);
      }
      else
      {
        self.upload.uploader.fade(0).get('tween').chain(function() {
          self.upload.uploader.setStyle('display', 'none');
          self.show_our_info_sections(true);
            
          // Hide spinner
          self.browserLoader.fade(0);
          
          // Update
          for(var x = 0; x < fileUIs.length; x++)
          {
            if(fileUIs[x].valid)
            {
              self.load(self.CurrentDir.path, fileUIs[x].nameOnServer);
            }
          }
        });            
      }
    };
    hideList();
      
    
    // This is where the actual upload of the files happens:
    //  take file i,
    //    if it's not valid, skip it
    //    create a new request object
    //       append the file to it
    //       append tx_cfg.data (above) and the propagateData to it
    //       attach a progress event to it which calls the file's UI and tells it to update
    //       attach success/fail events to it which calls the file's UI and tells it to update
    //    start the upload
    //       wait asynchronously until it's done
    //       tell the file's UI that it's done and the result
    //         (the file's UI will remove itself from the list after a short delay)
    //    next i

    
    var doUploadNextFile = function()
    {
      if(i <= files.length-1)
      {
        var file   = files[i];      
        var fileUI = fileUIs[i];
        
        if(!fileUI.valid)
        {
          i++;
          return doUploadNextFile();
        }
        
        // For testing progess bar
        if(0)
        {
          var p = 1;
          (function(){
            if(p<100)
              fileUI.progress(p++);
          }).periodical(500);
          return;
        }
        
        var upload = new FileManager.FileUploadRequest({
          url: tx_cfg.url
        });
        
        upload.append(fieldName, file);
        Object.each(Object.merge(tx_cfg.data, self.options.propagateData), function(v,k) { upload.append(k,v); });
        
        upload.addEvent('progress', function(event, a){          
          if(event.total && event.loaded)
          {
            fileUI.progress((event.loaded / event.total) * 100);
          }
        });
        
        upload.addEvent('success', function(responseText){
          fileUI.complete({'text': responseText});
        });
        
        upload.addEvent('failure', function(responseXhr){
          fileUI.complete(responseXhr);
        });
        
        upload.send();
        
        (function waitTillDone(){
          if(!upload.isRunning())
          {
            i++;
            doUploadNextFile();
            
            fileUI.progress(100);
            fileUI.complete(upload);
          }
          else
          {
            waitTillDone.delay(500);
          }
        }).delay(500);
      }
    }
    
    doUploadNextFile();
  },
  
  /** Change handler for the form field that does not require HTML5, or much more than form fields.
   * 
   *  Uses the hidden iframe.
   * 
   *  Note that if you don't select a different file in the form field, no change, so no re-upload
   *  unless you actually pick a different file (as well or instead).
   * 
   */
  
  doUploadFallback: function()
  {
    var self = this;
    
    // discard old iframe, if it exists:
    if (this.upload.dummyframe)
    {
      // remove from the menu (dispose) and trash it (destroy)
      this.upload.dummyframe.dispose().destroy();
      this.upload.dummyframe = null;
    }

    this.upload.dummyframe = (new IFrame).set({src: 'about:blank', name: 'dummyframe'}).setStyles({display: 'none'});
    this.menu.adopt(this.upload.dummyframe);
    
    this.upload.dummyframe.addEvent('load', function()
    {
      var iframe = this;
      self.diag.log('NoFlash upload response: ', this, ', iframe: ', self.upload.dummyframe, ', ready:', (1 * self.upload.dummyframe_active));

      // make sure we don't act on premature firing of the event in MSIE browsers:
      if (!self.upload.dummyframe_active)
        return;

      self.browserLoader.fade(0);

      var response = null;
      Function.attempt(function() {
          response = iframe.contentDocument.documentElement.innerText;
        },
        function() {
          response = iframe.contentDocument.documentElement.textContent;
        },
        function() {
          response = iframe.contentWindow.document.innerText;
        },
        function() {
          response = iframe.contentDocument.innerText;
        },
        function() {
          // Maybe this.contentDocument.documentElement.innerText isn't where we need to look?
          //debugger;
          response = "{status: 0, error: \"noFlashUpload: document innerText grab FAIL: Can't find response.\"}";
        }
      );

      var j = JSON.decode(response);

      if (j && !j.status)
      {
        self.showError('' + j.error);
        self.load(self.CurrentDir.path);
      }
      else if (j)
      {
        self.load(self.CurrentDir.path, j.name);
      }
      else
      {
        // IE9 fires the load event on init! :-(
        if (self.CurrentDir)
        {
          self.showError('No or faulty JSON response! ' + response);
          self.load(self.CurrentDir.path);
        }
      }

      // Clear the file input, to do this it is remade
      self.make_file_input(self.upload.form);
    });
        
    // Notice here that propagateData is not passed into mkServerRequestURL
    //   this is how the rest of the system works too so it can't really be changed
    //   because mkServerRequestURL is passed into FileManager.Request normally
    //   and the propagateData is added there.
                      
    var tx_cfg = this.options.mkServerRequestURL(this, 'upload', Object.merge({},
        /*this.options.propagateData, */
        (this.options.uploadAuthData || {}), {
          directory: (this.CurrentDir ? this.CurrentDir.path : '/'),
          filter: this.options.filter,
          resize: this.options.resizeImages ? this.upload.resizer.hasClass('checkboxChecked') : false,
          reportContentType: 'text/plain'
        }));
    
    self.upload.form.action = tx_cfg.url;
    self.upload.form.getElements('input[type=hidden]').each(function(e){e.destroy();});
    
    Object.each(Object.merge(tx_cfg.data, self.options.propagateData), function(v,k) { 
      var input = new Element('input').set({type: 'hidden', name: k, value: v, id: 'filemanager_upload_' + k });
      self.upload.form.adopt(input);    
    });
    
    self.upload.dummyframe_active = true;
    self.browserLoader.fade(0);
    self.upload.form.submit();
    
  }
  
  
});


/** The UploadListEntry class handles entries in the file upload list
 * 
 *  Pass it an HTML5 file object (taken from input[type=file].files) 
 *  and the file manager to which it is being attached.
 * 
 *  During initialisation the UploadListEntry will inject an <li> into 
 *    [filemanager].upload.list
 *  which must already be created.
 * 
 * You can then call 
 *   invalidate("Reason") to invalidate the file and produce a message
 *   invalidate(false)    to invalidate the file and not produce a message
 *   progress(0 .. 100) to set the progress bar for the file
 *   complete({text: 'jsonencodedresponse'}) to complete the file with a json response
 *   complete(XMLHTTPRequest) to fail the file with some non-json failure
 */

FileManager.UploadListEntry = new Class({

  Implements: Events,
  
  initialize: function(file, fm) 
  {
    this.file = file;
    this.base = fm;    
    
    this.valid = true;
    this.validationError = null;
    this.has_completed  = false;
    
    this.id =  String.uniqueID();
    
    this.addEvents({
      start: this.onStart,
      progress: this.onProgress,
      stop: this.onStop,
      complete: this.onComplete
    });
    
    this.render();
  },

  /** Mark the upload as invalid/failed, display a message, highlight and remove the file.
   * 
   *  With no reason, no message is displayed, with a reason a message is displayed. 
   * 
   *  @param reason String|false
   */
  
  invalidate: function(reason) 
  {
    this.valid = false;
    this.validationError = reason;
    
    if(reason)
    {
      var message = this.base.language.uploader.unknown;
      var sub = {
        name: this.file.name,
        size: this.formatUnit(this.file.size, 'b')
      };

      if (this.base.language.uploader[this.validationError]) {
        message = this.base.language.uploader[this.validationError];
      }
      else
      {
        message = this.validationError;
      }

      if (this.validationError === 'sizeLimitMin')
        sub.size_min = this.formatUnit(this.base.options.fileSizeMin, 'b');
      else if (this.validationError === 'sizeLimitMax')
        sub.size_max = this.formatUnit(this.base.options.fileSizeMax, 'b');

      this.base.showError(message.substitute(sub, /\\?\$\{([^{}]+)\}/g));
    }
    
    this.highlightAndClear();
  },
  
  /** Highlight the file in the list and then remove it from the list (ie when it's finished/failed)
   * 
   *  Used by invalidate, and complete
   */
  
  highlightAndClear: function()
  {
    var self = this;
    
    // Highlight the line
    self.ui.element.set('tween', {duration: 1000}).highlight(null, (self.valid ? '#e6efc2' : '#f0c2c2')).get('tween').chain(function(){self.ui.element.style.backgroundColor=(self.valid ? '#e6efc2' : '#f0c2c2');});
    
    // Remove it after a delay
    (function() {
      self.ui.element.setStyle('overflow', 'hidden').morph({
        opacity: 0,
        height: 0
      }).get('morph').chain(function() {
        self.ui.element.destroy();
      });
    }).delay(self.valid ? 2500 : 5000, self);
  },
  
  /** Format a number into a human readable size 
   * 
   */
  
  formatUnit: function(base, type, join) {
    var unitLabels =  {
      b: [{min: 1, unit: 'B'}, {min: 1024, unit: 'kB'}, {min: 1048576, unit: 'MB'}, {min: 1073741824, unit: 'GB'}],
      s: [{min: 1, unit: 's'}, {min: 60, unit: 'm'}, {min: 3600, unit: 'h'}, {min: 86400, unit: 'd'}]
    };
    var labels = unitLabels[(type == 'bps') ? 'b' : type];
    var append = (type == 'bps') ? '/s' : '';
    var i, l = labels.length, value;

    if (base < 1) return '0 ' + labels[0].unit + append;

    if (type == 's') {
      var units = [];

      for (i = l - 1; i >= 0; i--) {
        value = Math.floor(base / labels[i].min);
        if (value) {
          units.push(value + ' ' + labels[i].unit);
          base -= value * labels[i].min;
          if (!base) break;
        }
      }

      return (join === false) ? units : units.join(join || ', ');
    }

    for (i = l - 1; i >= 0; i--) {
      value = labels[i].min;
      if (base >= value) break;
    }

    return (base / value).toFixed(1) + ' ' + labels[i].unit + append;
  },
  
  /** Draw the list item 
   *
   */
  
  render: function() {
    var self = this;
    if (!this.valid) {

      return this;
    }

    this.ui = {};
    this.ui.icon = new Asset.image(this.base.assetBasePath+'Images/Icons/' + this.file.name.replace(/.*\./, '').toLowerCase() + '.png', {
      'class': 'icon',
      onerror: function() {
        new Asset.image(self.base.assetBasePath + 'Images/Icons/default.png').replaces(this);
      }
    });
    this.ui.element = new Element('li', {'class': 'file', id: 'file-' + this.id});
    // keep filename in display box at reasonable length:
    var laname = this.file.name;
    if (laname.length > 36) {
      laname = laname.substr(0, 36) + '...';
    }
    this.ui.title = new Element('span', {'class': 'file-title', text: laname, title: this.file.name});
    this.ui.size = new Element('span', {'class': 'file-size', text: this.formatUnit(this.file.size, 'b')});

    this.ui.cancel = new Asset.image(this.base.assetBasePath+'Images/cancel.png', {'class': 'file-cancel', title: this.base.language.cancel}).addEvent('click', function() {
      self.invalidate(false); // No reason
      self.base.tips.hide();
      self.base.tips.detach(this);
    });
    this.base.tips.attach(this.ui.cancel);

    var progress = new Element('img', {'class': 'file-progress', src: this.base.assetBasePath+'Images/bar.gif'});


    this.ui.element.adopt(
      this.ui.cancel,
      progress,
      this.ui.icon,
      this.ui.title,
      this.ui.size
    ).inject(this.base.upload.list).highlight();

    this.ui.progress = progress;
    
    // Initialise the progress position to zero     
    this.ui.progress.setStyle('background-position-x', Math.floor(100-((0/100)*20+40))+'%');
  },

  /** Update the progress bar of the list item.
   * 
   *  @param integer 0 to 100 percent
   */
  
  progress: function(percentLoaded){
    if(this.has_completed) return;
    
    this.ui.element.addClass('file-running');
    
    // Setting the backhround to between 60% for empty and 40% for full works
    //   so that is a range of 20, an offset of 40, flipped backwards (100-N)    
    this.ui.progress.setStyle('background-position-x', (100-((percentLoaded/100)*20+40))+'%');
  },
  
  /** Mark the file as completed, and then remove from the list.
   * 
   *  @param object {text: 'jsonencodedresponse'}
   */
  
  complete: function(response)
  {
    var self = this;
    
    if(this.has_completed) return;
                               
    this.response = response;
    
    var jsonresponse = null;

    this.has_completed = true;
    this.ui.cancel = this.ui.cancel.destroy();

    try
    {
      jsonresponse = JSON.decode(response.text);
    }
    catch(e)
    {
      this.base.diag.log(response);
    }

    if (typeof jsonresponse === 'undefined' || jsonresponse == null)
    {
      if (response == null || !response.text)
      {
        // The 'mod_security' has shown to be one of the most unhelpful error messages ever; particularly when it happened on a lot on boxes which had a guaranteed utter lack of mod_security and friends.
        // So we restrict this report to the highly improbable case where we get to receive /nothing/ /at/ /all/.
        this.invalidate(this.base.language.uploader.mod_security);
      }
      else
      {
        this.invalidate(("Server response:\n" + this.response.text).substitute(this.base.language, /\\?\$\{([^{}]+)\}/g));
      }
    }
    else if (!jsonresponse.status)
    {
      this.invalidate(('' + jsonresponse.error).substitute(this.base.language, /\\?\$\{([^{}]+)\}/g));
    }
    else
    {
      this.valid = true;
      this.nameOnServer = jsonresponse.name;
    }

    this.highlightAndClear();
  }

});


/** This class is used to handle the file uploads themselves (XMLHTTPRequest)
 * 
 * It does extend Request, but you should't expect everything to work, it is
 * not really general purpose.
 * 
 * Taken originally from https://gist.github.com/mloberg/1342473 and messed 
 *  about a bit.
 *
 * Important differences to Request
 * ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
 * use append() to insert your form data (key and value, which might be a file)
 * add a progress event listener to get progress of the UPLOAD not the DOWNLOAD
 * send does not accept options, pass them to the constructor only
 *   (namely {url: '....'} )
 * I don't know if POST() or GET() etc aliases will work, just use send()
 * 
 */

FileManager.FileUploadRequest = new Class({

  Extends: Request,
  
  options: {
    emulation: false,
    urlEncoded: false
  },
  
  initialize: function(options){
    this.xhr = new Browser.Request();
    this.formData = new FormData();
    this.setOptions(options);
    this.headers = this.options.headers;
  },
  
  /** Append "something" to the request.
   *   
   *  In Our case that something is either a field name and value 
   *   ( you don't seem to need to encode it yourself)
   *  or field name and a file taken from an (input[type=file]).files list
   * 
   *  It's kinda nice how it "just works", and also kinda worrying.
   */
  
  append: function(key, value){
    this.formData.append(key, value);
    return this.formData;
  },
  
  reset: function(){
    this.formData = new FormData();
  },
  
  send: function(){
    var url = this.options.url;
    
    this.options.isSuccess = this.options.isSuccess || this.isSuccess;
    this.running = true;
    
    var xhr = this.xhr;
    xhr.open('POST', url, true);
    xhr.onreadystatechange = this.onStateChange.bind(this);
    
    if (('onprogress' in xhr))
    {
      xhr.onloadstart = this.loadstart.bind(this);
      
      // By attaching to xhr.upload we get progress of that, 
      // rather than the unknowable response progress
      xhr.upload.onprogress = this.progress.bind(this);
    }
    
    Object.each(this.headers, function(value, key){
      try{
        xhr.setRequestHeader(key, value);
      }catch(e){
        this.fireEvent('exception', [key, value]);
      }
    }, this);
    

    
    this.fireEvent('request');
    xhr.send(this.formData);
    
    if(!this.options.async) this.onStateChange();
    if(this.options.timeout) this.timer = this.timeout.delay(this.options.timeout, this);
    return this;
  }

});
