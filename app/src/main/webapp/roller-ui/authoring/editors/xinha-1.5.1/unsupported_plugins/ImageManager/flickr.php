<?php 
/**
 * Show a list of images in a long horizontal table.
 * @author $Author: gogo $
 * @version $Id: images.php 877 2007-08-12 15:50:03Z gogo $
 * @package ImageManager
 */

require_once('config.inc.php');

require_once('Classes/ImageManager.php');
require_once('Classes/Flickr.php');

// Search for youtube videos
$opts = array(
              'per_page'=> 20,
              'sort'   => 'relevance',              
              'extras' => 'license,owner_name,tags',
              'license' => @$_REQUEST['flkLicense'] ? $_REQUEST['flkLicense'] : flickr_get_default_usage_id(),              
              );

if(@$_REQUEST['flkSearch'])
{
  $opts['text'] = $_REQUEST['flkSearch']; 
}

if(@$_REQUEST['flkUsername'])
{
  $user_id = flickr_get_user_id($_REQUEST['flkUsername']);
  if($user_id)
  {
    $opts['user_id'] = $user_id;
  }
}

$data = flickr_request('flickr.photos.search', $opts);


$records = $data['photos']['photo'];

// For future purposes, we may want an image manager
$relative = '/';
$manager = new ImageManager($IMConfig);

/* ================= OUTPUT/DRAW FUNCTIONS ======================= */


function rip_flickr_data($record)
{  
  $lics = flickr_get_licenses();
  // echo '<pre>'; print_r($lics);  die();
  
  $title = $record['title'];
  $license = $lics[$record['license']];
  
  // echo '<pre>';print_r($license); die();
  
  $description = '';
  $sizes = flickr_request('flickr.photos.getSizes', array('photo_id' => $record['id']));
  $sizes = $sizes['sizes'];
  $thumbs = array(); // w:h => array( file, file, file)
  $largest = array(0,'', 0, 0);   
  $smallest = array(3000*3000,'', 3000, 3000); // Ok, hacky, sosumi
  
  foreach($sizes['size'] as $thumb) 
  {
    if($thumb['label'] == 'Square') continue; // These are a little smaller than default
    if(!isset($thumbs["{$thumb['width']}x{$thumb['height']}"]))
    {
      $thumbs["{$thumb['width']}x{$thumb['height']}"]  = array();
      
      if(($thumb['width'] * $thumb['height']) > $largest[0])
        $largest = array($thumb['width'] * $thumb['height'], $thumb['width'] . 'x' . $thumb['height'], $thumb['width'] , $thumb['height']);
      
      if(($thumb['width'] * $thumb['height']) < $smallest[0])
        $smallest = array($thumb['width'] * $thumb['height'], $thumb['width'] . 'x' . $thumb['height'], $thumb['width'] , $thumb['height']);
    }
    $thumbs["{$thumb['width']}x{$thumb['height']}"][] = $thumb;
    
  }

  // Find the main image link
  $mainImage = $thumbs[$largest[1]];
  $mainImage = array_pop($mainImage);
  
  // Find the thumb image link
  $thumbImage = $thumbs[$smallest[1]];
  $thumbImage = array_pop($thumbImage);
  
  // Final image to pass to manager (use query param to record the embed url)
  $combinedImage = $mainImage['source'] . 
  '?x-flickr-photo='.rawurlencode($record['id']) .
  '&x-lic='.rawurlencode($license['x-id'])   .
  '&x-uid='.rawurlencode($record['owner'])       .  
  '&x-by='.rawurlencode($record['ownername'])    .   
  '&x-tn='.rawurlencode($thumbImage['source']);
  
  
  return array
  (
   'title'         => $title,
   'description'   => $description,
   'dimensions'    => "{$mainImage['width']}x{$mainImage['height']}",
   'license'       => $license,
   'mainImage'     => $mainImage['source'],
   'thumbImage'    => $thumbImage['source'],
   'combinedImage' => $combinedImage,
   
   'smallest' => $smallest,
   'largest'  => $largest,
   'thumbs'   => $thumbs,   
  );
}
                           
