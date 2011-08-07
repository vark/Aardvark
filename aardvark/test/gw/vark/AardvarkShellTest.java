package gw.vark;

import gw.util.ProcessStarter;
import gw.util.Shell;
import gw.util.ShellProcess;
import gw.util.StreamUtil;
import gw.vark.testapi.AardvarkAssertions;
import org.apache.tools.ant.launch.Locator;
import org.junit.AfterClass;
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
  private static ShellProcess _proc;

  @BeforeClass
  public static void setUp() throws Exception {
    File tmpDir = new File(System.getProperty("java.io.tmpdir"));
    _testDir = new File(tmpDir, AardvarkShellTest.class.getSimpleName());
    deleteRecursively(_testDir);
    _testDir.mkdir();
    File varkFile = new File(_testDir, "build.vark");
    writeToFile(varkFile, "" +
            "function hello() {\n" +
            "  print(\"Hello World\")\n" +
            "}\n");

    String javaCommand = System.getProperty("java.home") + "/bin/java";
    String classpathString = Locator.getClassSource(gw.vark.launch.Launcher.class).getPath()
            + File.pathSeparator
            + Locator.getClassSource(org.apache.tools.ant.launch.Launcher.class).getPath();
    String command = javaCommand
            + " -Daardvark.dev=true"
            + " -cp " + classpathString + " gw.vark.launch.Launcher"
            + " -f " + varkFile
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

  @Test
  public void testHello() {
    String read = writeToProcessAndRead("hello\n");
    assertThat(read).contains("hello:\nHello World\n\nBUILD SUCCESSFUL\nTotal time: ");
  }

  @Test
  public void testBlankLine() {
    String read = writeToProcessAndRead("\n");
    assertThat(read).isEqualTo("\n");
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
