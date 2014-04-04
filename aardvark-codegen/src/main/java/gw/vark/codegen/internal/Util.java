package gw.vark.codegen.internal;

import java.nio.file.Path;

public class Util {

  public static String getExtension(Path file) {
    String name = file.getFileName().toString();
    int pos = name.lastIndexOf('.');
    return pos != -1 ? name.substring(pos + 1) : null;
  }

  public static String getBaseName(Path file) {
    String name = file.getFileName().toString();
    int pos = name.lastIndexOf('.');
    return pos != -1 ? name.substring(0, pos) : name;
  }
}
