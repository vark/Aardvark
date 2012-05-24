package gw.vark;

import org.junit.Assert;
import org.junit.Test;

import java.io.PrintWriter;
import java.io.StringWriter;

/**
 * Created by IntelliJ IDEA.
 * User: bchang
 * Date: 3/20/12
 * Time: 8:24 PM
 * To change this template use File | Settings | File Templates.
 */
public class AardvarkHelpModeTest extends Assert {

  @Test
  public void printHelp() {
    StringWriter writer = new StringWriter();
    PrintWriter out = new PrintWriter(writer);
    AardvarkHelpMode.printHelp(out);
    String eol = System.getProperty("line.separator");
    assertEquals(
            "Usage:" + eol +
                    "        vark [-f FILE] [options] [targets...]" + eol +
                    "" + eol +
                    "Options:" + eol +
                    "        -f, -file FILE              load a file-based Gosu source" + eol +
                    "            -url URL                load a url-based Gosu source" + eol +
                    "            -classpath PATH         additional elements for the classpath, separated by commas" + eol +
                    "        -p, -projecthelp            show project help (e.g. targets)" + eol +
                    "            -logger LOGGERFQN       class name for a logger to use" + eol +
                    "        -q, -quiet                  run with logging in quiet mode" + eol +
                    "        -v, -verbose                run with logging in verbose mode" + eol +
                    "        -d, -debug                  run with logging in debug mode" + eol +
                    "            -verify                 verifies the Gosu source" + eol +
                    "            -version                displays the version of Aardvark" + eol +
                    "        -h, -help                   displays this command-line help" + eol +
                    "",
            writer.toString());
  }
}
