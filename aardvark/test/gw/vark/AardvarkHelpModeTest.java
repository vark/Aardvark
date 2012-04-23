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
    assertEquals(
            "Usage:\n" +
                    "        vark [-f file] [options] [targets...]\n" +
                    "\n" +
                    "Options:\n" +
                    "        -f, -file FILE              load a file-based Gosu source\n" +
                    "            -url URL                load a url-based Gosu source\n" +
                    "        -e, -eval EXPR              load a Gosu expression\n" +
                    "            -classpath PATH         additional elements for the classpath, separated by commas\n" +
                    "        -p, -projecthelp            show project help (e.g. targets)\n" +
                    "            -logger LOGGERFQN       class name for a logger to use\n" +
                    "        -q, -quiet                  run with logging in quiet mode\n" +
                    "        -v, -verbose                run with logging in verbose mode\n" +
                    "        -d, -debug                  run with logging in debug mode\n" +
                    "            -verify                 verifies the Gosu source\n" +
                    "            -version                displays the version of Aardvark\n" +
                    "        -h, -help                   displays this command-line help\n" +
                    "",
            writer.toString());
  }
}
