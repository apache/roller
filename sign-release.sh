
export rcstring="-rc7"
export vstring="3.1"

cp dist/apache-roller-${vstring}.tar.gz dist/apache-roller-${vstring}${rcstring}.tar.gz
cp dist/apache-roller-${vstring}.zip    dist/apache-roller-${vstring}${rcstring}.zip

cp dist/apache-roller-src-${vstring}.tar.gz dist/apache-roller-src-${vstring}${rcstring}.tar.gz
cp dist/apache-roller-src-${vstring}.zip    dist/apache-roller-src-${vstring}${rcstring}.zip

gpg --armor --output dist/apache-roller-${vstring}${rcstring}.tar.gz.asc     --detach-sig dist/apache-roller-${vstring}${rcstring}.tar.gz
gpg --armor --output dist/apache-roller-${vstring}${rcstring}.zip.asc        --detach-sig dist/apache-roller-${vstring}${rcstring}.zip

gpg --armor --output dist/apache-roller-src-${vstring}${rcstring}.tar.gz.asc --detach-sig dist/apache-roller-src-${vstring}${rcstring}.tar.gz
gpg --armor --output dist/apache-roller-src-${vstring}${rcstring}.zip.asc    --detach-sig dist/apache-roller-src-${vstring}${rcstring}.zip

