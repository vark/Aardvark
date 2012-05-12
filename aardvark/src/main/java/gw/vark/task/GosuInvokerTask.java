/*
 * Copyright (c) 2010 Guidewire Software, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
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
