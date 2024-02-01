@file:Suppress("UnstableApiUsage")

plugins {
    id("com.android.application")
    id("kotlin-android")
}

repositories {
    mavenCentral()
}

android {
    compileSdkVersion(31)
    defaultConfig {
        applicationId = "juniojsv.minimum"
        versionCode = 130
        versionName = "1.3.0"
        setMinSdkVersion(21)
        targetSdkVersion(31)
        resConfigs("pt-rBr")
        setProperty("archivesBaseName", "$applicationId-v$versionName")
    }
    kotlinOptions {
        jvmTarget = "1.8"
        useIR = true
    }
    buildTypes {
        getByName("release") {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }
    buildFeatures {
        viewBinding = true
    }
    buildToolsVersion = "30.0.3"
}

dependencies {
    implementation(fileTree(mapOf("dir" to "libs", "include" to listOf("*.jar"))))
    implementation("androidx.preference:preference-ktx:1.1.1")
}
