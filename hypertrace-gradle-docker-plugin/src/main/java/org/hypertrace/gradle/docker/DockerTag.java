package org.hypertrace.gradle.docker;

import javax.annotation.Nonnull;
import org.gradle.api.Named;
import org.gradle.api.Task;
import org.gradle.api.specs.AndSpec;
import org.gradle.api.specs.Spec;

public class DockerTag implements Named {

  private final String name;
  private AndSpec<DockerImage> onlyIfSpec = AndSpec.empty();

  public DockerTag(String name) {
    this.name = name;
  }

  @Override
  @Nonnull
  public String getName() {
    return this.name;
  }

  String getTaskSafeName() {
    // Spaces are for readability, but also can safely be replaced. Replaces chars: / \ : < > " ? *
    // |
    return getName().replaceAll("[\\\\ \\/ \\: \\< \\> \\\" \\? \\* \\|]", "_");
  }

  public void onlyIf(Spec<DockerImage> onlyIfSpec) {
    this.onlyIfSpec = this.onlyIfSpec.and(onlyIfSpec);
  }

  boolean isEnabledForImage(DockerImage image) {
    return onlyIfSpec.isSatisfiedBy(image);
  }

  Spec<? super Task> taskOnlyIf(DockerImage image) {
    return (Spec<Task>) element -> this.isEnabledForImage(image) && image.isEnabled();
  }
}
