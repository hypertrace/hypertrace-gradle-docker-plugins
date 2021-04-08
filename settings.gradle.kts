pluginManagement {
  repositories {
    mavenLocal()
    gradlePluginPortal()
    maven("https://hypertrace.jfrog.io/artifactory/maven")
  }
}

plugins {
  id("org.hypertrace.version-settings") version "0.2.0"
}

rootProject.name = "hypertrace-gradle-docker-plugins"

include(":hypertrace-gradle-docker-plugin")
include(":hypertrace-gradle-docker-java-application-plugin")
include(":hypertrace-gradle-docker-publish-plugin")