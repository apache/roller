
export rcstring="-rc1"

cp dist/apache-roller-3.0-incubating.tar.gz dist/apache-roller-3.0${rcstring}-incubating.tar.gz
cp dist/apache-roller-3.0-incubating.zip    dist/apache-roller-3.0${rcstring}-incubating.zip

cp dist/apache-roller-src-3.0-incubating.tar.gz  dist/apache-roller-src-3.0${rcstring}-incubating.tar.gz
cp dist/apache-roller-src-3.0-incubating.zip     dist/apache-roller-src-3.0${rcstring}-incubating.zip

gpg --armor --output dist/apache-roller-3.0-incubating.tar.gz.asc     --detach-sig dist/apache-roller-3.0${rcstring}-incubating.tar.gz
gpg --armor --output dist/apache-roller-3.0-incubating.zip.asc        --detach-sig dist/apache-roller-3.0${rcstring}-incubating.zip

gpg --armor --output dist/apache-roller-src-3.0-incubating.tar.gz.asc --detach-sig dist/apache-roller-src-3.0${rcstring}-incubating.tar.gz
gpg --armor --output dist/apache-roller-src-3.0-incubating.zip.asc    --detach-sig dist/apache-roller-src-3.0${rcstring}-incubating.zip


