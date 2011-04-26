package gw.vark;

import gw.util.ProcessStarter;
import gw.util.Shell;
import gw.util.ShellProcess;
import gw.util.StreamUtil;
import gw.vark.testapi.AardvarkTestCase;
import org.apache.tools.ant.launch.Locator;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

/**
 */
public class AardvarkShellTest extends AardvarkTestCase {

  private File _testDir;
  private ShellProcess _proc;

  @Override
  public void setUp() throws Exception {
    File tmpDir = new File(System.getProperty("java.io.tmpdir"));
    _testDir = new File(tmpDir, getClass().getSimpleName());
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

  @Override
  public void tearDown() throws Exception {
    writeToProcessAndRead("quit\n");
    deleteRecursively(_testDir);
  }

  public void testHello() {
    String read = writeToProcessAndRead("hello\n");
    assertThat(read).contains("hello:\nHello World\n");
  }

  private String writeToProcessAndRead(String write) {
    writeToProcess(write);
    return readFromProcess();
  }

  private void writeToProcess(String write) {
    _proc.write(write);
    System.out.print(write);
  }

  private String readFromProcess() {
    String read = _proc.readUntil("vark> ", true);
    System.out.print(read);
    return read;
  }

  private void writeToFile(File file, String content) throws IOException {
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
