plugins {
    id("com.android.application")
    id("kotlin-android")
}

repositories {
    mavenCentral()
}

android {
    namespace = "juniojsv.minimum"
    defaultConfig {
        applicationId = "juniojsv.minimum"
        minSdk = 21
        targetSdk = 33
        compileSdk = 33
        versionCode = 132
        versionName = "1.4.0"
        setProperty("archivesBaseName", "$applicationId-v$versionName")
    }
    androidResources {
        generateLocaleConfig = true
    }
    buildTypes {
        getByName("release") {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    buildFeatures {
        viewBinding = true
    }
}

dependencies {
    implementation(fileTree(mapOf("dir" to "libs", "include" to listOf("*.jar"))))
    implementation("androidx.preference:preference-ktx:1.1.1")
}
