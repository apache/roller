<?php require_once('require-password.php'); ?>
<!DOCTYPE html>
<html xmlns="http://www.w3.org/1999/xhtml" xml:lang="en" lang="en">
<head>

  <meta http-equiv="Content-Type" content="text/html; charset=utf-8" />
  <title>Example of Xinha</title>
  <link rel="stylesheet" href="files/full_example.css" />

  <script src="//s3-us-west-1.amazonaws.com/xinha/xinha-1.5/XinhaEasy.js" type="text/javascript">
    
    // The following options are, optional...
    xinha_options = 
    {
      // Specify language and skin
      _editor_lang:   'en',         // Language to use
      _editor_skin:   'silva',      // Name of the skin to use (see skins directory for available skins)
      
      // Simply a CSS selector to pick the textarea(s) you want, eg 'textarea'
      //  converts all textarea, or textarea.my-xinha,textarea.your-xinha 
      //  would convert only those textareas with the my-xinha or your-xinha 
      //  classes on them
      xinha_editors:  'textarea',   

      // Plugins can be a default set - 'common', 'minimal', 'loaded'
      //   xinha_plugins: 'minimal'
      //
      // or you can specify the plugins you want exactly
      //   xinha_plugins: [ 'Linker', 'Stylist' ]
      //
      // or you can do both to add extras to the set
      //   xinha_pass_to_php_backend: ['minimal', 'Linker' ]
      xinha_plugins:  
      [ 
        'minimal', // Load the standard minimal set from Xinha's norma plugins
        
        // Since this example might be using Xinha from a CMS, we 
        //  make sure to load our PHP plugins from our local plugins
        //  directory on our own server, not the CMS server (which wouldn't
        //  support PHP).
        { 
           from: '../plugins',      // From our local plugins directory
           load:['MootoolsFileManager', 'Linker'] // Load these plugins
        } 
      ],
      
      // The default toolbar can be one of the pre-defined toolbars, 
      //   'default', 'minimal', 'minimal+fonts', 'supermini'
      //   xinha_toolbar: 'minimal+fonts'
      //
      // or you an specify a toolbar structure completely
      //   xinha_toolbar: [ ["popupeditor"],["separator","bold","italic","underline","strikethrough","superscript"] ]
      xinha_toolbar:  'minimal+fonts',
      
      // To specify a stylesheet to load inside the editor (to style the contents
      //  the user is editing), simply specify the path to it here.
      //
      // Note as with all these options, it's optional, leave it out if you 
      //  don't want to load a stylesheet in the editor
      xinha_stylesheet: null, 
      
      // This is where you set the other default configuration globally
      xinha_config:            function(xinha_config) 
      {
        
        // Configure the File Manager
        with (xinha_config.MootoolsFileManager)
        { 
          <?php 
            require_once('../contrib/php-xinha.php');
            xinha_pass_to_php_backend
            (       
              array
              (
                'images_dir' => getcwd() . '/images',
                'images_url' => preg_replace('/\/examples.*/', '', $_SERVER['REQUEST_URI']) . '/examples/images',
                'images_allow_upload' => true,
                'images_allow_delete' => true,
                'images_allow_download' => true,
                'images_use_hspace_vspace' => false,
                
                'files_dir' => getcwd() . '/images',
                'files_url' => preg_replace('/\/examples.*/', '', $_SERVER['REQUEST_URI']) . '/examples/images',
                'files_allow_upload' => true,
                'max_files_upload_size' => '4M',
              )
            )
          ?>
        }
      
        // Configure the Linker
        with (xinha_config.Linker)
        { 
          <?php 
            require_once('../contrib/php-xinha.php');
            xinha_pass_to_php_backend
            (       
              array
              (
                'dir' => getcwd(),
                'url' => '/examples',                
              )
            )
          ?>
        }
        
      },
      
      // Here is where you can customise configs for each editor area
      //  See the Newbie.html example for usage
      xinha_config_specific:   function(xinha_config, textarea)
      {

      },
      
      // Here you can limit the plugins to certain editor areas
      //  See the Newbie.html example for usage
      xinha_plugins_specific:  function(xinha_plugins, textarea)
      {

      }
    }

  </script>
  
</head>

<body>
  <h1> Demonstration of MootoolsFileManager and Linker with Xinha integration </h1>
  <p>  MootoolsFileManager (for uploading Images and Files) and Linker (for browsing files on the server and making a link to them) plugins require PHP configuration to be passed in a secure way, this example shows you how to do this!  View the source, all the code is in the head.</p>
  
  <form action="javascript:void(0);" id="editors_here" onsubmit="alert(this.myTextArea.value);">
     <div>
    <textarea id="myTextArea" name="myTextArea" style="width:100%;height:320px;">
&lt;p&gt;Lorem ipsum dolor sit amet, consectetuer adipiscing elit.
Aliquam et tellus vitae justo varius placerat. Suspendisse iaculis
velit semper dolor. Donec gravida tincidunt mi. Curabitur tristique
ante elementum turpis. Aliquam nisl. Nulla posuere neque non
tellus. Morbi vel nibh. Cum sociis natoque penatibus et magnis dis
parturient montes, nascetur ridiculus mus. Nam nec wisi. In wisi.
Curabitur pharetra bibendum lectus.&lt;/p&gt;
</textarea>
    <input type="submit" /> <input type="reset" />
    </div>
  </form>

</body>
</html>
