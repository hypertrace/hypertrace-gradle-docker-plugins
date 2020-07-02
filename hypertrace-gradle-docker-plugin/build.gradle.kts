plugins {
  `java-gradle-plugin`
  id("org.hypertrace.publish-plugin")
  id("org.hypertrace.repository-plugin") version "0.1.0"
}

java {
  targetCompatibility = JavaVersion.VERSION_11
  sourceCompatibility = JavaVersion.VERSION_11
}

gradlePlugin {
  plugins {
    create("gradlePlugin") {
      id = "org.hypertrace.docker-plugin"
      implementationClass = "org.hypertrace.gradle.docker.DockerPlugin"
    }
  }
}

dependencies {
  implementation("com.bmuschko:gradle-docker-plugin:6.4.0")
}