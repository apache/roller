<?php
  /** Parse a master translation file for a given language 
   *   and create a directory containing split translation files
   *   for each module.
   *
   * Essentially the opposite of lc_create_merged_file.php
   *  note that this does not overwrite the existing language files
   *  it creates new ones in a new directory.
   *
   * You can then use diff to compare the results before you copy 
   *  them over.
   */
   

  $InFile = $argv[1];
  $TargetLanguage = $argv[2];

  function usage()
  {
    global $argv;
    echo "Usage: ";
    echo $argv[0] . ' /path/to/fr.js fr [outputdirectory]' . "\n\n";
    
    echo " Output directory defaults to a new unique directory in /tmp\n\n";
    
    echo "PROTIP: To split all language files in lang/merged/*.js you can do...
    
    if ! [ -f XinhaCore.js ] ; then cd ../; fi
    OUTDIR=\"/tmp/lang-output-$(date -u | sed -r 's/ /-/g')\"
    mkdir \$OUTDIR
    for file in lang/merged/*.js
    do
      if ! [ \"$(basename \$file)\" = \"__new__.js\" ]
      then
        php contrib/".basename($argv[0])." \$file \"\$(basename \$file | sed -r 's/\.js//')\" \$OUTDIR
      fi
    done

";
    exit;
  }
   
  if(!file_exists($InFile) || !strlen($TargetLanguage))
  {
    usage();
  }

  $OutDir = $argv[3] ? $argv[3] : uniqid('/tmp/xinha-lang-import-');
  @mkdir($OutDir);
  
  $XinhaRoot = realpath(dirname(__FILE__).'/..');
  
  $languageDirs = array(
    $XinhaRoot.'/lang',
  );
  
  function load_lang_file($file)
  {
    if(!file_exists($file)) return array();
    
    $contents = file_get_contents($file);
    // Remove comments
    $contents = preg_replace('/^(\xEF\xBB\xBF)?\s*\/\/.*$/m', '', trim($contents));
    $contents = preg_replace('/\/\*.*?\*\//s', '', $contents);
    
    $contents= trim(trim(trim($contents),';'));
    
    file_put_contents('/tmp/o', $contents);
    $decode = json_decode($contents, true);
    
    if(!$decode)
    {
      //JSON only doubel quotes
      $contents = preg_replace_callback('/^(\s*)\'(([^\']|\\\\\')+?)\'(\s*:)/m', function($m){
        $m[2] =  preg_replace('/\\\\\'/', '\'', $m[2]);
        $m[2] = preg_replace('/"/', '\\"',$m[2]);
        return $m[1].'"'.$m[2].'"'.$m[4];
      }, $contents);
      
      $contents = preg_replace_callback('/^(\s*"(?:[^"]|\\\\")+?"\s*:\s*)\'(([^\']|\\\\\')+)(\s*)\'/m', function($m){
        $m[2] =  preg_replace('/\\\\\'/', '\'', $m[2]);
        $m[2] = preg_replace('/"/', '\\"',$m[2]);
        return $m[1].'"'.$m[2].'"'.$m[4];
      }, $contents);
      
      // Escaped singles are forbidden?
      $contents = preg_replace('/\\\\\'/', "'", $contents);
      
      $contents= trim(trim(trim($contents),';'));
      $decode = json_decode($contents, true);
    }
    
    if(!$decode)
    {
      echo $contents;
      throw new Exception("Decode of {$file} failed. " . json_last_error());
    }
    return $decode;
  }

  $Data = load_lang_file($InFile);
  
  // Merge the __NEW_TRANSLATIONS__ section
  foreach(array_keys($Data['__NEW_TRANSLATIONS__']) as $moduleName)
  {
    foreach($Data['__NEW_TRANSLATIONS__'][$moduleName] as $englishString => $localString)
    {
      if(preg_match('/<<([a-z0-9_-]+)>>/i', $localString, $M))
      {
        $localString = @$Data[$M[1]][$englishString];
      }
     
      if($localString && $localString[0] == '<') 
      {
          die("Unable to find reference for {$localString}");
      }
     
      if(strlen($localString))
      {
        $Data[$moduleName][$englishString] = $localString;
      }
    }
  }
  
  // Done with that now
  unset($Data['__NEW_TRANSLATIONS__']);
  
  foreach($Data as $moduleName => $Data)
  {
    // Where is it
    if($moduleName == 'Xinha')
    {
      $langFile = 'lang/'.$TargetLanguage.'.js';
    }
    elseif(file_exists($XinhaRoot.'/modules/'.$moduleName))
    {
      $langFile = 'modules/'.$moduleName.'/lang/'.$TargetLanguage.'.js';
    }
    elseif(file_exists($XinhaRoot.'/plugins/'.$moduleName))
    {
      $langFile = 'plugins/'.$moduleName.'/lang/'.$TargetLanguage.'.js';
    }
    elseif(file_exists($XinhaRoot.'/unsupported_plugins/'.$moduleName))
    {
      $langFile = 'unsupported_plugins/'.$moduleName.'/lang/'.$TargetLanguage.'.js';
    }
    else
    {
      echo "I don't know where to put {$moduleName} for {$TargetLanguage}.";
      continue;
    }
    
    // Load the existing one if there is and find any obsoleted strings
    if(file_exists($XinhaRoot . '/'.$langFile))
    {
      $obsoleteStrings = array();
      $existingFile = load_lang_file($XinhaRoot . '/'.$langFile);
      if(isset($existingFile['__OBSOLETE__']))
      {
        // Roll obsoletes back in to be picked up again
        $existingFile = array_merge($existingFile['__OBSOLETE__']);
        unset($existingFile['__OBSOLETE__']);
      }
      
      // if the existing file string isn't in the new data
      // it is obsolete
      foreach($existingFile as $English=>$Local)
      {
        if(!isset($Data[$English]))
        {
          $obsoleteStrings[$English] = $Local;
        }
      }
      
      // Remove any note
      unset($Data['__ TRANSLATOR NOTE __']);
      
      // Sort on the key (english text)
      ksort($Data);
      
      // Push those obsoletes into the data so marked
      if(count($obsoleteStrings))
      {
        ksort($obsoleteStrings);
        $Data['__OBSOLETE__'] = $obsoleteStrings;
      }
      
      // Resort so that the existing data order is preserved
      //  if possible
      /* Nope, don't do this, we will just make a clean break of it 
         and have everything alpha sorted
         
          $newData = array();
          foreach(array_keys($existingFile) as $English)
          {
            if(isset($Data[$English]))
            {
              $newData[$English] = $Data[$English];
              unset($Data[$English]);
            }
          }
          foreach(array_keys($Data) as $English)
          {
            $newData[$English] = $Data[$English];
            unset($Data[$English]);
          }
          $Data = $newData;
      */
      
    }
    
    $Dest = $OutDir . '/'. $langFile;
    if(!file_exists(dirname($Dest)))
    {
      mkdir(dirname($Dest), 0777, true);
    }
    
    $outputData = "// I18N constants
// LANG: \"{$TargetLanguage}\", ENCODING: UTF-8
//
// IMPORTANT NOTICE FOR TRANSLATORS
// ============================================================================
//
// Please be sure you read the README_TRANSLATORS.TXT in the Xinha Root 
// Directory.  Unless you are making a new plugin or module it is unlikely 
// that you want to be editing this file directly.
//
".json_encode($Data, JSON_PRETTY_PRINT|JSON_UNESCAPED_SLASHES|JSON_UNESCAPED_UNICODE).';';
    file_put_contents($Dest, $outputData);
  }
  
  echo "Parsing complete, files have been written to: $OutDir\n";
  echo "You should now inspect those files and copy to the Xinha directory tree as appropriate.\n";
  echo "If this is a half decent Unix type system, you can do something like this to copy everything over blindly.\n";
  echo 
"
    for file in $(find $OutDir -type f)
    do 
      target=" . dirname(dirname(__FILE__)) . "$(echo \$file | sed -r 's/^.*".(basename($OutDir))."//')
      echo \"Installing: \$file -> \$target\"
      cp \$file \$target
    done
";
  echo "\n";
?>