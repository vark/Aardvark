package gw.vark;

import gw.lang.launch.IArgKey;
import gw.lang.launch.IArgKeyList;
import gw.lang.launch.Launch;
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
    return _argInfo.consumeArg(AardvarkOptions.ARGKEY_HELP);
  }

  @Override
  public int run() throws Exception {
    printHelp(new PrintWriter(System.out));
    return 0;
  }

  static void printHelp(PrintWriter out) {
    out.println("Usage:");
    out.println("        vark [-f FILE] [options] [targets...]");
    out.println();
    out.println("Options:");

    IArgKeyList keys = Launch.factory().createArgKeyList();
    for (IArgKey key : AardvarkOptions.getArgKeys()) {
      keys.register(key);
    }
    keys.printHelp(out);
  }
}
