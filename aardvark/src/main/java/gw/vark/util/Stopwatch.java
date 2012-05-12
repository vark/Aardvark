package gw.vark.util;

import gw.vark.Aardvark;
import org.apache.tools.ant.BuildLogger;
import org.apache.tools.ant.Project;

/**
 * Created by IntelliJ IDEA.
 * User: bchang
 * Date: 2/8/12
 * Time: 4:16 PM
 * To change this template use File | Settings | File Templates.
 */
public class Stopwatch {

  private final String _name;
  private long _start = Long.MIN_VALUE;
  private long _total;

  public Stopwatch() {
    this("Stopwatch");
  }

  public Stopwatch(String name) {
    _name = name;
  }

  public void start() {
    _start = System.nanoTime();
  }

  public void stop() {
    _total += calcElapsed();
    _start = Long.MIN_VALUE;
  }

  public void reset() {
    if (isRunning()) {
      start();
    }
    _total = 0;
  }

  public long getElapsedInNS() {
    long total = _total;
    if (isRunning()) {
      total += calcElapsed();
    }
    return total;
  }

  public long getElapsedInMS() {
    return getElapsedInNS() / 1000 / 1000;
  }

  public long getElapsedInSeconds() {
    return getElapsedInMS() / 1000;
  }

  public long getElapsedInMinutes() {
    return getElapsedInMS() / 60;
  }

  public void print() {
    Aardvark.getProject().log(_name + ": " + getElapsedInMS() + " ms");
  }

  private boolean isRunning() {
    return _start != Long.MIN_VALUE;
  }

  private long calcElapsed() {
    return System.nanoTime() - _start;
  }
}
