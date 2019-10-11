#!/usr/bin/bash
if ! [ -f XinhaCore.js ]
then
  echo "$0: Execute this from the Xinha Root Directory" >&2
  exit 1
fi

# To satisfy my sense of order I want them all lined up :-/
NAME_LENGTH=22
repl() { printf " "'%.s' $(eval "echo {1.."$(($1))"}"); }

for dir in plugins modules unsupported_plugins
do
  cd $dir
  for file in $(ls -d *)
  do
    TARGET="$file.js"
    
    # Some special cases
    if [ "$file" = "CreateLink" ] && [ -f "$file/link.js" ]
    then
      TARGET="link.js"
    elif [ "$file" = "Dialogs" ] && [ -f "$file/dialog.js" ]
    then
      TARGET="dialog.js"
    elif [ "$file" = "GetHtml" ] && [ "$dir" = "modules" ]
    then
      continue
    fi
    
    if ! [ -f "$file/$TARGET" ]
    then
      TARGET="$(echo $file | sed -r 's/([a-z])([A-Z])/\1_\2/' | tr '[:upper:]' '[:lower:]').js"
    fi
    
    if ! [ -f "$file/$TARGET" ]
    then
      TARGET="$(echo $file | sed -r 's/([a-z])([A-Z])/\1-\2/' | tr '[:upper:]' '[:lower:]').js"
    fi
    
    if ! [ -f "$file/$TARGET" ]
    then        
      echo "$0: Target For $dir/$file Not Found" >&2
      exit 1
    fi
    
    SPACES="$(repl $(expr $NAME_LENGTH - $(echo $file | wc -c)))"
    echo "  $file:$SPACES{ url: _editor_url+'$dir/$file/$TARGET' },"
  done
  cd ../
done