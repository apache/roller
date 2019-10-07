<?php
//die("this script is disabled for security");

/**
  * LC-Parse-Strings-Script
  *
  * This script parses all xinhas source-files and creates base language files...
  *
  *   /lang/lc_base.js
  *   /modules/.../lang/lc_base.js
  *   /plugins/.../lang/lc_base.js
  *   /unsupported_plugins/.../lang/lc_base.js
  *
  * This script should be used from the command line, not from a web browser
  *  (well it should work, but anyway).
  *
  * How To use it: - remove the die() in line 2 (security)
  *                - cd contrib && php ./lc_parse_strings.php
  *                - lang/lc_base.js will be written
  *                - open lc_base.js, translate all strings into your language and save it
  *                  as yourlangauge.js
  *                - send the translated file to the xinha-team
 **/


error_reporting(E_ALL);

$XinhaRoot = realpath(dirname(__FILE__).'/..');

$ret = array();
$files = getFiles($XinhaRoot, "/js$/");
foreach($files as $file) {
    $fp = fopen($file, "r");
    $data = "";
    while(!feof($fp)) {
        $data .= fread($fp, 1024);
    }

    preg_match_all("#_lc\(\s*\"((?:[^\"]|\\\\\")+)\"\s*[,)]#", $data, $m);            
    foreach($m[1] as $i) {
        if(trim(strip_tags($i))=="") continue;
        $ret[] = preg_replace('/\\\\"/', '"', $i);
    }
    
    preg_match_all('#_lc\(\s*\'((?:[^\']|\\\\\')+)\'\s*[,)]#', $data, $m);
    foreach($m[1] as $i) {
        if(trim(strip_tags($i))=="") continue;
        $ret[] = preg_replace("/\\\\'/", "'", $i);
    }

    if(preg_match('/(XinhaCore|XinhaLoader|htmlarea)\.js$/', $file)) {
        //toolbar-buttons
        //bold:          [ "Bold"
        preg_match_all('#[a-z]+: *\[ * "([^"]+)"#', $data, $m);
        foreach($m[1] as $i) {
            if(trim($i)=="") continue;
            $ret[] = $i;
        }

        //HTMLArea._lc({key: 'button_bold', string
        preg_match_all('#(Xinha|HTMLArea)\\._lc\\({key: \'(([^\']|\\\\\')+)\'\s*[,)]#', $data, $m);
        foreach($m[1] as $i) {
            if(trim($i)=="") continue;
            $ret[] = $i;
        }

        //config.fontname, fontsize and formatblock
        $data = substr($data, strpos($data, "this.fontname = {"), strpos($data, "this.customSelects = {};")-strpos($data, "this.fontname = {"));
        preg_match_all('#"([^"]+)"[ \t]*:[ \t]*["\'][^"\']*["\'],?#', $data, $m);
        foreach($m[1] as $i) {
            if(trim($i)=="") continue;
            $ret[] = $i;
        }
    }
}

$files = getFiles("{$XinhaRoot}/popups", "/html$/");
foreach($files as $file)
{
    if(preg_match("#custom2.html$#", $file)) continue;
    if(preg_match('#old_#', $file)) continue;
    $ret = array_merge($ret, parseHtmlFile($file));
}
$ret = array_unique($ret);
$langData['HTMLArea'] = $ret;


foreach(array('plugins', 'modules', 'unsupported_plugins') as $pDir)
{
$plugins = getFiles("{$XinhaRoot}/{$pDir}");
foreach($plugins as $pluginDir) {
    $plugin = basename($pluginDir);//preg_replace('/\.\.\/[^/]+\//', '', $pluginDir);
    
    $ret = array();

    $files = getFiles("$pluginDir", "/js$/");
    $files = array_merge($files, getFiles("$pluginDir/popups", "/html$/"));
    $files = array_merge($files, getFiles("$pluginDir", "/php$/"));
    foreach($files as $file)
    {
        $fp = fopen($file, "r");
        $data = "";
        if($fp) {
            echo "$file open\n";
            while(!feof($fp)) {
              $data .= fread($fp, 1024);
            }
            
            preg_match_all("#_lc\(\s*\"((?:[^\"]|\\\\\")+)\"\s*[,)]#", $data, $m);            
            foreach($m[1] as $i) {
                if(trim(strip_tags($i))=="") continue;
                $ret[] = preg_replace('/\\\\"/', '"', $i);
            }
            
            preg_match_all('#_lc\(\s*\'((?:[^\']|\\\\\')+)\'\s*[,)]#', $data, $m);
            foreach($m[1] as $i) {
                if(trim(strip_tags($i))=="") continue;
                $ret[] = preg_replace("/\\\\'/", "'", $i);
            }
            
            
        }
    }

    $files = array_merge($files, getFiles("$pluginDir", "/\.(html|php)$/"));
    $files = array_merge($files, getFiles("$pluginDir/popups", "/\.(html|php)$/"));
    $files = array_merge($files, getFiles("$pluginDir/dialogs", "/\.(html|php)$/"));
    foreach($files as $file)
    {
        $ret = array_merge($ret, parseHtmlFile($file, $plugin));
    }
    
    $langData[$plugin] = array_unique($ret);    
}
}



