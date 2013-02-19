#!/bin/bash

# Android API 17
mvn install:install-file \
    -Dfile=$ANDROID_HOME/platforms/android-17/android.jar \
    -DgroupId=com.google.android \
    -DartifactId=android \
    -Dversion=17 \
    -Dpackaging=jar

# Android support lib r11
mvn install:install-file \
    -Dfile=$ANDROID_HOME/extras/android/support/v4/android-support-v4.jar \
    -DgroupId=com.google.android \
    -DartifactId=support-v4 \
    -Dversion=11 \
    -Dpackaging=jar

# jars
# (https://github.com/androidquery/androidquery/issues/12)
mvn install:install-file \
    -Dfile=android-query.0.24.3.jar \
    -DgroupId=com.androidquery \
    -DartifactId=androidquery \
    -Dversion=0.24.3 \
    -Dpackaging=jar

mvn install:install-file \
    -Dfile=CWAC-AdapterWrapper.jar \
    -DgroupId=com.commonsware.cwac \
    -DartifactId=adapter \
    -Dversion=SNAPSHOT \
    -Dpackaging=jar

mvn install:install-file \
    -Dfile=CWAC-EndlessAdapter-1.2.jar \
    -DgroupId=com.commonsware.cwac \
    -DartifactId=endless \
    -Dversion=1.2 \
    -Dpackaging=jar

mvn install:install-file \
    -Dfile=CWAC-WakefulIntentService_50dc5a6f.jar \
    -DgroupId=com.commonsware.cwac \
    -DartifactId=wakeful \
    -Dversion=50dc5a6f \
    -Dpackaging=jar

# apklib
mvn install:install-file \
    -Dfile=AndroidGridViewCompatLib-75fd1df.apklib \
    -DgroupId=com.rokoder \
    -DartifactId=gridviewcompat \
    -Dversion=75fd1df \
    -Dpackaging=apklib

mvn install:install-file \
    -Dfile=appirater-android_ca52540.apklib \
    -DgroupId=com.sbstrm \
    -DartifactId=appirater \
    -Dversion=ca52540 \
    -Dpackaging=apklib

