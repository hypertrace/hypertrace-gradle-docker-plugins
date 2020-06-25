package org.hypertrace.gradle.docker;

import com.bmuschko.gradle.docker.DockerJavaApplication;
import javax.inject.Inject;
import org.gradle.api.model.ObjectFactory;
import org.gradle.api.plugins.JavaApplication;
import org.gradle.api.provider.Property;
import org.gradle.api.provider.ProviderFactory;

public class HypertraceDockerJavaApplication {

  public final Property<String> baseImage;
  private final Property<String> mainClassName;

  @Inject
  public HypertraceDockerJavaApplication(
      ObjectFactory objectFactory, ProviderFactory providerFactory, JavaApplication application) {
    this.baseImage = objectFactory.property(String.class).convention("gcr.io/distroless/java:11");
    this.mainClassName =
        objectFactory
            .property(String.class)
            .convention(providerFactory.provider(application::getMainClassName));
  }

  void configureDockerJavaApplication(DockerJavaApplication dockerJavaApplication) {
    dockerJavaApplication.getBaseImage().set(this.baseImage);
    dockerJavaApplication.getMainClassName().set(this.mainClassName);
  }
}
