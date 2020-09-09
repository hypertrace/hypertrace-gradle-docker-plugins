package org.hypertrace.gradle.docker;

import static java.util.Collections.singletonMap;
import static java.util.Objects.requireNonNull;
import static org.gradle.api.plugins.JavaPlugin.CLASSES_TASK_NAME;
import static org.gradle.api.plugins.JavaPlugin.RUNTIME_CLASSPATH_CONFIGURATION_NAME;

import com.bmuschko.gradle.docker.tasks.image.Dockerfile;
import com.bmuschko.gradle.docker.tasks.image.Dockerfile.CopyFile;
import com.bmuschko.gradle.docker.tasks.image.Dockerfile.From;
import java.io.File;
import java.net.URL;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.file.Directory;
import org.gradle.api.file.FileCollection;
import org.gradle.api.plugins.ApplicationPlugin;
import org.gradle.api.plugins.JavaApplication;
import org.gradle.api.plugins.JavaPluginConvention;
import org.gradle.api.provider.Provider;
import org.gradle.api.resources.TextResource;
import org.gradle.api.tasks.SourceSet;
import org.gradle.api.tasks.SourceSetOutput;
import org.gradle.api.tasks.Sync;
import org.gradle.api.tasks.TaskProvider;
import org.gradle.api.tasks.application.CreateStartScripts;
import org.gradle.jvm.application.scripts.TemplateBasedScriptGenerator;

public class HypertraceDockerJavaApplicationPlugin implements Plugin<Project> {
  public static final String EXTENSION_NAME = "javaApplication";
  public static final String DOCKERFILE_TASK_NAME = "generateJavaApplicationDockerfile";
  public static final String DOCKER_START_SCRIPT_TASK_NAME = "generateJavaApplicationDockerStartScript";
  public static final String SYNC_BUILD_CONTEXT_TASK_NAME = "syncJavaApplicationDockerContext";
  private static final String DOCKER_BUILD_CONTEXT_LIBS_DIR = "libs";
  private static final String DOCKER_BUILD_CONTEXT_CLASSES_DIR = "classes";
  private static final String DOCKER_BUILD_CONTEXT_RESOURCES_DIR = "resources";
  private static final String DOCKER_BUILD_CONTEXT_SCRIPTS_DIR = "scripts";

  @Override
  public void apply(Project target) {
    target.getPluginManager()
          .apply(ApplicationPlugin.class);
    target.getPluginManager()
          .apply(DockerPlugin.class);
    this.addApplicationExtension(target);
    this.createDockerfileTask(target, this.getHypertraceDockerApplicationExtension(target));
    this.createDockerStartScriptTask(target);
    this.createContextSyncTask(target);
    this.updateDefaultPublication(target);
  }

  private void addApplicationExtension(Project project) {
    this.getHypertraceDockerExtension(project)
        .getExtensions()
        .create(EXTENSION_NAME, HypertraceDockerJavaApplication.class, project.getName());
  }

  private void updateDefaultPublication(Project project) {
    this.getHypertraceDockerExtension(project)
        .defaultImage(
            image -> {
              TaskProvider<Dockerfile> dockerfileTaskProvider = this.getDockerfileTask(project);
              image.dependsOn(dockerfileTaskProvider);
              image.dockerFile.set(dockerfileTaskProvider.flatMap(Dockerfile::getDestFile));
            });
  }

  private DockerPluginExtension getHypertraceDockerExtension(Project project) {
    return project.getExtensions()
                  .getByType(DockerPluginExtension.class);
  }

  private HypertraceDockerJavaApplication getHypertraceDockerApplicationExtension(Project project) {
    return this.getHypertraceDockerExtension(project)
               .getExtensions()
               .getByType(HypertraceDockerJavaApplication.class);
  }

  private TaskProvider<CreateStartScripts> getStartScriptTask(Project project) {
    return project.getTasks()
                  .named(DOCKER_START_SCRIPT_TASK_NAME, CreateStartScripts.class);
  }

  private TaskProvider<Dockerfile> getDockerfileTask(Project project) {
    return project.getTasks()
                  .named(DOCKERFILE_TASK_NAME, Dockerfile.class);
  }

