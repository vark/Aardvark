package gw.vark;

import gw.lang.Gosu;
import gw.lang.launch.ArgInfo;
import gw.lang.launch.ArgKey;
import gw.lang.launch.ArgKeys;
import gw.lang.mode.GosuMode;

import java.io.PrintWriter;

/**
 */
public class AardvarkHelpMode extends GosuMode {
  @Override
  public int getPriority() {
    return Aardvark.GOSU_MODE_PRIORITY_AARDVARK_HELP;
  }

  @Override
  public boolean accept() {
    return _argInfo.consumeArg(Gosu.ARGKEY_HELP);
  }

  @Override
  public int run() throws Exception {
    printHelp(new PrintWriter(System.out));
    return 0;
  }

  static void printHelp(PrintWriter out) {
    out.println("Usage:");
    out.println("        vark [-f file] [options] [targets...]");
    out.println();
    out.println("Options:");

    ArgKeys keys = new ArgKeys();
    keys.register(ArgInfo.getArgKeys());
    keys.register(Gosu.getArgKeys());
    keys.register(AardvarkOptions.getArgKeys());
    keys.moveKey(AardvarkOptions.ARGKEY_PROJECTHELP, Gosu.ARGKEY_VERIFY);
    keys.moveKey(AardvarkOptions.ARGKEY_LOGGER, Gosu.ARGKEY_VERIFY);
    keys.moveKey(AardvarkOptions.ARGKEY_QUIET, Gosu.ARGKEY_VERIFY);
    keys.moveKey(AardvarkOptions.ARGKEY_VERBOSE, Gosu.ARGKEY_VERIFY);
    keys.moveKey(AardvarkOptions.ARGKEY_DEBUG, Gosu.ARGKEY_VERIFY);
    keys.printHelp(out);
  }
}
