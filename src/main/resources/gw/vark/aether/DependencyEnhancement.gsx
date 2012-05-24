package gw.vark.aether

uses org.sonatype.aether.ant.org.apache.maven.model.Dependency

enhancement DependencyEnhancement : Dependency {

  property get Id() : String {
    return this.GroupId + ":" + this.ArtifactId + ":" + this.Type + ":" + this.Version
  }

  // TODO - should probably use Aether for this type of thing
  property get PathInMavenRepo() : String {
    return "${this.GroupId.replace(".", "/")}/${this.ArtifactId}/${this.Version}/${this.ArtifactId}-${this.Version}.jar"
  }

}
