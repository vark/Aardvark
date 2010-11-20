package gw.vark.enums;

import gw.lang.reflect.IType;
import gw.lang.reflect.TypeSystem;
import gw.lang.reflect.java.IJavaType;
import gw.lang.shell.Gosu;
import gw.util.GosuEscapeUtil;
import gw.util.GosuStringUtil;
import gw.util.StreamUtil;
import org.apache.tools.ant.types.EnumeratedAttribute;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Created by IntelliJ IDEA.
 * User: cgross
 * Date: Nov 18, 2010
 * Time: 1:45:33 PM
 */
public class EnumGenerator {

  // bootstrap
  public static void main(String[] args) throws Exception {
    Gosu.initGosu( new File("C:\\opensource\\vark\\devel\\build.vark"), getSystemClasspath() );
    generateEnums(new File("C:\\opensource\\vark\\devel\\vark\\src"));
  }

  public static void generateEnums( File srcDirectory ) throws Exception {
    Set<? extends CharSequence> sequences = TypeSystem.getAllTypeNames();
    
    for (CharSequence sequence : sequences) {
      IType iType = null;
      try {
        iType = TypeSystem.getByFullNameIfValid(sequence.toString());
      } catch (Throwable e) {
        System.out.println("Error : " + e.getMessage());
      }
      maybeGenEnum(iType, new File(srcDirectory, "/gw/vark/enums"));
    }
  }

  private static void maybeGenEnum(IType iType, File srcRoot) throws Exception {
    if (TypeSystem.get(EnumeratedAttribute.class).isAssignableFrom(iType) && !iType.isAbstract()) {
      try {
        writeEnumTypeToFile(iType, srcRoot);
      } catch (Throwable e) {
        System.out.println("Error : " + e.getMessage());
      }
    }
    else {
      if (iType instanceof IJavaType) {
        try {
          for (IJavaType aClass : ((IJavaType) iType).getInnerClasses()) {
            maybeGenEnum(aClass, srcRoot);
          }
        } catch (Throwable e) {
          System.out.println("Error : " + e.getMessage());
        }
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
    sb.append("\n  var _val : String as Val\n")
            .append("\n  private construct( s : String ) { Val = s }\n");
    sb.append("\n\n}");

    File actualFile = new File(file, makeName(iType) + ".gs");
    Writer writer = StreamUtil.getOutputStreamWriter(new FileOutputStream(actualFile, false));
    try {
      writer.write(sb.toString());
    } finally {
      writer.close();
    }
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
