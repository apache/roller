<?php

/** 
 *  Obtain id3 information from mp3 files.
 *
 *  @author James Sleeman (james@gogo.co.nz)
 *  @website http://www.gogo.co.nz
 *  @license MIT  (http://en.wikipedia.org/wiki/MIT_License)
 * 
 *  With thanks for inspiration and a small amount of code to:
 *  Author    : de77
 *  Website   : www.de77.com
 *  Class desc  : http://de77.com/php/php-class-how-to-read-id3v2-tags-from-mp3-files
 *  Class desc  : http://de77.com/php/php-class-how-to-read-id3-v1-tag-from-mp3-files
 *
 *  Acknowledgement to: http://www.autistici.org/ermes/index.php?pag=1&post=15
 *  which I was going to use, but looked to be a bit fragile
 */
 
class id3Parser
{ 
  public $error;
  
  
  private $tags = array(
    // V2.3/4
    'TALB' => 'album',
    'TCON' => 'genre',
    'TENC' => 'encoder',
    'TIT2' => 'title',
    'TPE1' => 'artist',
    'TPE2' => 'ensemble',
    'TYER' => 'year',
    'TCOM' => 'composer',
    'TCOP' => 'copyright',
    'TRCK' => 'track',
    'WXXX' => 'url',
    'COMM' => 'comment',
    
    // V2.2     
    'TAL' => 'album',     
    'TCO' => 'genre',     
    'TEN' => 'encoder',
    'TT2' => 'title',     
    'TP1' => 'artist',
    'TP2' => 'ensemble',
    'TYE' => 'year',
    'TCM' => 'composer',
    'TCR' => 'copyright',
    'TRK' => 'track',
    'WXX' => 'url',
    'COM' => 'comment'
  );
  
  // ID3v1 Genre Mapping
  private $genres = array(
    'Blues',
    'Classic Rock',
    'Country',
    'Dance',
    'Disco',
    'Funk',
    'Grunge',
    'Hip-Hop',
    'Jazz',
    'Metal',
    'New Age',
    'Oldies',
    'Other',
    'Pop',
    'R&B',
    'Rap',
    'Reggae',
    'Rock',
    'Techno',
    'Industrial',
    'Alternative',
    'Ska',
    'Death Metal',
    'Pranks',
    'Soundtrack',
    'Euro-Techno',
    'Ambient',
    'Trip-Hop',
    'Vocal',
    'Jazz+Funk',
    'Fusion',
    'Trance',
    'Classical',
    'Instrumental',
    'Acid',
    'House',
    'Game',
    'Sound Clip',
    'Gospel',
    'Noise',
    'AlternRock',
    'Bass',
    'Soul',
    'Punk',
    'Space',
    'Meditative',
    'Instrumental Pop',
    'Instrumental Rock',
    'Ethnic',
    'Gothic',
    'Darkwave',
    'Techno-Industrial',
    'Electronic',
    'Pop-Folk',
    'Eurodance',
    'Dream',
    'Southern Rock',
    'Comedy',
    'Cult',
    'Gangsta',
    'Top 40',
    'Christian Rap',
    'Pop/Funk',
    'Jungle',
    'Native American',
    'Cabaret',
    'New Wave',
    'Psychadelic',
    'Rave',
    'Showtunes',
    'Trailer',
    'Lo-Fi',
    'Tribal',
    'Acid Punk',
    'Acid Jazz',
    'Polka',
    'Retro',
    'Musical',
    'Rock & Roll',
    'Hard Rock',
    'Folk',
    'Folk-Rock',
    'National Folk',
    'Swing',
    'Fast Fusion',
    'Bebob',
    'Latin',
    'Revival',
    'Celtic',
    'Bluegrass',
    'Avantgarde',
    'Gothic Rock',
    'Progressive Rock',
    'Psychedelic Rock',
    'Symphonic Rock',
    'Slow Rock',
    'Big Band',
    'Chorus',
    'Easy Listening',
    'Acoustic',
    'Humour',
    'Speech',
    'Chanson',
    'Opera',
    'Chamber Music',
    'Sonata',
    'Symphony',
    'Booty Bass',
    'Primus',
    'Porn Groove',
    'Satire',
    'Slow Jam',
    'Club',
    'Tango',
    'Samba',
    'Folklore',
    'Ballad',
    'Power Ballad',
    'Rhythmic Soul',
    'Freestyle',
    'Duet',
    'Punk Rock',
    'Drum Solo',
    'Acapella',
    'Euro-House',
    'Dance Hall'
    );   
  
