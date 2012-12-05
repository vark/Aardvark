package gw.vark.idea;

import com.intellij.openapi.fileEditor.OpenFileDescriptor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.FileViewProvider;
import gw.config.CommonServices;
import gw.lang.parser.ITypeUsesMap;
import gw.lang.parser.ParserOptions;
import gw.lang.reflect.IType;
import gw.lang.reflect.TypeSystem;
import gw.lang.reflect.module.IModule;
import gw.plugin.ij.filetypes.IGosuFileTypeProvider;
import gw.plugin.ij.icons.GosuIcons;
import gw.plugin.ij.lang.psi.IGosuFile;
import gw.plugin.ij.lang.psi.impl.GosuFragmentFileImpl;
import gw.plugin.ij.lang.psi.impl.GosuProgramFileImpl;
import gw.plugin.ij.lang.psi.impl.GosuScratchpadFileImpl;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.util.Arrays;
import java.util.List;

public class AardvarkProgramFileProvider implements IGosuFileTypeProvider {
  public static final String EXT_PROGRAM = "vark";
  public static final List<String> DEFAULT_USES = Arrays.asList("gw.vark.annotations.*", "gw.vark.antlibs.*");

  public static boolean isScratchpad(@NotNull VirtualFile file) {
    return isProgram(file) && file.getName().startsWith(GosuScratchpadFileImpl.GOSU_SCRATCHPAD_NAME);
  }

  public static boolean isDebuggerFragement(@NotNull VirtualFile file) {
    return isProgram(file) && file.getName().contains( "Gosu_Frag" );
  }

  public static boolean isProgram(@Nullable VirtualFile file) {
    return file != null && EXT_PROGRAM.equals(file.getExtension());
  }

  @NotNull
  @Override
  public IGosuFile createGosuFile(@NotNull FileViewProvider viewProvider) {
    if (isScratchpad(viewProvider.getVirtualFile())) {
      return new GosuScratchpadFileImpl(viewProvider) {
        @Override
        protected ParserOptions createParserOptions() {
          return createParserOptionsInternal(getModule());
        }
      };
    }
    else if( isDebuggerFragement( viewProvider.getVirtualFile() ) ) {
      return new GosuFragmentFileImpl( viewProvider ) {
        @Override
        protected ParserOptions createParserOptions() {
          return createParserOptionsInternal(getModule());
        }
      };
    }
    else {
      return new GosuProgramFileImpl(viewProvider) {
        @Override
        protected ParserOptions createParserOptions() {
          return createParserOptionsInternal(getModule());
        }
      };
    }
  }

  private ParserOptions createParserOptionsInternal(IModule module) {
    // FIXME: module is wrong for some reason.
    module = TypeSystem.getGlobalModule();
    IType supertype = TypeSystem.getByFullName("gw.vark.AardvarkFile", module);
    ITypeUsesMap typeUsesMap = CommonServices.getGosuIndustrialPark().createTypeUsesMap(DEFAULT_USES);
    return new ParserOptions()
            .withSuperType(supertype)
            .withTypeUsesMap(typeUsesMap);
  }

  @Nullable
  @Override
  public Icon getIcon(@NotNull VirtualFile file) {
    return isScratchpad(file) ? GosuIcons.FILE_SCRATCHPAD : GosuIcons.FILE_PROGRAM;
  }

  public OpenFileDescriptor getOpenFileDescriptor(Project project, VirtualFile virtualFile, int offset) {
    return new OpenFileDescriptor(project, virtualFile, offset);
  }
}
