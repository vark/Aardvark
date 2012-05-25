package gw.vark.maven

uses org.apache.tools.ant.types.Path
uses gw.vark.antlibs.Ant
uses org.apache.tools.ant.types.Commandline.Argument
uses gw.vark.IAardvarkUtils
uses java.io.File
uses java.util.List
uses java.util.ArrayList
uses java.lang.System

class Maven implements IAardvarkUtils {
  var _arguments : List<String> as Arguments
  var _goals : List<String> as Goals
  var _profiles : List<String> as Profiles
  var _alsoMake : boolean as AlsoMake
  var _dir : File as Dir
  var _failOnError : boolean as FailOnError = true
  var _skipTests : boolean as SkipTests = false

  static function clean(dir : File) {
    invoke(dir, "clean")
  }

  static function compile(dir : File) {
    invoke(dir, "compile")
  }

  static function pack(dir : File) {
    invoke(dir, "package")
  }

  static function invoke(dir : File, goal : String) {
    // Build only modules for the current application list
    new Maven() {
      :Dir = dir,
      :Goals = {goal}
    }.run()
  }

  function run() {
    var javaHome = getProperty("java.home")
    var m2HomeProp = System.getenv()["M2_HOME"]
    if (m2HomeProp == null) {
      throw "no M2_HOME environment variable set"
    }
    var m2Home = file(m2HomeProp)
    if (!m2Home.exists()) {
      throw "M2_HOME path does not exist on file system: ${m2HomeProp}"
    }
    var plexusClassworldsJar = m2Home.file("boot").file("plexus-classworlds-2.4.jar")
    var m2Conf = m2Home.file("bin").file("m2.conf")

    verifyFile(plexusClassworldsJar)
    verifyFile(m2Conf)

    Ant.java(:classname = "org.codehaus.plexus.classworlds.launcher.Launcher",
             :dir = _dir != null ? _dir : file(System.getProperty("java.io.tmpdir")),
             :fork = true,
             :failonerror = _failOnError,
             :classpathBlocks = { \ p -> p.withFile(plexusClassworldsJar) },
             :jvmargBlocks = {
                 \ arg -> arg.setLine("-Dclassworlds.conf=" + m2Conf.Path),
                 \ arg -> arg.setLine("-Dmaven.home=" + m2Home.Path)
             },
             :argBlocks = createArgs()
             )
  }

  private function createArgs() : List<block(arg : Argument)> {
    var argBlocks : List<block(arg : Argument)> = {}
    if (_profiles != null) {
      argBlocks.add(\ arg -> arg.setLine("-P" + _profiles.join(",")))
    }
    if (_alsoMake) {
      argBlocks.add(\ arg -> arg.setLine("-am"))
    }
    if (_skipTests) {
      argBlocks.add(\ arg -> arg.setLine("-DskipTests"))
    }
    for (argument in _arguments) {
      argBlocks.add(\ arg -> arg.setLine(argument))
    }
    for (goal in _goals) {
      argBlocks.add(\ arg -> arg.setLine(goal))
    }
    return argBlocks
  }

  private static function verifyFile(file : File) {
    if (!file.exists()) {
      throw "M2_HOME file not found: ${file}"
    }
  }
}
