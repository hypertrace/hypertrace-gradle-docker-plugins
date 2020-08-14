package org.hypertrace.gradle.docker;

import com.bmuschko.gradle.docker.DockerExtension;
import com.bmuschko.gradle.docker.DockerJavaApplication;
import com.bmuschko.gradle.docker.DockerJavaApplicationPlugin;
import com.bmuschko.gradle.docker.tasks.image.Dockerfile;
import org.gradle.api.GradleException;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.plugins.ApplicationPlugin;
import org.gradle.api.plugins.ExtensionAware;
import org.gradle.api.plugins.JavaApplication;
import org.gradle.api.tasks.Copy;
import org.gradle.api.tasks.TaskProvider;

public class HypertraceDockerJavaApplicationPlugin implements Plugin<Project> {
  public static final String EXTENSION_NAME = "javaApplication";

  @Override
  public void apply(Project target) {
    target.getPluginManager()
          .apply(ApplicationPlugin.class);
    target.getPluginManager()
          .apply(DockerJavaApplicationPlugin.class);
    target.getPluginManager()
          .apply(DockerPlugin.class);
    this.addApplicationExtension(target);
    this.updateDefaultPublication(target);
    this.addAnyVariants(target);
  }

  private void addApplicationExtension(Project project) {
    this.getHypertraceDockerExtension(project)
        .getExtensions()
        .create(
            EXTENSION_NAME, HypertraceDockerJavaApplication.class, this.getJavaApplication(project), project.getName())
        .configureDockerJavaApplication(this.getThirdPartyDockerJavaApp(project), this.getDockerfileTaskProvider(project));
  }

  private void updateDefaultPublication(Project project) {
    this.getHypertraceDockerExtension(project)
        .defaultImage(
            image -> {
              image.dependsOn(DockerJavaApplicationPlugin.DOCKERFILE_TASK_NAME);
              // We want to use the first half of their build chain, then take over ourselves for
              // build and push. Hide the built in ones.
              this.hideTask(
                  project,
                  DockerJavaApplicationPlugin.BUILD_IMAGE_TASK_NAME,
                  DockerJavaApplicationPlugin.BUILD_IMAGE_TASK_NAME
                      + " is not supported. To build the default image only use "
                      + image.getBuildTaskName()
                      + ", or to build all images, use "
                      + DockerPlugin.BUILD_IMAGE_LIFECYCLE_TASK_NAME);

              this.hideTask(
                  project,
                  DockerJavaApplicationPlugin.PUSH_IMAGE_TASK_NAME,
                  DockerJavaApplicationPlugin.PUSH_IMAGE_TASK_NAME
                      + " is not supported. To push this image, please apply the plugin org.hypertrace.docker-publish-plugin, and view the push tasks it provides");
              image.dockerFile.set(this.getDockerfileTaskProvider(project)
                                       .flatMap(Dockerfile::getDestFile));
            });
  }

  private TaskProvider<Dockerfile> getDockerfileTaskProvider(Project project) {
    return project
        .getTasks()
        .named(DockerJavaApplicationPlugin.DOCKERFILE_TASK_NAME, Dockerfile.class);
  }

  private void hideTask(Project project, String taskName, String onUsageMessage) {
    // "Hides" the provided task name. Gradle doesn't support removing existing tasks, so we'll
    // disable it and document.
    project
        .getTasks()
        .named(taskName)
        .configure(
            task -> {
              task.doFirst(
                  t -> {
                    throw new GradleException(onUsageMessage);
                  });
              task.setGroup(null);
              task.setDescription("Not supported");
            });
  }

  private DockerJavaApplication getThirdPartyDockerJavaApp(Project project) {
    return ((ExtensionAware) project.getExtensions()
                                    .getByType(DockerExtension.class))
        .getExtensions()
        .getByType(DockerJavaApplication.class);
  }

  private DockerPluginExtension getHypertraceDockerExtension(Project project) {
    return project.getExtensions()
                  .getByType(DockerPluginExtension.class);
  }

  private HypertraceDockerJavaApplication getHypertraceDockerApplicationExtension(Project project) {
    return this.getHypertraceDockerExtension(project)
               .getExtensions()
               .getByType(HypertraceDockerJavaApplication.class);
  }

  private JavaApplication getJavaApplication(Project project) {
    return project.getExtensions()
                  .getByType(JavaApplication.class);
  }

  private void addAnyVariants(Project project) {
    DockerImage base = this.getHypertraceDockerExtension(project)
                           .defaultImage();
    this.getHypertraceDockerApplicationExtension(project)
        .variants
        .all(variant -> this.addVariant(project, base, variant));

  }

  private void addVariant(Project project, DockerImage base, DockerImageVariant variant) {
    this.getHypertraceDockerExtension(project)
        .image(base.getName() + "-" + variant.getName(), image -> {
          TaskProvider<?> dockerFileTask = this.createModifiedDockerfileTask(project, base, variant);
          image.imageName.set(base.imageName);
          image.dockerFile.set(project.file(base.dockerFile.getAsFile()
                                                           .map(file -> file.getAbsolutePath() + "." + variant.getName())));
          image.setTagNameTransform(tag -> tag.getName() + "-" + variant.getName());
          image.dependsOn(dockerFileTask);
        });
  }

  private TaskProvider<?> createModifiedDockerfileTask(Project project, DockerImage base, DockerImageVariant variant) {
    return project.getTasks()
                  .register("createDockerfileVariant_" + variant.getName(), Copy.class, copy -> {
                    copy.dependsOn(base.getDependsOn());
                    copy.from(base.dockerFile);
                    copy.into("build/docker");
                    copy.rename(name -> name + "." + variant.getName());
                    copy.filter(line -> line.replaceAll(variant.javaApplication.baseImage.get(), variant.baseImage.get()));
                  });
  }
}
