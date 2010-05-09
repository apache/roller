export rcstring="-RC2"
export vstring="5.0.0"

#cp dist/roller-weblogger-${vstring}-binary.tar.gz dist/roller-weblogger-${vstring}${rcstring}-binary.tar.gz
#cp dist/roller-weblogger-${vstring}-binary.zip    dist/roller-weblogger-${vstring}${rcstring}-binary.zip
#cp dist/roller-weblogger-${vstring}-source.tar.gz dist/roller-weblogger-${vstring}${rcstring}-source.tar.gz
#cp dist/roller-weblogger-${vstring}-source.zip    dist/roller-weblogger-${vstring}${rcstring}-source.zip

gpg --armor --detach-sig dist/roller-weblogger-${vstring}${rcstring}-binary.tar.gz
gpg --armor --detach-sig dist/roller-weblogger-${vstring}${rcstring}-binary.zip
gpg --armor --detach-sig dist/roller-weblogger-${vstring}${rcstring}-source.tar.gz 
gpg --armor --detach-sig dist/roller-weblogger-${vstring}${rcstring}-source.zip

