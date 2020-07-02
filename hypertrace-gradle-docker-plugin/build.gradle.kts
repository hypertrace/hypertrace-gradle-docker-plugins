plugins {
  `java-gradle-plugin`
  id("org.hypertrace.publish-plugin")
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
  api("com.bmuschko:gradle-docker-plugin:6.4.0")
}