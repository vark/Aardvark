/*
 * Copyright (c) 2010 Guidewire Software, Inc.
 */

package gw.vark.task;

import gw.lang.parser.GosuParserFactory;
import gw.lang.parser.IGosuParser;
import gw.lang.parser.StandardSymbolTable;
import gw.lang.parser.exceptions.IEvaluationException;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Task;

@SuppressWarnings({"UnusedDeclaration"})
public class GosuInvokerTask extends Task {

  private StringBuilder _expression = new StringBuilder();

  public void addText(String text) {
    _expression.append(getProject().replaceProperties(text));
  }

  public void execute() throws BuildException {
    if (_expression.length() == 0) {
      throw new BuildException("no expression to evaluate");
    }

    try {
      IGosuParser parser = GosuParserFactory.createParser( _expression.toString(), new StandardSymbolTable( true ) );
      parser.parseExpOrProgram( null, false, true ).evaluate();
    } catch (IEvaluationException e) {
      if (e.getCause() != null && e.getCause() instanceof BuildException) {
        // TODO - blc - the Gosu stack trace could be useful here
        throw (BuildException) e.getCause();
      }
      else {
        throw e;
      }
    } catch (Exception e) {
      throw new BuildException(e);
    }
  }
}
