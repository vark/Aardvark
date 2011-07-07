package gw.vark

uses gw.vark.antlibs.*
uses gw.vark.xsd.maven_4_0_0.Project
uses gw.vark.xsd.maven_4_0_0.anonymous.elements.Model_Dependencies_Dependency
uses java.io.File
uses java.util.HashMap
uses org.apache.tools.ant.types.Path

class Pom implements IAardvarkUtils {

  private static var _allPoms = new HashMap<String, Pom>()

  static function load(pomFile : File) {
    loadPom(pomFile)
    createTargets()
  }

  private static function loadPom(pomFile : File) {
    if (!pomFile.exists()) {
      buildException("POM file ${pomFile.Path} not found")
    }
    var pom = new Pom(pomFile)
    _allPoms[pom.Id] = pom
    if (pom.Project.Packaging == "pom") {
      if (pom.Project.Modules != null) {
        for (module in pom.Project.Modules.Module) {
          loadPom(pom.Dir.file("${module}/pom.xml"))
        }
      }
    }
  }

  private static function createTargets() {
    var aardvarkProject = Aardvark.getProject()
    var compileTarget = aardvarkProject.registerTarget("@compile", null)
    for (pom in _allPoms.values()) {
      var projectCompileTarget = aardvarkProject.registerTarget("@compile-${pom.Id}", \ -> pom.compile())
      if (pom.Project.Dependencies != null) {
        for (dep in pom.Project.Dependencies.Dependency) {
          var depId = idFromDep(dep)
          if (_allPoms.containsKey(depId)) {
            projectCompileTarget.addDependency("@compile-${depId}")
          }
        }
      }
      compileTarget.addDependency(projectCompileTarget.Name)
    }
  }

  var _project : Project as Project
  var _dir : File as Dir
  var _id : String as Id

  property get SrcPath() : Path {
    var srcMainJava = Dir.file("src/main/java")
    return srcMainJava.exists() ? path(srcMainJava) : null
  }

  property get ClassesDir() : File {
    return Dir.file("target/classes")
  }

  property get JarFile() : File {
    return Dir.file("target/${Project.ArtifactId}-${Project.Version}.jar")
  }

  construct(pomFile : File) {
    _project = Project.parse(pomFile)
    _dir = pomFile.ParentFile
    _id = idFromProject(_project)
    Maven.pom(:file = pomFile, :id = "pom.${idFromProject(_project)}")
  }

  private function compile() {
    if (SrcPath != null) {
      Maven.dependencies(:pathid = "path.${Id}", :pomrefid = "pom.${Id}", :usescope = "compile")
      var path = Aardvark.getProject().getReference("path.${Id}") as Path
      Ant.mkdir(:dir = ClassesDir)
      Ant.javac(:srcdir = SrcPath, :destdir = ClassesDir,
        :includeantruntime = false, :fork = true,
        :classpath = path)
      Ant.jar(:basedir = ClassesDir, :destfile = JarFile)
      Maven.install(:file = JarFile, :pomrefid = "pom.${Id}")
    }
  }

  private static function idFromDep(element : Model_Dependencies_Dependency) : String {
    return element.GroupId + ":" + element.ArtifactId + "-" + element.Version
  }

  private static function idFromProject(element : Project) : String {
    return element.GroupId + ":" + element.ArtifactId + "-" + element.Version
  }
}
