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
      id = "org.hypertrace.docker-publish-plugin"
      implementationClass = "org.hypertrace.gradle.docker.DockerPublishPlugin"
    }
  }
}

dependencies {
  api("com.bmuschko:gradle-docker-plugin:6.4.0")
  api(project(":hypertrace-gradle-docker-plugin"))
}

