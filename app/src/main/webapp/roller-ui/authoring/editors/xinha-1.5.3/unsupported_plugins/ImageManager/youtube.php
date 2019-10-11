<?php 
/**
 * Show a list of images in a long horizontal table.
 * @author $Author: gogo $
 * @version $Id: images.php 877 2007-08-12 15:50:03Z gogo $
 * @package ImageManager
 */

require_once('config.inc.php');
require_once('ddt.php');
require_once('Classes/ImageManager.php');

// Compatability for PHP lt 5.2
require_once('Classes/JSON_Compat.php');
  
// Search for youtube videos
// ?vq=funny+dogs&start-index=20&max-results=15
$youtube = "http://gdata.youtube.com/feeds/api/videos?max-results=50&format=5&alt=json&";

if(@$_REQUEST['ytSearch'])
{
  $youtube .= 'vq='.rawurlencode($_REQUEST['ytSearch']) .'&';   
}

if(@$_REQUEST['ytUsername'])
{
  $youtube .= 'author='.rawurlencode($_REQUEST['ytUsername']);  
}

$feed = file_get_contents($youtube);
$data = json_decode($feed, true);

$numRows  = $data['feed']['openSearch$totalResults']['$t'];
$firstRow = $data['feed']['openSearch$startIndex']['$t'];
$perPage  = $data['feed']['openSearch$itemsPerPage']['$t'];
$lastRow  = $startRow-1+$perPage;
$records  = $data['feed']['entry'];



//default path is /
$relative = '/';
$manager = new ImageManager($IMConfig);

/* ================= OUTPUT/DRAW FUNCTIONS ======================= */

function format_duration($duration)
{
  $hours = floor($duration / (60 * 60));
  $duration = $duration - ($hours * 60 * 60);
  
  $minutes = floor($duration / 60);
  $duration = $duration - ($minutes * 60);
  
  $seconds = $duration;
  
  if($hours)
  {        
    return $hours . '.' . floor(($minutes/60) * 100) . " hr" . ($minutes ? "s" : '');  
  }
  
  if($minutes)
  {
    return $minutes . ' min' . ($minutes > 1 ? 's' : '');
  }
  
  return $seconds . ' sec'  . ($seconds > 1 ? 's' : '');
  
}
 
function rip_youtube_data($record)
{  
  $media = $record['media$group'];
  $title = $media['media$title']['$t'];
  $description = substr($media['media$description']['$t'],0,100);
  $duration = $media['yt$duration']['seconds'];
    
  $thumbs = array(); // w:h => array( file, file, file)
  $largest = array(0,'', 0, 0);   
  $smallest = array(3000*3000,'', 3000, 3000); // Ok, hacky, sosumi
  foreach($media['media$thumbnail'] as $thumb) 
  {
    if(!isset($thumbs["{$thumb['width']}x{$thumb['height']}"]))
    {
      $thumbs["{$thumb['width']}x{$thumb['height']}"]  = array();
      if(($thumb['width'] * $thumb['height']) > $largest[0])
        $largest = array($thumb['width'] * $thumb['height'], $thumb['width'] . 'x' . $thumb['height'], $thumb['width'] , $thumb['height']);
      if(($thumb['width'] * $thumb['height']) < $smallest[0])
        $smallest = array($thumb['width'] * $thumb['height'], $thumb['width'] . 'x' . $thumb['height'], $thumb['width'] , $thumb['height']);
    }
    $thumbs["{$thumb['width']}x{$thumb['height']}"][$thumb['time']] = $thumb['url'];
    
  }
 
  // Find the main image link
  $mainImage = $thumbs[$largest[1]];
  $mainImage = array_pop($mainImage);
  
  // Find the thumb image link
  $thumbImage = $thumbs[$smallest[1]];
  $thumbImage = array_pop($thumbImage);
  
  $embed = NULL;
  foreach($media['media$content'] as $vid)
  {
    if($vid['type'] == 'application/x-shockwave-flash')
    {
      $embed = $vid['url'];
      break;
    }
  }
  
  // Final image to pass to manager (use query param to record the embed url)
  $combinedImage = $mainImage . 
  '?x-shockwave-flash='.rawurlencode($embed).
  '&x-tn='.rawurlencode($thumbImage);
  
  
  return array
  (
   'title'         => $title,
   'description'   => $description,
   'duration'      => format_duration($duration),
   
   'mainImage'     => $mainImage,
   'thumbImage'    => $thumbImage,
   'combinedImage' => $combinedImage,
   
   'smallest' => $smallest,
   'largest'  => $largest,
   'thumbs'   => $thumbs,
   'embed'    => $embed,
  );
}
                           
function drawFiles($list, &$manager)
{
	global $relative;
	global $IMConfig;
//$IMConfig['ViewMode'] = 'thumbs';
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
        <tr><th>Title</th><th>Description</th><th>Duration</th></tr></thead>
        <tbody>
          <?php
          foreach($list as $record)
          {
            
            extract(rip_youtube_data($record));
            
            ?>
            <tr>
              <th><a href="#" class="thumb" style="cursor: pointer;" onclick="selectImage('<?php echo xinha_js_encode($combinedImage)?>', '<?php echo xinha_js_encode($title); ?>', <?php echo $largest[2];?>, <?php echo $largest[3]; ?>);return false;" title="<?php echo htmlspecialchars($title); ?> - <?php echo htmlspecialchars($duration); ?>" onmouseover="showPreview('<?php echo xinha_js_encode($combinedImage);?>')" onmouseout="showPreview(window.parent.document.getElementById('f_url').value)" ><?php echo htmlspecialchars($title) ?></a></th>
              <td><?php echo htmlspecialchars($description); ?></td>
              <td><?php echo htmlspecialchars($duration); ?>              
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
          extract(rip_youtube_data($record));
          ?>
          <div class="thumb_holder" id="holder_<?php echo asc2hex($combinedImage) ?>">
            <a href="#" class="thumb" style="cursor: pointer;" onclick="selectImage('<?php echo xinha_js_encode($combinedImage)?>', '<?php echo xinha_js_encode($title); ?>', <?php echo $largest[2];?>, <?php echo $largest[3]; ?>);return false;" title="<?php echo htmlspecialchars($title); ?> - <?php echo htmlspecialchars($duration); ?>">
              <img src="<?php print $thumbImage ?>" alt="<?php echo $title; ?> - <?php echo htmlspecialchars($duration); ?>"/>
            </a>
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
<div class="noResult">No Videos Found</div>
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
	<title>YouTube Video List</title>
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
<style type="text/css">
.dir_holder, .thumb_holder
{
  width:140px; height:110px;
  float:left;
  margin:6px;
  background-color:ButtonFace;
  border: 1px outset;
}
</style>
</head>

<body>
<?php if ($manager->isValidBase() == false) { drawErrorBase($manager); } 
	elseif(count($records)) { ?>

	<?php drawFiles($records, $manager); ?>

<?php } else { drawNoResults(); } ?>
</body>
</html>
