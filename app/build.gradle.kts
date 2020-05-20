plugins {
    id("com.android.application")
    id("kotlin-android")
    id("kotlin-android-extensions")
}

repositories {
    mavenCentral()
}

android {
    compileSdkVersion(29)
    defaultConfig {
        applicationId = "juniojsv.minimum"
        versionCode = 12
        versionName = "1.12"
        setMinSdkVersion(21)
        targetSdkVersion(29)
        resConfig("en")
        setProperty("archivesBaseName", "$applicationId-v$versionName")
    }
    buildTypes {
        getByName("release") {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }
}

dependencies {
    implementation(fileTree(mapOf("dir" to "libs", "include" to listOf("*.jar"))))
    implementation("androidx.preference:preference-ktx:1.1.1")
}
