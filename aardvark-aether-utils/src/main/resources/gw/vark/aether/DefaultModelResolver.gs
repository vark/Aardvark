package gw.vark.aether

uses org.apache.maven.model.Repository
uses org.apache.maven.model.building.FileModelSource
uses org.apache.maven.model.building.ModelSource
uses org.apache.maven.model.resolution.ModelResolver
uses org.apache.maven.model.resolution.UnresolvableModelException
uses org.sonatype.aether.RepositorySystem
uses org.sonatype.aether.RepositorySystemSession
uses org.sonatype.aether.resolution.ArtifactRequest
uses org.sonatype.aether.resolution.ArtifactResolutionException
uses org.sonatype.aether.util.artifact.DefaultArtifact
uses org.sonatype.aether.graph.Dependency
uses org.sonatype.aether.util.graph.DefaultDependencyNode

/**
 * Minimal implementation of model resolver that can only resolve in local repositories.
 */
class DefaultModelResolver implements ModelResolver {
  var _session: RepositorySystemSession
  var _repositorySystem : RepositorySystem

  construct(session : RepositorySystemSession,
            repositorySystem : RepositorySystem ) {
    _session = session
    _repositorySystem = repositorySystem
  }

  private construct( original : DefaultModelResolver ) {
    _session = original._session
    _repositorySystem = original._repositorySystem
  }

  override function addRepository(repository : Repository) {
  }

  override function newCopy(): ModelResolver {
    return new DefaultModelResolver( this )
  }

  override function resolveModel(groupId: String, artifactId: String, version: String) : ModelSource {
    var pomArtifact = new DefaultArtifact(groupId, artifactId, "", "pom", version)
    var root = new Dependency( pomArtifact, null )
    var node = new DefaultDependencyNode( root )
    node.Repositories = Aether.RemoteRepositories
    var artifactRequest = new ArtifactRequest( node )

    try {
      var result = Aether.RepositorySystem.resolveArtifact( _session, artifactRequest  )
      var pomFile = result.Artifact.File
      return new FileModelSource(pomFile)
    } catch (e: ArtifactResolutionException) {
      throw new UnresolvableModelException(e.getMessage(), groupId, artifactId, version, e)
    }
  }
}
