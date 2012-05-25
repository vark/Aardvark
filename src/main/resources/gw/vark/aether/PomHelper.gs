package gw.vark.aether

uses gw.vark.*
uses gw.vark.util.*
uses java.io.File
uses java.lang.Object
uses java.util.*
uses org.apache.maven.model.Model
uses org.apache.tools.ant.types.Path
uses org.apache.tools.ant.Task

class PomHelper implements IAardvarkUtils {

  static function load(pomFile : File) : PomHelper {
    return new PomHelper(pomFile)
  }

  var _file : File as File
  var _dir : File as Dir
  var _pom : org.sonatype.aether.ant.types.Pom as Pom
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

  function dependencies(scope : MavenScope, additionalDeps : List<org.sonatype.aether.ant.types.Dependency> = null) : DependenciesWrapper {
    var dependencies = new DependenciesWrapper(scope, additionalDeps)
    return dependencies
  }

  override function toString() : String {
    return "PomHelper [" + Id + "] (" + File + ")"
  }

  private static function parsePom(file : File) : org.sonatype.aether.ant.types.Pom {
    var pom = initTask(new org.sonatype.aether.ant.types.Pom(), "pom")
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

  class DependenciesWrapper {
    var _scope : MavenScope
    var _dependencies : org.sonatype.aether.ant.types.Dependencies

    construct(scope : MavenScope, additionalDeps : List<org.sonatype.aether.ant.types.Dependency> = null) {
      _scope = scope
      _dependencies = new()
      _dependencies.addPom(_pom)
      additionalDeps?.each( \ dep -> _dependencies.addDependency(dep) )
    }

    private function expandScope(scope : MavenScope) : String {
      switch (scope) {
      case COMPILE:
        return "compile,system,provided"
      case RUNTIME:
        return "compile,runtime"
      case TEST:
        return "compile,system,provided,runtime,test"
      default:
        return null
      }
    }

    property get Path() : Path {
      var resolve = initTask(new org.sonatype.aether.ant.tasks.Resolve(), "resolve")
      for (repo in Model.Repositories) {
        resolve.addRemoteRepo(new org.sonatype.aether.ant.types.RemoteRepository() {
          :Id = repo.Id,
          :Url = repo.Url,
          :Releases = repo.Releases == null || repo.Releases.isEnabled(),
          :Snapshots = repo.Snapshots == null || repo.Snapshots.isEnabled()
        })
      }
      resolve.addDependencies(_dependencies)

      var resolvePath = resolve.createPath()
      resolvePath.Project = Aardvark.getProject()
      resolvePath.setRefId("tmp.path")
      resolvePath.setScopes(expandScope(_scope))
      resolve.execute()
      var p = Aardvark.getProject().getReference("tmp.path") as Path

      // filter out non-jars
      var filteredPath = new Path(Aardvark.getProject())
      p.list().where(\ elt -> elt.endsWith(".jar")).each(\ elt -> {
        filteredPath.createPathElement().setPath(elt)
      })
      return filteredPath
    }
  }

  enum MavenScope {
    COMPILE,
    RUNTIME,
    TEST,
  }
}
