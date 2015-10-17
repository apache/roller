export rcstring=""
export vstring="5.2.0"

gpg --armor --detach-sig target/roller-release-${vstring}${rcstring}-standard.tar.gz
gpg --armor --detach-sig target/roller-release-${vstring}${rcstring}-standard.zip
gpg --armor --detach-sig target/roller-release-${vstring}${rcstring}-source.tar.gz 
gpg --armor --detach-sig target/roller-release-${vstring}${rcstring}-source.zip

