package com.androidassistant.buildlogic

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.dependencies
import org.gradle.kotlin.dsl.implementation
import org.gradle.kotlin.dsl.ksp

class AndroidHiltConventionPlugin : Plugin<Project> {
    override fun apply(project: Project) {
        project.plugins.apply("com.android.library")
        project.plugins.apply("org.jetbrains.kotlin.android")
        project.plugins.apply("com.google.devtools.ksp")

        project.configure<com.android.build.api.dsl.BaseLibraryExtension> {
            compileSdk = 35
            namespace = "com.androidassistant.${project.name.replace(":core:", "core.").replace(":data:", "data.").replace(":agent:", "agent.").replace(":tool:", "tool.").replace(":ui:", "ui.").replace(":android:", "android.")}"

            defaultConfig {
                minSdk = 26
                targetSdk = 35
            }

            compileOptions {
                sourceCompatibility = JavaVersion.VERSION_17
                targetCompatibility = JavaVersion.VERSION_17
            }

            kotlinOptions {
                jvmTarget = "17"
            }

            ksp {
                arg("ksp.incremental", "true")
            }
        }

        project.dependencies {
            add("implementation", "com.google.dagger:hilt-android:2.51.1")
            add("ksp", "com.google.dagger:hilt-compiler:2.51.1")
            add("implementation", "androidx.hilt:hilt-navigation-compose:1.2.0")
        }
    }
}