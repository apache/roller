<?php
  /** If there is no json_encode() or json_decode() function already in existance (recent PHP will provide native)
   *  use the PEAR::JSON class to do the job.
   *
   */
   
  if(!function_exists('json_decode'))
  {
        
    function &get_json()
    {
      global $JSON_Singleton;
      if(!isset($JSON_Singleton))
      {
        require_once(dirname(__FILE__) . '/JSON.php');
        $JSON_Singleton = new Services_JSON();
      }
           
      return $JSON_Singleton;
    }
    
    function json_decode($str, $loose = FALSE) 
    {
      $json =& get_json();
      
      if($loose)
      {
        $json->use = SERVICES_JSON_LOOSE_TYPE;
      }
      else
      {
        $json->use = 0;
      }
      
      return $json->decode($str);
    }
    
    function json_encode($var, $loose = FALSE)
    {
      $json =& get_json();
      if($loose)
      {
        $json->use = SERVICES_JSON_LOOSE_TYPE;
      }
      else
      {
        $json->use = 0;
      }
      return $json->encode($var);
    }
  }
?>