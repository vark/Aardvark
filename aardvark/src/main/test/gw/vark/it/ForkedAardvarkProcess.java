package gw.vark.it;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 */
public class ForkedAardvarkProcess extends ForkedBuildProcess<ForkedAardvarkProcess> {

  public ForkedAardvarkProcess(File varkFile) {
    super(varkFile);
  }

  @Override
  protected List<File> createClasspath() {
    try {
      File assemblyDir = ITUtil.getAssemblyDir();
      File libDir = new File(assemblyDir, "lib");
      File[] launcherJar = ITUtil.findFiles(libDir, "gosu-launcher-.+\\.jar");
      return Arrays.asList(launcherJar);
    } catch (FileNotFoundException e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  protected String getMainClass() {
    return "gw.lang.launch.impl.GosuLauncher";
  }
}
