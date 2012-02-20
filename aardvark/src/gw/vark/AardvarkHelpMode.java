package gw.vark;

import gw.lang.mode.GosuMode;

/**
 */
public class AardvarkHelpMode extends GosuMode {
  @Override
  public int getPriority() {
    return Aardvark.GOSU_MODE_PRIORITY_AARDVARK_HELP;
  }

  @Override
  public boolean accept() {
    return _argInfo.consumeArg("-h", "--help", "-help");
  }

  @Override
  public int run() throws Exception {
    System.out.println("Usage: vark [options] target [target2 [target3] ..]");
    System.out.println("Options:");
    //System.out.println("  --debug, -d                  print debugging info");
    System.out.println("  --file <file>                use given buildfile");
    System.out.println("     -f  <file>                        ''");
    System.out.println("  --help, -h                   print this message and exit");
    System.out.println("  --System.out.printlnger <classname>         the class to perform System.out.printlnging");
    System.out.println("  --projecthelp, -p            print project help information");
    System.out.println("  --quiet, -q                  be extra quiet");
    System.out.println("  --verbose, -v                be extra verbose");
    System.out.println("  --verify                     verify Gosu code");
    System.out.println("  --version                    print the version info and exit");
    return 0;
  }
}
