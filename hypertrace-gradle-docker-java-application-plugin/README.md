
# Hypertrace Docker Java Application Plugin
###### org.hypertrace.docker-java-application-plugin

### Purpose
This plugin is a highly opinionated plugin that configures the target project to build docker images of the target project.
It configures the default image from `org.hypertrace.docker-plugin` to use the docker file produced by
`com.bmuschko.docker-java-application`. It allows configuring one value, the base image
(which defaults to `gcr.io/distroless/java:11`). Additionally, the main class is taken from the application plugin - the
default behavior of `com.bmuschko.docker-java-application` for identifying main classes is not used.

For further configuration, use `org.hypertrace.docker-plugin` directly and create a dockerfile.

### Full example

```kotlin
plugins {
  id("org.hypertrace.docker-java-application-plugin") version "<version>"
}

application {
  mainClassName = "org.hypertrace.example.app.MyApplication"
}

hypertraceDocker {
  javaApplication {
    baseImage.set("openjdk:11-jre-slim")
  }
}
```