  void createDockerfileTask(Project project, HypertraceDockerJavaApplication javaApplication) {
    project.getTasks()
           .register(DOCKERFILE_TASK_NAME, Dockerfile.class, dockerfile -> {
             Provider<String> relativeScriptPath = dockerfile.getDestDir()
                                                             .map(Directory::getAsFile)
                                                             .map(File::toPath)
                                                             .flatMap(contextPath -> this.getStartScriptTask(project)
                                                                                         .map(CreateStartScripts::getUnixScript)
                                                                                         .map(File::toPath)
                                                                                         .map(contextPath::relativize)
                                                                                         .map(Path::toString));
             dockerfile.dependsOn(this.getStartScriptTask(project), SYNC_BUILD_CONTEXT_TASK_NAME);
             dockerfile.setGroup(DockerPlugin.TASK_GROUP);
             dockerfile.setDescription("Creates a Dockerfile for the java application");
             dockerfile.from(javaApplication.baseImage.map(From::new));
             dockerfile.label(javaApplication.maintainer.map(maintainer -> singletonMap("maintainer", maintainer)));
             dockerfile.workingDir("/app");
             dockerfile.copyFile(relativeScriptPath.map(relativePath -> new CopyFile(relativePath, "run")));
             dockerfile.copyFile(provideIfDirectoryExists(dockerfile.getDestDir()
                                                                    .map(dir -> dir.dir(DOCKER_BUILD_CONTEXT_LIBS_DIR)))
                 .map(unused -> new CopyFile(DOCKER_BUILD_CONTEXT_LIBS_DIR, DOCKER_BUILD_CONTEXT_LIBS_DIR)));
             dockerfile.copyFile(provideIfDirectoryExists(dockerfile.getDestDir()
                                                                    .map(dir -> dir.dir(DOCKER_BUILD_CONTEXT_RESOURCES_DIR)))
                 .map(unused -> new CopyFile(DOCKER_BUILD_CONTEXT_RESOURCES_DIR, DOCKER_BUILD_CONTEXT_RESOURCES_DIR)));
             dockerfile.copyFile(new CopyFile(DOCKER_BUILD_CONTEXT_CLASSES_DIR, DOCKER_BUILD_CONTEXT_CLASSES_DIR));
             dockerfile.instruction(javaApplication.healthCheck);
             dockerfile.environmentVariable(javaApplication.envVars);
             dockerfile.entryPoint("./run");
             dockerfile.exposePort(project.provider(() -> {
               List<Integer> ports = new ArrayList<>();
               if (javaApplication.port.isPresent()) {
                 ports.add(javaApplication.port.get());
               }
               if (javaApplication.adminPort.isPresent()) {
                 ports.add(javaApplication.adminPort.get());
               }
               return ports;
             }));
           });
  }

  private void createDockerStartScriptTask(Project project) {
    project.getTasks()
           .register(DOCKER_START_SCRIPT_TASK_NAME, CreateStartScripts.class, startScript -> {
             ((TemplateBasedScriptGenerator) startScript.getUnixStartScriptGenerator()).setTemplate(this.getStartScriptTemplate(project));
             startScript.setGroup(DockerPlugin.TASK_GROUP);
             startScript.setDescription("Creates a startup script for use by the docker container");
             startScript.mustRunAfter(SYNC_BUILD_CONTEXT_TASK_NAME);

             Directory contextDir = project.getLayout()
                                           .getBuildDirectory()
                                           .dir("docker")
                                           .get();
             JavaApplication javaApplication = project.getExtensions()
                                                      .getByType(JavaApplication.class);
             startScript.getMainClass()
                        .set(javaApplication.getMainClass());
             startScript.setApplicationName(javaApplication.getApplicationName());
             startScript.setOutputDir(contextDir.file(DOCKER_BUILD_CONTEXT_SCRIPTS_DIR)
                                                .getAsFile());
             startScript.setDefaultJvmOpts(javaApplication.getApplicationDefaultJvmArgs());
           });
  }

  private void createContextSyncTask(Project project) {
    project.getTasks()
           .register(SYNC_BUILD_CONTEXT_TASK_NAME, Sync.class, sync -> {
             sync.dependsOn(CLASSES_TASK_NAME);
             sync.setGroup(DockerPlugin.TASK_GROUP);
             sync.setDescription("Copies required artifacts into the build context directory");
             sync.into(this.getDockerfileTask(project)
                           .map(Dockerfile::getDestDir)
                           .get());
             sync.with(project.copySpec(spec -> {
               spec.into(DOCKER_BUILD_CONTEXT_LIBS_DIR, childSpec -> childSpec.from(getRuntimeClasspath(project)));
               spec.into(DOCKER_BUILD_CONTEXT_CLASSES_DIR, childSpec -> childSpec.from(mainSourceSetOutput(project).getClassesDirs()));
               spec.into(DOCKER_BUILD_CONTEXT_RESOURCES_DIR, childSpec -> childSpec.from(mainSourceSetOutput(project).getResourcesDir()));
             }));
           });
  }

  private FileCollection getRuntimeClasspath(Project project) {
    return project.getConfigurations()
                  .getByName(RUNTIME_CLASSPATH_CONFIGURATION_NAME);
  }

  private SourceSetOutput mainSourceSetOutput(Project project) {
    return project.getConvention()
                  .getPlugin(JavaPluginConvention.class)
                  .getSourceSets()
                  .getByName(SourceSet.MAIN_SOURCE_SET_NAME)
                  .getOutput();
  }

  private Provider<File> provideIfDirectoryExists(Provider<Directory> directoryProvider) {
    return directoryProvider.map(directory -> directory.getAsFile()
                                                       .isDirectory() ? directory.getAsFile() : null);
  }

  private TextResource getStartScriptTemplate(Project project) {
    URL resourceUrl = HypertraceDockerJavaApplicationPlugin.class.getClassLoader()
                                                                 .getResource("application-start-script.template.sh");
    return project.getResources()
                  .getText()
                  .fromUri(requireNonNull(resourceUrl));
  }
}
