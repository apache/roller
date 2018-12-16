<?php require_once('require-password.php'); ?>
<?php 
  switch(@$_REQUEST['DocType'])
  {
    
    case 'quirks':
      break;
      
   case 'almost':
      echo '<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.1//EN" "http://www.w3.org/TR/xhtml11/DTD/xhtml11.dtd">';
      break;
    
    case 'standards':
    default:
      echo '<!DOCTYPE html>';
      break;
      
  }
?>
<html xmlns="http://www.w3.org/1999/xhtml" xml:lang="en" lang="en">
<head>

  <meta http-equiv="Content-Type" content="text/html; charset=utf-8" />
  <title>Example of Xinha</title>
  <link rel="stylesheet" href="files/full_example.css" />

  <script src="../XinhaEasy.js" type="text/javascript">
    
    // The following options are, optional...
    xinha_options = 
    {
      // Specify language and skin
      _editor_lang:   'en',         // Language to use
      _editor_skin:   'silva',      // Name of the skin to use (see skins directory for available skins)
      
      // Simply a CSS selector to pick the textarea(s) you want, eg 'textarea' converts all textarea,
      // or textarea.my-xinha,textarea.your-xinha would convert only those textareas with the
      // my-xinha or your-xinha classes on them
      xinha_editors:  'textarea',   

      // Plugins can be one of the simple pre-defined sets, common, minimal and loaded
      //   xinha_plugins: 'minimal'
      //
      // or you can specify the plugins you want exactly
      //   xinha_plugins: [ 'Linker', 'Stylist' ]
      //
      // or you can do both to add extras to the set
      //   xinha_pass_to_php_backend: ['minimal', 'Linker' ]
      xinha_plugins:  [ 'common', 'MootoolsFileManager', 'Linker' ],
      
      // Toolbar can be one of the pre-defined toolbars, 'default', 'minimal', 'minimal+fonts'
      //   xinha_toolbar: 'minimal+fonts'
      //
      // or you an specify a toolbar structure completely
      //   xinha_toolbar: [ ["popupeditor"],["separator","bold","italic","underline","strikethrough","superscript"] ]
    //  xinha_toolbar:  'minimal+fonts',
      
      // This is where you set the default configuration which apply globally
      xinha_config:            function(xinha_config) 
      {
        xinha_config.CharacterMap.mode = 'panel';
        xinha_config.ContextMenu.customHooks = { 'a': [ ['Label', function() { alert('Action'); }, 'Tooltip', '/xinha/images/ed_copy.gif' ] ] }
       
        /*
          // We can load an external stylesheet like this - NOTE : YOU MUST GIVE AN ABSOLUTE URL
          //  otherwise it won't work!
          xinha_config.stylistLoadStylesheet(document.location.href.replace(/[^\/]*\.html/, 'files/stylist.css'));

          // Or we can load styles directly
          xinha_config.stylistLoadStyles('p.red_text { color:red }');

          // If you want to provide "friendly" names you can do so like
          // (you can do this for stylistLoadStylesheet as well)
          xinha_config.stylistLoadStyles('p.pink_text { color:pink }', {'p.pink_text' : 'Pretty Pink'});
       */

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
        
      }
    }

  </script>
  
</head>

<body>
  
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
<a href="../../foo.html">../../foo.html</a><br/>
<a href="./foo.html">./foo.html</a><br/>
<a href="foo/bar/../foo.html">foo/bar/../foo.html</a><br/>
<a href="/foo/bar/../foo.html">/foo/bar/../foo.html</a><br/>
<a href="/foo//bar/foo.html">/foo//bar/foo.html</a><br/>
<a href="../../../../../foo.html">../../../../../foo.html</a><br/>
<a href="http://foo/foo.html">http://foo/foo.html</a><br/>

</textarea>
    <input type="submit" /> <input type="reset" />
    </div>
  </form>

  <ul>
    <li><a href="<?php echo basename(__FILE__) ?>?DocType=standards">Test Standards Mode</a></li>
    <li><a href="<?php echo basename(__FILE__) ?>?DocType=almost">Test Almost Standards Mode</a></li>
    <li><a href="<?php echo basename(__FILE__) ?>?DocType=quirks">Test Quirks Mode</a></li>
  </ul>
  
  <!-- This script is used to show the rendering mode (Quirks, Standards, Almost Standards) --> 
  <script type="text/javascript" src="render-mode-developer-help.js"></script>
</body>
</html>
