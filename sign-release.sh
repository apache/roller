
export rcstring="-rc1"

cp build/apache-roller-2.3.1-incubating.tar.gz  build/apache-roller-2.3.1${rcstring}-incubating.tar.gz
cp build/apache-roller-2.3.1-incubating.zip     build/apache-roller-2.3.1${rcstring}-incubating.zip

gpg --armor --output build/apache-roller-2.3.1${rcstring}-incubating.tar.gz.asc  --detach-sig build/apache-roller-2.3.1${rcstring}-incubating.tar.gz
gpg --armor --output build/apache-roller-2.3.1${rcstring}-incubating.zip.asc     --detach-sig build/apache-roller-2.3.1${rcstring}-incubating.zip

