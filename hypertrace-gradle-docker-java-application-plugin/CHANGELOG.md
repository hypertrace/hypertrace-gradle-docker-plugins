# Changelog


## [0.2.4]
### Changed
- Default image has changed from gcr.io/distroless/java:11 to gcr.io/distroless/java:11-debug

## [0.3.0]
### Changed
- Added support for producing multiple variants of an application with different base images
- Added a default variant, `slim` based off `gcr.io/distroless/java:11`

## [0.3.2]
### Changed
- Removed default `slim` variant

## [0.3.3]
### Changed
- Support Java 8+

## [0.4.0]
### Changed
- Change default image to `hypertrace/java:11`

## [0.5.0]
### Changed
- Exposed for configuration:
    - `maintainer`
    - `port`
    - `adminPort`
    - `serviceName`
    - `healthCheck`
    - `envVars`
## [0.7.0]
### Changed
- Remove variants
- Remove internal usage of `com.bmuschko.docker-java-application`, cleaning up ghost tasks
- Use gradle application start script to run application in docker, allowing reuse and accounting
  for env vars like JAVA_OPTS

## [0.7.1]
### Changed
- Simplify start script to use provided parameters as given