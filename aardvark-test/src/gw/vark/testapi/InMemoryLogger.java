/*
 * Copyright (c) 2010 Guidewire Software, Inc.
 */

package gw.vark.testapi;

import org.apache.tools.ant.DefaultLogger;

import java.util.ArrayList;

/**
 */
public class InMemoryLogger extends DefaultLogger {

  private final ArrayList<String> _messages = new ArrayList<String>();
  private static InMemoryLogger _lastInstance;

  public static InMemoryLogger getLastInstance() {
    return _lastInstance;
  }

  public InMemoryLogger() {
    super();
    _lastInstance = this;
  }

  public void dump() {
    for (String message : _messages) {
      System.out.print(">");
      System.out.print(message);
      System.out.println("");
    }
  }

  public ArrayList<String> getMessages() {
    return _messages;
  }

  @Override
  protected void log(String message) {
    _messages.add(message);
  }
}
