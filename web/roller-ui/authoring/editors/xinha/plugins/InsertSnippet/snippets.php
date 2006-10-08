var snippets = new Array();
var i = 0;
<?php

function get_all_parts($string)
{
	preg_match_all('#<!--(.*?)-->(.*?)<!--/.*?-->#s',$string,$matches);
	
	$array=array();
	for ($i=0;$i<count($matches[1]);$i++)
	{
		$array[$matches[1][$i]] = $matches[2][$i];
	}
	return $array;
}
$snippets = file_get_contents('snippets.html');

$matches = get_all_parts($snippets);
foreach ($matches as $name =>$html)
{
	print "snippets[i] = new Object();\n";
	print "snippets[i]['id'] = '$name';\n";
	print "snippets[i]['HTML'] = '".str_replace("\n",'\n',addcslashes($html,"'"))."';\n";
	print "i++;\n";
}

?>