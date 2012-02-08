package gw.vark;

import gw.lang.mode.GosuMode;
import gw.util.GosuExceptionUtil;
import gw.util.StreamUtil;

import java.io.IOException;
import java.net.URL;

/**
 */
public class AardvarkVersionMode extends GosuMode {
  @Override
  public int getPriority() {
    return Aardvark.GOSU_MODE_PRIORITY_AARDVARK_VERSION;
  }

  @Override
  public boolean accept() {
    return _argInfo.consumeArg("--version") || _argInfo.consumeArg("-version"); 
  }

  @Override
  public int run() throws Exception {
    System.out.println(Aardvark.getVersion());
    return 0;
  }
}
