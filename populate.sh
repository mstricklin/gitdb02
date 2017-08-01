#!/usr/bin/env bash

mkdir t
cd t

git init
mkdir -p 01/02/03
echo 1 > 01/02/03/K.10203
git add --all
git commit -m 00
git tag baseline

cd ..
git clone --bare t t.git

