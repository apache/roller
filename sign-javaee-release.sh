export rcstring=""
export vstring="5.1"

gpg --armor --detach-sig assembly-release/target/roller-weblogger-${vstring}${rcstring}-for-javaee.tar.gz
gpg --armor --detach-sig assembly-release/target/roller-weblogger-${vstring}${rcstring}-for-javaee.zip
