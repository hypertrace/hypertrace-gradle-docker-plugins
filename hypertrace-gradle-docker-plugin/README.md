# Hypertrace Docker Plugin
###### org.hypertrace.docker-plugin

### Purpose
This plugin configures the target project to communicate with docker, and build docker images based
on one or more Dockerfiles. 

Each registered image will be tagged with each registered tag, unless configured otherwise.
Build tasks will be created for each image, and a lifecycle tasks that builds all image is available as `dockerBuildImages`.
Images and tags can not be deleted once registered, only disabled (similar to tasks). The supporting tasks
will follow this same behavior, always existing, but enabled based on the rules for its images and/or tags.

### Images
Each image can be configured with:
 - Name (required): used as publication name
 - Image name: Actual name to for built image. Defaults to name.
 - Dockerfile: Dockerfile to use. Defaults to `Dockerfile.${name}` for non-default images
 - Build Context: Directory context to build. Defaults to parent dir of Dockerfile.
 - Build Args: Map of Custom arguments (String/String) for docker build. Defaults to empty.
 - onlyIf: Disable image based on certain rules
 - dependsOn: Dependency tasks to run before building the image.
 - tagNameTransform: How to map a tag string from a tag object. Defaults to tag.getName()
    
By default, there is a single image registered, looking for a file named `Dockerfile` with an image name `${project.name}`.
This image is accessible in the DSL via `defaultImage()`

Images are registered in the build DSL:

```kotlin
hypertraceDocker {
  // Create an image named "test" with a dockerfile at ${projectDir}/hidden/special ,
  // a build context of ${projectDir}/hidden, and one build arg.
  image("test") {
    dockerFile.set(file("hidden/special"))
    buildArgs.put("key", "value")
  }
  defaultImage {
    onlyIf { false } // Disable default image
  }
}
```
### Tags
Each tag can be configured with:
- Name (required): The actual value of the tag
- onlyIf: Disable tag based on rules. This spec will be run for each image, providing it as an argument

By default, two tags are registered:
- `${project.version()}`
- `latest` - onlyIf `hypertraceDocker.tagLatest` - see below for more information

Tags are registered in the build DSL:
```kotlin

hypertraceDocker {
  // Disable default version and instead use date to tag.
  tag(project.version.toString()) {
    onlyIf { false }
  }
  tag(Date().toString())
}
```

### Other configuration
The following other values can also be set via the `hypertraceDocker` dsl. 

- `registryCredentials` - user, password and registry used for pulling and pushing images.
 By default, these will point to dockerhub with no user or password. If the DOCKER_REGISTRY,
 DOCKER_USERNAME or DOCKER_PASSWORD environment variables are set, they will be used as the
 defaullt, overridable by dsl.

- `namespace` - this will prefix any pushed image name, and defaults to `hypertrace`.
 
- `tagLatest` - if true, adds a latest tag to each image in addition to other tags (defaults to `true`)

The full image name is calculated as `[{customRegistry}/][{namespace}/]imageName:tag`

### Full example
Project name: `example-dockerfile`  
Project version: `0.3.0`

```kotlin
hypertraceDocker {
  image("secondImage")
  tag("tagForSecondImageOnly") {
    onlyIf { candidateImage ->
      candidateImage.name == "secondImage"
    }
  }
}
```

Output of `gradle tasks`
```
Docker tasks
------------
dockerBuildImage_default - Builds docker image hypertrace/example-dockerfile
dockerBuildImage_secondImage - Builds docker image hypertrace/secondImage
dockerBuildImages - Builds all registered docker images for this project
```

Note the following behavior. The plugin will attempt to use the docker file `${projectDir}/Dockerfile.secondImage`.
`secondImage` will be tagged with `${project.version}` and `tagForSecondImageOnly`.
It will also use the default image, `${project.name}` based on the docker file `${projectDir}/DockerFile`,
which will be tagged with `${project.version}` only.

