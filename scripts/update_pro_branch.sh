#!/bin/sh -ex

PKG='com\.bourke\.glimmr'
PRO_PKG='com\.bourke\.glimmrpro'
VERSION=$(grep 'android:versionName' AndroidManifest.xml |\
    cut -d' ' -f5 | cut -d'=' -f2 | tr -d \")
STAGING_BRANCH="release/glimmr-l_$(date +%s)"
PRO_BRANCH="glimmr-l"

echo "Creating release for '$VERSION'"
echo

git checkout --quiet dev

echo "Creating release branch $STAGING_BRANCH"
git checkout -b $STAGING_BRANCH

echo "Updating all instances of '$PKG' with '$PRO_PKG'"
grep -Iril "$PKG" src res AndroidManifest.xml |\
    xargs sed -i '' "s/$PKG/$PRO_PKG/g"

echo "Updating pro constant"
find src -name Constants.java |\
    xargs sed -i '' 's/PRO_VERSION = false/PRO_VERSION = true/g'

echo "Updating app name in strings"
find res -name strings.xml |\
    xargs sed -i '' 's#name="app_name">Glimmr#name="app_name">Glimmr-L#g'

echo "Updating oauth callback scheme"
sed -i '' 's/glimmr/glimmrpro/g' res/values/settings_oauth.xml

echo "Updating package directory name"
git mv src/main/java/com/bourke/glimmr/ src/main/java/com/bourke/glimmrpro

echo "Updating pom artifact"
sed -i '' 's/<artifactId>glimmr/<artifactId>glimmrpro/g' pom.xml

echo "Committing changes"
git commit -q -am "Prepare pro release from g$(git rev-parse --short dev)"

echo "Merging to $PRO_BRANCH with 'ours' strategy"
git checkout -q $PRO_BRANCH
git merge -q --no-edit -s recursive -X theirs $STAGING_BRANCH

echo "Cleaning up"
git branch -D $STAGING_BRANCH
