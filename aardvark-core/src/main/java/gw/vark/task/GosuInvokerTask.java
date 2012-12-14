/*
 * Copyright (c) 2012 Guidewire Software, Inc.
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

import gw.config.CommonServices;
import gw.lang.parser.GosuParserFactory;
import gw.lang.parser.IFileContext;
import gw.lang.parser.IGosuProgramParser;
import gw.lang.parser.IParseResult;
import gw.lang.parser.ITypeUsesMap;
import gw.lang.parser.ParserOptions;
import gw.lang.parser.StandardSymbolTable;
import gw.lang.parser.exceptions.IEvaluationException;
import gw.vark.AardvarkProgram;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Location;
import org.apache.tools.ant.Task;

import java.util.List;

@SuppressWarnings({"UnusedDeclaration"})
public class GosuInvokerTask extends Task {

  private StringBuilder _expression = new StringBuilder();

  public void addText(String text) {
    _expression.append(getProject().replaceProperties(text));
  }

  @Override
  public void init() throws BuildException {
    // Append N-1 empty lines, to match Gosu line numbers with build.xml.
    Location loc = getLocation();
    if (loc != null) {
      for (int i = 0; i < getLocation().getLineNumber() - 1; i++) {
        _expression.append('\n');
      }
    }
  }

  public void execute() throws BuildException {
    if (_expression.length() == 0) {
      throw new BuildException("no expression to evaluate");
    }

    try {
      IGosuProgramParser parser = GosuParserFactory.createProgramParser();

      String script = _expression.toString();
      IParseResult result =
              parser.parseExpressionOrProgram(script,
                      new StandardSymbolTable(true), createParserOptions());
      result.getProgram().evaluate(null);
    } catch (IEvaluationException e) {
      if (e.getCause() != null && e.getCause() instanceof BuildException) {
        // TODO - blc - the Gosu stack trace could be useful here
        throw (BuildException) e.getCause();
      } else {
        throw e;
      }
    } catch (Exception e) {
      throw new BuildException(e);
    }
  }

  private ParserOptions createParserOptions() {
    IFileContext fileContext = new IFileContext() {
      @Override
      public String getContextString() {
        return null;
      }

      @Override
      public String getClassName() {
        return null;
      }

      @Override
      public String getFilePath() {
        return getLocation().getFileName();
      }
    };

    List<String> packages = AardvarkProgram.getDefaultTypeUsesPackages();
    ITypeUsesMap typeUses = CommonServices.getGosuIndustrialPark().createTypeUsesMap(packages);
    for (String aPackage : packages) {
      typeUses.addToDefaultTypeUses(aPackage);
    }

    ParserOptions options =
            new ParserOptions()
                    .withFileContext(fileContext)
                    .withTypeUsesMap(typeUses);
    return options;
  }
}
