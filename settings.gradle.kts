

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
        maven { url = uri("https://jitpack.io") }
        gradlePluginPortal()
        mavenLocal()
        maven { url = uri("https://plugins.gradle.org/m2/") }
        maven { url = uri("https://repo.maven.apache.org/maven2") }
        maven { url = uri("https://chaquo.com/maven") }
        maven { url = uri("https://plugins.gradle.org/m2/") }
    }



}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
        maven { url = uri("https://jitpack.io") }
        maven { url = uri("https://plugins.gradle.org/m2/") }
        maven { url = uri("https://repo.maven.apache.org/maven2") }
        maven { url = uri("https://chaquo.com/maven") }
        maven { url = uri("https://plugins.gradle.org/m2/") }
    }
}

rootProject.name = "Newtonix AI Revived Edition"
include(":app")
