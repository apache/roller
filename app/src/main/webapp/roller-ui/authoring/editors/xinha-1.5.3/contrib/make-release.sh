#!/bin/bash

if ! [ -f XinhaCore.js ]
then
  echo "Run this from the root of your working copy." >&2
  echo                                                >&2
  exit 1
fi


if ! [ -d .svn ]
then 
  echo "This script must be run inside a subversion working copy." >&2
  echo                                                             >&2
  exit 1
fi

if [ "$1" = "" ]
then
  echo "Usage: $0 {VersionNumber}"                    >&2
  echo                                                >&2
  exit 1
fi

# Run this with bash from the root of your SVN working copy checkout of the trunk
# it will dump int /tmp the archived release files
# eg bash contrib/make-release.sh

VER="$1"

# Export
if [ -d /tmp/xinha-$VER ]
then
  echo "/tmp/xinha-$VER exists, you need to delete it first."
  exit
fi

# Update plugin Manifest
MANIFEST="$(bash contrib/generate-plugin-manifest.sh | sort | perl -0777 -pe 's/,\s*$//s' )"
cat XinhaCore.js | perl -0777  -pe 's/(.pluginManifest\s+=)\s+.+?;/\1 {\nPUT_THE_MANIFEST_HERE_YO\n};/is' | replace PUT_THE_MANIFEST_HERE_YO "$MANIFEST" >XinhaCore2.js
mv XinhaCore2.js XinhaCore.js


mkdir /tmp/xinha-$VER
svn export $(pwd) /tmp/xinha-$VER/xinha
cd /tmp/xinha-$VER/xinha
echo "xinha-$VER" >VERSION.TXT

# Create the merged language files
mkdir lang/merged
php contrib/lc_parse_strings.php
for lang in $(find . -wholename "*/lang/*.js" | sed -r 's/.*\///' | sort | uniq | grep -v base | sed -r 's/.js//')
do
  php contrib/lc_create_merged_file.php $lang lang/merged/$lang.js
done
php contrib/lc_create_merged_file.php NEW lang/merged/__new__.js


cd ../

# Create the main distribution zip and bz2
zip -r xinha-$VER.zip        xinha
#tar -cjvf xinha-$VER.tar.bz2 xinha

# Make a stripped down plugins set for the plugins which must be run locally
#  ie, ones that upload files or deal with the local server file system
mkdir xinha-cdn
mkdir xinha-cdn/contrib
mkdir xinha-cdn/plugins
cp -rp xinha/contrib/php-xinha.php         xinha-cdn/contrib
cp -rp xinha/contrib/.htaccess             xinha-cdn/contrib
cp -rp xinha/plugins/MootoolsFileManager   xinha-cdn/plugins
cp -rp xinha/plugins/Linker                xinha-cdn/plugins
cp -rp xinha/examples                      xinha-cdn/examples

# Some examples are not appropriate for the cdn
rm -rf xinha-cdn/examples/Old_Newbie_Guide
rm -rf xinha-cdn/examples/ExtendedDemo.html
rm -rf xinha-cdn/examples/files/ext_example*php
rm -rf xinha-cdn/examples/files/Extended.html

# Comment out some examples from the index
cat xinha-cdn/examples/index.html | sed -r 's/<h2>Exper/<!-- These are not applicable in a CDN environment: <h2>Exper/' | sed -r 's/(<.body>)/-->\1/' >xinha-cdn/examples/index.html.2
mv xinha-cdn/examples/index.html.2  xinha-cdn/examples/index.html

# Replace the XinhaEasy.js link to the "CDN" link 
for file in xinha-cdn/examples/*
do
  replace "../XinhaEasy.js" "//s3-us-west-1.amazonaws.com/xinha/xinha-${VER}/XinhaEasy.js" -- $file
done

cat >xinha-cdn/README.TXT <<'EOF'
Xinha CDN Local Distribution
--------------------------------------------------------------------------------

This directory contains plugins for Xinha (www.xinha.org) which must be run on
the local web server rather than from an external server/content delivery
network.

Consult the NewbieGuide ( http://trac.xinha.org/wiki/Documentation/NewbieGuide ) 
for complete details on Xinha configuration and see the examples in the directory
here for, err, examples.

Especially take note of examples/UsingPhpPlugins.php

EOF
echo "xinha-$VER" >xinha-cdn/VERSION.TXT
zip -r    xinha-$VER-cdn.zip     xinha-cdn
#tar -cjvf xinha-cdn.tar.bz2 xinha-cdn

# Update the s3 bucket
read -p "Upload to \"s3://xinha/xinha-${VER}/\"? [yN]: "
if [ "$REPLY" == "y" ] || [ "$REPLY" == "Y" ]
then
  cd xinha 
  s3cmd --delete-removed --acl-public sync ./ s3://xinha/xinha-${VER}/
  cd ..
fi

read -p "Upload to \"s3://xinha/xinha-latest/\"? [yN]: "
if [ "$REPLY" == "y" ] || [ "$REPLY" == "Y" ]
then
  cd xinha 
  s3cmd --delete-removed --acl-public sync ./ s3://xinha/xinha-latest/
  cd ..
fi

cd xinha
php contrib/compress_yui.php
sleep 5
cd ../
zip -r    xinha-$VER-minified.zip     xinha
# tar -cjvf xinha-$VER-minified.tar.bz2 xinha

ls -l *.zip
read -p "Upload zip files to to \"s3://xinha/releases/\"? [yN]: "
if [ "$REPLY" == "y" ] || [ "$REPLY" == "Y" ]
then
  for file in *.zip
  do
    s3cmd --acl-public put $file s3://xinha/releases/
  done
fi
