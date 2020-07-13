pluginManagement {
  repositories {
    mavenLocal()
    gradlePluginPortal()
    maven("https://dl.bintray.com/hypertrace/maven")
  }
}

plugins {
  id("org.hypertrace.version-settings") version "0.1.2"
}

rootProject.name = "hypertrace-gradle-docker-plugins"

include(":hypertrace-gradle-docker-plugin")
include(":hypertrace-gradle-docker-java-application-plugin")
include(":hypertrace-gradle-docker-publish-plugin")