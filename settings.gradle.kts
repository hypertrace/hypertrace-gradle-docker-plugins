pluginManagement {
  repositories {
    mavenLocal()
    gradlePluginPortal()
    maven {
      url = uri("https://dl.bintray.com/hypertrace/maven")
    }
  }
}

plugins {
  id("org.hypertrace.version-settings") version "0.1.0"
}

rootProject.name = "hypertrace-gradle-docker-plugins"

include(":hypertrace-gradle-docker-plugin")
include(":hypertrace-gradle-docker-java-application-plugin")
include(":hypertrace-gradle-docker-publish-plugin")