package gw.vark.aether

uses org.apache.maven.model.Model
uses org.apache.maven.repository.internal.ArtifactDescriptorUtils
uses org.sonatype.aether.artifact.Artifact
uses org.sonatype.aether.collection.DependencySelector
uses org.sonatype.aether.graph.DependencyFilter
uses org.sonatype.aether.repository.ArtifactRepository
uses org.sonatype.aether.repository.RemoteRepository
uses org.sonatype.aether.resolution.ArtifactDescriptorResult
uses org.sonatype.aether.resolution.ArtifactResolutionException
uses org.sonatype.aether.util.artifact.DefaultArtifact

uses java.io.File

enhancement AetherArtifactEnhancement : Artifact {
  /**
   * Build classpath using unpackaged versions of local projects (target/classes directory)
   */
  property get ClassPath() : List<File> {
    return ClassPath( )
  }

  /**
   * Build test classpath using unpackaged versions of local projects (target/classes directory)
   */
  property get TestClassPath() : List<File> {
    var result = ClassPath( :dependencySelector = Aether.TestSelector )
    // Add test-classes/test JAR artifact
    result.add( this.WithClassifier( "tests" ).ResolveClasses )
    return result
  }

  /**
   * Build classpath using unpackaged versions of local projects (target/classes directory)
   */
  function ClassPath(
      dependencyFilter : DependencyFilter = null,
      dependencySelector : DependencySelector = null ) : List<File> {
    return Aether.resolveTransitively( :artifact = this,
          :dependencyFilter = dependencyFilter,
          :dependencySelector = dependencySelector)
        .ArtifactResults
        .where( \elt -> elt.Artifact.Extension == "jar" )
        .map( \ elt -> elt.ClassesDirectory )
  }

  /**
   * Build classpath using packaged versions of local projects
   */
  property get JarsPath() : List<File> {
    return Aether.resolveTransitively( :artifact = this ).ArtifactResults
        .where( \elt -> elt.Artifact.Extension == "jar" )
        .map( \ elt -> elt.Artifact.File )
  }

  property get ResolveClasses() : File {
    var result = Aether.resolve( this )
    return result.ClassesDirectory
  }

  property get Resolve() : File {
    return Aether.resolve( this ).Artifact.File
  }

  property get ModuleDir() : File {
    var pom = ArtifactDescriptorUtils.toPomArtifact( this )
    return Aether.resolve( pom ).Artifact.File.ParentFile
  }

  property get Descriptor() : ArtifactDescriptorResult {
    return Aether.resolveDescriptor( this )
  }

  property get DescriptorArtifact() : Artifact {
    var pom = ArtifactDescriptorUtils.toPomArtifact( this )
    return Aether.resolve( pom ).Artifact
  }

  property get MavenModel() : Model {
    return Aether.lookupMavenModel( this ).EffectiveModel
  }

  function WithClassifier( classifier : String ) : Artifact {
    return new DefaultArtifact( this.GroupId, this.ArtifactId, classifier, this.Extension, this.Version )
  }

  property get WorkspaceArtifact() : boolean {
    try {
      return Aether.resolve( this ).Repository.Id == VarkWorskpace.WORKSPACE_ID
    } catch (e : ArtifactResolutionException) {
      return false
    }
  }
}
