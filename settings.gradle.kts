pluginManagement {
  repositories {
    gradlePluginPortal()
    maven(url = "../local-plugin-repository")
  }
}

dependencyResolutionManagement {
  repositories {
    mavenCentral()
    maven("https://plugins.gradle.org/m2/")
    maven("https://jitpack.io")
    maven("https://repo.kotlin.link")
  }
}

rootProject.name = "abacus-plugin"
include("abacus")
