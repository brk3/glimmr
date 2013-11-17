#!/bin/sh -e

if [ "$#" -ne 1 ]
then
    echo "Usage: $0 <version>"
    echo "e.g. $0 2.18"
    exit 1
fi

VERSION=$1

git checkout dev
git reset --hard origin/dev

git checkout glimmr-l
git reset --hard origin/glimmr-l

git tag -d glimmrpro-$VERSION glimmr-$VERSION
git push origin :refs/tags/glimmrpro-$VERSION :refs/tags/glimmr-$VERSION

rm -rf ~/Dropbox/android/glimmr/releases/com/bourke/glimmr/$VERSION/
rm -rf ~/Dropbox/android/glimmr/releases/com/bourke/glimmrpro/$VERSION/

echo "Done - check all looks ok and run:"
echo "git push origin dev glimmr-l -f"