foreach($langData as $plugin=>$strings) {
    

    $data = "// I18N constants\n";
    $data .= "//\n";
    $data .= "// LANG: \"en\", ENCODING: UTF-8\n";
    $data .= "// Author: Translator-Name, <email@example.com>\n";
    $data .= "//\n";   
    $data .= "// Last revision: ".date('Y-m-d')."\n";
    $data .= "// Please don´t remove this information\n";
    $data .= "// If you modify any source, please insert a comment with your name and e-mail\n";
    $data .= "//\n";
    $data .= "// Distributed under the same terms as HTMLArea itself.\n";
    $data .= "// This notice MUST stay intact for use (see license.txt).\n";
    $data .= "//\n";
    $data .= "// (Please, remove information below)\n";   
    $data .= "// FOR TRANSLATORS:\n";
    $data .= "//\n";
    $data .= "//   1. PLEASE PUT YOUR CONTACT INFO IN THE ABOVE LINE\n";
    $data .= "//      (at least a valid email address)\n";
    $data .= "//\n";
    $data .= "//   2. PLEASE TRY TO USE UTF-8 FOR ENCODING;\n";
    $data .= "//      (if this is not possible, please include a comment\n";
    $data .= "//       that states what encoding is necessary.)\n";
    $data .= "\n";
    /*
    $data .= "{\n";
    
    sort($strings);
    foreach($strings as $string) {
        $string = str_replace(array('\\', '"'), array('\\\\', '\\"'), $string);
        $data .= "  \"".$string."\": \"\",\n";
    }
    $data = substr($data, 0, -2);
    $data .= "\n";
    $data .= "}\n";
    */
    sort($strings);
    $js_data = array();
    foreach($strings as $string)
    {
      $js_data[$string] = '';
    }    
    $data .= json_encode($js_data,  JSON_PRETTY_PRINT|JSON_UNESCAPED_SLASHES|JSON_UNESCAPED_UNICODE);
    
    $langfile = false;
    if(preg_match('/HTMLArea|Xinha/', $plugin))
    {
      $langfile = dirname(__FILE__).'/../lang/lc_base.js';
    }
    elseif (is_dir(dirname(__FILE__).'/../modules/'.$plugin)) 
    {
      $langfile = dirname(__FILE__).'/../modules/'.$plugin.'/lang/lc_base.js';
    }
    elseif (is_dir(dirname(__FILE__).'/../plugins/'.$plugin)) 
    {
      $langfile = dirname(__FILE__).'/../plugins/'.$plugin.'/lang/lc_base.js';
    }
    elseif (is_dir(dirname(__FILE__).'/../unsupported_plugins/'.$plugin)) 
    {
      $langfile = dirname(__FILE__).'/../unsupported_plugins/'.$plugin.'/lang/lc_base.js';
    }
    else
    {
      echo "Unknown {$plugin}\n";
      continue;
      throw new Exception("What sort of plugin is {$plugin}?");
    }
    
    // If we don't have any data, remove any existing lc_base.js
    if(!count($js_data))
    {
      if(file_exists($langfile)) unlink($langfile);
      continue;
    }
    
     if(!file_exists(dirname($langfile)))
     {
       mkdir(dirname($langfile));
     }
    file_put_contents($langfile, $data);
    echo "$langfile written\n";
}




function parseHtmlFile($file, $plugin="")
{
echo "Parsing $file\n";
    $ret = array();
    
    $data = file_get_contents($file);
    
    if(preg_match('/<l10n>/i', $data) || preg_match('/"_\([^"]*\)"/', $data))
    {
      // Newer plugin dialogs use our <l10n>english</l10n> tag to indicate a translation text.
      // also a attribute="_([english])"
      $elems = array('l10n');
    }
    else
    {
      $elems = array("title", "input", "select", "legend", "span", "option", "td", "button", "div", "label");
    }

    foreach($elems as $elem) {
        preg_match_all("#<{$elem}[^>]*>([^<^\"]+)</$elem>#i", $data, $m);
        foreach($m[1] as $i) {
            if(trim(strip_tags($i))=="") continue;
            if($i=="/") continue;
            if($plugin=="ImageManager" && preg_match('#^--+$#', $i)) continue; //skip those ------
            if($plugin=="CharacterMap" && preg_match('#&[a-z0-9]+;#i', trim($i)) || $i=="@") continue;
            if($plugin=="SpellChecker" && preg_match('#^\'\\.\\$[a-z]+\\.\'$#', $i)) continue;
            $ret[] = trim($i);
        }
    }
    
    // "_( ... )" is only valide for an attribute value
    preg_match_all('#"_\(([^"]+)\)"#i', $data, $m);
    foreach($m[1] as $i) {
        if(trim($i)=="") continue;
        $ret[] = $i;
    }
      
    // Older plugins translate the title attribute
    if(count($elems) > 1)
    {
        preg_match_all('#title="([^"]+)"#i', $data, $m);
        foreach($m[1] as $i) {
            if(trim(strip_tags($i))=="") continue;
            if(strip_tags($i)==" - ") continue; //skip those - (ImageManager)
            $ret[] = $i;
        }
    }
    
    return($ret);
}


function getFiles($rootdirpath, $preg_match='') {
 $array = array();
 if ($dir = @opendir($rootdirpath)) {
   $array = array();
   while (($file = readdir($dir)) !== false) {
     if($file=="." || $file==".." || $file==".svn" || $file == ".htaccess") continue;
      if($preg_match=="")
        $array[] = $rootdirpath."/".$file;
      else if(preg_match($preg_match,$file))
        $array[] = $rootdirpath."/".$file;
      
   }
   closedir($dir);
 }
 return $array;
}


?>