package gw.vark.maven

uses gw.vark.IAardvarkUtils
uses gw.vark.antlibs.Ant
uses org.apache.maven.settings.Settings
uses org.apache.maven.settings.building.DefaultSettingsBuilderFactory
uses org.apache.maven.settings.building.DefaultSettingsBuildingRequest
uses org.codehaus.plexus.classworlds.ClassWorld

uses java.io.File
uses java.lang.System
uses org.sonatype.aether.util.artifact.DefaultArtifact
uses gw.vark.aether.Aether
uses java.util.Collection
uses org.sonatype.aether.artifact.Artifact
uses org.apache.maven.cli.MavenCli
uses gw.vark.Aardvark
uses org.apache.tools.ant.types.Path

class Maven implements IAardvarkUtils {

  enum MavenGoal {
    CLEAN, INITIALIZE, GENERATE_SOURCES, COMPILE, PROCESS_CLASSES, TEST_COMPILE, PACKAGE
  }

  // Global settings
  private static var _globalSettings : File as GlobalSettings

  // Settings
  private static var _settings : File
  static property get Settings( ) : File {
    return _settings ?: DefaultSettings
  }
  static property set Settings( settings : File ) {
    _settings = settings
  }
  static property get DefaultSettings( ) : File {
    return file(IAardvarkUtils.getProperty("user.home")).file(".m2").file("settings.xml")
    }

  // Read settings
  static property get CurrentSettings( ) : Settings {
    var settingsBuilder = new DefaultSettingsBuilderFactory().newInstance()
    var req = new DefaultSettingsBuildingRequest()
    req.setUserSettingsFile(Settings)
    req.setGlobalSettingsFile(GlobalSettings)
    var result = settingsBuilder.build(req)
    return result.EffectiveSettings
  }

  // Local repository location
  private static var _localRepository : File
  public static property get LocalRepository( ) : File {
    return _localRepository ?: DefaultLocalRepository
    }
  public static property set LocalRepository( repo : File ) {
    _localRepository = repo
    }
  static property get DefaultLocalRepository( ) : File {
    var path = CurrentSettings.LocalRepository
    if ( path != null ) {
      return file( path )
    }
    return file(IAardvarkUtils.getProperty("user.home")).file(".m2").file("repository")
  }

  static function execute(
      goal : MavenGoal = null,
      arguments : List<String> = null,
      goals : Collection<String> = null,
      profiles : Collection<String> = null,
      projects : Collection<String> = null,
      alsoMake : boolean = false,
      dir : File = null,
      settings : File = null,
      globalSettings : File = null,
      logFile : File = null,
      threads : String = "3",
      skipTests : boolean = true,
      batchMode : boolean = true,
      showErrors : boolean = true,
      quiet : boolean = false,
      debug : boolean = false,
      localRepository : File = null,
      offline : Boolean = null,
      updateSnapshots : boolean = false,
      nonRecursive : boolean = false) {

    var args : List<String> = {}
    var profs : List<String> = {}
    if (profiles != null) {
      profs.addAll(profiles)
    }
    if (!profs.Empty) {
      args.add("--activate-profiles")
      args.add(profs.join(","))
    }
    if (projects != null && !projects.Empty) {
      args.add("--projects")
      args.add(projects.join(","))
    }
    if (alsoMake) {
      args.add("--also-make")
    }

    // Add product.buildnumber property
    var buildnumber = getProperty("product.buildnumber")
    if (buildnumber != null) {
      args.add("-Dproduct.buildnumber=${buildnumber}")
    }

    if (threads != null && threads != "1") {
      args.add("--threads")
      args.add(threads)
    }
    if (batchMode) {
      args.add("--batch-mode")
    }
    if (showErrors) {
      args.add("--errors")
    }
    if (quiet) {
      args.add("--quiet")
    }
    if (settings != null) {
      args.add("--settings")
      args.add(settings.AbsolutePath)
    }
    if (globalSettings != null) {
      args.add("--global-settings")
      args.add(globalSettings.AbsolutePath)
    }
    if (logFile != null) {
      args.add("--log-file")
      args.add(logFile.AbsolutePath)
    }
    if (updateSnapshots) {
      args.add("--update-snapshots")
    }
    if (nonRecursive) {
      args.add("--non-recursive")
    }
    if (offline) {
      args.add("--offline")
    }
    if (arguments != null) {
      args.addAll(arguments)
    }
    if (goals != null) {
      args.addAll(goals)
    }
    if (goal != null) {
      args.add(goal.Name.toLowerCase().replace('_', '-'))
    }
    if (dir == null) {
      dir = file(".")
    }
    // FIXME-isd: Way to select how to start maven, embedded/external.
    if (localRepository == null) {
      localRepository = LocalRepository
    }
    startExternal( localRepository, args, dir )
    //startEmbedded( localRepo, args, dir )
    }

