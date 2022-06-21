plugins {
  `java-gradle-plugin`
  id("org.hypertrace.publish-plugin")
}

java {
  targetCompatibility = JavaVersion.VERSION_1_8
  sourceCompatibility = JavaVersion.VERSION_1_8
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
  api("com.bmuschko:gradle-docker-plugin:7.4.0")
}