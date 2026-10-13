plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
}

android {
    namespace = "com.bizcal.app"
    compileSdk = 34
    defaultConfig {
        applicationId = "com.bizcal.app"
        minSdk = 23
        targetSdk = 34
        versionCode = 1
        versionName = "1.0.0"
    }
}
