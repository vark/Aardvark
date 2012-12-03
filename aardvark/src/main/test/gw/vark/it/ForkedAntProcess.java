package gw.vark.it;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Arrays;
import java.util.List;

/**
 */
public class ForkedAntProcess extends ForkedBuildProcess<ForkedAntProcess> {

  public ForkedAntProcess(File buildFile) {
    super(ForkedAntProcess.class, buildFile);
  }

  @Override
  protected List<File> createClasspath() {
    try {
      File assemblyDir = ITUtil.getAssemblyDir();
      File libDir = new File(assemblyDir, "lib");
      File antJar = ITUtil.findFile(libDir, "ant-[\\d\\.]+\\.jar");
      File antLauncherJar = ITUtil.findFile(libDir, "ant-launcher-[\\d\\.]+\\.jar");
      return Arrays.asList(antJar, antLauncherJar);
    } catch (FileNotFoundException e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  protected String getMainClass() {
    return "org.apache.tools.ant.Main";
  }
}
