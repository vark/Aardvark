/*
 * Copyright (c) 2010 Guidewire Software, Inc.
 */

package gw.vark;

import gw.lang.reflect.IType;
import gw.lang.reflect.TypeSystem;
import gw.lang.reflect.gs.IGosuClass;
import gw.util.GosuStringUtil;
import gw.vark.testapi.AardvarkTestCase;
import gw.vark.testapi.InMemoryLogger;
import org.apache.tools.ant.launch.Locator;

import java.io.File;

/**
 */
public class AardvarkEnhancementTasksTest extends AardvarkTestCase {

  private File _testprojectDir;
  private InMemoryLogger _logger;

  public AardvarkEnhancementTasksTest() {
    super();
  }

  @Override
  protected void setUp() throws Exception {
    File classSource = Locator.getClassSource(getClass());
    File home = getHome(classSource.isDirectory() ? classSource : classSource.getParentFile()).getCanonicalFile();
    _logger = new InMemoryLogger();
    _testprojectDir = new File(home, "testproject");
  }

  public void testEnhancementContributedTaskIsPresent() {
    String output = runAardvark(0, "-p");
    IType type = TypeSystem.getByFullNameIfValid("vark.SampleVarkFileEnhancement");
    if (!type.isValid()) {
      fail("Enhancement should be valid: " + ((IGosuClass) type).getParseResultsException().getFeedback());
    }
    assertTrue("Should be able to contribute properly annotated target to project via an enhancement",
               output.contains("target-from-enhancment"));
    assertFalse("Shouldn't be in targets",
               output.contains("not-a-target-from-enhancment"));
  }

  private static File getHome(File dir) {
    if (new File(dir, "lib/gosu").exists()) {
      return dir;
    }
    return getHome(dir.getParentFile());
  }


  private String runAardvark(int expectedExitCode, String args) {
    Aardvark a = new Aardvark(_logger);
    int exitCode = a.start(("-f " + _testprojectDir.getAbsolutePath() + File.separator + "build.vark " + args).split(" "));
    assertEquals("exit code", expectedExitCode, exitCode);
    return GosuStringUtil.join(_logger.getMessages().toArray());
  }
}