function drawFiles($list, &$manager)
{
	global $relative;
	global $IMConfig;
$IMConfig['ViewMode'] = 'thumbs';
    switch($IMConfig['ViewMode'])
    {
      case 'details':
      {
        
        ?>
        <script language="Javascript">
          <!--
            function showPreview(f_url)
            {              
              window.parent.document.getElementById('f_preview').src = window.parent._backend_url + '__function=thumbs&img=' + encodeURIComponent(f_url);
            }
          //-->
        </script>
        <table class="listview">
        <thead>
        <tr><th>Title</th><th>Description</th><th>Dimensions</th></tr></thead>
        <tbody>
          <?php
          foreach($list as $record)
          {
            
            extract(rip_flickr_data($record));
            
            ?>
            <tr>
              <th><a href="#" class="thumb" style="cursor: pointer;" onclick="selectImage('<?php echo xinha_js_encode($combinedImage)?>', '<?php echo xinha_js_encode($title); ?>', <?php echo $largest[2];?>, <?php echo $largest[3]; ?>);return false;" title="<?php echo htmlspecialchars($title); ?> - <?php echo htmlspecialchars($dimensions); ?>" onmouseover="showPreview('<?php echo xinha_js_encode($combinedImage);?>')" onmouseout="showPreview(window.parent.document.getElementById('f_url').value)" ><?php echo htmlspecialchars($title) ?></a></th>
              <td><?php echo htmlspecialchars($description); ?></td>
              <td><?php echo htmlspecialchars($dimensions); ?>              
            </tr>
            <?php        
          }
          ?>
        </tbody>
        </table>
        <?php
      }      
      break;
      
      case 'thumbs':
      default      :
      {
        foreach($list as $record)
        {
          extract(rip_flickr_data($record));
          ?>
          <div class="thumb_holder" id="holder_<?php echo asc2hex($combinedImage) ?>">            
            <a href="#" class="thumb" style="cursor: pointer;" onclick="selectImage('<?php echo xinha_js_encode($combinedImage)?>', '<?php echo xinha_js_encode($title); ?>', <?php echo $largest[2];?>, <?php echo $largest[3]; ?>);return false;" title="<?php echo htmlspecialchars($title); ?> - <?php echo htmlspecialchars($dimensions); ?>">
              <img src="<?php print $thumbImage ?>" alt="<?php echo $title; ?> - <?php echo htmlspecialchars($dimensions); ?>"/>             
            </a>
            <div class="flk-license"><a href="<?php echo $license['url'] ?>" target="_blank"><?php echo htmlspecialchars($license['name']) ?></a></div>
          </div>
          <?php
        }
      }
    }
}//function drawFiles



/**
 * No directories and no files.
 */
function drawNoResults() 
{
?>
<div class="noResult">No Photos Found</div>
<?php 
}

/**
 * No directories and no files.
 */
function drawErrorBase(&$manager) 
{
?>
<div class="error"><span>Invalid base directory:</span> <?php echo $manager->config['images_dir']; ?></div>
<?php 
}

/**
 * Utility to convert ascii string to hex
 */
function asc2hex ($temp)
{
  $len = strlen($temp);
  $data = "";
  for ($i=0; $i<$len; $i++) $data.=sprintf("%02x",ord(substr($temp,$i,1)));
  return $data;
}

?>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">

<html>
<head>
	<title>Flickr Picture List</title>
  <meta http-equiv="Content-Type" content="text/html; charset=utf-8" />
	<link href="<?php print $IMConfig['base_url'];?>assets/imagelist.css" rel="stylesheet" type="text/css" />
  <script type="text/javascript">
   _backend_url = "<?php print $IMConfig['backend_url']; ?>";
  </script>

<script type="text/javascript" src="<?php print $IMConfig['base_url'];?>assets/dialog.js"></script>
<script type="text/javascript">
/*<![CDATA[*/

	if(window.top)
    HTMLArea = Xinha    = window.top.Xinha;

	function hideMessage()
	{
		var topDoc = window.top.document;
		var messages = topDoc.getElementById('messages');
		if(messages)
			messages.style.display = "none";
	}

	init = function()
	{
	  __dlg_translate('ImageManager');
		hideMessage();
		var topDoc = window.top.document;

//    update_selected();
	}	
	
/*]]>*/
</script>
<script type="text/javascript" src="<?php print $IMConfig['base_url'];?>assets/images.js"></script>
<script type="text/javascript" src="../../popups/popup.js"></script>
<script type="text/javascript" src="assets/popup.js"></script>

</head>

<body class="flickr">
<?php if ($manager->isValidBase() == false) { drawErrorBase($manager); } 
	elseif(count($records)) { ?>

	<?php drawFiles($records, $manager); ?>

<?php } else { drawNoResults(); } ?>
</body>
</html>
