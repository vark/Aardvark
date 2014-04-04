package gw.vark.codegen;

import com.guidewire.codegen.CGContext;
import com.guidewire.codegen.declarations.CGClass;
import gw.vark.codegen.internal.Generator;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

public class AntlibCodegen {
  private final Path _output;
  private final List<Path> _antlibs;
  private final List<Path> _libraries;

  private AntlibCodegen(Path output, List<Path> antlibs, List<Path> libraries) {
    _output = output;
    _antlibs = antlibs;
    _libraries = libraries;
  }

  public void generate() throws IOException {
    Generator generator = new Generator(_libraries);
    for (Path source : _antlibs) {
      List<CGClass> classes = generator.generateAntlib(source);
      writeGeneratedCode(_output, classes);
    }
  }

  private static void writeGeneratedCode(Path output, Collection<CGClass> classes) throws IOException {
    for (CGClass topClass : classes) {
      String filePath = topClass.getFilePath();
      Path targetFile = output.resolve(filePath);
      CGContext ctx = new CGContext(topClass);
      topClass.writeClassSource(ctx);
      updateFile(targetFile, ctx.getCode().getBytes(StandardCharsets.UTF_8));
    }
  }

  private static void updateFile(Path path, byte[] contents) throws IOException {
    boolean isUpToDate = false;
    if (Files.exists(path)) {
      byte[] current = Files.readAllBytes(path);
      isUpToDate = Arrays.equals(contents, current);
    }

    if (!isUpToDate) {
      Files.createDirectories(path.getParent());
      // FIXME: Not supported by vfsnio yet...
      //Files.setAttribute(path, "dos:readonly", false);
      Files.write(path, contents);
    }
  }


  public static class Builder {

    private Path _output;
    private List<Path> _antlibs = new ArrayList<>();
    private List<Path> _libraries = new ArrayList<>();


    public Builder withClassPath(Path path) {
      _libraries.add(path);
      return this;
    }

    public Builder withAntlib(Path path) {
      _antlibs.add(path);
      return this;
    }

    public Builder withOutput(Path path) {
      _output = path;
      return this;
    }

    public AntlibCodegen build() {
      return new AntlibCodegen(_output, _antlibs, _libraries);
    }
  }
}
