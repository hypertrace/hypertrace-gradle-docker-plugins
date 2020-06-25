# Hypertrace Docker Publish Plugin
###### org.hypertrace.docker-publish-plugin

### Purpose
This plugin configures the target project to push docker images to a remote registry
based on the configuration of the plugin `org.hypertrace.docker-plugin`. It adds push tasks
for each image and tag pair, as registered in the `hypertraceDocker` extension, as well as a lifecycle task to push 
everything - `dockerPushImages`

### Full example
Project name: `example-dockerfile`  
Project version: `0.3.0`

```kotlin
hypertraceDocker {
  image("secondImage") {
    namespace.set("ht2")
  }

  tag("tagForSecondImageOnly") {
    onlyIf { candidateImage ->
      candidateImage.name == "secondImage"
    }
  }
}
```

Partial output of `gradle tasks`
```
Docker tasks
------------
...
dockerPushImage_default_0.3.0 - Pushes docker image hypertraceorg/example-dockerfile with tag 0.3.0
dockerPushImage_default_tagForSecondImageOnly - Pushes docker image hypertraceorg/example-dockerfile with tag tagForSecondImageOnly. Disabled.
dockerPushImage_secondImage_0.3.0 - Pushes docker image ht2/secondImage with tag 0.3.0
dockerPushImage_secondImage_tagForSecondImageOnly - Pushes docker image ht2/secondImage with tag tagForSecondImageOnly
dockerPushImages - Pushes all tags for all registered docker images for this project
```

Ultimately, the following images would be pushed by running `dockerPushImages`:

- `hypertraceorg/example-dockerfile:0.3.0`
- `ht2/secondImage:0.3.0`
- `ht2/secondImage:tagForSecondImageOnly`

If the remote reegistry is set to anything other than the default, dockerhub, it will also be added as
a prefix.