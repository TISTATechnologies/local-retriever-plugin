package io.jenkins.plugins;

import hudson.Extension;
import hudson.FilePath;
import hudson.model.Node;
import hudson.model.Run;
import hudson.model.TaskListener;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import jenkins.model.Jenkins;
import org.jenkinsci.Symbol;
import org.jenkinsci.plugins.workflow.libs.LibraryRetriever;
import org.jenkinsci.plugins.workflow.libs.LibraryRetrieverDescriptor;

public class LocalRetriever extends LibraryRetriever {
  private final Node jenkins;
  private final String localLibPath;

  LocalRetriever(String localLib, Node jenkins) {
    this.jenkins = jenkins;
    this.localLibPath = System.getenv("JENKINS_HOME") + localLib;
  }

  @Override
  public void retrieve(String name, String version, FilePath target,
      Run<?, ?> run, TaskListener listener) throws Exception {

    retrieve(name, version, true, target, run, listener);
  }

  @Override
  public void retrieve(String name, String version, boolean changelog, FilePath target,
      Run<?, ?> run, TaskListener listener) throws Exception {

    listener.getLogger().println(
        String.format(
          "Copy local dir: %s into target dir: %s", this.localLibPath, target.getRemote()));
    this.copyDirectory(this.localLibPath, target.getRemote());

  }

  // ---------- DESCRIPTOR ------------ //
  @Override
  public LibraryRetrieverDescriptor getDescriptor() {
    return super.getDescriptor();
  }

  @Symbol("local")
  @Extension
  public static class DescriptorImpl extends LibraryRetrieverDescriptor {
    @Override
    public String getDisplayName() {
      return "Nexus";
    }
  }

  private void copyDirectory(String sourceDirectoryLocation, String destinationDirectoryLocation)
      throws IOException {
    Files.walk(Paths.get(sourceDirectoryLocation))
        .forEach(source -> {
          Path destination = Paths.get(destinationDirectoryLocation, source.toString()
                .substring(sourceDirectoryLocation.length()));
          try {
            Files.copy(source, destination);
          } catch (IOException e) {
            e.printStackTrace();
          }
        });
  }
}
