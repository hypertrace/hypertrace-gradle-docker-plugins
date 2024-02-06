import org.hypertrace.gradle.publishing.HypertracePublishExtension
import org.hypertrace.gradle.publishing.License

plugins {
  id("org.hypertrace.ci-utils-plugin") version "0.3.0"
  id("org.hypertrace.publish-plugin") version "1.0.4" apply false
  id("org.hypertrace.repository-plugin") version "0.4.0"
  id("org.hypertrace.code-style-plugin") version "1.2.0" apply false
}

subprojects {
  apply(plugin = "org.hypertrace.code-style-plugin")
  group = "org.hypertrace.gradle.docker"
  pluginManager.withPlugin("org.hypertrace.publish-plugin") {
    configure<HypertracePublishExtension> {
      license.set(License.APACHE_2_0)
    }
  }
  repositories {
    gradlePluginPortal()
  }
}
