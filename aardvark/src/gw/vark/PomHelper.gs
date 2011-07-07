package gw.vark

uses gw.vark.antlibs.*
uses java.io.File
uses java.util.HashMap
uses org.apache.maven.artifact.ant.Pom
uses org.apache.maven.model.Dependency
uses org.apache.tools.ant.types.Path

class PomHelper implements IAardvarkUtils {

  private static var _allPoms = new HashMap<String, PomHelper>()

  static function load(pomFile : File) {
    loadPom(pomFile)
    createTargets()
  }

  private static function loadPom(pomFile : File) {
    if (!pomFile.exists()) {
      buildException("POM file ${pomFile.Path} not found")
    }
    var pom = new PomHelper(pomFile)
    _allPoms[pom.Id] = pom
    if (pom.Pom.Packaging == "pom") {
      for (module in pom.Pom.Modules) {
        loadPom(pom.Dir.file("${module}/pom.xml"))
      }
    }
  }

  private static function createTargets() {
    var aardvarkProject = Aardvark.getProject()
    var compileTarget = aardvarkProject.registerTarget("@compile", null)
    for (pom in _allPoms.values()) {
      var projectCompileTarget = aardvarkProject.registerTarget("@compile-${pom.Id}", \ -> pom.compile())
      for (dep in pom.Pom.Dependencies) {
        var depId = idFromDep(dep)
        if (_allPoms.containsKey(depId)) {
          projectCompileTarget.addDependency("@compile-${depId}")
        }
      }
      compileTarget.addDependency(projectCompileTarget.Name)
    }
  }

  var _pom : Pom as Pom
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
    return Dir.file("target/${Pom.ArtifactId}-${Pom.Version}.jar")
  }

  construct(pomFile : File) {
    _pom = Maven.pom(:file = pomFile, :id = "tmp.pom")
    Aardvark.getProject().addReference("pom.${Id}", _pom)
    _dir = pomFile.ParentFile
    _id = idFromProject(Pom)
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

  private static function idFromDep(element : Dependency) : String {
    return element.GroupId + ":" + element.ArtifactId + "-" + element.Version
  }

  private static function idFromProject(element : Pom) : String {
    return element.GroupId + ":" + element.ArtifactId + "-" + element.Version
  }
}
