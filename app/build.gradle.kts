plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("org.jetbrains.kotlin.plugin.compose")
}

android {
    namespace = "com.tuxunbrowse.android"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.tuxunbrowse.android"
        minSdk = 26
        targetSdk = 35
        versionCode = 157
        versionName = "0.157.0"
        // GeckoView pushes the method count past 64k in debug builds.
        multiDexEnabled = true
    }

    signingConfigs {
        // Pinned TEST signing: without this, every CI runner generates
        // a fresh debug keystore and test builds can never be installed
        // over each other. This key is a throwaway committed on purpose
        // (standard debug alias/password); it is NOT a release key.
        create("testPin") {
            storeFile = rootProject.file("tools/test-signing.keystore")
            storePassword = "android"
            keyAlias = "androiddebugkey"
            keyPassword = "android"
        }
    }

    splits {
        abi {
            // GeckoView's fat AAR carries four ABIs; ship one APK per
            // ABI we support so nobody downloads the other architectures.
            isEnable = true
            reset()
            include("arm64-v8a", "x86_64")
            isUniversalApk = false
        }
    }

    buildTypes {
        debug {
            signingConfig = signingConfigs.getByName("testPin")
        }
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions {
        jvmTarget = "17"
    }
    buildFeatures {
        compose = true
    }
}

dependencies {
    val composeBom = platform("androidx.compose:compose-bom:2024.09.00")
    implementation(composeBom)
    implementation("androidx.core:core-ktx:1.13.1")
    implementation("androidx.activity:activity-compose:1.9.2")
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-graphics")
    implementation("androidx.compose.ui:ui-text")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.compose.material:material-icons-extended:1.7.2")
    // Only for explicitly enabling WebView Safe Browsing
    // (WebSettingsCompat.setSafeBrowsingEnabled); 1.11.0 is compatible
    // with compileSdk 35 and adds only a small library to the APK.
    implementation("androidx.webkit:webkit:1.11.0")
    // Firefox engine. Version verified on maven.mozilla.org.
    implementation("org.mozilla.geckoview:geckoview:132.0.20241110192737")
    debugImplementation("androidx.compose.ui:ui-tooling")
    implementation("androidx.compose.ui:ui-tooling-preview")
    testImplementation("junit:junit:4.13.2")
    // Real org.json for JVM unit tests (v0.72.0): the android.jar
    // stubs throw "not mocked", so the bookmark/backup JSON parsers
    // could not be exercised in testDebugUnitTest before. This jar
    // shadows the stubs on the unit-test classpath only; it is NOT
    // packaged into the APK.
    testImplementation("org.json:json:20240303")
}
