export rcstring=""
export vstring="5.0.1"

gpg --armor --detach-sig weblogger-assembly/target/roller-weblogger-${vstring}${rcstring}-for-tomcat.tar.gz
gpg --armor --detach-sig weblogger-assembly/target/roller-weblogger-${vstring}${rcstring}-for-tomcat.zip
gpg --armor --detach-sig weblogger-assembly/target/roller-weblogger-${vstring}${rcstring}-source.tar.gz 
gpg --armor --detach-sig weblogger-assembly/target/roller-weblogger-${vstring}${rcstring}-source.zip

