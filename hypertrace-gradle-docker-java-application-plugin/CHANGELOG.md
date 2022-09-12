# Changelog

## [0.2.4]

- Default image has changed from gcr.io/distroless/java:11 to gcr.io/distroless/java:11-debug

## [0.3.0]

- Added support for producing multiple variants of an application with different base images
- Added a default variant, `slim` based off `gcr.io/distroless/java:11`

## [0.3.2]

- Removed default `slim` variant

## [0.3.3]

- Support Java 8+

## [0.4.0]

- Change default image to `hypertrace/java:11`

## [0.5.0]

- Exposed for configuration:
    - `maintainer`
    - `port`
    - `adminPort`
    - `serviceName`
    - `healthCheck`
    - `envVars`

## [0.7.0]

- Remove variants
- Remove internal usage of `com.bmuschko.docker-java-application`, cleaning up ghost tasks
- Use gradle application start script to run application in docker, allowing reuse and accounting
  for env vars like JAVA_OPTS

## [0.7.1]

- Simplify start script to use provided parameters as given

## [0.8.0]

- Add COMMIT_SHA to the generated dockerfile as a build arg defaulting to unknown
- Use COMMIT_SHA build arg to set a label `commit_sha` and environment variable `COMMIT_SHA`

## [0.8.2]

- Add support for exposing multiple docker image ports via `ports` config

## [0.9.9]

- Fix bug introduced in 0.9.8 preventing startup. Docker laying improvements not yet working, so
  currently equivalent to 0.9.4