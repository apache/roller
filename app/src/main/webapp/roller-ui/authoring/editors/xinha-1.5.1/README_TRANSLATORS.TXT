Translating Xinha
################################################################################

Hello you multi-lingual person you, so you would like to help translate Xinha
or create your own special translation to use?

It's really easy!

In the Xinha distribution you will find some files under the following directory

   /lang/merged/*.js

you can see that there is a file for every currently existing translation there
and also one called "__new__.js".  You will also see there are other lang files 
around the place, they are used only when not using a merged file, you don't need
to touch them, just work on /lang/merged/*.js !

If your language is there already, simply open that .js file in your favourite
UTF-8 compliant text editor.

If your language is not there, copy the __new__.js file and create a new one called
[yourlanguagecode].js, for example if you are translating into Klingon, perhaps
"kl.js" and then open it in your favourite UTF-8 compliant text editor.

Please note that all translation is in UTF-8 Encoding, do not try to use other 
character encodings (character sets), it's not going to end well.

To actually test or use your translation, in your code right before you load 
Xinha specify the language and file like this...

    <!-- Set Language To Klingon -->
    <script type="text/javascript>
      _editor_lang              = 'kl';
      _editor_lang_merged_file  = '/xinha/lang/merged/kl.js';
    </script>
    
    <!-- And Load Xinha As Usual -->
    <script type="text/javascript" src="../XinhaCore.js"></script>

that's it, try out Xinha and your language file should provide the translations.

When you have made your translation file and would like to submit it
to Xinha for inclusion in a future distibution of Xinha, please just open a 
ticket at http://trac.xinha.org/ and attach your new or improved language file, 
be sure to let us know your details to add the the contributors list below!

Tools
################################################################################

In the contrib directory there are a few scripts written in PHP to handle the 
translation setup tasks.

lc_parse_strings.php
--------------------------------------------------------------------------------

This script goes through all the Xinha files, javascript, php, html, and pulls
out the strings which should get translated.  These are written into lc_base.js
translation files spread through the system, eg /lang/lc_base.js, 
/modules/.../lang/lc_base.js, /plugins/.../lang/lc_base.js and so forth.

lc_base.js files do not get committed to the Subversion repository they are 
just temporary working files.

lc_create_merged_file.php
--------------------------------------------------------------------------------

This script takes all the lc_base.js files and the existing lang files for a given
language and creates a merged language file, for example

    php contrib/lc_create_merged_file.php fr /tmp/fr.js

creates the French translation merged file /tmp/fr.js

Note that existing merged files are not consulted during this process.

Translators can then work on the merged file to do their translation work.

To make things easy the `lang/merged/` directory contains pre-generated merged 
language files of each language of the release, again these are not committed
to the subversion repository.

If you run this script without arguments it will give you some help showing how
you can generate all the merged language files at once, this is done as part of
the release process.

lc_split_merged_file.php
--------------------------------------------------------------------------------

This script takes a merged file and splits it into the constituent lang files
these files are written into a temporary directory structure so that you can 
use a diff/merge tool if you wish to compare them rather than actually copying 
them into place.  For example,

  php contrib/lc_split_merged_file.php /tmp/fr.js fr

will create a directory structure (it will be printed at completion) in /tmp, 

  /tmp/something-unique-here/lang/fr.js
  /tmp/something-unique-here/modules/.../lang/fr.js
  /tmp/something-unique-here/plugins/.../lang/fr.js
  /tmp/something-unique-here/unsupported_plugins/.../lang/fr.js

the directory and some instructions for easily "installing" the generated files
is printed.

Contributors
################################################################################

The following people are thanked for providing and updating translations!

ch: Samuel Stone, http://stonemicro.com/
cz: Jiri Löw, <jirilow@jirilow.com>
da: Bjarke M. Lauridsen, http://www.detailx.dk, detailx@gmail.com
da: rene, <rene@laerke.net>
da: Steen Sønderup, <steen@soenderup.com>
de: Broxx, <broxx@broxx.com>
de: Holger Hees, <hhees@systemconcept.de>, http://www.systemconcept.de
de: Mihai Bazon, http://dynarch.com/mishoo
de: Raimund Meyer xinha@ray-of-light.org
de: sven schomacker, http://www.hilope.de, mail@hilope.de
de: Udo Schmal (gocher), http://www.schaffrath-neuemedien.de/, udo.schmal@t-online.de
ee: Martin Raie, <albertvill@hot.ee>
el: Dimitris Glezos, dimitris@glezos.com
es: Derick Leony <dleony@gmail.com>
es: michael Hauptmnn (mhauptma73) , http://www.azahost.com/, miguel@azalorea.com
eu: Unkown
fa: Unknown
fi: Unknown
fr: Kevin Huppert - Créabilis, http://www.creabilis.com, kevin.huppert@worldonline.fr
he: Liron Newman, http://www.eesh.net, <plastish at ultinet dot org>
he: Mihai Bazon, http://dynarch.com/mishoo
hu: Miklós Somogyi, <somogyine@vnet.hu>
it: Fabio Rotondo <fabio@rotondo.it>
it: Mattia Landoni, http://www.webpresident.org/
it: Udo Schmal (gocher), http://www.schaffrath-neuemedien.de/, udo.schmal@t-online.de
ja: Unknown
lt: Jaroslav Šatkevič, <jaro@akl.lt>
lv: Mihai Bazon, http://dynarch.com/mishoo
nb: Kim Steinhaug,  http://www.steinhaug.com/, http://www.easycms.no/, kim@easycms.no
nb: Mihai Bazon, <mihai_bazon@yahoo.com>
nb: Håvard Wigtil <havardw@extend.no>
nb: ses<ses@online.no>
nl: A.H van den Broek  http://www.kontaktfm.nl, tonbroek@kontaktfm.nl
nl: Arthur Bogaart a.bogaart@onehippo.org
nl: Holger Hees, <hhees@systemconcept.de>
nl: Maarten Molenschot, maarten@nrgmm.nl
nl: Michel Weegeerink (info@mmc-shop.nl), http://mmc-shop.nl
nl: Mihai Bazon, http://dynarch.com/mishoo
nl: Raimund Meyer xinha@ray-of-light.org
nl: Udo Schmal (gocher), http://www.schaffrath-neuemedien.de/, udo.schmal@t-online.de
pl: Krzysztof Kotowicz, http://www.web.eskot.pl, http://www.eskot.krakow.pl/portfolio/, koto@webworkers.pl, koto1sa@o2.pl
pt_br: Cau guanabara (independent developer), caugb@ibest.com.br
pt_br: Marcio Barbosa, <marcio@mpg.com.br>, http://www.mpg.com.br/
ro: Mihai Bazon, http://dynarch.com/mishoo
ru: Andrei Blagorazumov, a@fnr.ru
ru: Helen Mitkina (zerok) <e.mitkina at gmail>
ru: Yulya Shtyryakova, <yulya@vdcom.ru>
sh: Ljuba Ranković, http://www.rankovic.net/ljubar
si: Tomaz Kregar, x_tomo_x@email.si
sr: Ljuba Ranković, http://www.rankovic.net/ljubar
sv: Erik Dalén, <dalen@jpl.se>
th: Suchin Prasongbundit, <suchin@joolsoft.com>
tr: Serdar Domurcuk, <serdar.domurcuk@km.co.at>, http://www.km.co.at/
vn: Nguyễn Đình Nam, <hncryptologist@yahoo.com>
zh_cn: Dave Lo, dlo@interactivetools.com
b5: Dave Lo, dlo@interactivetools.com