  private static function startExternal( localRepo : File, args : List<String>, dir : File ) {
    var path = new Path(Aardvark.getProject())

    // Resolve all artifacts at once (so their versions are properly unified)
    var p = Aether.resolveTransitively( :dependencies = mavenArtifacts() ).ArtifactResults
        .where( \elt -> elt.Artifact.Extension == "jar" )
        .map( \ elt -> elt.ClassesDirectory )
    path.append(p)

    var jvmargs = {
        "-Xmx1200m",
        "-XX:PermSize=400m",
        "-D${MavenCli.LOCAL_REPO_PROPERTY}=${localRepo.AbsolutePath}",
        "-Djava.awt.headless=true",
        "-Djava.nio.file.spi.DefaultFileSystemProvider=com.guidewire.pl.winfsfix.WrappedFileSystemProvider"
    }
    var mavenOpts = System.getenv()["MAVEN_OPTS"]
    if (mavenOpts != null) {
       jvmargs.addAll(mavenOpts.split(" ").toList())
    }
    Ant.java(
        :classname = "org.apache.maven.cli.MavenCli",
            :dir = dir,
        :fork = true,
        :classpath = path,
        :failonerror = true,
        :jvmargBlocks = jvmargs.toArgumentBlocks(),
        :argBlocks = args.toArgumentBlocks()
      )
  }

  private static function startEmbedded( localRepo : String, args : List<String>, dir : File ) {
      if (localRepo != null) {
        System.setProperty(MavenCli.LOCAL_REPO_PROPERTY, localRepo)
      } else {
        System.clearProperty(MavenCli.LOCAL_REPO_PROPERTY)
      }



      // MavenCli does not restore context classloader, start in a separate thread to isolate
      var tccl = java.lang.Thread.currentThread().ContextClassLoader
      try {
        var classWorld = new ClassWorld( "plexus.core", Maven.Type.BackingClass.ClassLoader );
        var result = new MavenCli( classWorld ).doMain( args.toTypedArray(), dir.AbsolutePath, null, null );
        if (result != 0) {
          java.lang.System.exit(result)
        }
      } finally {
        java.lang.Thread.currentThread().ContextClassLoader = tccl
      }
    }

    static function mavenArtifacts( ) : List<Artifact> {
      // FIXME: hard-coded maven versions!
      var mavenArtifacts = {
          "com.guidewire.build:maven-fixes:0.1.0",    // PL-25436
          "com.guidewire.pl.winfsfix:winfsfix:0.1.1", // PL-26775
          "org.apache.maven:maven-embedder:jar:3.0.4",
          "org.apache.maven.wagon:wagon-http:jar:2.3",
          "org.apache.maven.wagon:wagon-file:jar:2.3",
          "org.sonatype.aether:aether-connector-wagon:jar:1.13.1"
      }.map( \ elt -> new DefaultArtifact(elt) )
      return mavenArtifacts
  }
}
