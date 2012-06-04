package gw.vark;

import gw.lang.Gosu;
import gw.lang.mode.GosuMode;

/**
 */
public class AardvarkVersionMode extends GosuMode {
  @Override
  public int getPriority() {
    return Aardvark.GOSU_MODE_PRIORITY_AARDVARK_VERSION;
  }

  @Override
  public boolean accept() {
    return _argInfo.consumeArg(AardvarkOptions.ARGKEY_VERSION);
  }

  @Override
  public int run() throws Exception {
    System.out.println(Aardvark.getVersion());
    return 0;
  }
}
