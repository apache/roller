A small number of smileys are included in this release.

To add more smileys, 

  1. Dump as many as you would like as image files into the smileys folder (gif, jpg, png).
  2. EITHER:
    a. Edit smileys/smileys.js as appropriate, or;
    b. Set this Xinha configuration (if your server handles PHP ok)
       
        xinha_config.InsertSmiley.smileys = _editor_url+'/plugins/InsertSmiley/smileys/smileys.php';
       
       it will automatically pickup the new smileys without you needing to edit the js file.
      
