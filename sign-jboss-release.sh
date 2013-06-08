export rcstring=""
export vstring="5.1"

gpg --armor --detach-sig assembly-release/target/roller-weblogger-${vstring}${rcstring}-for-jboss.tar.gz
gpg --armor --detach-sig assembly-release/target/roller-weblogger-${vstring}${rcstring}-for-jboss.zip
