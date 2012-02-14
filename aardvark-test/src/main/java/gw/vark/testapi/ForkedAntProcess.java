package gw.vark.testapi;

import gw.util.ProcessStarter;
import gw.util.Shell;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

/**
 */
public class ForkedAntProcess extends ForkedBuildProcess<ForkedAntProcess> {

  public ForkedAntProcess(File buildFile) {
    super(buildFile);
  }

  protected boolean accept(String element) {
    return element.contains(File.separator + "org" + File.separator + "apache" + File.separator + "ant" + File.separator);
  }

  @Override
  protected String getMainClass() {
    return "org.apache.tools.ant.Main";
  }
}
