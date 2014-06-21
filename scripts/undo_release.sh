#!/bin/sh -e

if [ "$#" -ne 1 ]
then
    echo "Usage: $0 <version>"
    echo "e.g. $0 2.18"
    exit 1
fi

VERSION=$1

git checkout master
git reset --hard origin/master

git checkout glimmr-l
git reset --hard origin/glimmr-l

git tag -d glimmrpro-$VERSION glimmr-$VERSION
git push origin :refs/tags/glimmrpro-$VERSION :refs/tags/glimmr-$VERSION

git push origin master glimmr-l -f

echo "OK"
