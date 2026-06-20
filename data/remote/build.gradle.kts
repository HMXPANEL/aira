plugins {
    id("com.androidassistant.buildlogic.android-lib")
}

android {
    namespace = "com.androidassistant.data.remote"
    compileSdk = 35

    defaultConfig {
        minSdk = 26
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = "17"
    }
}

dependencies {
    implementation(project(":core:model"))
    implementation(project(":core:common"))
    implementation(project(":core:network"))

    // Koin
    implementation(libs.koin.core)
    implementation(libs.koin.android)

    // OkHttp
    implementation(libs.okhttp)
    implementation(libs.okhttp.logging)
    implementation(libs.okhttp.sse)

    // Data
    implementation(project(":data:local"))

    // Coroutines
    implementation(libs.coroutines.core)
    implementation(libs.coroutines.android)
}