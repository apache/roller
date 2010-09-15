export rcstring="-RC2"
export vstring="5.0.0"

gpg --armor --detach-sig weblogger-assembly/target/roller-weblogger-${vstring}${rcstring}-binary.tar.gz
gpg --armor --detach-sig weblogger-assembly/target/roller-weblogger-${vstring}${rcstring}-binary.zip
gpg --armor --detach-sig weblogger-assembly/target/roller-weblogger-${vstring}${rcstring}-source.tar.gz 
gpg --armor --detach-sig weblogger-assembly/target/roller-weblogger-${vstring}${rcstring}-source.zip

