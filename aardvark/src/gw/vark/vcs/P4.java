package gw.vark.vcs;

import gw.util.GosuExceptionUtil;
import gw.util.Shell;
import gw.vark.Aardvark;
import org.apache.tools.ant.Project;

import java.io.File;
import java.io.IOException;

public class P4 {
  public static void edit(File f) {
    exec("edit", getPerforceRepresentation(f));
  }

  public static void add(File f) {
    exec("add", getPerforceRepresentation(f));
  }

  public static void delete(File f) {
    exec("delete", getPerforceRepresentation(f));
  }

  public static void revert(File f) {
    exec("revert", getPerforceRepresentation(f));
  }

  public static void revertUnchanged(File f) {
    exec("revert -a ", getPerforceRepresentation(f));
  }

  private static String getCanonicalPath(File f) {
    try {
      return f.getCanonicalPath();
    } catch (IOException e) {
      throw GosuExceptionUtil.forceThrow(e);
    }
  }

  private static String getPerforceRepresentation(File f) {
    String path = getCanonicalPath(f);
    if (f.isDirectory()) {
      path = path + File.separator + "...";
    }
    return path;
  }

  private static String exec(String cmd, String path) {
    String fullCommand = "p4 " + cmd + " " + path;

    Aardvark.getProject().log("Executing " + fullCommand, Project.MSG_VERBOSE);
    String s = Shell.buildProcess(fullCommand).includeStdErrInOutput().exec();
    return s;
  }
}
