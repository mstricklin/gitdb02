#!/usr/bin/env bash

mkdir t
cd t

git init
git commit --allow-empty -m empty
git tag root

mkdir -p 00/00/00
#echo 1 > 01/02/03/K.1020301
#echo 2 > 01/02/03/K.1020302
#echo 3 > 01/02/03/K.1020303
#echo 4 > 01/02/03/K.1020304

# This funky syntax is b/c a regular 'cat' appends a newline, which mismatches
# what the serializer puts out.
export A=$(cat <<EOF
{
  "@class" : "edu.utexas.arlut.ciads.FrameA\$Impl",
  "id" : "shazam1",
  "s0" : "zero1",
  "s1" : "one1",
  "s2" : "two1",
  "__type" : "FrameA"
}
EOF
)
Echo -n "$A" > 00/00/00/00000001

export A=$(cat <<EOF
{
  "@class" : "edu.utexas.arlut.ciads.FrameA\$Impl",
  "id" : "shazam2",
  "s0" : "zero2",
  "s1" : "one2",
  "s2" : "two2",
  "__type" : "FrameA"
}
EOF
)
echo -n "$A" > 00/00/00/00000002


export A=$(cat <<EOF
{
  "@class" : "edu.utexas.arlut.ciads.FrameA\$Impl",
  "id" : "shazam3",
  "s0" : "zero3",
  "s1" : "one3",
  "s2" : "two3",
  "__type" : "FrameA"
}
EOF
)
echo -n "$A" > 00/00/00/00000003

git add --all
git commit -m baseline

git tag baseline

cd ..
git clone --bare t t.git