  /** Given a path to an mp3 file, interrogate the file to find any id3 tags in it.
   *  return an associative array
   *  can handle id3v1, v2.2, v2.3 and v2.4, however it is not a complete parser, just good-enough
   *  compressed and encrypted frames are skipped, only (T)ext and (W)ww frames are typically returned
   *
   */
  
  public function read($file)
  {
    $f = fopen($file, 'r');
    $result = array();
    if( fread($f, 3) == 'ID3')
    {
      // ID3v2 tag at start of file, use that
      rewind($f);
      $result = array_merge($result, $this->read_v2($f));
    }
    
    if(!count($result))
    {
      fseek($f, -10, SEEK_END);
      if( fread($f, 3) == '3DI' )
      {
        // Looking at a footer of an ID3v2, find the length and seek backwards to the start
        fseek($f, -10, SEEK_END);
        $result = array_merge($result, $this->read_v2($f));
      }
    }
    
    if(!count($result))
    {
      fseek($f, -128, SEEK_END);
      if( fread($f, 3) == 'TAG' )
      {
        // v1 tag        
        fseek($f, -128, SEEK_END);
        $result = array_merge($result, $this->read_v1($f));
      }
    }
    
    if(!count($result))
    {
      // Still nothing, let's make a title anyway
      $result['title'] = trim(preg_replace('/(\.mp3|%20|[_+ -]|(^[0-9]+\.?))+/i', ' ', basename($file)));
      $result['id3']   = '0';
    }
    
    return $result;
   // echo ("NO ID3 ($file)\n");
  }
  
  /** Decode the value of a text frame, returns in UTF-8 always */
  
  private function decode_v23_text_value($tag)
  {
    //mb_convert_encoding is corrupted in some versions of PHP so I use iconv
    switch (ord($tag[0]))
    {
      case 0: //ISO-8859-1        
          return @iconv('ISO-8859-1', 'UTF-8', substr($tag, 1));
      case 1: //UTF-16 BOM      
          return @iconv('UTF-16LE', 'UTF-8//IGNORE',  substr($tag.chr(0x00), 3));
      case 2: //UTF-16BE
          return @iconv('UTF-16BE', 'UTF-8', substr($tag.chr(0x00), 3));
      case 3: //UTF-8
          return substr($tag, 1);
    }
    return false;
  }
          
  /** Some size fields in 2.3+ headers are "sync safe", we need to strip out certain bits and rebuild the size integer, bitwise.
   */

  private function desync_size($headersize)
  {
    // The header size needs fixing by stripping out certain bits (1st, 9th, 17th, 25th)
    // 011111111 === 0x7F
    $size =    $headersize & 0x7F; // Grab least sig 7 bits
               $headersize = $headersize >> 8;   // shift out 8 bits
    
    $size =    (($headersize & 0x7F)<<7)|$size; // grab least sig 7 bits and shift 7 to the left then add to size
               $headersize = $headersize >> 8;   // shift out 8 bits
    
    $size =    (($headersize & 0x7F)<<14)|$size; // grab least sig 7 bits and shift 14 to the left then add to size
               $headersize = $headersize >> 8;   // shift out 8 bits
    
    $size =    (($headersize & 0x7F)<<21)|$size; // grab least sig 7 bits and shift 21 to the left then add to size
               $headersize = $headersize >> 8;   // shift out 8 bits    
    
    return $size;
  }

