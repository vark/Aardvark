package gw.vark.aether

uses org.apache.maven.model.building.*
uses org.apache.maven.repository.internal.ArtifactDescriptorUtils
uses org.apache.maven.repository.internal.MavenRepositorySystemSession
uses org.apache.maven.repository.internal.MavenServiceLocator
uses org.sonatype.aether.RepositorySystem
uses org.sonatype.aether.artifact.Artifact
uses org.sonatype.aether.collection.CollectRequest
uses org.sonatype.aether.collection.DependencySelector
uses org.sonatype.aether.graph.Dependency
uses org.sonatype.aether.graph.DependencyFilter
uses org.sonatype.aether.repository.LocalRepository
uses org.sonatype.aether.repository.RemoteRepository
uses org.sonatype.aether.resolution.*
uses org.sonatype.aether.spi.locator.ServiceLocator
uses org.sonatype.aether.util.DefaultRepositoryCache
uses org.sonatype.aether.util.DefaultRepositorySystemSession
uses org.sonatype.aether.util.graph.DefaultDependencyNode

uses java.io.File
uses java.util.Map
uses java.util.Properties
uses org.sonatype.aether.connector.wagon.WagonRepositoryConnectorFactory
uses org.sonatype.aether.connector.wagon.WagonProvider
uses org.sonatype.aether.spi.connector.RepositoryConnectorFactory
uses org.sonatype.aether.spi.log.Logger
uses gw.vark.maven.Maven

/**
 * Entry points to the Aether support (artifacts resolution).
 */
class Aether {

  static var _serviceLocator : ServiceLocator as ServiceLocator = createServiceLocator( )
  static var _repositorySystem : RepositorySystem as RepositorySystem = _serviceLocator.getService( org.sonatype.aether.RepositorySystem )
  static var _workspace : VarkWorskpace as Workspace = new VarkWorskpace( )
  static var _defaultSession : DefaultRepositorySystemSession as DefaultSession = createDefaultSession( )
  static var _defaultCache : ModelCache as DefaultCache = DefaultModelCache.newInstance( _defaultSession )
  static var _testSelector : DependencySelector as TestSelector = TestDependencySelector.Instance
  static var _remoteRepositories : List<RemoteRepository> as RemoteRepositories = { }

  static property get Offline() : boolean {
    return _defaultSession.Offline
  }
  static property set Offline(offline : boolean) {
    _defaultSession.Offline = offline
  }

  static function createDefaultSession( ) : DefaultRepositorySystemSession {
    var sess = new MavenRepositorySystemSession()
    var localRepo = new LocalRepository( Maven.LocalRepository )
    sess.LocalRepositoryManager = RepositorySystem.newLocalRepositoryManager( localRepo )
    sess.WorkspaceReader = _workspace
    sess.Cache = new DefaultRepositoryCache()
    return sess
  }

  static function resolveTransitively( artifact : Artifact = null,
                                       dependencies : List<Artifact> = null,
                                       dependencyFilter : DependencyFilter = null,
                                       dependencySelector : DependencySelector = null,
                                       ignoreFailures : boolean = false) : DependencyResult {
    var root = artifact != null ? new Dependency( artifact, null ) : null
    var deps = dependencies != null ? dependencies.map( \ elt -> new Dependency(elt, null ) ) : null
    var collectRequest = new CollectRequest( root, deps, _remoteRepositories )
    var dependencyRequest = new DependencyRequest( collectRequest, dependencyFilter )
    var sess: DefaultRepositorySystemSession
    if (dependencySelector != null) {
      sess = new DefaultRepositorySystemSession( _defaultSession )
      sess.setDependencySelector( dependencySelector )
    } else {
      sess = _defaultSession
    }
    try {
      var result = _repositorySystem.resolveDependencies( sess, dependencyRequest  )
      return result
    } catch (e : DependencyResolutionException) {
      // Clean cache every time we have resolution exception, so next time we try to resolve something we get error message!
      // FIXME: Correct way would be not to run resolutions that could fail.
      sess.Cache = new DefaultRepositoryCache()
      if (ignoreFailures) {
        return e.Result
      }
      throw e
    }
  }

  static function resolveVersionRange( artifact : Artifact ) : VersionRangeResult {
    var req = new VersionRangeRequest( artifact, _remoteRepositories, null );
    var result = _repositorySystem.resolveVersionRange( _defaultSession, req )
    return result
  }

  static function resolve( artifact : Artifact ) : ArtifactResult {
    var root = new Dependency( artifact, null )
    var node = new DefaultDependencyNode( root )
    node.Repositories = _remoteRepositories
    var artifactRequest = new ArtifactRequest( node )
    var result = _repositorySystem.resolveArtifact( _defaultSession, artifactRequest  )
    return result
  }

  static function resolveDescriptor( art : Artifact ) : ArtifactDescriptorResult {
    var req = new ArtifactDescriptorRequest( art, _remoteRepositories, null )
    var result = _repositorySystem.readArtifactDescriptor( _defaultSession, req )
    return result
  }

  static function buildMavenModel( pomFile : File, profiles : List<String> = null) : ModelBuildingResult {
    var req = new DefaultModelBuildingRequest()
    req.ValidationLevel = ModelBuildingRequest.VALIDATION_LEVEL_MINIMAL
    req.ModelResolver = new DefaultModelResolver( _defaultSession, _repositorySystem )
    req.ProcessPlugins = false
    req.TwoPhaseBuilding = false
    req.ModelCache = _defaultCache
    req.PomFile = pomFile
    req.ActiveProfileIds = profiles
    req.SystemProperties = toProperties( _defaultSession.UserProperties, _defaultSession.SystemProperties )
    var resp = new DefaultModelBuilderFactory( ).newInstance( ).build(req)
    return resp
  }

  static function lookupMavenModel(art : Artifact) : ModelBuildingResult {
    var pom = ArtifactDescriptorUtils.toPomArtifact( art )
    var resolved = Aether.resolve( pom ).Artifact
    var model = buildMavenModel( resolved.File )
    return model
  }

  private static function toProperties( dominant : Map<String, String>, recessive : Map<String, String> ) : Properties {
    var props = new Properties()
    if ( recessive != null ) {
      props.putAll( recessive )
    }
    if ( dominant != null ) {
      props.putAll( dominant )
    }
    return props
  }

  private static function createServiceLocator( ) : ServiceLocator {
    var locator = new MavenServiceLocator( )
    locator.addService( WagonProvider, ManualWagonProvider )
    locator.addService( RepositoryConnectorFactory, WagonRepositoryConnectorFactory )
    locator.addService( Logger, AardvarkLogger )
    return locator
  }
}
