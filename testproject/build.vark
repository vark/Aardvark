var buildDir = file("build")
var classesDir = buildDir.file("classes")
var testClassesDir = buildDir.file("testclasses")
var distDir = buildDir.file("dist")

function echoHello() {
  Ant.echo(:message = "Hello World")
}

function nap() {
  Ant.echo(:message = "Sleeping 1.5 sec")
  Ant.sleep(:seconds = 1, :milliseconds = 500)
  Ant.echo(:message = "Done sleeping")
}

function setup() {
  Ant.mkdir(:dir = buildDir)
}

@Depends("setup")
function compile() {
  Ant.mkdir(:dir = classesDir)
  Ant.javac(:srcdir = path(file("src")),
            :destdir = classesDir,
            :includeantruntime = false)
}

@Depends("compile")
function compileTests() {
  Ant.mkdir(:dir = testClassesDir)
  Ant.javac(:srcdir = path(file("test")),
            :destdir = testClassesDir,
            :classpath = path(file("lib").fileset("junit*.jar")).withFile(classesDir),
            :includeantruntime = false)
}

@Depends("compile")
function jar() {
  Ant.mkdir(:dir = distDir)
  Ant.jar(:destfile = distDir.file("testproject.jar"),
          :basedir = classesDir)
}

@Depends("compileTests")
function test() {
  Ant.junit(:haltonfailure = true, :printsummary = Yes,
    :classpathBlocks = {
      \ cp -> cp.withFileSet(file("lib").fileset("junit*.jar")),
      \ cp -> cp.withFile(classesDir),
      \ cp -> cp.withFile(testClassesDir)
    }, :batchtestBlocks = {
      \ bt -> bt.addFileSet(testclassesDir.fileset("gw/vark/test/HelloWorldTest.class"))
    })
}

@Depends("setup")
function writeAndZipSomeStuff() {
  var stuff = buildDir.file("stuff")
  var a = stuff.file("a")
  Ant.mkdir(:dir = a)
  Ant.echo(:message = "text1\n", :file = a.file("foo.txt"))
  Ant.echo(:message = "text2\n", :file = a.file("foo.txt"), :append = true)
  var b = stuff.file("b")
  Ant.mkdir(:dir = b)
  Ant.echo(:message = "text3\n", :file = b.file("bar.txt"))
  Ant.echo(:message = "text4\n", :file = b.file("bar.txt"), :append = false)
  Ant.echo(:message = "text5\ntext6\n", :file = b.file("baz.txt"))
  var c = b.file("c")
  Ant.mkdir(:dir = c)
  Ant.echo(:message = "text7\n", :file = c.file("dontreadme.txt"))
  Ant.zip(:destfile = buildDir.file("stuff1.zip"), :basedir = stuff)
  Ant.zip(:destfile = buildDir.file("stuff2.zip"), :filesetList = { a.fileset(), b.fileset() })
}

@Depends({"jar", "writeAndZipSomeStuff"})
function run() {
  Ant.java(:classname = "gw.vark.test.HelloWorld",
           :classpath = path(file("lib").fileset()).withFile(classesDir),
           :fork = true)
}

function clean() {
  Ant.delete(:dir = buildDir)
}
