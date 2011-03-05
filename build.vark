/*
 * Copyright (c) 2010 Guidewire Software, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

uses gw.util.Shell
uses java.io.File
uses java.lang.System
uses java.util.HashMap
uses org.apache.tools.ant.BuildException
uses org.apache.tools.ant.taskdefs.optional.junit.JUnitTest

var rootDir = file( "" )
var buildDir = file( "build" )
var distDir = buildDir.file("aardvark")
var launcherModule = file( "launcher" )
var aardvarkModule = file( "aardvark" )
var aardvarkTestModule = file( "aardvark-test" )
var releasesDepotPath = "//depot/aardvark/..."
var version : String

function compileLauncher() {
  var classesDir = launcherModule.file( "classes" )
  Ant.mkdir(:dir = classesDir)
  Ant.copy(
          :filesetList = { launcherModule.file("src").fileset(null, "**/*.java") },
          :todir = classesDir,
          :includeemptydirs = false)
  Ant.javac(
          :srcdir = path(launcherModule.file("src")),
          :destdir = classesDir,
          :classpath = classpath(file("lib/ant/ant-launcher.jar")),
          :debug = true,
          :includeantruntime = false)
}

@Depends("compileLauncher")
function jarLauncher() {
  var destDir = launcherModule.file("dist")
  var classesDir = launcherModule.file("classes")
  Ant.mkdir(:dir = destDir)
  Ant.jar(
          :destfile = destDir.file("aardvark-launcher.jar"),
          :basedir = classesDir,
          :zipfilesetList = { rootDir.zipfileset(:includes = "LICENSE", :prefix = "META-INF") })
}

@Depends("compileLauncher")
function compileAardvark() {
  var classesDir = aardvarkModule.file( "classes" )
  Ant.mkdir(:dir = classesDir)
  Ant.copy(
          :filesetList = { aardvarkModule.file("src").fileset(null, "**/*.java") },
          :todir = classesDir,
          :includeemptydirs = false)
  if (version != null) {
    classesDir.file("gw/vark/version.txt").write(version)
  }
  Ant.javac(
          :srcdir = path(aardvarkModule.file("src")),
          :destdir = classesDir,
          :classpath = classpath( rootDir.fileset( "lib/ant/*.jar,lib/gosu/gw-gosu-core-api.jar,lib/gosu/gw-gosu-core.jar", null ) )
              .withFile( launcherModule.file("classes" ) ),
          :debug = true,
          :includeantruntime = false)
}

@Depends("compileAardvark")
function jarAardvark() {
  var destDir = aardvarkModule.file("dist")
  var classesDir = aardvarkModule.file("classes")
  Ant.mkdir(:dir = destDir)
  Ant.jar(
          :destfile = destDir.file("aardvark.jar"),
          :basedir = classesDir,
          :zipfilesetList = { rootDir.zipfileset(:includes = "LICENSE", :prefix = "META-INF") })
}

@Depends("compileAardvark")
function compileAardvarkTest() {
  var classesDir = aardvarkTestModule.file( "classes" )
  Ant.mkdir(:dir = classesDir)
  Ant.javac(
          :srcdir = path(aardvarkTestModule.file("src")),
          :destdir = classesDir,
          :classpath = classpath( rootDir.fileset( "lib/ant/*.jar,lib/gosu/gw-gosu-core-api.jar,lib/test/*.jar", null ) )
              .withFile( launcherModule.file("classes" ) )
              .withFile( aardvarkModule.file("classes" ) ),
          :debug = true,
          :includeantruntime = false)
}

@Depends({"compileLauncher", "compileAardvark", "compileAardvarkTest"})
function compile() {
}

/*
 * Creates the aardvark jars
 */
@Depends({"jarLauncher", "jarAardvark"})
function jar() {
}

/*
 * Runs the tests
 */
@Depends("compile")
function test() {
  Ant.junit(:fork = true, :printsummary = Yes,
  /*
    :jvmargBlocks = {
      \ jvmarg -> jvmarg.setValue("-Xdebug"),
      \ jvmarg -> jvmarg.setValue("-Xrunjdwp:transport=dt_socket,server=y,suspend=n,address=5005")
    },
  */
    :classpathBlocks = {
      \ p -> p.withFileset(rootDir.fileset("lib/ant/*.jar,lib/ivy/*.jar,lib/gosu/*.jar,lib/test/*.jar", null)),
      \ p -> p.withFile(launcherModule.file("classes")),
      \ p -> p.withFile(aardvarkModule.file("classes")),
      \ p -> p.withFile(aardvarkTestModule.file("classes"))
    },
    :testList = {
      new JUnitTest("gw.vark.AardvarkSuite")
    })
}

/*
 * Creates the aardvark distribution
 */
@Depends("jar")
function dist() {
  if (version != null) {
    distDir = buildDir.file("aardvark-${version}")
  }
  Ant.mkdir(:dir = distDir)
  Ant.copy(
          :filesetList = { rootDir.fileset("LICENSE,bin/*,lib/ant/*,lib/gosu/*,lib/ivy/*", null) },
          :todir = distDir)
  Ant.copy(
          :filesetList = { rootDir.fileset("*/dist/aardvark*.jar", null) },
          :todir = distDir.file("lib"),
          :flatten = true
  )
}

@Depends({"clean", "calcVersion", "test", "dist"})
function release() {
  var zipName = distDir.Name
  Ant.zip(:destfile = buildDir.file("${zipName}.zip"), :zipfilesetList = { distDir.zipfileset(:prefix = zipName) })
  Ant.tar(:destfile = buildDir.file("${zipName}.tar"), :tarfilesetBlocks = {
    \ tfs -> {
      tfs.Dir = distDir
      tfs.setIncludes("bin/vark,bin/vedit")
      tfs.setFileMode("755")
      tfs.setPrefix(zipName)
    },
    \ tfs -> {
      tfs.Dir = distDir
      tfs.setExcludes("bin/vark,bin/vedit")
      tfs.setPrefix(zipName)
    }
  })
  Ant.gzip(:src = buildDir.file("${zipName}.tar"), :destfile = buildDir.file("${zipName}.tgz"))
}

function calcVersion() {
  var checkedInString = aardvarkModule.file("src/gw/vark/version.txt").read().trim()
  var fmt = new java.text.SimpleDateFormat("yyyyMMdd-hhmm") { :TimeZone = java.util.TimeZone.getTimeZone("GMT") }
  version = checkedInString + "-" + fmt.format(new java.util.Date())
}

function clean() {
  Ant.delete( :dir = buildDir )
  Ant.delete( :dir = launcherModule.file("classes") )
  Ant.delete( :dir = launcherModule.file("dist") )
  Ant.delete( :dir = aardvarkModule.file("classes") )
  Ant.delete( :dir = aardvarkModule.file("dist") )
  Ant.delete( :dir = aardvarkTestModule.file("classes") )
  Ant.delete( :dir = aardvarkTestModule.file("dist") )
}
