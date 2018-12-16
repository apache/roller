<?php
  /**
    = MootoolsFileManager Configuration File =
        
    Configure either by directly editing the config.php file (not recommended) or
    as follows...

    1. You need to be able to put PHP in your XinhaConfig.js, so
      you may want to call it XinhaConfig.php instead, or whatever other
      method you choose (eg put the config as an inline script in your 
      main php page).
      
    2. In step 3 of your XinhaConfig write something like...
      {{{
        with (xinha_config.MootoolsFileManager)
        { 
          <?php 
            require_once('/path/to/xinha/contrib/php-xinha.php');
            xinha_pass_to_php_backend
            (       
              array
              (
                'images_dir' => '/home/your/directory',
                'images_url' => '/directory',
                'allow_images_upload' => true,
              )
            )
          ?>
        }
      }}}

    This will work provided you are using normal file-based PHP sessions
    (most likely), if not, you may need to modify the php-xinha.php
    file to suit your setup.
  
  * @author $Author$
  * @version $Id$
  * @package MootoolsFileManager
  *
  */


  /**    
    == File Paths REQUIRED ==
    
    This plugin operates (optionally) in two modes.
    
    1. As a File Manager where people are inserting a link to a file 
      (eg a doc or pdf commonly), we call this "files" mode.
    2. As an Image Manager where people are inserting an inline image,
      we call this "images" mode.
    
    You may provide one of, or both of, files_dir and images_dir.  If you do not 
    provide one, that mode of MootoolsFileManager will be disabled.
    
    # `files_dir` -- Directory path to the location where ordinary files are stored
                      (eg /home/you/public_html/downloads/ )                   
    # `files_url` -- The URL path to the files_dir
                      (eg /downloads/)  
    # `images_dir` -- Directory path to the location where inline images are stored
                      (eg /home/you/public_html/images/)
    # `images_url` -- The URL path to the images_dir

    === Security Caution ===

    You should ensure that the paths you specify are protected against having PHP and
    other scripts from being executed.  The following .htaccess file is a good idea

    {{{
      <IfModule mod_php.c>
        php_flag engine off
      </IfModule>
      AddType text/html .html .htm .shtml .php .php3 .php4 .php5 .php6 .php7 .php8 .phtml .phtm .pl .py .cgi
      RemoveHandler .php
      RemoveHandler .php8
      RemoveHandler .php7
      RemoveHandler .php6
      RemoveHandler .php5
      RemoveHandler .php4
      RemoveHandler .php3
    }}}
    
  */

  $IMConfig['files_dir']  = FALSE; // No trailing slash 
  $IMConfig['files_url']  = FALSE; // No trailing slash

  $IMConfig['images_dir'] = FALSE; // No trailing slash
  $IMConfig['images_url'] = FALSE; // No trailing slash
  
  $IMConfig['thumbs_dir'] = NULL;  // Will default to images_dir/.thumbs or files_dir/.thumbs
  $IMConfig['thumbs_url'] = NULL;  // Will default to images_url/.thumbs or files_url/.thumbs
  /**
    == Turning On Uploads ==
    We have two sets of settings for turning on uploads, one controls the files mode
    of the plugin, the other is for images mode.

    Note that allowing upload also permits the user to create subdirectories to better
    organise the files.
    
    === Maximum File Sizes ===

    Each mode can have a different maximum file size that can be uploaded, this
    size is a number followed by one of M, KB or B.

    === Suggested Image Dimensions ===

    Each mode can have a different "suggested maximum image dimension", when the
    user uses the Mootools File Manager to upload a file, they are able to choose
    to "resize large images" on upload.  This defines what "large" means.
  
  */

  $IMConfig['files_allow_upload']     = false;
  $IMConfig['files_allow_delete']     = false;
  $IMConfig['files_max_upload_size']  = '3M';
  $IMConfig['files_suggested_image_dimension']  = array('width' => 2048, 'height' => 1536);

  $IMConfig['images_allow_upload']     = false;
  $IMConfig['images_allow_delete']     = false;
  $IMConfig['images_max_upload_size']  = '3M';
  $IMConfig['images_suggested_image_dimension']  = $IMConfig['files_suggested_image_dimension'];


// -------------------------------------------------------------------------
//                OPTIONAL SETTINGS 
// -------------------------------------------------------------------------

/** Expanded Permissions */

$IMConfig['images_allow_create_dir']  = NULL;  // Defaults to allow_images_upload
$IMConfig['images_allow_move']        = false; 
$IMConfig['images_allow_download']    = false; 
  
