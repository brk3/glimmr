#!/bin/bash

android update lib-project -p \
    libs/JakeWharton-ActionBarSherlock-aece21a/library/ -t android-14

android update lib-project -p \
    libs/JakeWharton-Android-ViewPagerIndicator-8cd549f/library/ -t android-16

android update lib-project -p \
    libs/appirater-android-6a954f19/ -t android-3

android update project -p . -t android-16

