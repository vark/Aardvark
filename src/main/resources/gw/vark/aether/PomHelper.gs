package gw.vark.aether

uses gw.vark.*
uses gw.vark.antlibs.*
uses gw.vark.util.*
uses java.io.File
uses java.lang.Object
uses java.util.*
uses org.sonatype.aether.ant.tasks.Install
uses org.sonatype.aether.ant.types.Artifact
uses org.sonatype.aether.ant.types.Dependencies
uses org.sonatype.aether.ant.types.Pom
uses org.sonatype.aether.ant.org.apache.maven.model.Dependency
uses org.sonatype.aether.ant.org.apache.maven.model.Model

class PomHelper implements IAardvarkUtils {

  static var _defaultGroupId : String as readonly DefaultGroupId

  static function load(pomFile : File) : PomHelper {
    AetherAntUtils.setProject(Aardvark.getProject())
    var pom = new PomHelper(pomFile, null)
    _defaultGroupId = pom.Model.GroupId
    createTargets(pom)
    return pom
  }

  private static function createTargets(pom : PomHelper) {
    var aardvarkProject = Aardvark.getProject()
    var cleanTarget = aardvarkProject.registerTarget("@pom-clean", null)
    var compileTarget = aardvarkProject.registerTarget("@pom-compile", null)
    for (subPom in pom.AllInTree.values().toSet()) {
      var projectCleanTarget = aardvarkProject.registerTarget("@pom-clean-${subPom.Model.ArtifactId}", \ -> subPom.clean())
      var projectCompileTarget = aardvarkProject.registerTarget("@pom-compile-${subPom.Model.ArtifactId}", \ -> subPom.compile())
      for (dep in subPom.Model.Dependencies) {
        if (pom.AllInTree.containsKey(dep.ArtifactId)) {
          projectCompileTarget.addDependency("@pom-compile-${dep.ArtifactId}")
        }
      }
      for (module in subPom.Model.Modules) {
        projectCompileTarget.addDependency("@pom-compile-${module}")
      }
      cleanTarget.addDependency(projectCleanTarget.Name)
      compileTarget.addDependency(projectCompileTarget.Name)
    }
  }

  var _pom : Pom
  var _model : Model as Model
  var _file : File as File
  var _dir : File as Dir
  var _id : String as Id
  var _parent : PomHelper as Parent
  var _children : List<PomHelper> as Children = {}
  var _allInTree : Map<String, PomHelper> as AllInTree = new HashMap<String, PomHelper>()

  property get SrcDir() : File {
    return file(Model.Build.SourceDirectory)
  }

  property get TargetDir() : File {
    return Dir.file("target")
  }

  property get ClassesDir() : File {
    return TargetDir.file("classes")
  }

  property get JarFile() : File {
    return TargetDir.file("${Model.ArtifactId}-${Model.Version}.jar")
  }

  property get LocalDependencies() : List<PomHelper> {
    var rootPom = this
    while (rootPom.Parent != null) {
      rootPom = rootPom.Parent
    }
    return this.Model.Dependencies.where(\ dep -> rootPom.AllInTree.containsKey(dep.ArtifactId)).map(\ dep -> rootPom.AllInTree[dep.ArtifactId])
  }

  property get ThirdPartyDependencies() : List<Dependency> {
    var rootPom = this
    while (rootPom.Parent != null) {
      rootPom = rootPom.Parent
    }
    return this.Model.Dependencies.where(\ dep -> !rootPom.AllInTree.containsKey(dep.ArtifactId))
  }

  construct(pomFile : File) {
    this(pomFile, null)
  }

  construct(pomFile : File, parent_ : PomHelper) {
    if (!pomFile.exists()) {
      buildException("POM file ${pomFile.Path} not found")
    }

    _file = pomFile
    _dir = pomFile.ParentFile
    _pom = AetherAntUtils.pom(pomFile)
    _model = _pom.getModel(_pom)
    _id = Model.Id
    _parent = parent_
    Aardvark.getProject().addReference("pom.${Id}", _pom)

    for (module in Model.Modules) {
      var child = new PomHelper(_dir.file("${module}/pom.xml"), this)
      _children.add(child)
      _allInTree.putAll(child.AllInTree)
    }
    _allInTree[Model.ArtifactId] = this
  }

  function compile() {
    if (Model.Packaging == "jar") {
      Ant.mkdir(:dir = TargetDir)
      var dependencies = new Dependencies()
      dependencies.addPom(_pom)
      var path = AetherAntUtils.resolveToPath(dependencies, "compile")
      Ant.mkdir(:dir = ClassesDir)
      // TODO - GW-specific code here - make these parameters configurable somehow...
      Ant.javac(:srcdir = path(SrcDir), :destdir = ClassesDir, :classpath = path,
        :includeantruntime = false,
        :fork = true, :memorymaximumsize = "768m",
        :encoding = "UTF-8", :nowarn = true, :debug = true)
      Ant.copy(:filesetList = { SrcDir.fileset(:excludes = "**/*.java") }, :todir = ClassesDir, :includeemptydirs = false)
      // TODO - GW-specific code here
      if (Dir.file("res").exists()) {
        Ant.copy(:filesetList = { Dir.file("res").fileset() }, :todir = ClassesDir, :includeemptydirs = false)
      }
      Ant.jar(:basedir = ClassesDir, :destfile = JarFile)
      AetherAntUtils.install(_pom, new Artifact() { :File = JarFile })
    } else if (Model.Packaging == "pom") {
      AetherAntUtils.install(_pom)
    }
  }

  function clean() {
    Ant.delete(:dir = TargetDir, :includeemptydirs = true)
  }

  override function toString() : String {
    return "PomHelper [" + Id + "] (" + File + ")"
  }

  override function hashCode() : int {
    return Id.hashCode()
  }

  override function equals(that : Object) : boolean {
    return that != null && that typeis PomHelper && that.Id == Id
  }

}
