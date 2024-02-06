plugins {
    id("com.android.application")
    id("kotlin-android")
}

repositories {
    mavenCentral()
}

android {
    defaultConfig {
        applicationId = "juniojsv.minimum"
        minSdk = 21
        targetSdk = 33
        compileSdk = 33
        versionCode = 133
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
    namespace = defaultConfig.applicationId
}

dependencies {
    implementation(fileTree(mapOf("dir" to "libs", "include" to listOf("*.jar"))))
    implementation("androidx.recyclerview:recyclerview:1.3.2")
    implementation("androidx.preference:preference-ktx:1.2.1")
}
