package org.hypertrace.gradle.docker;

import javax.annotation.Nonnull;
import javax.inject.Inject;
import org.gradle.api.Named;
import org.gradle.api.model.ObjectFactory;
import org.gradle.api.provider.Property;

public abstract class DockerImageVariant implements Named {
  // Super barebone right now, just allowing changing of the base image. Will add functionality as needed
  private final String name;
  public final Property<String> baseImage;
  public final HypertraceDockerJavaApplication javaApplication;

  @Inject
  public DockerImageVariant(String name, HypertraceDockerJavaApplication javaApplication, ObjectFactory objectFactory) {
    this.name = name;
    this.javaApplication = javaApplication;
    this.baseImage = objectFactory.property(String.class);
  }

  @Override
  @Nonnull
  public String getName() {
    return name;
  }
}
