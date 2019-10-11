<?php
/*
 * Script: XinhaFileManager.php
 *   MooTools FileManager - Backend for the Xinha editor
 *
 *   Derived class which includes special tweaks to make MFM work with Xinha.
 *
 * Note: derive from FileManagerWithAlias instead when you need aliasing support; this is left as an exercise
 *       for the reader. (A non-trivial exercise. You'll need to alias images_dir to images_url
 *	     and files_dir to files_url, at least.
 *
 * Authors:
 *  - Ger Hobbelt (http://hebbut.net)
 *
 * License:
 *   MIT-style license.
 *
 * Copyright:
 *   Copyright (c) 2011 [Ger Hobbelt](http://hobbelt.com)
 *
 * Dependencies:
 *   - FileManager.php
 */

require_once(str_replace('\\', '/', dirname(__FILE__)) . '/mootools-filemanager/Assets/Connector/FileManagerWithAliasSupport.php');


class XinhaFileManager extends FileManagerWithAliasSupport
{
	public function __construct($options)
	{
    if(isset($options['images_dir']) && isset($options['images_url']))
    {
      $options['Aliases'][$options['images_url']] = $options['images_dir'];
      $options['directory'] = $options['images_url'];
    }
    
    if(isset($options['files_dir']) && isset($options['files_url']))
    {
      $options['Aliases'][$options['files_url']] = $options['files_dir'];
      $options['directory'] = $options['files_url'];
    }
    
    if(isset($options['thumbs_dir']) && isset($options['thumbs_url']))
    {
      $options['Aliases'][$options['thumbs_url']] = $options['thumbs_dir'];
      $options['thumbnailPath'] = $options['thumbs_url'];
    }
    
    if(isset($options['suggestedMaxImageDimension']))
    {
      $options['maxImageDimension'] = $options['suggestedMaxImageDimension'];
    }
        
		parent::__construct($options);
	}

	// when you want img.preview style max-width = 164px (was before: max-width: 140px)
	// 
	// Note that this will force all 250px thumbnails to a maximum width of 140px
	/*
	public function getThumb($meta, $path, $width, $height, $onlyIfExistsInCache = false)
	{
		return parent::getThumb($meta, $path, min(164, $width), $height, $onlyIfExistsInCache);
	}
	*/
}


