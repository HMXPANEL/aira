package com.androidassistant.buildlogic

import com.android.build.api.dsl.LibraryExtension
import org.gradle.api.JavaVersion
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure

class AndroidHiltConventionPlugin : Plugin<Project> {
    override fun apply(project: Project) {
        project.plugins.apply("com.android.library")
        project.plugins.apply("org.jetbrains.kotlin.android")
        project.plugins.apply("com.google.devtools.ksp")

        project.extensions.configure<LibraryExtension> {
            compileSdk = 35
            defaultConfig {
                minSdk = 26
                targetSdk = 35
            }
            compileOptions {
                sourceCompatibility = JavaVersion.VERSION_17
                targetCompatibility = JavaVersion.VERSION_17
            }
        }

        project.dependencies.add("implementation", "com.google.dagger:hilt-android:2.51.1")
        project.dependencies.add("ksp", "com.google.dagger:hilt-compiler:2.51.1")
        project.dependencies.add("implementation", "androidx.hilt:hilt-navigation-compose:1.2.0")
    }
}