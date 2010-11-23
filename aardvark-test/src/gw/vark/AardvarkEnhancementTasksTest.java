/*
 * Copyright (c) 2010 Guidewire Software, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
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
