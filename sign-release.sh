export rcstring=""
export vstring="5.1"

gpg --armor --detach-sig assembly-release/target/roller-weblogger-${vstring}${rcstring}.tar.gz
gpg --armor --detach-sig assembly-release/target/roller-weblogger-${vstring}${rcstring}.zip
gpg --armor --detach-sig assembly-release/target/roller-weblogger-${vstring}${rcstring}-source.tar.gz 
gpg --armor --detach-sig assembly-release/target/roller-weblogger-${vstring}${rcstring}-source.zip
