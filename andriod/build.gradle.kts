// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    id("com.android.application") version "8.2.1" apply false
    id("com.google.gms.google-services") version "4.4.1" apply false
}

buildscript {
    dependencies {
        classpath("com.android.tools.build:gradle:3.4.2")
        classpath("com.google.gms:google-services:4.4.1")
    }
}

tasks.register("clean", Delete::class) {
    delete(rootDir.resolve("build"))
}