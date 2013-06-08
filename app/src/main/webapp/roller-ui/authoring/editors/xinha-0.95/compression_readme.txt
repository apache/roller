You can use the contrib/compress.php to batch-convert the current Xinha snapshot. To use this script, open it in a text editor, comment out the die() command at the top and open it in your browser.
 
You can use the contrib/compress.bat to compress JavaScript files by drag&drop in Windows.

Please be aware that the language files cannot be compressed.

If you want the original files to be kept, open compress.bat and remvove the # in the line
# FOR %%V IN (%*) DO del %%V_uncompressed.js