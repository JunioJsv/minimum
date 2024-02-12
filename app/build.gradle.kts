plugins {
    id("com.android.application")
    id("kotlin-android")
}

repositories {
    mavenCentral()
}

android {
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    defaultConfig {
        applicationId = "juniojsv.minimum"
        minSdk = 21
        targetSdk = 33
        compileSdk = 33
        versionCode = 136
        versionName = "1.5.2"
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
