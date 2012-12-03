package gw.vark.interactive;

import gw.lang.launch.IBooleanArgKey;
import gw.lang.launch.Launch;
import gw.lang.mode.GosuMode;
import gw.vark.Aardvark;

/**
 */
public class AardvarkInteractiveMode extends GosuMode {
  public static final IBooleanArgKey ARGKEY_INTERACTIVE = Launch.factory().createArgKeyBuilder("run Aardvark in interactive mode")
          .withShortSwitch('i').withLongSwitch("interactive").withOtherSwitch("-interactive").build();

  @Override
  public int getPriority() {
    return Aardvark.GOSU_MODE_PRIORITY_AARDVARK_INTERACTIVE;
  }

  @Override
  public boolean accept() {
    return _argInfo.consumeArg(ARGKEY_INTERACTIVE);
  }

  @Override
  public int run() throws Exception {
    InteractiveShell shell = new InteractiveShell(_argInfo.getProgramSource());
    shell.run();
    return 0;
  }
}
