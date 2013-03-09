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
    -Dversion=r11 \
    -Dpackaging=jar

# jars
mvn install:install-file \
    -Dfile=libs/CWAC-AdapterWrapper.jar \
    -DgroupId=com.commonsware.cwac \
    -DartifactId=adapter \
    -Dversion=1.0 \
    -Dpackaging=jar

mvn install:install-file \
    -Dfile=libs/CWAC-EndlessAdapter-1.2.jar \
    -DgroupId=com.commonsware.cwac \
    -DartifactId=endless \
    -Dversion=1.2 \
    -Dpackaging=jar

mvn install:install-file \
    -Dfile=libs/CWAC-WakefulIntentService_50dc5a6f.jar \
    -DgroupId=com.commonsware.cwac \
    -DartifactId=wakeful \
    -Dversion=50dc5a6f \
    -Dpackaging=jar

# apklib
mvn install:install-file \
    -Dfile=libs/AndroidGridViewCompatLib-75fd1df.apklib \
    -DgroupId=com.rokoder \
    -DartifactId=gridviewcompat \
    -Dversion=75fd1df \
    -Dpackaging=apklib

mvn install:install-file \
    -Dfile=libs/appirater-android_ca52540.apklib \
    -DgroupId=com.sbstrm \
    -DartifactId=appirater \
    -Dversion=ca52540 \
    -Dpackaging=apklib