  /** Read a specified number of bytes from the stream counted AFTER re-synchonisation (if necessary).
   *  The spec isn't very clear, but I believe that in a 2.2/3 if the unsynchronised flag is on, 
   *  then the frame headers are unsynchronised also, which means if they happen to include an FF00
   *  simply reading 10 raw bytes would not get a proper frame header.
   *  We have to unsynchronise as we go and maybe ready more bytes.
   *
   *  In contrast, once we HAVE that frame header, the size specified in that header is the unsynchronised size
   *  of the frame without header, so we should get that specific # of bytes in that case.
   */
   
  private function fread_id3_synchronised_length($f, $num, $IsUnsynchronised, &$LeftToRead)
  {
    $frame = '';
    $totalread = 0;
    
    while((strlen($frame) < $num) && $LeftToRead && !feof($f))
    {
      $LeftToRead -= $num-strlen($frame);
      $frame .= fread($f, $num-strlen($frame));          
      if($IsUnsynchronised) 
      {
        $frame = str_replace(chr(0xff).chr(0x00), chr(0xff), $frame);          
      }
      
      while(strlen($frame) && (ord($frame[0]) == 0))
      {
        // We have picked up a NUL padding?        
        $frame = substr($frame,1);
      }
    }
    
    return $frame;
  }
  
  /** Given a file handle seeked to the first byte of an id3v2.X header, 
   *  return an array of Property => Value for the id3 properties we can handle (currently T and W prefixes)
   *  if a property has a given name in id3ParserDe77::$tags, then this will be set also (as a reference).
   */
   
  private function read_v2($f)
  {   
    $header = fread($f, 10);
    $header = @unpack("a3signature/C1version_major/C1version_minor/C1flags/Nsize", $header);
    $header['size'] = $this->desync_size($header['size']);
    
    if($header['signature'] == '3DI')
    {
      // This is a footer for a v4, seek up to the start of the data after the header
      // We don't need to read the header, it's the same as the footer      
      fseek($f, 0-$header['size']-10, SEEK_CUR); 
    }
    
    $header['version_major'] = hexdec($header['version_major']);
    $header['version_minor'] = hexdec($header['version_minor']);
    
    switch($header['version_major'])
    {
      case 4:
        $result = $this->read_v24($f, $header);       
        break;
        
      case 3:
        $result = $this->read_v23($f, $header);        
        break;
        
      case 2:
        $result = $this->read_v22($f, $header);
        break;
        
      default:        
        $result = array();
        break;
    }
    
    if(count($result)) $result['id3'] = '2.'.$header['version_major'].'.'.$header['version_minor'];
     
    return $result; 
  } 
  
  private function read_v22($f, $header)
  {    
    $LeftToRead = $header['size'];
    $IsUnsynchronised  = $header['flags'] & (1<<7);
    $IsCompressed      = $header['flags'] & (1<<6);
    
    if($IsCompressed) { return array(); }
    
    // At this point we should be looking at a frame header on the stream   
    $result = array();
    while(($LeftToRead > 6) && !feof($f))
    {    
      $frame = fread($f, 6);//$this->fread_id3_synchronised_length($f, 6, $IsUnsynchronised, $LeftToRead);
            
      if(strlen($frame) < 6) continue; // Bad frame
      $frame = unpack('a3id/C3size', $frame );  
      $frame['size'] = ($frame['size1']<<14)|($frame['size2']<<7)|($frame['size3']);
      
      if($frame['size'] == 0) break; // We are now into padding area.      
      if($frame['size'] > (1024*1024)) { fseek($f, $frame['size'], SEEK_CUR); $LeftToRead -= $frame['size']; continue; }

      // Read the value of the frame        
      $value = fread($f, $frame['size']);
      $LeftToRead -= $frame['size'];
      $frame['value'] = $value;
      
      if($IsUnsynchronised) 
      { 
        $value = str_replace(chr(0xff).chr(0x00), chr(0xff), $value); 
      }
          
      switch($frame['id'][0])
      {
        case 'T':
          $value = $this->decode_v23_text_value($value);
          // The old id3v1 genre can be included in this textual information
          if($frame['id'] == 'TCO' && preg_match('/\(([0-9]+)\)/', $value, $M))
          {
            if(isset($this->genres[$M[1]]))
            {
              $value = $this->genres[$M[1]];
            }
          }
          $result[$frame['id']] = $value;
          if(isset($this->tags[$frame['id']])) 
          {
            $result[$this->tags[$frame['id']]] =& $result[$frame['id']];
          }
          break;
          
        case 'W':
          $result[$frame['id']] = $value;
          if(isset($this->tags[$frame['id']])) 
          {
            $result[$this->tags[$frame['id']]] =& $result[$frame['id']];
          }          
        break;
      }
    }
        
    return $result;
  
  }
  
