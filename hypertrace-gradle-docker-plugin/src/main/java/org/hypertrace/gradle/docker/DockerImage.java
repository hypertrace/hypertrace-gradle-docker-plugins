package org.hypertrace.gradle.docker;

import com.bmuschko.gradle.docker.DockerRegistryCredentials;
import java.util.List;
import javax.annotation.Nonnull;
import javax.inject.Inject;
import org.gradle.api.Named;
import org.gradle.api.Task;
import org.gradle.api.Transformer;
import org.gradle.api.file.DirectoryProperty;
import org.gradle.api.file.ProjectLayout;
import org.gradle.api.file.RegularFileProperty;
import org.gradle.api.model.ObjectFactory;
import org.gradle.api.provider.ListProperty;
import org.gradle.api.provider.MapProperty;
import org.gradle.api.provider.Property;
import org.gradle.api.provider.Provider;
import org.gradle.api.specs.AndSpec;
import org.gradle.api.specs.Spec;

public abstract class DockerImage implements Named {
  private static final String BUILD_IMAGE_TASK_NAME_BASE = "dockerBuildImage";
  private static final String PUSH_IMAGE_TASK_NAME_BASE = "dockerPushImage";

  private final String name;
  private AndSpec<DockerImage> onlyIfSpec = AndSpec.empty();
  private final ListProperty<Object> dependsOn;
  private final DockerPluginExtension extension;
  public final DirectoryProperty buildContext;
  public final Property<String> imageName;
  public final RegularFileProperty dockerFile;
  public final MapProperty<String, String> buildArgs;
  public final ListProperty<String> platforms;
  private Transformer<String, DockerTag> tagNameTransform = DockerTag::getName;

  @Inject
  public DockerImage(
      String name,
      ObjectFactory objectFactory,
      ProjectLayout projectLayout,
      DockerPluginExtension extension) {
    this.name = name;
    this.extension = extension;
    this.buildArgs = objectFactory.mapProperty(String.class, String.class);
    this.imageName = objectFactory.property(String.class).convention(name);
    this.platforms =
        objectFactory.listProperty(String.class).convention(List.of("linux/amd64", "linux/arm64"));
    this.dockerFile =
        objectFactory
            .fileProperty()
            .convention(projectLayout.getProjectDirectory().file("Dockerfile." + this.getName()));

    this.buildContext =
        objectFactory
            .directoryProperty()
            .convention(
                projectLayout
                    .getProjectDirectory()
                    .dir(dockerFile.map(file -> file.getAsFile().getParent())));
    this.dependsOn = objectFactory.listProperty(Object.class);
  }

  @Override
  @Nonnull
  public String getName() {
    return name;
  }

  public void onlyIf(Spec<DockerImage> onlyIfSpec) {
    this.onlyIfSpec = this.onlyIfSpec.and(onlyIfSpec);
  }

  public void dependsOn(Object... paths) {
    this.dependsOn.addAll(paths);
  }

  public String getBuildTaskName() {
    return BUILD_IMAGE_TASK_NAME_BASE + "_" + this.getTaskSafeName();
  }

  public String getPushTaskNameForTag(DockerTag tag) {
    return PUSH_IMAGE_TASK_NAME_BASE + "_" + this.getTaskSafeName() + "_" + tag.getTaskSafeName();
  }

  public void setTagNameTransform(Transformer<String, DockerTag> tagNameTransform) {
    this.tagNameTransform = tagNameTransform;
  }

  List<Object> getDependsOn() {
    return this.dependsOn.get();
  }

  boolean isEnabled() {
    return onlyIfSpec.isSatisfiedBy(this);
  }

  Spec<? super Task> taskOnlyIf() {
    return (Spec<Task>) element -> this.isEnabled();
  }

  String getTaskSafeName() {
    // Spaces are for readability, but also can safely be replaced. Replaces chars: / \ : < > " ? *
    // |
    return getName().replaceAll("[\\\\ \\/ \\: \\< \\> \\\" \\? \\* \\|]", "_");
  }

  Provider<String> getFullImageNameWithTag(DockerTag tag) {
    return this.getFullImageNameWithoutTag()
        .map(imageName -> imageName + ":" + this.tagNameTransform.transform(tag));
  }

  Provider<String> getFullImageNameWithoutTag() {
    return this.extension
        .registryCredentials
        .getUrl()
        .map(url -> url.equals(DockerRegistryCredentials.DEFAULT_URL) ? "" : url + "/")
        .orElse("")
        .flatMap(registry -> this.getNamespacedImageName().map(imageName -> registry + imageName));
  }

  Provider<String> getNamespacedImageName() {
    return this.extension
        .namespace
        .map(namespace -> namespace.isEmpty() ? "" : namespace + "/")
        .orElse("")
        .flatMap(namespacePrefix -> imageName.map(imageName -> namespacePrefix + imageName));
  }
}
