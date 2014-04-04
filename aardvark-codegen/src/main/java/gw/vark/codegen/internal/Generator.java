package gw.vark.codegen.internal;

import com.guidewire.codegen.declarations.CGClass;
import com.guidewire.codegen.declarations.CGGosuClass;
import com.guidewire.codegen.declarations.CGMethod;
import com.guidewire.codegen.declarations.CGModifier;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.ProjectHelper;
import org.apache.tools.ant.ProjectHelperRepository;
import org.apache.tools.ant.UnknownElement;
import org.apache.tools.ant.taskdefs.Antlib;
import org.apache.tools.ant.types.resources.URLResource;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

public class Generator {

  private static final Project NULL_PROJECT = new Project();

  public static final String GW_VARK_TASKS_PACKAGE = "gw.vark.antlibs";

  private final List<Path> _libraries;
  private final URLClassLoader _libLoader;

  public Generator(List<Path> libraries) throws MalformedURLException {
    _libraries = libraries;

    URL[] urls = new URL[_libraries.size()];
    int i = 0;
    for (Path lib : libraries) {
      urls[i++] = lib.toUri().toURL();
    }

    _libLoader = new URLClassLoader(urls);
  }

  public List<CGClass> generateAntlib(Path source) throws IOException {
    String typeName = GW_VARK_TASKS_PACKAGE + '.' + Util.getBaseName(source);
    List<CGClass> classes = new ArrayList<>();
    List<String> antlib = Files.readAllLines(source, StandardCharsets.UTF_8);
    for (String resource : antlib) {
      Path antlibResource = retrieveResource(resource);
      if (antlibResource == null) {
        throw new IllegalArgumentException("Cannot locate Antlib resource: " + antlibResource);
      }
      CGGosuClass taskClass = new CGGosuClass(typeName, CGModifier.PUBLIC);
      generateMethods(taskClass, antlibResource);
      classes.add(taskClass);
    }
    return classes;
  }

  private void generateMethods(CGClass taskClass, Path resource) throws IOException {
    String ext = Util.getExtension(resource);
    Map<String, String> taskdefs;
    switch (ext) {
      case "properties":
        taskdefs = readTaskListingFromPropertiesFile(resource);
        break;
      case "xml":
        taskdefs = readTaskListingFromAntlib(resource);
        break;
      default:
        throw new IllegalArgumentException("resourceName must have suffix .properties or .xml");
    }

    for (Map.Entry<String, String> taskdef : taskdefs.entrySet()) {
      CGMethod method = createTaskAsMethod(taskdef.getKey(), taskdef.getValue());
      if (method != null) {
        taskClass.addMethod(method);
      }
    }
  }

  private Map<String, String> readTaskListingFromPropertiesFile(Path resource) throws IOException {
    try (InputStream in = Files.newInputStream(resource)) {
      Properties tasks = new Properties();
      tasks.load(in);
      return (Map) tasks;
    }
  }

  private Map<String, String> readTaskListingFromAntlib(Path resource) throws MalformedURLException {
    URLResource antlibResource = new URLResource(resource.toUri().toURL());
    ProjectHelperRepository helperRepository = ProjectHelperRepository.getInstance();
    ProjectHelper parser = helperRepository.getProjectHelperForAntlib(antlibResource);
    UnknownElement ue = parser.parseAntlibDescriptor(NULL_PROJECT, antlibResource);
    if (!ue.getTag().equals(Antlib.TAG)) {
      throw new IllegalArgumentException("Unexpected tag " +
              ue.getTag() + " expecting " + Antlib.TAG);
    }

    Map<String, String> taskdefs = new HashMap<>();
    for (Object childObj : ue.getChildren()) {
      UnknownElement child = (UnknownElement) childObj;
      if (child.getTag().equals("taskdef")) {
        Map<?, ?> attributes = child.getWrapper().getAttributeMap();
        String name = (String) attributes.get("name");
        String className = (String) attributes.get("classname");
        taskdefs.put(name, className);
      }
    }
    return taskdefs;
  }

  private CGMethod createTaskAsMethod(String name, String className) {
    try {
      Introspector helper = new Introspector(_libLoader, className);
      return new MethodGenerator(helper).createTaskAsMethod(name, className);
    } catch (ClassNotFoundException e) {
      System.err.println("Task class '" + className + "' not found, ignoring!");
      return null;
    }
  }

  private Path retrieveResource(String resource) {
    for (Path library : _libraries) {
      Path path = library.resolve(resource);
      if (Files.exists(path)) {
        return path;
      }
    }
    return null;
  }

}
