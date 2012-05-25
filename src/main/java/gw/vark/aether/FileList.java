package gw.vark.aether;

import org.apache.tools.ant.types.ResourceCollection;
import org.apache.tools.ant.types.resources.FileResource;

import java.util.ArrayList;

/**
 * An implementation of {@link ResourceCollection} that is no more than an {@link ArrayList}
 * of {@link FileResource}s.  This can be passed to task functions which accept a <code>resources</code>
 * (<code>List&lt;ResourceCollection&gt;</code>) parameter, such as <code>gw.vark.antlibs.Ant.copy</code>.
 */
public class FileList extends ArrayList<FileResource> implements ResourceCollection {
  @Override
  public boolean isFilesystemOnly() {
    return true;
  }
}
