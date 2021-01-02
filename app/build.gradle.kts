plugins {
    id("com.android.application")
    id("kotlin-android")
}

repositories {
    mavenCentral()
}

android {
    compileSdkVersion(30)
    defaultConfig {
        applicationId = "juniojsv.minimum"
        versionCode = 13
        versionName = "1.2.2"
        setMinSdkVersion(21)
        targetSdkVersion(30)
        resConfigs("pt-rBr")
        setProperty("archivesBaseName", "$applicationId-v$versionName")
    }
    kotlinOptions {
        jvmTarget = "1.8"
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
    implementation("com.jmedeisis:draglinearlayout:1.1.0")
}
