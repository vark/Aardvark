package gw.vark.interactive;

import gw.lang.mode.GosuMode;
import gw.vark.Aardvark;

/**
 */
public class AardvarkInteractiveMode extends GosuMode {
  @Override
  public int getPriority() {
    return Aardvark.GOSU_MODE_PRIORITY_AARDVARK_INTERACTIVE;
  }

  @Override
  public boolean accept() {
    return _argInfo.consumeArg("-i", "--interactive", "-interactive");
  }

  @Override
  public int run() throws Exception {
    InteractiveShell shell = new InteractiveShell(_argInfo.getProgramSource());
    shell.run();
    return 0;
  }
}
