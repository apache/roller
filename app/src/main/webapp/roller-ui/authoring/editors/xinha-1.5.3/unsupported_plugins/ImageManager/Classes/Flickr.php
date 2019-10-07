<?php 
/**
 * Simple functions to access the flickr API (yes I know this is the "Classes" directory and this isn't a class).
 * @author $Author: gogo $
 * @version $Id: images.php 877 2007-08-12 15:50:03Z gogo $
 * @package ImageManager
 */
 
require_once(dirname(__FILE__) . '/JSON_Compat.php');
  
function flickr_request($method, $params = array())
{
  global $IMConfig;
  $flickr = "http://api.flickr.com/services/rest/?method={$method}&format=json&nojsoncallback=1&api_key=" . $IMConfig['Flickr']['Key'];
  foreach($params as $k => $v)
  {
    $flickr .= "&{$k}=".rawurlencode($v);
  }
  
  $feed = file_get_contents($flickr);
  if($feed)
  {
    $feed = json_decode($feed, true);
    if(!$feed || !isset($feed['stat']) || ($feed['stat'] != 'ok'))
    {
      print_r($params);
      trigger_error($feed['message'], E_USER_ERROR);
      return FALSE;
    }
  }    
  else
  {
    trigger_error('Null response from Flickr', E_USER_ERROR);
  }
  
  return $feed; 
}

function flickr_get_licenses()
{
  static $lics;
  
  if(!$lics) 
  {
    if(0 && isset($_SESSION['flickr_licenses']))
    {
      $lics = $_SESSION['flickr_licenses'];
      return $lics;
    }
    
    $lics = array();
    $x = flickr_request('flickr.photos.licenses.getInfo');
    $x = $x['licenses']['license'];
    foreach($x as $l)
    {
      // Add out own descriptive "usage" text
      switch($l['url'])
      {        
        case 'http://creativecommons.org/licenses/by/2.0/':
        case 'http://creativecommons.org/licenses/by-sa/2.0/':
          $l['usage'] = 'Attribution Required';
          break;
          
        case 'http://creativecommons.org/licenses/by-nd/2.0/':
          $l['usage'] = 'Attribution Required, No Modifications';
          break;
          
        case 'http://creativecommons.org/licenses/by-nc-nd/2.0/':
          $l['usage'] = 'Non Commercial ONLY, Attribution Required, No Modifications';
          break;
          
        case 'http://creativecommons.org/licenses/by-nc/2.0/':          
        case 'http://creativecommons.org/licenses/by-nc-sa/2.0/':
          $l['usage'] = 'Non Commercial ONLY, Attribution Required';
          break;
          
        default: 
          $l['usage'] = 'Use ONLY Permitted With Written Permission';
          break;
      }
      
      // And our own identifier
      switch($l['url'])
      {        
        case 'http://creativecommons.org/licenses/by/2.0/':
          $l['x-id'] = 'cc2';
          break;
          
        case 'http://creativecommons.org/licenses/by-sa/2.0/':
          $l['x-id'] = 'ccsa2';
          break;
        
        case 'http://creativecommons.org/licenses/by-nd/2.0/':
          $l['x-id'] = 'ccnd2';
          break;
          
        case 'http://creativecommons.org/licenses/by-nc-nd/2.0/':
          $l['x-id'] = 'ccncnd2';
          break;
          
        case 'http://creativecommons.org/licenses/by-nc/2.0/':          
          $l['x-id'] = 'ccnc2';
          break;
          
        case 'http://creativecommons.org/licenses/by-nc-sa/2.0/':
          $l['x-id'] = 'ccncsa2';
          break;
          
        default: 
          $l['x-id'] = '';
          break;
      }
      
      $lics[$l['id']] = $l;      
    }
    
    $_SESSION['flickr_licenses'] = $lics;
  }
  
  return $lics;
}

function flickr_get_license_id_by_usage()
{
  $lics = flickr_get_licenses();
  $use = array();
  foreach($lics as $lic)
  {
    if(!isset($use[$lic['usage']]))
    {
      $use[$lic['usage']] = $lic['id'];
    }
    else
    {
      $use[$lic['usage']] .= "," . $lic['id'];
    }
  }
  
  return $use;
}

function flickr_is_default_license($licIDs)
{
  global $IMConfig;
  $lics = flickr_get_licenses();
  foreach($lics as $lic)
  {
    if($lic['url'] == $IMConfig['Flickr']['Default License'])
    {
      if(in_array($lic['id'], explode(',', $licIDs))) return TRUE; 
    }
  }
  
  return FALSE;
}

function flickr_get_default_usage_id()
{
  $usages = flickr_get_license_id_by_usage();
  foreach($usages as $usage => $id)
  {
    if(flickr_is_default_license($id)) return $id;
  }
  
  return 0;
}

function flickr_get_user_id($NameOrEmail)
{
  if(preg_match('/@/', $NameOrEmail))
  {
    $d = flickr_request('flickr.people.findByEmail', array('find_email' => $NameOrEmail));
    if($d)
    {
     return $d['user']['id'];
    }
  }
  else
  {
    $d = flickr_request('flickr.people.findByUsername', array('username' => $NameOrEmail));
    if($d)
    {
      return $d['user']['id'];
    }
  }  
}

?>