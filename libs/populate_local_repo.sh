#!/bin/bash

# Android API 19
mvn install:install-file \
    -Dfile=$ANDROID_HOME/platforms/android-19/android.jar \
    -DgroupId=com.google.android \
    -DartifactId=android \
    -Dversion=19 \
    -Dpackaging=jar

# Android support lib v4
mvn install:install-file \
    -Dfile=libs/android-support-v4.jar \
    -DgroupId=com.google.android \
    -DartifactId=support-v4 \
    -Dversion=r13 \
    -Dpackaging=jar

# https://github.com/keyboardsurfer/Crouton/issues/108
mvn install:install-file \
    -Dfile=libs/android-support-v4.jar \
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

# Google Play Services
cd $ANDROID_HOME/extras/google/google_play_services/libproject/google-play-services_lib
curl https://raw.github.com/brk3/gms-mvn-install/patch-1/gms-mvn-install.sh > gms-mvn-install.sh
bash ./gms-mvn-install.sh 13
cd -
