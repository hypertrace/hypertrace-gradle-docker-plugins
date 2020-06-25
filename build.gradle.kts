plugins {
  id("org.hypertrace.ci-utils-plugin") version "0.1.0"
  id("org.hypertrace.publish-plugin") version "0.1.0" apply false
}

subprojects {
  group = "org.hypertrace.gradle.docker"
}