$IMConfig['files_allow_create_dir']  = NULL; // Defaults to allow_files_upload
$IMConfig['files_allow_move']        = false; 
$IMConfig['files_allow_download']    = false; 
  
/** Listing Mode Configuration */

$IMConfig['images_list_type']        = 'list'; // Or 'thumb'
$IMConfig['images_pagination_size']  = 10000;  // By default, a large number to avoid pagination strongly
                                               // The MFM may reduce this automatically however.                                               
$IMConfig['images_list_mode_over']   = 30;     // If a folder contains more than this many entries
                                               // it will fall back to 'list' mode when in 'thumb' mode
                                               // the user can switch back to 'thumb' if they want.
$IMConfig['images_list_start_in']    = NULL;   // You can set this to a path relative to the images_dir


$IMConfig['files_list_type']        = 'list';  // Or 'thumb'
$IMConfig['files_pagination_size']  = 10000;   // By default, a large number to avoid pagination strongly
                                               // The MFM may reduce this automatically however.                                               
$IMConfig['files_list_mode_over']   = 30;      // If a folder contains more than this many entries
                                               // it will fall back to 'list' mode when in 'thumb' mode
                                               // the user can switch back to 'thumb' if they want.
$IMConfig['files_list_start_in']    = NULL;    // You can set this to a path relative to the images_dir
                                               
/**

== Plugin Path ==
 
 For most people the defaults will work fine, but if you have trouble, you can set
 `base_dir` to be the directory path to xinha/plugins/MootoolsFileManager
 `base_url` to be the url path to xinha/plugins/MootoolsFileManager
*/

$IMConfig['base_dir'] = getcwd();
$IMConfig['base_url'] = preg_replace('/\/backend\.php.*/', '', $_SERVER['REQUEST_URI']);

/**

== HTML Compatability ==

 If the HTML you are editing in Xinha is destined for an email you will probably want to use hspace and vspace instead of CSS margins because of poor Email support for CSS.
 
*/

$IMConfig['images_use_hspace_vspace'] = TRUE;

////////////////////////////////////////////////////////////////////////////////
//       ================== END OF CONFIGURATION =======================      //
////////////////////////////////////////////////////////////////////////////////

// Standard PHP Backend Data Passing
//  if data was passed using xinha_pass_to_php_backend() we merge the items
//  provided into the Config
require_once(realpath(dirname(__FILE__) . '/../../contrib/php-xinha.php'));

if($passed_data = xinha_read_passed_data())
{
  $IMConfig = array_merge($IMConfig, $passed_data);
}

// Back Compat, Some of our config options have been renamed, 
// if the old name is present, that takes precendence.
$RenamedConfigVars = array(
  'UseHSpaceVSpace'        => 'images_use_hspace_vspace',
  
  'allow_files_upload'     => 'files_allow_upload',
  'allow_files_delete'     => 'files_allow_delete',
  'allow_files_create_dir' => 'files_allow_create_dir',
  'allow_files_move'       => 'files_allow_move',
  'allow_files_download'   => 'files_allow_download',
  
  'max_files_upload_size'   => 'files_max_upload_size',
  'suggested_files_image_dimension' => 'files_suggested_image_dimension',
  
  'allow_images_upload'     => 'images_allow_upload',
  'allow_images_delete'     => 'images_allow_delete',
  'allow_images_create_dir' => 'images_allow_create_dir',
  'allow_images_move'       => 'images_allow_move',
  'allow_images_download'   => 'images_allow_download',
  
  'max_images_upload_size'  => 'images_max_upload_size',
  'suggested_images_image_dimension' => 'images_suggested_image_dimension',  
);

foreach($RenamedConfigVars as $Old => $New)
{
  if(isset($IMConfig[$Old]))
  {
    $New = $IMConfig[$Old];
    unset($IMConfig[$Old]);    
  }
}

if(!isset($IMConfig['thumbs_dir']))
{
  $IMConfig['thumbs_dir'] = (isset($IMConfig['images_dir']) ? $IMConfig['images_dir'] : $IMConfig['files_dir']) . '/.thumbs';
}

if(!isset($IMConfig['thumbs_url']))
{
  $IMConfig['thumbs_url'] = (isset($IMConfig['images_url']) ? $IMConfig['images_url'] : $IMConfig['files_url']) . '/.thumbs';
}

if(!isset($IMConfig['images_allow_create_dir'])) 
{
  $IMConfig['images_allow_create_dir'] = $IMConfig['images_allow_upload'];
}

if(!isset($IMConfig['files_allow_create_dir'])) 
{
  $IMConfig['files_allow_create_dir'] = $IMConfig['files_allow_upload'];
}

?>
