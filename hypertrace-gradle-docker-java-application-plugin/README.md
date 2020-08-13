
# Hypertrace Docker Java Application Plugin
###### org.hypertrace.docker-java-application-plugin

### Purpose
This plugin is a highly opinionated plugin that configures the target project to build docker images of the target project.
It configures the default image from `org.hypertrace.docker-plugin` to use the docker file produced by
`com.bmuschko.docker-java-application`. It allows configuring several values, detailed below. 
Additionally, the main class is taken from the application plugin - the
default behavior of `com.bmuschko.docker-java-application` for identifying main classes is not used.
An application can define multiple variants which allow specifying different base images to use.

- `baseImage` - String, Defaults to `hypertrace/java:11`
- `maintainer` - String, Defaults to `Hypertrace 'https://www.hypertrace.org/'`
- `port` - Integer, no default. Only exposed if set.
- `adminPort` - Integer, defaults to `${port} + 1` if `port` is set. Only exposed if set.
- `serviceName` - String, defaults to `${project.name}`. Added as `SERVICE_NAME` env var
- `healthCheck` - String, defaults to `-interval=2s --start-period=15s --timeout=2s CMD wget  -qO http://127.0.0.1:${adminPort}/health &> /dev/null || exit` if `adminPort` is set. Only added if set.
- `envVars` - Map<String, String>, defaults to `{"SERVICE_NAME": "${serviceName}"}`


For further configuration, use `org.hypertrace.docker-plugin` directly and create a dockerfile.

### Full example

```kotlin
plugins {
  id("org.hypertrace.docker-java-application-plugin") version "<version>"
}
// project.name == "example-app"

application {
  mainClassName = "org.hypertrace.example.app.MyApplication"
}

hypertraceDocker {
  defaultImage {
    javaApplication {
      maintainer.set("Hypertrace 'https://www.hypertrace.org/'")
      port.set(9001)
      adminPort.set(9002)
      serviceName.set("hypertrace-federated-service")
      healthCheck.set("-interval=2s --start-period=15s --timeout=2s CMD wget -qO- http://127.0.0.1:${adminPort.get()}/health &> /dev/null || exit 1")
      envVars.put("ENV_VAR", "val")
    }
  }
}
```

Producing a dockerfile:
```Dockerfile
FROM hypertrace/java:11
LABEL maintainer="Hypertrace 'https://www.hypertrace.org/'"
WORKDIR /app
COPY libs libs/
COPY resources resources/
COPY classes classes/
ENTRYPOINT ["java", "-cp", "/app/resources:/app/classes:/app/libs/*", "org.hypertrace.example.app.MyApplication"]
EXPOSE 9001 9002
HEALTHCHECK -interval=2s --start-period=15s --timeout=2s CMD wget -qO- http://127.0.0.1:9002/health &> /dev/null || exit 1
ENV SERVICE_NAME=example-app ENV_VAR=val
```