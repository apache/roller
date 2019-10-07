<?php
  /** Pass data to a Xinha plugin backend without using PHP session handling functions.
   *
   *  Why?  Suhosin, that's why.  Suhosin has an option to encrypt sessions, which sounds
   *  great, but is a bad idea for us, particularly when it includes the user_agent in the
   *  encryption key, because any flash file doing a GET/POST has a different user agent to 
   *  the browser itself causing the session to not decrypt, and worse, causes the session
   *  to reset even though it doesn't decrypt.
   *
   *  As a result, this method (in combination with the above) uses a simple file-storage
   *  mechanism to record the necessary secret data instead of the php sessions.
   *
   *  Since this method doesn't pass much data in the url/post data, it can also be used
   *  if you run into mod_security type restrictions.
   *
   *  Caution, if you are load balancing, then all the servers in that cluster will need 
   *  to be able to get to the tmp_path, or you tie each user to a certain server.
   *
   * @param array   The data to pass
   * @param string  The path to a temporary folder where we can create some folders and files
   *                  if not supplied, it will be sys_get_temp_dir()
   * @param bool    If you want the data returned as a PHP structure instead of echo'd as javascript
   */
   
  function xinha_pass_to_php_backend_without_php_sessions($data, $tmp_path = NULL, $ReturnPHP = FALSE)
  {   
    $bk = array();        
    $bk['data']                     = serialize($data);                   
    $key = xinha_get_backend_hash_key($bk, $tmp_path);    
    
    $bk['hash']         = 
      function_exists('sha1') ? 
        sha1($key . $bk['data']) 
      : md5($key . $bk['data']);
      
      
    // The data will be passed via a postback to the 
    // backend, we want to make sure these are going to come
    // out from the PHP as an array like $bk above, so 
    // we need to adjust the keys.
    $backend_data = array();
    foreach($bk as $k => $v)
    {
      $backend_data["backend_data[$k]"] = $v; 
    }
    
    if($ReturnPHP)
    {      
      return array('backend_data' => $backend_data);      
    }
    else
    {      
      echo 'backend_data = ' . xinha_to_js($backend_data) . "; \n";  
    }                
  }  
   
  /** The completment of xinha_pass_to_php_backend_without_php_sessions(), read the backend
   *  data which was passed and return it.
   *
   *  @return array|NULL As was supplied as $data to xinha_pass_to_php_backend_without_php_sessions()
   *        
   */
   
  function xinha_read_passed_data_without_php_sessions()
  {
    $bk = $_REQUEST['backend_data'];
    $key  = xinha_get_backend_hash_key($bk);   
    $hash = function_exists('sha1') ? 
        sha1($key . $bk['data']) 
      : md5($key . $bk['data']);
      
    if(!strlen($hash) || ($hash !== $bk['hash']))
    {
      throw new Exception("Security error, hash mis-match.");
    }
    
    return unserialize($bk['data']);
  }
  
  /** 
   * For a given backend data, return a key (salt) to use when hashing.
   *
   * @param  array  array('data' =>  'serialized data' )
   * @param  string Path to store temporary files which contain the keys, must be writable
   *                 defaults to sys_get_temp_dir()
   *
   * @modifies $bk will be modified to add ['tmp_path'] and ['xinha-backend-key-id']
   *                 the former is added only if it does not exist and will be equivalent to
   *                 the supplied $tmp_path
   *
   *                 the latter is added only if it does not exist, and is generated as a unique 
   *                 identifier of arbitrary length
   *
   * @access private
   */
   
  function xinha_get_backend_hash_key(&$bk, $tmp_path = NULL)
  {  
    // The key itself will be written into a file inside the "tmp_path", 
    if(!isset($bk['tmp_path']))
    {
      if(!isset($tmp_path))
      {
        $tmp_path = sys_get_temp_dir();
      }
      
      $bk['tmp_path'] = $tmp_path;
    }
            
    // The file will be formed from the "key id"
    if(isset($bk['xinha-backend-key-id']))
    {
      $key_id = $bk['xinha-backend-key-id'];
    }
    elseif(isset($_COOKIE['xinha-backend-key-id']))
    {      
      $key_id = $_COOKIE['xinha-backend-key-id'];      
      $bk['xinha-backend-key-id'] = $key_id;
    }
    else
    {
      $key_id = uniqid('xinha-', TRUE);
      @setcookie('xinha-backend-key-id', $key_id, 0, '/'); // Not the end of the world if this fails      
      $bk['xinha-backend-key-id'] = $key_id;
    }
            
    // We don't trust the key-id itself to not be some naughty construct, so
    // the filename is md5'd, we chunk_split it to ensure that we don't go using
    // too many inodes in a single folder.  We create that split path in a sub folder
    // of the tmp_path so that multiple Xinha installs on the same server don't trample
    // each other.  So the final result is...
    // [tmp_path]/xinha-[install-path-md5-hash]/a1/a1/a1/a1/a1/a1/a1...../xinha_key
    $KeyFile = realpath($bk['tmp_path']) . "/xinha-".md5(__FILE__) . "/" . chunk_split(md5($key_id),2, "/") . "xinha_key";
    
    if(!file_exists($KeyFile))
    {
      // Without a keyfile, this could mean 2 things
      //  1. We have been asked to create a keyfile, in this case, $tmp_path MUST be set (by now, see default above)
      //  2. The keyfile has disappeared
      
      // Case 2
      if(!isset($tmp_path))
      {
        throw new Exception("Unable to locate security key, reload the page and try again.");
      }
      
      // Case 1, because we named the keyfile from the tmp_path in $bk, double check somebody isn't fiddling with that
      if($tmp_path !== $bk['tmp_path'])
      {
        throw new Exception("Attempt to write new key with invalid tmp_path");
      }
      
      // If we can't write to the path, then we are no good
      if(!is_dir($bk['tmp_path']) || !is_writable($bk['tmp_path']))
      {
        throw new Exception( "Xinha is unable to write to {$bk['tmp_path']} while trying to pass to backend without sessions." );     
      }
        
      // Finally looks to be OK, so write a new key into the keyfile
      mkdir(dirname($KeyFile), 0700, TRUE);
      file_put_contents($KeyFile, uniqid('Key_'));
      
      // Roll the dice to see if we should garbage collect
      if(rand(0,100) >= 90) 
      {
        xinha_garbage_collect(realpath($bk['tmp_path']) . "/xinha-".md5(__FILE__));
      }
    }
    else
    {
      // We have a keyfile, touch it to make sure the server knows it's been used recently
      touch($KeyFile);
    }
        
    return file_get_contents($KeyFile);
  }
  
  
  /** 
   * Garbage collect old key files which are created by xinha_pass_to_php_backend_without_php_sessions()
   *
   * Key files which are 12 hours old or more are culled.
   *
   * This method is called randomly by xinha_get_backend_hash_key when creating a new key (10% of the time)
   *
   * @param  string path to start garbage collection on
   * @param  string the maximum number of files to check (limits time taken)
   * @return bool true = folder now empty, false = folder still contains data
   * @access private 
   */
   
  function xinha_garbage_collect($path, &$maxcount = 100)
  {
    $d = opendir($path);
    $empty = true;
    while($f = readdir($d))
    {
      // If this is the key file, check it's age, unlink and return true (dir empty) if older than 12 hours
      // otherwise, return false (dir not empty)
      if($f === 'xinha_key')
      {
        $maxcount--;
        
        if(@filemtime($path . '/' . $f) < (time() - 60*60*12))
        {          
          if(@unlink($path . '/' . $f))
          {
            return true;
          }
        }
        
        return false;
      }
      
      // If this is a chunk directory recurse, if the recursion return false (not empty)
      //  or it returns true but we can't delete for some reason, then set empty to false
      elseif(preg_match('/^[0-9a-f]{2,2}$/', $f))
      {
        if($maxcount <= 0)
        { 
          // If we have already checked our maximum, don't do any more
          $empty = false;
        }
        elseif(!xinha_garbage_collect($path . '/' . $f, $maxcount) || !rmdir($f))
        {
          $empty = false;
        }
      }
    }
    closedir($d);
        
    return $maxcount ? $empty : false;
  }
  
  /** Write the appropriate xinha_config directives to pass data to a PHP (Plugin) backend file.
   *
   *  ImageManager Example:
   *  The following would be placed in step 3 of your configuration (see the NewbieGuide 
   *  (http://xinha.python-hosting.com/wiki/NewbieGuide)
   *
   * <script language="javascript">
   *  with (xinha_config.ImageManager)
   *  { 
   *    <?php 
   *      xinha_pass_to_php_backend
   *      (       
   *        array
   *        (
   *         'images_dir' => '/home/your/directory',
   *         'images_url' => '/directory'
   *        )
   *      )
   *    ?>
   *  }
   *  </script>
   * 
   */
      
  function xinha_pass_to_php_backend($Data, $KeyLocation = 'Xinha:BackendKey', $ReturnPHP = FALSE)
  {
    // A non default KeyLocation which is an existing directory is treated as
    // a request to not use sessions
    if($KeyLocation != 'Xinha:BackendKey' && file_exists($KeyLocation) && is_dir($KeyLocation))
    {
      return xinha_pass_to_php_backend_without_php_sessions($Data, $KeyLocation, $ReturnPHP);
    }
  
    // If we are using session based key passing, then make sure that suhosin isn't 
    // going to screw things up, fall back to no-sessions version
    if(@ini_get('suhosin.session.cryptua'))
    {      
      if($KeyLocation == 'Xinha:BackendKey')
      {
        // Really should throw up a warning here, because the file-based key storage might
        // not be suitable out of the box for cluster type environments
        return xinha_pass_to_php_backend_without_php_sessions($Data, NULL, $ReturnPHP);      
      }
      else
      {
        throw new Exception("Use of the standard xinha_pass_to_php_backend() is not possible because this server uses suhosin.session.cryptua.  Use xinha_pass_to_php_backend_without_php_sessions() instead, or disable suhosin.session.cryptua.");
      }
    } 
   
    $bk = array();
    $bk['data']       = serialize($Data);
    
    @session_start();
    if(!isset($_SESSION[$KeyLocation]))
    {
      $_SESSION[$KeyLocation] = uniqid('Key_');
    }
    
    $bk['session_name'] = session_name();      
    $bk['key_location'] = $KeyLocation;      
    $bk['hash']         = 
      function_exists('sha1') ? 
        sha1($_SESSION[$KeyLocation] . $bk['data']) 
      : md5($_SESSION[$KeyLocation] . $bk['data']);
      
      
    // The data will be passed via a postback to the 
    // backend, we want to make sure these are going to come
    // out from the PHP as an array like $bk above, so 
    // we need to adjust the keys.
    $backend_data = array();
    foreach($bk as $k => $v)
    {
      $backend_data["backend_data[$k]"] = $v; 
    }
    
    // The session_start() above may have been after data was sent, so cookies
    // wouldn't have worked.
    $backend_data[session_name()] = session_id();
    
    if($ReturnPHP)
    {      
      return array('backend_data' => $backend_data);      
    }
    else
    {      
      echo 'backend_data = ' . xinha_to_js($backend_data) . "; \n";  
    }                
  }  
   
  /** Convert PHP data structure to Javascript */
  
  function xinha_to_js($var, $tabs = 0)
  {
    if(is_numeric($var))
    {
      return $var;
    }
  
    if(is_string($var))
    {
      return "'" . xinha_js_encode($var) . "'";
    }
  
    if(is_bool($var))
    {
      return $var ? 'true': 'false';
    }
  
    if(is_array($var))
    {
      $useObject = false;
      foreach(array_keys($var) as $k) {
          if(!is_numeric($k)) $useObject = true;
      }
      $js = array();
      foreach($var as $k => $v)
      {
        $i = "";
        if($useObject) {
          if(preg_match('#^[a-zA-Z_]+[a-zA-Z0-9_]*$#', $k)) {
            $i .= "$k: ";
          } else {
            $i .= "'$k': ";
          }
        }
        $i .= xinha_to_js($v, $tabs + 1);
        $js[] = $i;
      }
      if($useObject) {
          $ret = "{\n" . xinha_tabify(implode(",\n", $js), $tabs) . "\n}";
      } else {
          $ret = "[\n" . xinha_tabify(implode(",\n", $js), $tabs) . "\n]";
      }
      return $ret;
    }
  
    return 'null';
  }
    
  /** Like htmlspecialchars() except for javascript strings. */
  
  function xinha_js_encode($string)
  {
    static $strings = "\\,\",',%,&,<,>,{,},@,\n,\r";
  
    if(!is_array($strings))
    {
      $tr = array();
      foreach(explode(',', $strings) as $chr)
      {
        $tr[$chr] = sprintf('\x%02X', ord($chr));
      }
      $strings = $tr;
    }
  
    return strtr($string, $strings);
  }
        
   
  /** Used by plugins to get the config passed via 
  *   xinha_pass_to_backend()
  *  returns either the structure given, or NULL
  *  if none was passed or a security error was encountered.
  */
  
  function xinha_read_passed_data($KeyLocation = 'Xinha:BackendKey')
  {
   if(isset($_REQUEST['backend_data']['xinha-backend-key-id']))
   {
      // This is a without sessions passing, 
      return xinha_read_passed_data_without_php_sessions();
   }
   if(isset($_REQUEST['backend_data']) && is_array($_REQUEST['backend_data']))
   {
     $bk = $_REQUEST['backend_data'];
     session_name($bk['session_name']);
     @session_start(); @session_write_close();
     if(!isset($_SESSION[$bk['key_location']])) return NULL;
     
     if($KeyLocation !== $bk['key_location'])
     {
      trigger_error('Programming Error - please contact the website administrator/programmer to alert them to this problem. A non-default backend key location is being used to pass backend data to Xinha, but the same key location is not being used to receive data.  The special backend configuration has been ignored.  To resolve this, find where you are using xinha_pass_to_php_backend and remove the non default key, or find the locations where xinha_read_passed_data is used (in Xinha) and add a parameter with the non default key location, or edit contrib/php-xinha.php and change the default key location in both these functions.  See: http://trac.xinha.org/ticket/1518', E_USER_ERROR);     
      return NULL;
     }
          
     if($bk['hash']         === 
        function_exists('sha1') ? 
          sha1($_SESSION[$bk['key_location']] . $bk['data']) 
        : md5($_SESSION[$bk['key_location']] . $bk['data']))
     {
       return unserialize(ini_get('magic_quotes_gpc') ? stripslashes($bk['data']) : $bk['data']);
     }
   }
   
   return NULL;
  }
   
  /** Used by plugins to get a query string that can be sent to the backend 
  * (or another part of the backend) to send the same data.
  */
  
  function xinha_passed_data_querystring()
  {
   $qs = array();
   if(isset($_REQUEST['backend_data']) && is_array($_REQUEST['backend_data']))
   {
     foreach($_REQUEST['backend_data'] as $k => $v)
     {
       $v =  ini_get('magic_quotes_gpc') ? stripslashes($v) : $v;
       $qs[] = "backend_data[" . rawurlencode($k) . "]=" . rawurlencode($v);
     }       
   }
   
   $qs[] = session_name() . '=' . session_id();
   return implode('&', $qs);
  }
   
    
  /** Just space-tab indent some text */
  function xinha_tabify($text, $tabs)
  {
    if($text)
    {
      return str_repeat("  ", $tabs) . preg_replace('/\n(.)/', "\n" . str_repeat("  ", $tabs) . "\$1", $text);
    }
  }       

  /** Return upload_max_filesize value from php.ini in kilobytes (function adapted from php.net)**/
  function upload_max_filesize_kb() 
  {
    $val = ini_get('upload_max_filesize');
    $val = trim($val);
    $last = strtolower($val{strlen($val)-1});
    switch($last) 
    {
      // The 'G' modifier is available since PHP 5.1.0
      case 'g':
        $val *= 1024;
      case 'm':
        $val *= 1024;
   }
   return $val;
}
?>