package com.androidassistant.buildlogic

import com.android.build.api.dsl.ApplicationExtension
import org.gradle.api.JavaVersion
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.withType
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

class AndroidAppConventionPlugin : Plugin<Project> {
    override fun apply(project: Project) {
        project.plugins.apply("com.android.application")
        project.plugins.apply("org.jetbrains.kotlin.android")
        project.plugins.apply("org.jetbrains.kotlin.plugin.compose")
        project.plugins.apply("com.google.devtools.ksp")

        project.extensions.configure<ApplicationExtension> {
            compileSdk = 35
            defaultConfig {
                minSdk = 26
                targetSdk = 35
            }
            compileOptions {
                sourceCompatibility = JavaVersion.VERSION_17
                targetCompatibility = JavaVersion.VERSION_17
            }
            buildFeatures {
                compose = true
            }
        }

        project.tasks.withType<KotlinCompile>().configureEach {
            kotlinOptions.jvmTarget = "17"
        }
    }
}