  private function read_v24($f, $header)
  {        
    $IsUnsynchronised  = $header['flags'] & (1<<7);
    $HasExtendedHeader = $header['flags'] & (1<<6);
    $IsExperimental    = $header['flags'] & (1<<5);
           
    $LeftToRead = $header['size'];
    if($HasExtendedHeader)
    {    
      $exHeader = unpack('Nsize', fread($f, 4));
      $exHeader['size'] = $this->desync_size($exHeader['size']);
      
      fread($f, $exHeader['size']-4); // Dont' care about this we are just getting rid of it.
      $LeftToRead -= $exHeader['size'];
    }
    
    // At this point we should be looking at a frame header on the stream
   
    $result = array();
    while(($LeftToRead > 10) && !feof($f))
    {    
      $frame = $this->fread_id3_synchronised_length($f, 10, $IsUnsynchronised, $LeftToRead);

      if(strlen($frame) < 10) continue; // Bad frame
      $frame = unpack('a4id/Nsize/C2flags', $frame );        
      $frame['size'] = $this->desync_size($frame['size']);
      
      if($frame['size'] == 0) break; // We are now into padding area.
      
      if( $frame['flags2'] & (1<<7) // Compressed
      ||  $frame['flags2'] & (1<<6) // Encrypted
      )
      {         
        // Can't work with these
        fseek($f, $frame['size'], SEEK_CUR); // Dont' care about this we are just getting rid of it.
        $LeftToRead -= $frame['size'];
        continue;
      }
      
      if($frame['flags2'] & (1<<5)) // Grouping
      {
        fread($f,1); // Get rid of the group byte        
        $LeftToRead -= 1;
        $frame['size']--; // it is included in the frame size?
      }
      
      if($frame['flags2'] & 1) // Data length
      {
        fread($f,4); // Get rid of the group byte        
        $LeftToRead -= 4;
        $frame['size'] -= 4; // it is included in the frame size?
      }
      
      if($frame['size'] > (1024*1024)) { fseek($f, $frame['size'], SEEK_CUR); $LeftToRead -= $frame['size']; continue; }

      // Read the value of the frame      
      $value = fread($f, $frame['size']);
      $LeftToRead -= $frame['size'];
      $frame['value'] = $value;
      
      if($IsUnsynchronised) 
      { 
        $value = str_replace(chr(0xff).chr(0x00), chr(0xff), $value); 
      }
          
      switch($frame['id'][0])
      {
        case 'T':
          $value = $this->decode_v23_text_value($value);
          // The old id3v1 genre can be included in this textual information
          if($frame['id'] == 'TCON' && preg_match('/\(([0-9]+)\)/', $value, $M))
          {
            if(isset($this->genres[$M[1]]))
            {
              $value = $this->genres[$M[1]];
            }
          }
          $result[$frame['id']] = $value;
          if(isset($this->tags[$frame['id']])) 
          {
            $result[$this->tags[$frame['id']]] =& $result[$frame['id']];
          }
          break;
          
        case 'W':
          $result[$frame['id']] = $value;
          if(isset($this->tags[$frame['id']])) 
          {
            $result[$this->tags[$frame['id']]] =& $result[$frame['id']];
          }          
        break;
      }
    }
    // echo "VERSION 4\n";   
    return $result;
  }

