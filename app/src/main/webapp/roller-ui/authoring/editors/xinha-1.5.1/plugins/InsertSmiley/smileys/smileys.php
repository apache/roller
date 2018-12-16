<?php
  /** This script can be used to automatically output all smiley images you
   *  dump into this folder, as long as they are gif, jpg or png.
   * 
   *  Simply set your 
   *
   *   xinha_config.InsertSmiley.smileys = 
   *      _editor_url+'/plugins/InsertSmiley/smilies.php';
   *
   * (or better, make a new smileys folder outside of Xinha 
   *  and copy this file to it and change the line above
   *  appropriately).
   *
   */
  
  // You will probably need to change this if you copy this file elsewhere!
  require_once(realpath(dirname(__FILE__) . '/../../../contrib/php-xinha.php'));
  
  $dh = opendir(dirname(__FILE__)); 
  $smileys = array();  
  while($f = readdir($dh))
  {
    $M = array();
    if(preg_match('/^(.*)\.(gif|jpg|png)$/i', $f, $M))
    {
      $smileys[] = array('title' => $M[1], 'src'=> $f);
    }
  }
  closedir($dh);
  
  
  echo xinha_to_js($smileys);
  
?>