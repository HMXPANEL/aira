pluginManagement {
    repositories {
        google {
            content {
                includeGroupByRegex("com\\.android.*")
                includeGroupByRegex("com\\.google.*")
                includeGroupByRegex("androidx.*")
            }
        }
        mavenCentral()
        gradlePluginPortal()
    }
    includeBuild("build-logic")
}

dependencyResolutionManagement {
    @Suppress("UnstableApiUsage")
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "AndroidAssistant"

include(":app")

// Core
include(":core:model")
include(":core:common")
include(":core:network")

// Data
include(":data:local")
include(":data:remote")

// Agent
include(":agent:llm")

// Agent
include(":agent:engine")

// Tool
include(":tool:registry")
include(":tool:system")

// Data
include(":data:memory")

// Agent
include(":agent:memory")

// Domain
include(":domain:memory")

// UI
include(":ui:chat")
include(":ui:settings")
include(":ui:memory")

// Android
include(":android:foreground")
include(":android:notification")
include(":android:overlay")
