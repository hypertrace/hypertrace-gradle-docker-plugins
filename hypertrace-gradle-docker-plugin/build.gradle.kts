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

  constraints {
    api("com.google.guava:guava:30.0-jre")
    api("com.fasterxml.jackson.core:jackson-databind:2.12.6.1")
    api("org.bouncycastle:bcprov-jdk15on:1.69")
    api("commons-io:commons-io:2.7")
  }
}
