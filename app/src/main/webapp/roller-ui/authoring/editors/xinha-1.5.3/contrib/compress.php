<?php
die("Run this script to batch-compress the current Xinha snapshot. To run the script, open the file and comment out the die() command");
$repository_url = 'http://svn.xinha.org/trunk';
$version ='';
$date = date('r');
error_reporting(E_ALL);
ini_set('show_errors',1);

$return = array();
function scan($dir, $durl = '',$min_size="0")
{
	static $seen = array();
	global $return;
	$files = array();

	$dir = realpath($dir);
	if(isset($seen[$dir]))
	{
		return $files;
	}
	$seen[$dir] = TRUE;
	$dh = @opendir($dir);


	while($dh && ($file = readdir($dh)))
	{
		if($file !== '.' && $file !== '..')
		{
			$path = realpath($dir . '/' . $file);
			$url  = $durl . '/' . $file;

			if(preg_match("/\.svn|lang/",$path)) continue;
			
			if(is_dir($path))
			{
				scan($path);
			}
			elseif(is_file($path))
			{
				if(!preg_match("/\.js$/",$path) || filesize($path) < $min_size) continue;
				$return[] =  $path;
			}

		}
	}
	@closedir($dh);

	return $files;
}
scan("../");
$cwd = getcwd();

$root_dir = realpath($cwd.'/..');

print "Processing ".count($return)." files<br />";

$prefix = "/* This compressed file is part of Xinha. For uncompressed sources, forum, and bug reports, go to xinha.org */";
if ($version) $prefix .= "\n/* This file is part of version $version released $date */";

$core_prefix = '
  /*--------------------------------------------------------------------------
    --  Xinha (is not htmlArea) - http://xinha.org
    --
    --  Use of Xinha is granted by the terms of the htmlArea License (based on
    --  BSD license)  please read license.txt in this package for details.
    --
    --  Copyright (c) 2005-2008 Xinha Developer Team and contributors
    --  
    --  Xinha was originally based on work by Mihai Bazon which is:
    --      Copyright (c) 2003-2004 dynarch.com.
    --      Copyright (c) 2002-2003 interactivetools.com, inc.
    --      This copyright notice MUST stay intact for use.
    -------------------------------------------------------------------------*/
';
foreach ($return as $file)
{
	set_time_limit ( 60 ); 
	print "Processed $file<br />";
	flush();
	$file_url = $repository_url.str_replace($root_dir,'',$file);

	copy($file,$file."_uncompr.js");
	
	$file_prefix = $prefix."\n/* The URL of the most recent version of this file is $file_url */";
	
	exec("echo \"".(preg_match('/XinhaCore.js$/',$file) ? $file_prefix.$core_prefix : $file_prefix)."\" > $file && java -jar ${cwd}/dojo_js_compressor.jar -c ${file}_uncompr.js >> $file 2>&1");
	if (preg_match('/js: ".*?", line \d+:/',file_get_contents($file)) || preg_match('/sh: java: command not found/', file_get_contents($file)))
	{
		unlink($file);
		rename($file."_uncompr.js",$file);
	}
	else
	{
		unlink($file."_uncompr.js");
	}

}
print "Operation complete."
?>
