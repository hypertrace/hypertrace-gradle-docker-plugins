import org.hypertrace.gradle.publishing.HypertracePublishExtension
import org.hypertrace.gradle.publishing.License

plugins {
  id("org.hypertrace.ci-utils-plugin") version "0.2.0"
  id("org.hypertrace.publish-plugin") version "1.0.2" apply false
  id("org.hypertrace.repository-plugin") version "0.4.0"
}

subprojects {
  group = "org.hypertrace.gradle.docker"
  pluginManager.withPlugin("org.hypertrace.publish-plugin") {
    configure<HypertracePublishExtension> {
      license.set(License.APACHE_2_0)
    }
  }
}
