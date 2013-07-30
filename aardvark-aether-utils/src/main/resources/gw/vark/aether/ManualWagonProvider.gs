package gw.vark.aether

uses org.sonatype.aether.connector.wagon.WagonProvider
uses org.apache.maven.wagon.providers.file.FileWagon
uses org.apache.maven.wagon.providers.http.HttpWagon
uses java.lang.IllegalStateException

/**
 */
class ManualWagonProvider implements WagonProvider {
  override function lookup(roleHint: java.lang.String): org.apache.maven.wagon.Wagon {
    if (roleHint == "http") {
      return new HttpWagon();
    } else if (roleHint == "file") {
      return new FileWagon();
    }
    throw new IllegalStateException("Cannot find wagon for '${roleHint}'!")
  }

  override function release(wagon: org.apache.maven.wagon.Wagon) {
  }
}