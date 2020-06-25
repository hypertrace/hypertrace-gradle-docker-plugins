import org.hypertrace.gradle.publishing.License.AGPL_V3

plugins {
  `java-gradle-plugin`
  id("org.hypertrace.publish-plugin")
  id("org.hypertrace.repository-plugin") version "0.1.0"
}

java {
  targetCompatibility = JavaVersion.VERSION_11
  sourceCompatibility = JavaVersion.VERSION_11
}

hypertracePublish {
  license.set(AGPL_V3)
}

gradlePlugin {
  plugins {
    create("gradlePlugin") {
      id = "org.hypertrace.docker-publish-plugin"
      implementationClass = "org.hypertrace.gradle.docker.DockerPublishPlugin"
    }
  }
}

dependencies {
  implementation("com.bmuschko:gradle-docker-plugin:6.4.0")
  implementation(project(":hypertrace-gradle-docker-plugin"))
}

