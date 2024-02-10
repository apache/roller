#!/usr/bin/env bash

export rcstring="r2"
export vstring="6.1.3"

# for rc releases we rename the release files
if [ rcstring != "" ]; then
    mv target/apache-roller-${vstring}-source.tar.gz   target/apache-roller-${vstring}${rcstring}-source.tar.gz
    mv target/apache-roller-${vstring}-source.zip      target/apache-roller-${vstring}${rcstring}-source.zip
    mv target/apache-roller-${vstring}-binary.tar.gz target/apache-roller-${vstring}${rcstring}-binary.tar.gz
    mv target/apache-roller-${vstring}-binary.zip    target/apache-roller-${vstring}${rcstring}-binary.zip
fi

gpg --armor --detach-sig target/apache-roller-${vstring}${rcstring}-binary.tar.gz
gpg --armor --detach-sig target/apache-roller-${vstring}${rcstring}-binary.zip
gpg --armor --detach-sig target/apache-roller-${vstring}${rcstring}-source.tar.gz
gpg --armor --detach-sig target/apache-roller-${vstring}${rcstring}-source.zip

gpg --print-md sha256 target/apache-roller-${vstring}${rcstring}-binary.tar.gz > \
target/apache-roller-${vstring}${rcstring}-binary.tar.gz.sha256

gpg --print-md sha256 target/apache-roller-${vstring}${rcstring}-binary.zip > \
target/apache-roller-${vstring}${rcstring}-binary.zip.sha256

gpg --print-md sha256 target/apache-roller-${vstring}${rcstring}-source.tar.gz > \
target/apache-roller-${vstring}${rcstring}-source.tar.gz.sha256

gpg --print-md sha256 target/apache-roller-${vstring}${rcstring}-source.zip > \
target/apache-roller-${vstring}${rcstring}-source.zip.sha256


