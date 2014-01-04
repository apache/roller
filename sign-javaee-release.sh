export rcstring=""
export vstring="5.0.3"

gpg --armor --detach-sig weblogger-assembly/target/roller-weblogger-${vstring}${rcstring}-for-javaee.tar.gz
gpg --armor --detach-sig weblogger-assembly/target/roller-weblogger-${vstring}${rcstring}-for-javaee.zip
