
# Hypertrace Docker Java Application Plugin
###### org.hypertrace.docker-java-application-plugin

### Purpose
This plugin is a highly opinionated plugin that configures the target project to build docker images of the target project.
It configures the default image from `org.hypertrace.docker-plugin` to use the docker file produced by
`com.bmuschko.docker-java-application`. It allows configuring several values, detailed below. 
Additionally, the main class is taken from the application plugin - the
default behavior of `com.bmuschko.docker-java-application` for identifying main classes is not used.
An application can define multiple variants which allow specifying different base images to use.

- `baseImage`
    - String
    - Defaults to `hypertrace/java:11`
- `maintainer`
    - String
    - Defaults to `Hypertrace 'https://www.hypertrace.org/'`
- `port`
    - Integer
    - No default
    - Exposed in dockerfile if set
- `adminPort`
    - Integer
    - If `port` is set, `adminPort` will default to `${port} + 1`
    - If `port` is unset, `adminPort` has no default
    - Exposed in dockerfile if set
- `serviceName`
    - String
    - Defaults to `${project.name}`
    - Added to envVars as `SERVICE_NAME`
- `healthCheck`
    - String
    - If `adminPort` is set, defaults to `-interval=2s --start-period=15s --timeout=2s CMD wget  -qO http://127.0.0.1:${adminPort}/health &> /dev/null || exit`
    - If `adminPort` is unset, `healthCheck` has no default
- `envVars`
    - Map<String, String>
    - Defaults to `{"SERVICE_NAME": "${serviceName}"}`
    - Can be appended to with `put`, or overwritten with `set`


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
      port.set(9001)
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
ENV SERVICE_NAME=example-app
```

Overriding or providing more variables:
```kotlin
hypertraceDocker {
  defaultImage {
    javaApplication {
      baseImage.set("hypertrace/java:14")
      port.set(9001)
      adminPort.set(9003)
      envVars.put("MY_VAR", "foo")
    }
  }
}
```

Produces a diff of:
```diff
-FROM hypertrace/java:11
+FROM hypertrace/java:14
-EXPOSE 9001 9002
+EXPOSE 9001 9003
-HEALTHCHECK -interval=2s --start-period=15s --timeout=2s CMD wget -qO- http://127.0.0.1:9002/health &> /dev/null || exit 1
+HEALTHCHECK -interval=2s --start-period=15s --timeout=2s CMD wget -qO- http://127.0.0.1:9003/health &> /dev/null || exit 1
-ENV SERVICE_NAME=example-app
+ENV SERVICE_NAME=example-app MY_VAR=foo
```