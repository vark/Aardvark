package gw.vark;

import gw.util.ProcessStarter;
import gw.util.Shell;
import gw.util.ShellProcess;
import gw.util.StreamUtil;
import gw.vark.testapi.AardvarkAssertions;
import org.apache.tools.ant.launch.Locator;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

/**
 */
public class AardvarkShellTest extends AardvarkAssertions {

  private static File _testDir;
  private static File _varkFile;
  private static File _userClass;
  private static ShellProcess _proc;
  private static long _mockFSClock = 0;

  private static final String VARK_FILE_0 = "" +
          "classpath \".\"\n" +
          "function hello() {\n" +
          "  print(\"Hello World\")\n" +
          "}\n";
  private static final String VARK_FILE_1 = "" +
          "classpath \".\"\n" +
          "uses testpackage.UserClass\n" +
          "function hello() {\n" +
          "  print(UserClass.getFoo())\n" +
          "}\n";
  private static final String USER_CLASS_0 = "" +
          "package testpackage\n" +
          "class UserClass {\n" +
          "  static function getFoo() : String {\n" +
          "    return \"Hello World 2\"\n" +
          "  }\n" +
          "}\n";
  private static final String USER_CLASS_1 = "" +
          "package testpackage\n" +
          "class UserClass {\n" +
          "  static function getFoo() : String {\n" +
          "    return \"Hello World 3\"\n" +
          "  }\n" +
          "}\n";

  @BeforeClass
  public static void setUp() throws Exception {
    File tmpDir = new File(System.getProperty("java.io.tmpdir"));
    _testDir = new File(tmpDir, AardvarkShellTest.class.getSimpleName());
    deleteRecursively(_testDir);

    _testDir.mkdir();
    _varkFile = new File(_testDir, "build.vark");
    writeToFile(_varkFile, VARK_FILE_0);

    File packageDir = new File(_testDir, "testpackage");
    packageDir.mkdir();
    _userClass = new File(packageDir, "UserClass.gs");
    writeToFile(_userClass, USER_CLASS_0);

    String javaCommand = System.getProperty("java.home") + "/bin/java";
    String classpathString = Locator.getClassSource(gw.vark.launch.Launcher.class).getPath()
            + File.pathSeparator
            + Locator.getClassSource(org.apache.tools.ant.launch.Launcher.class).getPath();
    String command = javaCommand
            + " -Daardvark.dev=true"
            + " -cp " + classpathString + " gw.vark.launch.Launcher"
            + " -f " + _varkFile
            + " -i";
    ProcessStarter processStarter = Shell.buildProcess(command);
    Process process = processStarter.start();
    _proc = new ShellProcess(process);
    readFromProcess();
  }

  @AfterClass
  public static void tearDown() throws Exception {
    writeToProcessAndRead("quit\n");
    deleteRecursively(_testDir);
  }

  @Before
  public void mockVirtualFSClock() {
    _mockFSClock += 1000;
  }

  @Test
  public void testHello() throws Exception {
    writeToFile(_varkFile, VARK_FILE_0);
    writeToFile(_userClass, USER_CLASS_0);
    String read = writeToProcessAndRead("hello\n");
    assertThat(read).contains("hello:\nHello World\n\nBUILD SUCCESSFUL\nTotal time: ");
  }

  @Test
  public void testBlankLine() throws Exception {
    writeToFile(_varkFile, VARK_FILE_0);
    writeToFile(_userClass, USER_CLASS_0);
    String read = writeToProcessAndRead("\n");
    assertThat(read).isEqualTo("\n");
  }

  @Test
  public void testRefreshOnVarkFile() throws Exception {
    writeToFile(_varkFile, VARK_FILE_1);
    writeToFile(_userClass, USER_CLASS_0);
    String read = writeToProcessAndRead("hello\n");
    assertThat(read).contains("hello:\nHello World 2\n\nBUILD SUCCESSFUL\nTotal time: ");
  }

  @Test
  public void testRefreshOnUserClass() throws Exception {
    writeToFile(_varkFile, VARK_FILE_1);
    writeToFile(_userClass, USER_CLASS_1);
    String read = writeToProcessAndRead("hello\n");
    assertThat(read).contains("hello:\nHello World 3\n\nBUILD SUCCESSFUL\nTotal time: ");
  }

  private static String writeToProcessAndRead(String write) {
    writeToProcess(write);
    return readFromProcess();
  }

  private static void writeToProcess(String write) {
    _proc.write(write);
    System.out.print(write);
  }

  private static String readFromProcess() {
    String read = _proc.readUntil("vark> ", false);
    System.out.print(read);
    return read;
  }

  private static void writeToFile(File file, String content) throws IOException {
    BufferedWriter writer = null;
    try {
      writer = new BufferedWriter(new FileWriter(file));
      writer.write(content);
    } finally {
      try {
        StreamUtil.close(writer);
      }
      catch (IOException closeException) {
        closeException.printStackTrace();
      }
    }
    file.setLastModified(_mockFSClock);
  }

  private static void deleteRecursively(File file) {
    if (file.isDirectory()) {
      for (File child : file.listFiles()) {
        deleteRecursively(child);
      }
    }
    file.delete();
  }
}
