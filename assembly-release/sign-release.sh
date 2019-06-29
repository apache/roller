#!/usr/bin/env bash

export rcstring="-rc-1"
export vstring="6.0.0"

# for rc releases we rename the release files
if [ rcstring != "" ]; then
    mv target/roller-release-${vstring}-source.tar.gz   target/roller-release-${vstring}${rcstring}-source.tar.gz
    mv target/roller-release-${vstring}-source.zip      target/roller-release-${vstring}${rcstring}-source.zip
    mv target/roller-release-${vstring}-standard.tar.gz target/roller-release-${vstring}${rcstring}-standard.tar.gz
    mv target/roller-release-${vstring}-standard.zip    target/roller-release-${vstring}${rcstring}-standard.zip
fi

gpg --armor --detach-sig target/roller-release-${vstring}${rcstring}-standard.tar.gz
gpg --armor --detach-sig target/roller-release-${vstring}${rcstring}-standard.zip
gpg --armor --detach-sig target/roller-release-${vstring}${rcstring}-source.tar.gz
gpg --armor --detach-sig target/roller-release-${vstring}${rcstring}-source.zip

gpg --print-md sha256 target/roller-release-${vstring}${rcstring}-standard.tar.gz > \
target/roller-release-${vstring}${rcstring}-standard.tar.gz.sha256

gpg --print-md sha256 target/roller-release-${vstring}${rcstring}-standard.zip > \
target/roller-release-${vstring}${rcstring}-standard.zip.sha256

gpg --print-md sha256 target/roller-release-${vstring}${rcstring}-source.tar.gz > \
target/roller-release-${vstring}${rcstring}-source.tar.gz.sha256

gpg --print-md sha256 target/roller-release-${vstring}${rcstring}-source.zip > \
target/roller-release-${vstring}${rcstring}-source.zip.sha256


