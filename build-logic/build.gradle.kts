plugins {
    `kotlin-dsl`
}

repositories {
    google()
    mavenCentral()
    gradlePluginPortal()
}

gradlePlugin {
    plugins {
        register("android-app") {
            id = "com.androidassistant.buildlogic.android-app"
            implementationClass = "com.androidassistant.buildlogic.AndroidAppConventionPlugin"
        }
        register("android-lib") {
            id = "com.androidassistant.buildlogic.android-lib"
            implementationClass = "com.androidassistant.buildlogic.AndroidLibConventionPlugin"
        }
        register("android-feature") {
            id = "com.androidassistant.buildlogic.android-feature"
            implementationClass = "com.androidassistant.buildlogic.AndroidFeatureConventionPlugin"
        }
        register("android-hilt") {
            id = "com.androidassistant.buildlogic.android-hilt"
            implementationClass = "com.androidassistant.buildlogic.AndroidHiltConventionPlugin"
        }
    }
}

dependencies {
    implementation("com.android.tools.build:gradle:8.7.3")
    implementation("org.jetbrains.kotlin:kotlin-gradle-plugin:2.0.21")
}