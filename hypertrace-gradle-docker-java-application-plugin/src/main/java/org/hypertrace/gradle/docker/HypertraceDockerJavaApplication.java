package org.hypertrace.gradle.docker;

import com.bmuschko.gradle.docker.DockerJavaApplication;
import com.bmuschko.gradle.docker.tasks.image.Dockerfile;
import java.util.Collections;
import javax.inject.Inject;
import org.gradle.api.NamedDomainObjectContainer;
import org.gradle.api.model.ObjectFactory;
import org.gradle.api.plugins.JavaApplication;
import org.gradle.api.provider.MapProperty;
import org.gradle.api.provider.Property;
import org.gradle.api.provider.ProviderFactory;
import org.gradle.api.tasks.TaskProvider;

public class HypertraceDockerJavaApplication {

  public final Property<String> baseImage;
  public final Property<String> maintainer;
  public final Property<String> serviceName;
  public final Property<Integer> port;
  public final Property<Integer> adminPort;
  public final Property<String> healthCheck;
  public final MapProperty<String, String> envVars;
  public final NamedDomainObjectContainer<DockerImageVariant> variants;
  private final Property<String> mainClassName;

  @Inject
  public HypertraceDockerJavaApplication(
      ObjectFactory objectFactory, ProviderFactory providerFactory, JavaApplication application, String projectName) {
    this.baseImage = objectFactory.property(String.class)
                                  .convention("hypertrace/java:11");
    this.maintainer = objectFactory.property(String.class)
                                   .convention("Hypertrace 'https://www.hypertrace.org/'");
    this.serviceName = objectFactory.property(String.class)
                                    .convention(projectName);
    this.port = objectFactory.property(Integer.class);
    this.adminPort = objectFactory.property(Integer.class)
                                  .convention(this.port.map(port -> port + 1));
    this.envVars = objectFactory.mapProperty(String.class, String.class)
                                .value(
                                    this.serviceName.map(serviceName -> Collections.singletonMap("SERVICE_NAME", serviceName))
                                );

    this.healthCheck = objectFactory.property(String.class)
                                    .convention(this.adminPort.map(adminPort -> String.format(
                                        "HEALTHCHECK -interval=2s --start-period=15s --timeout=2s CMD wget -qO- http://127.0.0.1:%d/health &> /dev/null || exit 1", adminPort)));
    this.variants = objectFactory.domainObjectContainer(DockerImageVariant.class,
        name -> objectFactory.newInstance(DockerImageVariant.class, name, this));
    this.mainClassName =
        objectFactory
            .property(String.class)
            .convention(providerFactory.provider(application::getMainClassName));
  }

  void configureDockerJavaApplication(DockerJavaApplication dockerJavaApplication, TaskProvider<Dockerfile> dockerfileTaskProvider) {
    dockerJavaApplication.getBaseImage()
                         .set(this.baseImage);
    dockerJavaApplication.getMainClassName()
                         .set(this.mainClassName);
    dockerJavaApplication.getMaintainer()
                         .set(this.maintainer);
    dockerJavaApplication.getPorts()
                         .empty()
                         .add(this.port);
    dockerJavaApplication.getPorts()
                         .add(this.adminPort);
    dockerfileTaskProvider.configure(dockerfile -> {
      dockerfile.instruction(this.healthCheck);
      dockerfile.environmentVariable(this.envVars);
    });
  }
}
