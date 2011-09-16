package gw.vark.mavenish

uses gw.vark.*
uses gw.vark.antlibs.*
uses gw.maven.*
uses java.io.File
uses java.util.*
uses org.apache.maven.artifact.ant.*
uses org.apache.maven.artifact.resolver.*
uses org.apache.maven.model.Dependency
uses org.apache.tools.ant.types.Path

class PomHelper implements IAardvarkUtils {

  static var _stopwatch : gw.vark.Stopwatch
  static var _defaultGroupId : String

  static function init(defaultGroupId : String) {
    _defaultGroupId = defaultGroupId
    Dependencies.ArtifactCollectorFilter.TransitivityPredicate = \ node -> node.Artifact.GroupId == defaultGroupId
  }

  static function load(pomFile : File) : PomHelper {
    _stopwatch = new gw.vark.Stopwatch("POM maven task")
    var pom = new PomHelper(pomFile, null)
    createTargets(pom)
    _stopwatch.print()
    return pom
  }

  private static function createTargets(pom : PomHelper) {
    var aardvarkProject = Aardvark.getProject()
    var cleanTarget = aardvarkProject.registerTarget("@pom-clean", null)
    var compileTarget = aardvarkProject.registerTarget("@pom-compile", null)
    for (subPom in pom.AllInTree.values()) {
      var targetName = subPom.Pom.GroupId == _defaultGroupId ? subPom.Pom.ArtifactId : subPom.Id
      var projectCleanTarget = aardvarkProject.registerTarget("@pom-clean-${targetName}", \ -> subPom.clean())
      var projectCompileTarget = aardvarkProject.registerTarget("@pom-compile-${targetName}", \ -> subPom.compile())
      for (dep in subPom.Pom.Dependencies) {
        if (pom.AllInTree.containsKey(dep.Id)) {
          var depTargetName = dep.GroupId == _defaultGroupId ? dep.ArtifactId : dep.Id
          projectCompileTarget.addDependency("@pom-compile-${depTargetName}")
        }
      }
      for (child in subPom.Children) {
        var depTargetName = child.Pom.GroupId == _defaultGroupId ? child.Pom.ArtifactId : child.Id
        projectCompileTarget.addDependency("@pom-compile-${depTargetName}")
      }
      cleanTarget.addDependency(projectCleanTarget.Name)
      compileTarget.addDependency(projectCompileTarget.Name)
    }
  }

  var _pom : Pom as Pom
  var _file : File as File
  var _dir : File as Dir
  var _id : String as Id
  var _parent : PomHelper as Parent
  var _children : List<PomHelper> as Children = {}
  var _allInTree : Map<String, PomHelper> as AllInTree = new HashMap<String, PomHelper>()

  property get SrcDir() : File {
    return file(Pom.Build.SourceDirectory)
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

  property get LocalDependencies() : List<PomHelper> {
    var rootPom = this
    while (rootPom.Parent != null) {
      rootPom = rootPom.Parent
    }
    return this.Pom.Dependencies.map(\ dep -> rootPom.AllInTree[dep.Id]).where(\ ph -> ph != null)
  }

  property get ThirdPartyDependencies() : List<Dependency> {
    var rootPom = this
    while (rootPom.Parent != null) {
      rootPom = rootPom.Parent
    }
    return this.Pom.Dependencies.where(\ dep -> !rootPom.AllInTree.containsKey(dep.Id))
  }

  private construct(pomFile : File, parent_ : PomHelper) {
    if (!pomFile.exists()) {
      buildException("POM file ${pomFile.Path} not found")
    }

    _file = pomFile
    _dir = pomFile.ParentFile
    _stopwatch.start()
    _pom = Maven.pom(:file = pomFile, :id = "tmp.pom")
    _stopwatch.stop()
    _id = Pom.Id
    _parent = parent_
    Aardvark.getProject().addReference("pom.${Id}", _pom)

    for (module in _pom.Modules) {
      var child = new PomHelper(_dir.file("${module}/pom.xml"), this)
      _children.add(child)
      _allInTree.putAll(child.AllInTree)
    }
    _allInTree[_id] = this
  }

  function compile() {
    if (Pom.Packaging == "jar") {
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
    } else if (Pom.Packaging == "pom") {
      Maven.install(:file = _file, :pomrefid = "pom.${Id}")
    }
  }

  function clean() {
    Ant.delete(:dir = TargetDir, :includeemptydirs = true)
  }

  override function toString() : String {
    return "PomHelper [" + Id + "] (" + File + ")"
  }

  private static class Dependencies extends DependenciesTask implements IAardvarkUtils {
    static property get ArtifactCollectorFilter() : ArtifactCollectorFilter {
      var deps = new Dependencies() { :Project = Aardvark.getProject() }
      return deps.lookup("gw.maven.ArtifactCollectorFilter") as ArtifactCollectorFilter
    }
  }

}
