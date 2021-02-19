package org.hypertrace.gradle.docker;

import java.util.Collections;
import javax.inject.Inject;
import org.gradle.api.model.ObjectFactory;
import org.gradle.api.provider.ListProperty;
import org.gradle.api.provider.MapProperty;
import org.gradle.api.provider.Property;

public class HypertraceDockerJavaApplication {

  public final Property<String> baseImage;
  public final Property<String> maintainer;
  public final Property<String> serviceName;
  /**
   * Replaced by by {@link #ports}
   */
  @Deprecated
  public final Property<Integer> port;
  public final Property<Integer> adminPort;
  public final ListProperty<Integer> ports;
  public final Property<String> healthCheck;
  public final MapProperty<String, String> envVars;

  @Inject
  public HypertraceDockerJavaApplication(
      ObjectFactory objectFactory, String projectName) {
    this.baseImage = objectFactory.property(String.class)
                                  .convention("hypertrace/java:11");
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
  }
}
