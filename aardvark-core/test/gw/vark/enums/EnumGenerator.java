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

package gw.vark.enums;

import gw.lang.reflect.IType;
import gw.lang.reflect.TypeSystem;
import gw.lang.reflect.java.IJavaType;
import gw.lang.shell.Gosu;
import gw.util.GosuEscapeUtil;
import gw.util.GosuStringUtil;
import gw.util.StreamUtil;
import gw.vark.testapi.TestUtil;
import org.apache.tools.ant.types.EnumeratedAttribute;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Created by IntelliJ IDEA.
 * User: cgross
 * Date: Nov 18, 2010
 * Time: 1:45:33 PM
 */
public class EnumGenerator {

  private static final File SRC_DIR = new File(TestUtil.getHome(EnumGenerator.class), "aardvark/src");
  private static final File ENUMS_DIR = new File(SRC_DIR, "gw/vark/enums");
  private static Set<String> _filesInEnums;

  // bootstrap
  public static void main(String[] args) throws Exception {
    Gosu.init(getSystemClasspath());

    _filesInEnums = new HashSet<String>();
    for (File enumFile : ENUMS_DIR.listFiles()) {
      _filesInEnums.add(enumFile.getName());
    }

    for (CharSequence typeName : TypeSystem.getAllTypeNames()) {
      try {
        IType iType = TypeSystem.getByFullNameIfValid(typeName.toString());
        maybeGenEnum(iType, ENUMS_DIR);
      } catch (Throwable e) {
        System.out.println("Error : " + e.getMessage());
      }
    }

    for (String leftover : _filesInEnums) {
      System.err.println(leftover + " might be obsolete");
    }
  }

  private static void maybeGenEnum(IType iType, File srcRoot) throws Exception {
    if (!(iType instanceof IJavaType)) {
      return;
    }
    if (TypeSystem.get(EnumeratedAttribute.class).isAssignableFrom(iType) && !iType.isAbstract()) {
      writeEnumTypeToFile(iType, srcRoot);
    }
    else {
      for (IJavaType aClass : ((IJavaType) iType).getInnerClasses()) {
        maybeGenEnum(aClass, srcRoot);
      }
    }
  }

  private static void writeEnumTypeToFile(IType iType, File file) throws IOException {
    EnumeratedAttribute ea = (EnumeratedAttribute) iType.getTypeInfo().getConstructor().getConstructor().newInstance();
    StringBuilder sb = new StringBuilder();
    sb.append("package gw.vark.enums\n")
            .append("\n")
            .append("enum ").append(makeName(iType)).append("{\n\n");
    String[] Vals = ea.getValues();
    for (int i = 0, ValsLength = Vals.length; i < ValsLength; i++) {
      String string = Vals[i];
      if (string.equals("")) {
        string = "NoVal";
      }
      sb.append("  ").append(escape(GosuStringUtil.capitalize(string)))
              .append("(\"")
              .append(GosuEscapeUtil.escapeForGosuStringLiteral(string))
              .append("\")");
      sb.append(",\n");
    }
    sb.append("\n");
    sb.append("  property get Instance() : " + iType.getName() + " {\n");
    sb.append("    return " + EnumeratedAttribute.class.getName() + ".getInstance(" + iType.getName() + ", Val) as " + iType.getName() + "\n");
    sb.append("  }\n\n");
    sb.append("  var _val : String as Val\n\n");
    sb.append("  private construct( s : String ) { Val = s }\n\n");
    sb.append("}\n");

    String fileName = makeName(iType) + ".gs";
    File actualFile = new File(file, fileName);
    Writer writer = StreamUtil.getOutputStreamWriter(new FileOutputStream(actualFile, false));
    try {
      writer.write(sb.toString());
    } finally {
      writer.close();
    }
    _filesInEnums.remove(fileName);
  }

  private static String escape(String string) {
    String s = "";
    char[] charArray = string.toCharArray();
    for (int i = 0, charArrayLength = charArray.length; i < charArrayLength; i++) {
      char c = charArray[i];
      if (i == 0 && !Character.isJavaIdentifierStart(c)) {
        s = "_" + c;
      } else if (!Character.isJavaIdentifierPart(c)) {
        s += "_";
      } else {
        s += c;
      }
    }
    return s;
  }

  private static String makeName(IType iType) {
    return iType.getRelativeName().replace(".", "_");
  }

  static List<File> getSystemClasspath()
  {
    ArrayList<File> files = new ArrayList<File>();
    for( String file : System.getProperty( "java.class.path" ).split( File.pathSeparator ) )
    {
      files.add( new File( file ) );
    }
    return files;
  }

}
