# Changelog

## [0.3.0]

- Added support for latest tag, which defaults to enabled
- Added support for remapping tags at an image level

## [0.3.3]

- Support Java 8+

## [0.6.0]

- Only use version tags for release versions (not containing the string `SNAPSHOT`). For all builds,
  tag using the `CIRCLE_BRANCH` env variable if defined, else `test`

## [0.6.1]

- Disregard empty values for `CIRCLE_BRANCH` and only use branch name for non-release versions

## [0.8.0]

- Make `COMMIT_SHA` available as a build arg if the SHA can be determined from the environment.
  This currently uses the variable `CIRCLE_SHA1` but further resolution may be added in the future.

## [0.8.1]

- Add support for getting tag via `GITHUB_REF` env variable as a fallback from `CIRCLE_BRANCH`

## [0.9.1]

- Add `printDockerImageWithTags` task to print all tagged images to be produced

## [0.9.2]

- Add `printDockerImageDefaultTag` task to print the default docker image tag

## [0.9.3]

- Update third party gradle docker version to use HTTP transport rather than deprecated sockets

## [0.9.4]

- Extend the support added in `0.8.0` to infer `COMMMIT_SHA` from Github actions
  environment (`GITHUB_SHA`)