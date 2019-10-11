<?php

  /** Generate a master translation file for a given language.
   *
   *  The master file makes it easier for people to do a translation job, 
   *   it combines the text to be translated from all language files for
   *   a given language into a single file.  Duplicates are so marked
   *   so that they do not need to be translated multiple times.
   *  
   *  New translations are put in a section allowing that.
   *
   *  Pass a completed master translation file to 
   *    "split_translation_file.php"
   *  in order to break it apart and update the individual languages.
   *
   * The resulting file is a json file of this structure...
   
 {
    'Xinha': {
      'Hello World': 'Bonjour Tout Le Monde',
      'Good Bye':    'Au revoir',
    },
    
    'TableOperations': {
      'Insert Table': 'Insérer un tableau'
    },
    
    __NEW_TRANSLATIONS__: {
    
      'Xinha': {
        'Hi': '',                          // This translation is new, translate as appropriate.
      },
      
      'TableOperations': {

        'Good Bye': 'Au revoir',           // This translation is new for this context (TableOperations) but 
                                           //  was found in some other context (possibly as a obsolete translation)
                                           //  check that it is correct and appropriate in this context.
      
        'Hi': '<<Xinha>>'                  // This is a new translation in a prior context pending translation
                                           //  it will use that new translation, or if not appropriate the 
                                           //  translator can replace tehe link (<<Xinha>>) with a specific
                                           //  translation.
      }
    
    }
 }
   
*/

