package org.hypertrace.gradle.docker;

import com.bmuschko.gradle.docker.DockerExtension;
import com.bmuschko.gradle.docker.DockerRemoteApiPlugin;
import com.bmuschko.gradle.docker.tasks.image.DockerBuildImage;

import java.util.Arrays;
import java.util.Optional;
import java.util.Set;
import java.util.List;
import java.util.stream.Collectors;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.Task;
import org.gradle.api.provider.Provider;
import org.gradle.api.tasks.TaskProvider;

public class DockerPlugin implements Plugin<Project> {
  public static final String BUILD_IMAGE_LIFECYCLE_TASK_NAME = "dockerBuildImages";
  public static final String EXTENSION_NAME = "hypertraceDocker";
  public static final String TASK_GROUP = "docker";
  static final String COMMIT_SHA_BUILD_ARG = "COMMIT_SHA";

  @Override
  public void apply(Project target) {
    target.getPluginManager()
          .apply(DockerRemoteApiPlugin.class);
    DockerPluginExtension extension = this.registerExtension(target);
    this.configureDockerRegistryCredentials(extension);
    this.addDockerBuildTasks(target, extension);
    this.addDefaultTags(target, extension);
    this.setDefaultImage(target, extension);
  }

  private void configureDockerRegistryCredentials(DockerPluginExtension extension) {
    extension.registryCredentials(
        credentials -> {
          // Use set because the third party plugin uses set for its defaults :(
          getEnvironmentVariable("DOCKER_REGISTRY").ifPresent(credentials.getUrl()::set);
          getEnvironmentVariable("DOCKER_USERNAME").ifPresent(credentials.getUsername()::set);
          getEnvironmentVariable("DOCKER_PASSWORD").ifPresent(credentials.getPassword()::set);
        });
  }

  private void addDockerBuildTasks(Project project, DockerPluginExtension extension) {
    TaskProvider<Task> buildLifecycleTask =
        project
            .getTasks()
            .register(
                BUILD_IMAGE_LIFECYCLE_TASK_NAME,
                task -> {
                  task.setGroup(TASK_GROUP);
                  task.setDescription("Builds all registered docker images for this project");
                });

    extension.images.all(
        image -> this.addDockerBuildTaskForImage(project, extension, image, buildLifecycleTask));
  }

  private void addDockerBuildTaskForImage(
      Project project,
      DockerPluginExtension extension,
      DockerImage image,
      TaskProvider<Task> buildLifecycleTask) {
    TaskProvider<DockerBuildImage> imageBuildTask =
        project
            .getTasks()
            .register(
                image.getBuildTaskName(),
                DockerBuildImage.class,
                task -> {
                  task.setGroup(TASK_GROUP);

                  task.setDescription("Builds docker image " + image.getFullImageNameWithoutTag()
                                                                    .get());
                  task.dependsOn(project.provider(image::getDependsOn));
                  task.getDockerFile()
                      .set(image.dockerFile);
                  task.getInputDir()
                      .set(image.buildContext);
                  task.getImages()
                      .addAll(
                          project.provider(
                              () -> this.getEnabledTagNamesProviderForImage(extension, image)));
                  task.getBuildArgs()
                      .set(image.buildArgs);
                  this.tryGetBuildSha()
                      .ifPresent(sha -> task.getBuildArgs()
                                            .put(COMMIT_SHA_BUILD_ARG, sha));
                  task.onlyIf(image.taskOnlyIf());
                });
    buildLifecycleTask.configure(lifecycleTask -> lifecycleTask.dependsOn(imageBuildTask));
  }

  private void addDefaultTags(Project project, DockerPluginExtension extension) {
    if (this.isReleaseVersion(project)) {
      extension.tag(this.getVersionString(project));
      extension.tag(DockerTag.LATEST, tag -> tag.onlyIf(unused -> extension.tagLatest.get()));
    } else {
      extension.tag(this.getBranchTag());
    }
  }

  private String getVersionString(Project project) {
    return project.getVersion()
                  .toString();
  }

  private boolean isReleaseVersion(Project project) {
    return !this.getVersionString(project)
                .contains("SNAPSHOT");
  }

  private String getBranchTag() {
    return getFirstAvailableEnvironmentVariable(Arrays.asList("CIRCLE_BRANCH", "GHA_BRANCH"))
            .map(String::trim)
            .map(branch -> branch.replaceAll("[^A-Za-z0-9\\.\\_\\-]", ""))
            .filter(branch -> !branch.isEmpty())
            .orElse("test");
    }

  private Optional<String> tryGetBuildSha() {
    return getFirstAvailableEnvironmentVariable(Arrays.asList("CIRCLE_BRANCH", "GHA_BRANCH"))
            .map(String::trim)
            .filter(sha -> !sha.isEmpty());
  }

  private DockerPluginExtension registerExtension(Project project) {
    return project
        .getExtensions()
        .create(
            EXTENSION_NAME,
            DockerPluginExtension.class,
            project.getExtensions()
                   .getByType(DockerExtension.class)
                   .getRegistryCredentials());
  }

  private Set<String> getEnabledTagNamesProviderForImage(
      DockerPluginExtension extension, DockerImage image) {
    return extension.enabledTagsForImage(image)
                    .stream()
                    .map(image::getFullImageNameWithTag)
                    .map(Provider::get)
                    .collect(Collectors.toSet());
  }

  private void setDefaultImage(Project project, DockerPluginExtension extension) {
    extension.defaultImage(
        image -> {
          image.dockerFile.convention(
              project.provider(() -> project.getLayout()
                                            .getProjectDirectory()
                                            .file("Dockerfile")));
          image.imageName.convention(project.provider(project::getName));
        });
  }

  private Optional<String> getEnvironmentVariable(String variableName) {
    return Optional.ofNullable(System.getenv()
                                     .get(variableName));
  }

  private Optional<String> getFirstAvailableEnvironmentVariable(List<String> environmentKeys) {
    return environmentKeys.stream()
            .map(this::getEnvironmentVariable)
            .filter(Optional::isPresent)
            .findFirst()
            .flatMap(i -> i);
    }

}
