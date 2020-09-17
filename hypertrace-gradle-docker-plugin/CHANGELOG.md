# Changelog


## [0.3.0]
### Changed
- Added support for latest tag, which defaults to enabled
- Added support for remapping tags at an image level

## [0.3.3]
### Changed
- Support Java 8+

## [0.6.0]
### Changed
- Only use version tags for release versions (not containing the string `SNAPSHOT`). For all builds,
tag using the `CIRCLE_BRANCH` env variable if defined, else `test`

## [0.6.1]
### Fixed
- Disregard empty values for `CIRCLE_BRANCH` and only use branch name for non-release versions

## [0.8.0]
### Changed
- Make `COMMIT_SHA` available as a build arg if the SHA can be determined from the environment.
This currently uses the variable `CIRCLE_SHA1` but further resolution may be added in the future.