// Yes I know that this code is all a horrible mess of copy-paste-coding, it 
// was hacked togethor as I went along deciding what worked and what didn't 
// it's not worth refactoring to clean it up.

  $TargetLanguage = $argv[1];
  $OutputFile     = @$argv[2];
  if($OutputFile == '-') $OutputFile = null;
  
  if(!@$TargetLanguage || $TargetLanguage == '-h' || $TargetLanguage == '--help' )
  {
    echo "
    
Usage: {$argv[0]} {ln} [of]
   ln: language code, eg fr
   of: file to put the generated json into, eg lang/merged/fr.js
                   defaults to stdout
   
   You must have created the language base files using lc_parse_strings.php
   
   PROTIP:  On a half decent unix type box you should be able to do this from 
      the Xinha root directory to create all of the lang/merged/*.js files
      
   if ! [ -f XinhaCore.js ] ; then cd ../; fi
   php contrib/lc_parse_strings.php
   for lang in $(find . -wholename \"*/lang/*.js\" | sed -r 's/.*\///' | sort | uniq | grep -v base | sed -r 's/.js//')
   do
     php contrib/".basename($argv[0]) . " \$lang lang/merged/\$lang.js
   done
   php contrib/".basename($argv[0]) . " NEW lang/merged/__new__.js
   
";
    exit(1);
  }
  
  $XinhaRoot = realpath(dirname(__FILE__).'/..');
  
  $languageDirs = array(
    $XinhaRoot.'/lang',
  );
  
  function load_lang_file($file)
  {
    if(!file_exists($file)) return array();
   
    $contents = file_get_contents($file);
    $contents = preg_replace('/^(\xEF\xBB\xBF)?\s*\/\/.*$/m', '', trim($contents));
    $contents = preg_replace('/\/\*.*?\*\//s', '', $contents);
    
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
    
    if(!$decode)
    {
      echo $contents;
      throw new Exception("Decode of {$file} failed. " . json_last_error());
    }
    return $decode;
  }

  foreach(array('modules', 'plugins', 'unsupported_plugins') as $dir)
  {
    $dh = opendir($XinhaRoot.'/'.$dir);
    while($f = readdir($dh))
    {
      if($f[0] == '.') continue;
      if(is_dir($XinhaRoot.'/'.$dir.'/'.$f.'/lang'))
      {
        if(!file_exists($XinhaRoot.'/'.$dir.'/'.$f.'/lang'.'/lc_base.js'))
        {
          fprintf(STDERR, "Warning: No base language file found for " .$XinhaRoot.'/'.$dir.'/'.$f.'/lang'.'/lc_base.js'.", ensure that you run lc_parse_strings.php to generate lc_base.js\n");
          continue;
        }
        $languageDirs[] = $XinhaRoot.'/'.$dir.'/'.$f.'/lang';
      }
    }
  }

  // Sort the language directories by module/plugin name, except unsupported which are last (and then alpha)
  // and the core which is first
  function what_type($a)
  {
    if(preg_match('/modules/', $a)) return 'MODULE';
    if(preg_match('/unsupported/', $a)) return 'UNSUPPORTED';
    if(preg_match('/plugins/', $a)) return 'PLUGIN';
    return 'CORE';
  }
  
  function what_name($a)
  {
    switch(what_type($a))
    {
      case 'CORE': return 'Xinha';
    }
    return basename(dirname($a));
  }
  
  function sort_lang_dir($a, $b)
  {
    if(what_type($a) === what_type($b)) return strcmp(what_name($a), what_name($b));
    
    if(what_type($a) == 'CORE') return -1;
    if(what_type($b) == 'CORE') return  1;
    
    if(what_type($a) == 'UNSUPPORTED') return 1;
    if(what_type($b) == 'UNSUPPORTED') return -1;
    
     return strcmp(what_name($a), what_name($b));
  }
  usort($languageDirs, 'sort_lang_dir');
  
  // Record the reference for the first time we encounter a translated string
  $firstReference = array();
  
  $outputData     = array();
  
  // Setup the array so that the ordering is correct
  $Nt = '__ TRANSLATOR NOTE __';   
  foreach($languageDirs as $langX => $dir)
  {
    $moduleName = '';
    
    if($langX == 0)
    {
      $moduleName = 'Xinha';
      $moduleType = 'CORE';
    }
    elseif(preg_match('/\/(modules|unsupported_plugins|plugins)\/([^\/]+)/', $dir, $M))
    {
      $moduleName = $M[2];
      switch($M[1])
      {
        case 'modules':
          $moduleType = 'MODULE';
          break;
          
        case 'plugins':
          $moduleType = 'PLUGIN';
          break;
          
        case 'unsupported_plugins':
          $moduleType = 'UNSUPPORTED';
          break;
      }
    }
    else
    {
      fprintf(STDERR, "Unable to figure out a module name for {$dir} [".__LINE__."]");
      exit;
    }
    
    $outputData['__NEW_TRANSLATIONS__'][$moduleName] = array();
    $outputData[$moduleName] = array();
         
    if($moduleType == 'UNSUPPORTED')
    {
      $outputData['__NEW_TRANSLATIONS__'][$moduleName][$Nt] = "*** ".strtoupper($moduleName)." IS UNSUPPORTED (TRANSLATE AT YOUR DISCRETION) ***";
    }
  }
  ksort($outputData);
  ksort($outputData['__NEW_TRANSLATIONS__']);
  
  //Pull the unsuipported ones out of new translations and put last
  foreach(array_keys($outputData['__NEW_TRANSLATIONS__']) as $k)
  {
    if(@$outputData['__NEW_TRANSLATIONS__'][$k][$Nt])
    {
      $v = $outputData['__NEW_TRANSLATIONS__'][$k];
      unset($outputData['__NEW_TRANSLATIONS__'][$k]);
      $outputData['__NEW_TRANSLATIONS__'][$k] = $v;
    }
  }
  
  
  // First do the existing translations
  foreach($languageDirs as $langX => $dir)
  {
    $baseLang   = json_decode(trim(preg_replace('/^\s*\/\/.*$/m', '', file_get_contents($dir.'/lc_base.js'))),TRUE);
    $targetLang = load_lang_file(($dir.'/'.$TargetLanguage.'.js'));
    $moduleName = '';
    
    if($langX == 0)
    {
      $moduleName = 'Xinha';
      $moduleType = 'CORE';
    }
    elseif(preg_match('/\/(modules|unsupported_plugins|plugins)\/([^\/]+)/', $dir, $M))
    {
      $moduleName = $M[2];
      switch($M[1])
      {
        case 'modules':
          $moduleType = 'MODULE';
          break;
          
        case 'plugins':
          $moduleType = 'PLUGIN';
          break;
          
        case 'unsupported_plugins':
          $moduleType = '*** UNSUPPORTED (TRANSLATE AT YOUR DISCRETION) ***';
          break;
      }
    }
    else
    {
      fprintf(STDERR, "Unable to figure out a module name for {$dir} [".__LINE__."]");
      exit;
    }
    
    if(!is_array($baseLang))
    {
      echo "\n\n";
      print_r($baseLang);
      echo $dir.'/lc_base.js' . "\n";
      echo trim(preg_replace('/^\s*\/\/.*$/m', '', file_get_contents($dir.'/lc_base.js'))) . "\n";
      print_r($baseLang);
      echo "\n".json_last_error()."\n";
      die();
      
    }
    
    // Do the existing translations first
    foreach($baseLang as $English => $Nothing)
    {
        
      if(isset($targetLang[$English]) && strlen($targetLang[$English]))
      {
        if(!isset($outputData[$moduleName]))
        {
          $outputData[$moduleName]= array(
      //      '__TYPE__'      => $moduleType,
      //      '__LANG_FILE__' => $dir.'/'.$TargetLanguage.'.js'
          );
        }
        // It is translated
        if(!isset($firstReference[$English])) $firstReference[$English] = /*"<<$moduleName>>".*/$targetLang[$English];
        $outputData[$moduleName][$English] = $targetLang[$English];
      }
    }
  }
  
  foreach($languageDirs as $langX => $dir)
  {
    $baseLang   = json_decode(preg_replace('/^\s*\/\/.*$/m', '', file_get_contents($dir.'/lc_base.js')),TRUE);
    $targetLang = load_lang_file(($dir.'/'.$TargetLanguage.'.js'));
    $moduleName = '';
    
    if($langX == 0)
    {
      $moduleName = 'Xinha';
      $moduleType = 'CORE';
    }
    elseif(preg_match('/\/(modules|unsupported_plugins|plugins)\/([^\/]+)/', $dir, $M))
    {
      $moduleName = $M[2];
      switch($M[1])
      {
        case 'modules':
          $moduleType = 'MODULE';
          break;
          
        case 'plugins':
          $moduleType = 'PLUGIN';
          break;
          
        case 'unsupported_plugins':
          $moduleType = '*** UNSUPPORTED (TRANSLATE AT YOUR DISCRETION) ***';
          break;
      }
    }
    else
    {
      fprintf(STDERR, "Unable to figure out a module name for {$dir} [".__LINE__."]");
      exit;
    }
    
    // Record translations for any obsolete ones as a first reference
    //  the larget language files may have a section for obsolete translations
    //  called __OBSOLETE__  - this is added by the lc_split_merged_file.php
    //  script.
    // Merge this into all the strings to examine
    if(isset($targetLang['__OBSOLETE__']))
    {
      $targetLang = array_merge($targetLang, $targetLang['__OBSOLETE__']);
      unset($targetLang['__OBSOLETE__']);
    }
    
    // Record a first reference if not already recorded for all strings in the 
    // target
    foreach($targetLang as $English => $Local)
    { 
      if(!isset($firstReference[$English]))
      {
        $firstReference[$English] = $Local;
      }
    }
  }
  
  // Then do the new ones
  foreach($languageDirs as $langX => $dir)
  {
  
  echo "Process ".$dir.'/lc_base.js'."\n";
  
    $baseLang   = json_decode(trim(preg_replace('/^\s*\/\/.*$/m', '', file_get_contents($dir.'/lc_base.js'))), true);
    $targetLang = load_lang_file(($dir.'/'.$TargetLanguage.'.js'));
    $moduleName = '';
    $moduleType = '';
    if($langX == 0)
    {
      $moduleName = 'Xinha';
      $moduleType = 'CORE';
    }
    elseif(preg_match('/\/(modules|unsupported_plugins|plugins)\/([^\/]+)/', $dir, $M))
    {
      $moduleName = $M[2];
      switch($M[1])
      {
        case 'modules':
          $moduleType = 'MODULE';
          break;
          
        case 'plugins':
          $moduleType = 'PLUGIN';
          break;
          
        case 'unsupported_plugins':
          $moduleType = 'UNSUPPORTED';
          break;
      }
    }
    else
    {
      fprintf(STDERR, "Unable to figure out a module name for {$dir} [".__LINE__."]");
      exit;
    }
    
    // Do the new translations now
    foreach($baseLang as $English => $Nothing)
    {

      if(!isset($targetLang[$English]))
      {
        
        // Do we have an existing translation in some other module?
        if(isset($firstReference[$English]))
        {
          $outputData['__NEW_TRANSLATIONS__'][$moduleName][$English]  = "{$firstReference[$English]}";
        }
        // Do we have a lowercase translation?
        elseif(isset($firstReference[strtolower($English)]))
        {
          $Ref = $firstReference[strtolower($English)];
          $outputData['__NEW_TRANSLATIONS__'][$moduleName][$English]  = "{$Ref}";
        }
        // Do we have a translation without a :?
        elseif(preg_match('/:\s*$/',$English) && isset($firstReference[preg_replace('/:\s*$/', '', ($English))]))
        {
          $Ref = $firstReference[preg_replace('/:\s*$/', '', ($English))];
          $outputData['__NEW_TRANSLATIONS__'][$moduleName][$English]  = "{$Ref}";
        }
        // Do we have a translation with a :?
        elseif((!preg_match('/:\s*$/',$English)) && isset($firstReference[($English.':')]))
        {
          $Ref = $firstReference[($English.':')];
          $outputData['__NEW_TRANSLATIONS__'][$moduleName][$English]  = "{$Ref}";
        }
        // Nothing appropriate was found
        else
        {
          $firstReference[$English] = '<<'.$moduleName.'>>';
          $outputData['__NEW_TRANSLATIONS__'][$moduleName][$English] = '';
        }
      }
    }
  }
  
  // We want to sort the untranslated to the top, put the translated, and linked, to the bottom
  function sort_lang($array)
  {
    $temp = array();
    foreach($array as $k=>$v) 
    {
      $temp[] = array($k, $v);
    }
    
    usort($temp, function($a,$b) { 
    if(preg_match('/^__.*__$/', $a[0])  && !preg_match('/^__.*__$/', $b[0])) return -1; // Put __xxx__ informational at the start
    if(!preg_match('/^__.*__$/', $a[0]) && preg_match('/^__.*__$/', $b[0])) return   1;
    
    if($a[1] == '' && $b[1] != '') return -1; // Put  empty translations next
    if($a[1] != '' && $b[1] == '') return 1;
  
    // Put translated next
    if(preg_match('/^<</', $a[1]) && !preg_match('/^<</', $b[1])) return 1;
    if(preg_match('/^<</', $b[1]) && !preg_match('/^<</', $a[1])) return -1;
    
    // And now by alpha
    return strcmp(strtolower(trim($a[0])),strtolower(trim($b[0]))); 
    
    });
    
    //print_r($temp);
    $temp2 = array();
    foreach($temp as $k=>$v) 
    { 
      $temp2[$v[0]] = $v[1];
    }
    
    return $temp2;
  }
  
  foreach(array_keys($outputData['__NEW_TRANSLATIONS__']) as $moduleName)
  {
    $outputData['__NEW_TRANSLATIONS__'][$moduleName] = sort_lang($outputData['__NEW_TRANSLATIONS__'][$moduleName]);
  }
  
  // Clean up any empty sets
  
  foreach(array_keys($outputData['__NEW_TRANSLATIONS__']) as $k)
  {
    if(!count($outputData['__NEW_TRANSLATIONS__'][$k]))
    {
      unset($outputData['__NEW_TRANSLATIONS__'][$k]);
      continue;
    }
    
    // Unsupported ones with only the note
    if(count($outputData['__NEW_TRANSLATIONS__'][$k]) == 1 && isset($outputData['__NEW_TRANSLATIONS__'][$k][$Nt]))
    {
      unset($outputData['__NEW_TRANSLATIONS__'][$k]);
      continue;
    }
  }
  
  foreach(array_keys($outputData) as $k)
  {
    if(!count($outputData[$k])) unset($outputData[$k]);
  }
  
  
  
  $outputData = json_encode($outputData, JSON_PRETTY_PRINT|JSON_UNESCAPED_SLASHES|JSON_UNESCAPED_UNICODE);
  
  $data = "// Xinha Language Combined Translation File\n";
  $data .= "//\n";
  $data .= "// LANG: \"$TargetLanguage\", ENCODING: UTF-8\n";
  $data .= "//\n";   
  $data .= "// INSTRUCTIONS TO TRANSLATORS
