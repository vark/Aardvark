package gw.vark.it;

import gw.xml.simple.SimpleXmlNode;

import java.io.File;
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

  private static String _globalVersion = null;
  private static File _assemblyDir = null;

  public static File getAssemblyDir() {
    if (_assemblyDir == null) {
      init();
    }
    return _assemblyDir;
  }

  public static String getGlobalVersion() {
    if (_assemblyDir == null) {
      init();
    }
    return _globalVersion;
  }

  private static void init() {
    String globalVersion;
    File assemblyDir;
    try {
      URL location = ITUtil.class.getProtectionDomain().getCodeSource().getLocation();
      File pomRoot = findPomRoot(new File(location.toURI()));
      globalVersion = getLocalVersion(pomRoot);
      File targetDir = new File(pomRoot, "target");
      if (!targetDir.exists()) {
        throw new IllegalStateException("probably haven't run 'package' yet");
      }
      assemblyDir = new File(new File(targetDir, "aardvark-" + globalVersion + "-full"), "aardvark-" + globalVersion);
    } catch (URISyntaxException e) {
      throw new RuntimeException(e);
    }
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
