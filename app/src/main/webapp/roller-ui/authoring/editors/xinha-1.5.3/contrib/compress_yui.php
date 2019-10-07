<?php
// This is script that uses the YUI compressor (http://www.julienlecomte.net/blog/2007/08/11/)
// It yields gradually better results than the dojo comressor, but it produces unreadable code
//die("Run this script to batch-compress the current Xinha snapshot. To run the script, open the file and comment out the die() command");

$repository_url = 'http://svn.xinha.org/trunk';
$version ='';
$date = date('r');

$xinha_root = realpath(dirname(__FILE__).'/..');

error_reporting(E_ALL);
ini_set('show_errors',1);

$return = array();
function scan($dir, $durl = '',$min_size="3000")
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
				if(!preg_match("/\.(js|css)$/",$path) || filesize($path) < $min_size) continue;
				$return[] =  $path;
			}

		}
	}
	@closedir($dh);

	return $files;
}

scan($xinha_root,0);

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
    --  Copyright (c) 2005-'.date('Y').' Xinha Developer Team and contributors
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
	print "Processing $file\n";
	flush();
	
	$file_url = $repository_url.str_replace($xinha_root,'',$file);

	copy($file,$file."_uncompr.js");
	
	$file_prefix = $prefix."\n/* The URL of the most recent uncompressed version of this file is $file_url */";
	$ext = preg_replace('/.*?(\.js|\.css)$/','$1',$file);
	
	file_put_contents($file."_uncompr${ext}", preg_replace('/(\/\/[^\n]*)?(?![*])\\\[\n]/','',file_get_contents($file)));

	passthru("echo \"".(preg_match('/XinhaCore.js$/',$file) ? $file_prefix.$core_prefix : $prefix)."\" > $file && java -jar {$xinha_root}/contrib/yuicompressor-2.4.2.jar  --charset UTF-8 ${file}_uncompr${ext} >> $file 2>&1");
	if (preg_match('/syntax error/',file_get_contents($file)) || preg_match('/sh: java: command not found/', file_get_contents($file)))
	{
		unlink($file);
		rename($file."_uncompr${ext}",$file);
	}
	else
	{
		unlink($file."_uncompr${ext}");
	}

}
print "Operation complete."
?>
