package gw.vark.mavenish

uses gw.vark.*
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
    for (module in pom.Pom.Modules) {
      loadPom(pom.Dir.file("${module}/pom.xml"))
    }
  }

  private static function createTargets() {
    var aardvarkProject = Aardvark.getProject()
    var cleanTarget = aardvarkProject.registerTarget("@pom-clean", null)
    var compileTarget = aardvarkProject.registerTarget("@pom-compile", null)
    for (pom in _allPoms.values()) {
      var projectCleanTarget = aardvarkProject.registerTarget("@pom-clean-${pom.Id}", \ -> pom.clean())
      var projectCompileTarget = aardvarkProject.registerTarget("@pom-compile-${pom.Id}", \ -> pom.compile())
      for (dep in pom.Pom.Dependencies) {
        var depId = idFromDep(dep)
        if (_allPoms.containsKey(depId)) {
          projectCompileTarget.addDependency("@pom-compile-${depId}")
        }
      }
      cleanTarget.addDependency(projectCleanTarget.Name)
      compileTarget.addDependency(projectCompileTarget.Name)
    }
  }

  var _pom : Pom as Pom
  var _dir : File as Dir
  var _id : String as Id

  property get SrcDir() : File {
    return Dir.file("src/main/java")
  }

  property get TargetDir() : File {
    return Dir.file("target")
  }

  property get ClassesDir() : File {
    return TargetDir.file("classes")
  }

  property get JarFile() : File {
    return TargetDir.file("${Pom.ArtifactId}-${Pom.Version}.jar")
  }

  construct(pomFile : File) {
    _dir = pomFile.ParentFile
    _pom = Maven.pom(:file = pomFile, :id = "tmp.pom")
    _id = idFromProject(Pom)
    Aardvark.getProject().addReference("pom.${Id}", _pom)
  }

  function compile() {
    if (SrcDir.exists()) {
      Ant.mkdir(:dir = TargetDir)
      Maven.dependencies(:pathid = "path.${Id}", :pomrefid = "pom.${Id}", :usescope = "compile")
      var path = Aardvark.getProject().getReference("path.${Id}") as Path
      Ant.mkdir(:dir = ClassesDir)
      Ant.javac(:srcdir = path(SrcDir), :destdir = ClassesDir,
        :includeantruntime = false, :fork = true,
        :classpath = path)
      Ant.copy(:filesetList = { SrcDir.fileset(:excludes = "**/*.java") }, :todir = ClassesDir, :includeemptydirs = false)
      Ant.jar(:basedir = ClassesDir, :destfile = JarFile)
      Maven.install(:file = JarFile, :pomrefid = "pom.${Id}")
    }
  }

  function clean() {
    Ant.delete(:dir = TargetDir, :includeemptydirs = true)
  }

  private static function idFromDep(element : Dependency) : String {
    return element.GroupId + ":" + element.ArtifactId + "-" + element.Version
  }

  private static function idFromProject(element : Pom) : String {
    return element.GroupId + ":" + element.ArtifactId + "-" + element.Version
  }
}
