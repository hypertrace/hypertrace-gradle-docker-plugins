package org.hypertrace.gradle.docker;

import com.bmuschko.gradle.docker.DockerRemoteApiPlugin;
import com.bmuschko.gradle.docker.tasks.image.DockerPushImage;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.Task;
import org.gradle.api.tasks.TaskProvider;

public class DockerPublishPlugin implements Plugin<Project> {
  public static final String PUSH_IMAGE_LIFECYCLE_TASK_NAME = "dockerPushImages";
  public static final String TASK_GROUP = "docker";

  @Override
  public void apply(Project target) {
    target.getPluginManager().apply(DockerRemoteApiPlugin.class);
    target.getPluginManager().apply(DockerPlugin.class);
    this.addDockerPushTasks(target);
  }

  private void addDockerPushTasks(Project project) {
    TaskProvider<Task> pushLifecycleTask =
        project
            .getTasks()
            .register(
                PUSH_IMAGE_LIFECYCLE_TASK_NAME,
                task -> {
                  task.setGroup(DockerPublishPlugin.TASK_GROUP);
                  task.setDescription(
                      "Pushes all tags for all registered docker images for this project");
                });

    this.getHypertraceDockerExtension(project)
        .images
        .all(image -> this.addDockerPushTasksForImage(project, image, pushLifecycleTask));
  }

  private void addDockerPushTasksForImage(
      Project project, DockerImage image, TaskProvider<Task> pushLifecycleTask) {
    this.getHypertraceDockerExtension(project)
        .tags
        .all(tag -> this.addDockerPushTaskForImageAndTag(project, image, tag, pushLifecycleTask));
  }

  private void addDockerPushTaskForImageAndTag(
      Project project, DockerImage image, DockerTag tag, TaskProvider<Task> pushLifecycleTask) {
    TaskProvider<DockerPushImage> imagePushTask =
        project
            .getTasks()
            .register(
                image.getPushTaskNameForTag(tag),
                DockerPushImage.class,
                task -> {
                  task.setGroup(DockerPublishPlugin.TASK_GROUP);
                  task.setDescription(
                      "Pushes docker image " + image.getFullImageNameWithTag(tag).get());
                  task.dependsOn(image.getBuildTaskName());
                  task.getImages().add(image.getFullImageNameWithTag(tag));
                  task.onlyIf(tag.taskOnlyIf(image));
                });
    pushLifecycleTask.configure(lifecycleTask -> lifecycleTask.dependsOn(imagePushTask));
  }

  private DockerPluginExtension getHypertraceDockerExtension(Project project) {
    return project.getExtensions().getByType(DockerPluginExtension.class);
  }
}
