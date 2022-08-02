package io.jenkins.plugins;

import hudson.Extension;
import hudson.FilePath;
import hudson.model.Node;
import hudson.model.Run;
import hudson.model.TaskListener;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import jenkins.model.Jenkins;
import org.apache.commons.io.FileUtils;
import org.jenkinsci.Symbol;
import org.jenkinsci.plugins.workflow.libs.LibraryRetriever;
import org.jenkinsci.plugins.workflow.libs.LibraryRetrieverDescriptor;
import org.kohsuke.stapler.DataBoundConstructor;

public class LocalRetriever extends LibraryRetriever {
  private final Node jenkins;
  private final String localLibPath;

  @DataBoundConstructor
  public LocalRetriever() {
    String localLib = "jenkins-scripts";
    this.jenkins = Jenkins.get();
    this.localLibPath = System.getenv("JENKINS_HOME") + "/" + localLib;
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
          "Copy local libs from: %s into target dir: %s", this.localLibPath, target.getRemote()));

    this.copyDirectories(this.localLibPath, target.getRemote(), listener);

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
      return "local";
    }
  }

  private void copyDirectories(String sourceDir, String destDir, TaskListener listener)
      throws IOException {

    if (Files.isDirectory(Paths.get(sourceDir + "/src"))) {
      File src = new File(sourceDir + "/src");

      listener.getLogger().println(
          String.format("Copying %s", src));

      FileUtils.copyDirectory(src, new File(destDir + "/src"));

    } else if (Files.isDirectory(Paths.get(destDir + "/vars"))) {
      File vars = new File(sourceDir + "/vars");

      listener.getLogger().println(
          String.format("Copying %s", vars));

      FileUtils.copyDirectory(vars, new File(destDir + "/vars"));

    } else {
      throw new IOException("no src/ or vars/ dir present in local lib");

    }
  }
}