  /** Given a file handle seeked to the first byte after the header, and the header decoded in an array, 
   *  return an array of Property => Value for the id3 properties we can handle (currently T and W prefixes)
   *  if a property has a given name in id3ParserDe77::$tags, then this will be set also (as a reference).
   */
   
  private function read_v23($f, $header)
  {        
    $IsUnsynchronised  = $header['flags'] & (1<<7);
    $HasExtendedHeader = $header['flags'] & (1<<6);
    $IsExperimental    = $header['flags'] & (1<<5);
        
   
    $LeftToRead = $header['size'];
    if($HasExtendedHeader)
    {    
      $exHeader = unpack('Nsize', fread($f, 4));
      fread($f, $exHeader['size']); // Dont' care about this we are just getting rid of it.
      $LeftToRead -= 4 + $exHeader['size'];
    }
    
    // At this point we should be looking at a frame header on the stream
   
    $result = array();
    while(($LeftToRead > 10) && !feof($f))
    {    
      $frame = $this->fread_id3_synchronised_length($f, 10, $IsUnsynchronised, $LeftToRead);

      if(strlen($frame) < 10) continue; // Bad frame
      $frame = unpack('a4id/Nsize/C2flags', $frame );            
      if($frame['size'] == 0) break; // We are now into padding area.
      
      if( $frame['flags2'] & (1<<7) // Compressed
      ||  $frame['flags2'] & (1<<6) // Encrypted
      )
      {         
        // Can't work with these
        fseek($f, $frame['size'], SEEK_CUR); // Dont' care about this we are just getting rid of it.
        $LeftToRead -= $frame['size'];
        continue;
      }
      
      if($frame['flags2'] & (1<<5)) // Grouping
      {
        fread($f,1); // Get rid of the group byte        
        $LeftToRead -= 1;
        $frame['size']--; // it is included in the frame size
      }
      
      if($frame['size'] > (1024*1024)) { fseek($f, $frame['size'], SEEK_CUR); $LeftToRead -= $frame['size']; continue; }

      // Read the value of the frame        
      $value = fread($f, $frame['size']);
      $LeftToRead -= $frame['size'];
      $frame['value'] = $value;
      
      if($IsUnsynchronised) 
      { 
        $value = str_replace(chr(0xff).chr(0x00), chr(0xff), $value); 
      }
          
      switch($frame['id'][0])
      {
        case 'T':
          $value = $this->decode_v23_text_value($value);
          // The old id3v1 genre can be included in this textual information
          if($frame['id'] == 'TCON' && preg_match('/\(([0-9]+)\)/', $value, $M))
          {
            if(isset($this->genres[$M[1]]))
            {
              $value = $this->genres[$M[1]];
            }
          }
          $result[$frame['id']] = $value;
          if(isset($this->tags[$frame['id']])) 
          {
            $result[$this->tags[$frame['id']]] =& $result[$frame['id']];
          }
          break;
          
        case 'W':
          $result[$frame['id']] = $value;
          if(isset($this->tags[$frame['id']])) 
          {
            $result[$this->tags[$frame['id']]] =& $result[$frame['id']];
          }          
        break;
      }
    }
        
    return $result;
  }

  /** Given a file handle seeked to the first byte of the header
   *  return an array of Property => Value for the id3 properties we can handle 
   *  v1 properties are only title, artist, album, year, comment and genre
   */
   
  public function read_v1($f)
  {    
    fseek($f, -128, SEEK_END);
    $id3 = fread($f, 128);
    
    $id3 = @unpack("a3signature/a30title/a30artist/a30album/a4year/a30comment/c1genre", $id3);
    $id3['genre'] = @$this->genres[$id3['genre']];
    
    if (!$id3['signature'] == 'TAG')
    {
      $this->error = 'This file does not contain ID3 v1 tag';   
      return false;   
    }
    
    unset($id3['signature']);
    $id3['id3'] = 1;    
    return $id3;  
  }
}