// ===========================================================================
//
// Your translation must be in UTF-8 Character Encoding.
//
// This is a JSON encoded file (plus comments), strings should be double-quote
// only, do not use single quotes to surround strings - \"hello\", not 'hello'
// do not have a trailing comma after the last entry in a section.
//
// Only full line comments are allowed (that a comments occupy entire lines).
//
// Search for the __NEW_TRANSLATIONS__ section below, this is where you will
// want to focus, this section includes things that do not presently have a 
// translation or for which the translation needs to be checked for accuracy.
//
// In the New Translations section a translation string is one of the following
//
//  \"English String Here\" : \"\"
//     This means it is not translated yet, add your translation...
//     \"English String Here\" : \"Klingon String Here\"
//
//  \"English String Here\" : \"Translated String Here\"
//     This means that an existing translation for this string, in some other
//     section has been found, and used.  Check that it is approprite for this
//     section and if it is, that's fine leave it as is, otherwise change as
//     appropriate.
//
//  \"English String Here\" : \"<<AnotherSection>>\"
//     This means use the same translation for this string as <<AnotherSection>>
//     this saves you re-tranlating strings.  If the Context of this section
//     and the context of AnotherSection seem the same, that's fine leave it
//     using that translation, but if this section needs a different translation, 
//     you can provide it by replacing the link (<<AnotherSection>>) with that
//     new translation.  For example - a \"Table\" in say \"DataPlugin\" is 
//     perhaps translated differently to \"Table\" in \"FurniturePlugin\".
//
// TESTING YOUR TRANSLATION
// ===========================================================================
// Simply place your translation file on your webserver somewhere for example
//
//   /xinha/lang/merged/{$TargetLanguage}.js
//
// and then tell Xinha where to get it (before loading XinhaCore.js) by 
//
//  _editor_lang              = '{$TargetLanguage}';
//  _editor_lang_merged_file  = '/xinha/lang/merged/{$TargetLanguage}.js';
//
// Xinha will load your new language definition.
//
// SUBMITTING YOUR TRANSLATION
// ===========================================================================
// Simply create a Ticket on the Xinha website and attach your translation 
// file.
//
// The Xinha developers will take your file and use the 
//     contrib/lc_split_merged_file.php
// script to load it into the Xinha distribution.
";
  $data .= "\n";
  
  if(!@$OutputFile)
  {
    echo $data . $outputData;
  }
  else
  {
    file_put_contents($OutputFile, $data.$outputData);
  }
?>