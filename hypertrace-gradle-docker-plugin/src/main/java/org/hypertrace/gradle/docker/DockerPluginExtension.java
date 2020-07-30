package org.hypertrace.gradle.docker;

import com.bmuschko.gradle.docker.DockerRegistryCredentials;
import javax.inject.Inject;
import org.gradle.api.Action;
import org.gradle.api.NamedDomainObjectContainer;
import org.gradle.api.NamedDomainObjectSet;
import org.gradle.api.model.ObjectFactory;
import org.gradle.api.plugins.ExtensionAware;
import org.gradle.api.provider.Property;

public abstract class DockerPluginExtension implements ExtensionAware {
  private static final String DEFAULT_IMAGE_NAME = "default";
  private static final String DEFAULT_NAMESPACE = "hypertrace";
  public final NamedDomainObjectContainer<DockerTag> tags;
  public final NamedDomainObjectContainer<DockerImage> images;
  public final DockerRegistryCredentials registryCredentials;
  public final Property<String> namespace;
  public final Property<Boolean> tagLatest;

  @Inject
  public DockerPluginExtension(ObjectFactory objectFactory, DockerRegistryCredentials registryCredentials) {
    this.namespace = objectFactory.property(String.class)
            .convention(DEFAULT_NAMESPACE);
    this.tags = objectFactory.domainObjectContainer(DockerTag.class);
    this.tagLatest = objectFactory.property(Boolean.class).convention(true);
    this.registryCredentials = registryCredentials;
    this.images = objectFactory.domainObjectContainer(DockerImage.class, name -> objectFactory.newInstance(DockerImage.class, name, this));
  }

  public DockerTag tag(String tagName) {
    return tag(tagName, null);
  }

  public DockerTag tag(String tagName, Action<DockerTag> tagAction) {
    DockerTag tag = this.tags.maybeCreate(tagName);
    if (tagAction != null) {
      tagAction.execute(tag);
    }
    return tag;
  }

  public DockerImage image(String imageName) {
    return image(imageName, null);
  }

  public DockerImage image(String imageName, Action<DockerImage> imageAction) {
    DockerImage image = this.images.maybeCreate(imageName);
    if (imageAction != null) {
      imageAction.execute(image);
    }
    return image;
  }

  public void registryCredentials(Action<? super DockerRegistryCredentials> action) {
    action.execute(this.registryCredentials);
  }

  NamedDomainObjectSet<DockerTag> enabledTagsForImage(DockerImage image) {
    return tags.matching(tag -> tag.isEnabledForImage(image));
  }

  public DockerImage defaultImage() {
    return defaultImage(null);
  }

  public DockerImage defaultImage(Action<DockerImage> imageAction) {
    return image(DEFAULT_IMAGE_NAME, imageAction);
  }
}
