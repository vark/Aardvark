package gw.vark

uses gw.vark.*
uses gw.vark.util.*
uses java.io.File
uses java.lang.Object
uses java.util.*
uses org.sonatype.aether.ant.tasks.Install
uses org.sonatype.aether.ant.types.Artifact
uses org.sonatype.aether.ant.types.Dependencies
uses org.sonatype.aether.ant.types.Pom
uses org.apache.maven.model.Dependency
uses org.apache.maven.model.Model
uses org.apache.tools.ant.types.Path
uses org.apache.tools.ant.Task
uses org.sonatype.aether.ant.tasks.Resolve

class PomHelper implements IAardvarkUtils {

  static function load(pomFile : File) : PomHelper {
    return new PomHelper(pomFile, null)
  }

  var _file : File as File
  var _dir : File as Dir
  var _pom : Pom
  var _model : Model as Model

  var _parent : PomHelper as Parent
  var _children : List<PomHelper> as Children = {}
  var _allInTree : Map<String, PomHelper> as AllInTree = new HashMap<String, PomHelper>()

  property get Id() : String {
    return Model.Id
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
    _pom = parsePom(pomFile)
    _model = _pom.getModel(_pom)
    _parent = parent_
    Aardvark.getProject().addReference("pom.${Id}", _pom)

    for (module in Model.Modules) {
      var child = new PomHelper(_dir.file("${module}/pom.xml"), this)
      _children.add(child)
      _allInTree.putAll(child.AllInTree)
    }
    _allInTree[Model.ArtifactId] = this
  }

  function dependenciesPath(scope : MavenScope) : Path {
    var dependencies = new Dependencies()
    dependencies.addPom(_pom)
    var resolve = initTask(new Resolve(), "resolve")
    resolve.addDependencies(dependencies)
    var path = resolve.createPath()
    path.Project = Aardvark.getProject()
    path.setRefId("tmp.path")
    path.setScopes(scope.Name.toLowerCase())
    resolve.execute()
    return Aardvark.getProject().getReference("tmp.path") as Path
  }

  override function toString() : String {
    return "PomHelper [" + Id + "] (" + File + ")"
  }

  private static function parsePom(file : File) : Pom {
    var pom = initTask(new Pom(), "pom")
    pom.setFile(file)
    pom.execute()
    return pom
  }

  private static function initTask<T extends Task>(task : T, name : String) : T {
    task.setProject(Aardvark.getProject())
    task.setTaskName(name)
    task.init()
    return task
  }

  override function hashCode() : int {
    return Id.hashCode()
  }

  override function equals(that : Object) : boolean {
    return that != null && that typeis PomHelper && that.Id == Id
  }

  enum MavenScope {
    COMPILE,
    PROVIDED,
    RUNTIME,
    TEST,
    SYSTEM,
    IMPORT
  }
}
