package thad.gradle.ev3;

import org.gradle.api.Project;
import org.gradle.api.Task;
import org.gradle.api.plugins.ExtensionContainer;
import org.gradle.model.ModelMap;
import org.gradle.model.RuleSource;
import org.gradle.nativeplatform.NativeBinarySpec;
import org.gradle.platform.base.BinaryTasks;


import jaci.gradle.deploy.DeployExtension;
import jaci.gradle.deploy.artifact.ArtifactsExtension;
import jaci.gradle.deploy.target.RemoteTarget;
import jaci.gradle.deploy.target.location.SshDeployLocation;

public class Ev3 extends RemoteTarget {
  public Ev3(String name, Project project) {
    super(name, project);

    this.setDirectory("/home/robot");
    this.setMaxChannels(4);
    this.setTimeout(7);
    this.setAddresses("192.168.137.3");
  }

  public void setAddresses(String... addresses) {
    this.getLocations().clear();
    for (String addr : addresses) {
      addAddress(addr);
    }
  }

  public void addAddress(String address) {
    SshDeployLocation loc = new SshDeployLocation(this);
    loc.setAddress(address);
    loc.setIpv6(false);
    loc.setUser("robot");
    loc.setPassword("maker");

    this.getLocations().add(loc);
  }

  @Override
  public String toString() {
      return "RoboRIO[" + this.getName()  + "]";
  }


}
