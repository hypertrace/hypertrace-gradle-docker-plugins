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