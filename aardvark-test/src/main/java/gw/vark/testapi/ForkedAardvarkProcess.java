package gw.vark.testapi;

import java.io.File;

/**
 */
public class ForkedAardvarkProcess extends ForkedBuildProcess<ForkedAardvarkProcess> {

  public ForkedAardvarkProcess(File varkFile) {
    super(varkFile);
  }

  protected boolean accept(String element) {
    return !element.endsWith("rt.jar")
              && !element.contains("jre" + File.separator + "lib" + File.separator)
              && !element.endsWith("test-classes")
              && !element.endsWith("aardvark-test" + File.separator + "target" + File.separator + "classes")
              && !element.matches(".*[/\\\\]junit-[\\d\\.]+\\.jar$")
              && !element.matches(".*[/\\\\]fest-(assert|util)-[\\d\\.]+\\.jar$");
  }

  @Override
  protected String getMainClass() {
    return "gw.lang.Gosu";
  }
}
