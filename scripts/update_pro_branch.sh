#!/bin/sh -e

PKG='com\.bourke\.glimmr'
PRO_PKG='com\.bourke\.glimmrpro'
VERSION=$(grep 'android:versionName' AndroidManifest.xml |\
    cut -d' ' -f5 | cut -d'=' -f2 | tr -d \")
STAGING_BRANCH="release/glimmr-l_$(date +%s)"
PRO_BRANCH="glimmr-l"

echo "Creating release for '$VERSION'"

git checkout -q dev

echo "Creating release branch $STAGING_BRANCH"
git checkout -q -b $STAGING_BRANCH

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

# http://stackoverflow.com/a/4969679/663370
echo "Merging to $PRO_BRANCH with 'ours' strategy"
git checkout -q $PRO_BRANCH
git merge -q --no-edit -s ours $STAGING_BRANCH
git branch -q branchTEMP
git reset -q --hard $STAGING_BRANCH
git reset -q --soft branchTEMP
git commit -q --amend --no-edit
git branch -q -D branchTEMP

echo "Cleaning up"
git branch -q -D $STAGING_BRANCH

echo "OK"
