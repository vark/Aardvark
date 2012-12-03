package gw.vark.it;

import gw.util.process.OutputHandler;

import java.util.ArrayList;

/**
 * Created by IntelliJ IDEA.
 * User: bchang
 * Date: 2/13/12
 * Time: 6:14 PM
 * To change this template use File | Settings | File Templates.
 */
public class TestOutputHandler implements OutputHandler {
  ArrayList<String> _lines = new ArrayList<String>();
  String _name;
  TestOutputHandler(String name) {
    _name = name;
  }
  @Override
  public void handleLine(String line) {
    _lines.add(line);
    System.out.println(_name + ": " + line);
  }
}
