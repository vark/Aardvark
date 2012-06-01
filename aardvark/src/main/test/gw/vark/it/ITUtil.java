package gw.vark.it;

import gw.xml.simple.SimpleXmlNode;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FilenameFilter;
import java.net.URISyntaxException;
import java.net.URL;

/**
 * Created with IntelliJ IDEA.
 * User: bchang
 * Date: 5/21/12
 * Time: 6:02 PM
 * To change this template use File | Settings | File Templates.
 */
public class ITUtil {

  private static File _projectRoot = null;
  private static String _globalVersion = null;
  private static File _assemblyDir = null;

  public static File getProjectRoot() {
    if (_projectRoot == null) {
      init();
    }
    return _projectRoot;
  }

  public static File getAssemblyDir() {
    if (_assemblyDir == null) {
      init();
    }
    return _assemblyDir;
  }

  public static String getGlobalVersion() {
    if (_globalVersion == null) {
      init();
    }
    return _globalVersion;
  }

  public static File findFile(File dir, final String pattern) throws FileNotFoundException {
    if (!dir.exists()) {
      throw new IllegalArgumentException(dir + " does not exist");
    }
    if (!dir.isDirectory()) {
      throw new IllegalArgumentException(dir + " is not a directory");
    }
    File[] found = dir.listFiles(new FilenameFilter() {
      @Override
      public boolean accept(File dir, String name) {
        return name.matches(pattern);
      }
    });
    if (found.length == 0) {
      throw new FileNotFoundException("file matching pattern \"" + pattern + "\" not found in directory " + dir);
    }
    if (found.length > 1) {
      throw new FileNotFoundException("multiple files found matching pattern \"" + pattern + "\" in directory " + dir);
    }
    return found[0];
  }

  private static void init() {
    File projectRoot;
    String globalVersion;
    File assemblyDir;
    try {
      URL location = ITUtil.class.getProtectionDomain().getCodeSource().getLocation();
      File pomRoot = findPomRoot(new File(location.toURI()));
      projectRoot = pomRoot.getParentFile();
      globalVersion = getLocalVersion(pomRoot);
      File targetDir = new File(pomRoot, "target");
      if (!targetDir.exists()) {
        throw new IllegalStateException("probably haven't run 'package' yet");
      }
      assemblyDir = new File(new File(targetDir, "aardvark-" + globalVersion + "-bin"), "aardvark-" + globalVersion);
    } catch (URISyntaxException e) {
      throw new RuntimeException(e);
    }
    _projectRoot = projectRoot;
    _globalVersion = globalVersion;
    _assemblyDir = assemblyDir;
  }

  private static File findPomRoot(File file) {
    if (file.isDirectory() && new File(file, "pom.xml").exists()) {
      return file;
    }
    if (file.getParentFile() != null) {
      return findPomRoot(file.getParentFile());
    }
    return null;
  }

  private static String getLocalVersion(File pomRoot) {
    File gosuPom = new File(pomRoot, "pom.xml");
    SimpleXmlNode gosuNode = SimpleXmlNode.parse(gosuPom);
    for (SimpleXmlNode descendant : gosuNode.getDescendents()) {
      if (descendant.getName().equals("version")) {
        return descendant.getText();
      }
    }
    return null;
  }

}
