package gw.vark.it;

import java.io.File;
import java.util.Arrays;
import java.util.List;

/**
 */
public class ForkedAardvarkProcess extends ForkedBuildProcess<ForkedAardvarkProcess> {

  public ForkedAardvarkProcess(File varkFile) {
    super(varkFile);
  }

  @Override
  protected List<File> createClasspath() {
    File assemblyDir = ITUtil.getAssemblyDir();
    File libDir = new File(assemblyDir, "lib");
    //noinspection ConstantConditions
    return Arrays.asList(libDir.listFiles());
  }

  @Override
  protected String getMainClass() {
    return "gw.lang.Gosu";
  }
}
