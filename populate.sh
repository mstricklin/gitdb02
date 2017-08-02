#!/usr/bin/env bash

mkdir t
cd t

git init
git commit --allow-empty -m empty
git tag root

mkdir -p 01/02/03
echo 1 > 01/02/03/K.1020301
echo 2 > 01/02/03/K.1020302
echo 3 > 01/02/03/K.1020303
echo 4 > 01/02/03/K.1020304

mkdir FrameA FrameB
cat << EOF > FrameA/1
{
    "id": "shazam1",
    "s0": "zero1",
    "s1": "one1",
    "s2": "two1"
}
EOF
cat << EOF > FrameA/2
{
    "id": "shazam2",
    "s0": "zero2",
    "s1": "one2",
    "s2": "two2"
}
EOF
cat << EOF > FrameA/3
{
    "id": "shazam3",
    "s0": "zero3",
    "s1": "one3",
    "s2": "two3"
}
EOF

git add --all
git commit -m baseline

git tag baseline

cd ..
git clone --bare t t.git

