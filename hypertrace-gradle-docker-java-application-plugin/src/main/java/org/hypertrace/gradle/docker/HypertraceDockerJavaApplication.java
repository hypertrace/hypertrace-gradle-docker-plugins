package org.hypertrace.gradle.docker;

import java.util.Collections;
import javax.inject.Inject;

import org.gradle.api.JavaVersion;
import org.gradle.api.artifacts.ResolvedArtifact;
import org.gradle.api.model.ObjectFactory;
import org.gradle.api.provider.ListProperty;
import org.gradle.api.provider.MapProperty;
import org.gradle.api.provider.Property;
import org.gradle.api.specs.Spec;

public class HypertraceDockerJavaApplication {

  public final Property<String> baseImage;
  public final Property<String> maintainer;
  public final Property<String> serviceName;
  /**
   * Replaced by {@link #ports}
   */
  @Deprecated
  public final Property<Integer> port;
  public final Property<Integer> adminPort;
  public final ListProperty<Integer> ports;
  public final Property<String> healthCheck;
  public final MapProperty<String, String> envVars;
  public Spec<ResolvedArtifact> orgLibrarySpec;
  public final Property<JavaVersion> javaVersion;

  @Inject
  public HypertraceDockerJavaApplication(
      ObjectFactory objectFactory, String projectName) {
    this.javaVersion = objectFactory.property(JavaVersion.class)
                                    .convention(JavaVersion.VERSION_11);
    this.baseImage = objectFactory.property(String.class)
                                    .convention(this.javaVersion.map(javaVersion -> "hypertrace/java:" + javaVersion.getMajorVersion()));
    this.maintainer = objectFactory.property(String.class)
                                   .convention("Hypertrace 'https://www.hypertrace.org/'");
    this.serviceName = objectFactory.property(String.class)
                                    .convention(projectName);
    this.port = objectFactory.property(Integer.class);
    this.ports = objectFactory.listProperty(Integer.class);
    this.adminPort = objectFactory.property(Integer.class)
                                  .convention(this.port.map(port -> port + 1));
    this.envVars = objectFactory.mapProperty(String.class, String.class)
                                .value(
                                    this.serviceName.map(serviceName -> Collections.singletonMap("SERVICE_NAME", serviceName))
                                );
    this.healthCheck = objectFactory.property(String.class)
                                    .convention(this.adminPort.map(adminPort -> String.format(
                                        "HEALTHCHECK --interval=2s --start-period=15s --timeout=2s CMD wget -qO- http://127.0.0.1:%d/health &> /dev/null || exit 1", adminPort)));
    this.orgLibrarySpec = this::isHypertraceLibrary;
  }

  private boolean isHypertraceLibrary(ResolvedArtifact artifact) {
    return artifact.getModuleVersion()
                   .getId()
                   .getGroup()
                   .startsWith("org.hypertrace");
  }
}
