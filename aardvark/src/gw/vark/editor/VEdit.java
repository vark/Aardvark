package gw.vark.editor;

import gw.config.CommonServices;
import gw.internal.gosu.editor.DefaultContextMenuHandler;
import gw.internal.gosu.editor.GosuEditor;
import gw.internal.gosu.editor.ScriptChangeHandler;
import gw.internal.gosu.editor.undo.AtomicUndoManager;
import gw.internal.gosu.parser.GosuParser;
import gw.internal.gosu.parser.IGosuClassInternal;
import gw.lang.parser.IParseTree;
import gw.lang.parser.ITypeUsesMap;
import gw.lang.parser.ScriptabilityModifiers;
import gw.lang.parser.StandardSymbolTable;
import gw.lang.parser.statements.IFunctionStatement;
import gw.lang.shell.Gosu;
import gw.util.GosuExceptionUtil;
import gw.util.GosuStringUtil;
import gw.util.ProcessStarter;
import gw.util.Shell;
import gw.util.StreamUtil;
import gw.vark.Aardvark;
import gw.vark.annotations.Depends;
import gw.vark.launch.AardvarkMain;
import gw.vark.launch.Launcher;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.ExitStatusException;
import org.apache.tools.ant.Project;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class VEdit implements AardvarkMain
{
  private static final String DEFAULT_BUILD_FILE_NAME = "build.vark";

  static final int EXITCODE_VARKFILE_NOT_FOUND = 0x2;
  private File _varkFile;
  private String _lastSavedContent;
  private GosuEditor _editor;
  private JFrame _mainFrame;
  private JSplitPane _splitPane;
  private JPanel _outputPanel;
  private JTextArea _outputArea;
  private Thread _backgroundThread;
  private AtomicUndoManager _undoMgr;

  public static void main( String... args ) {
    VEdit a = new VEdit();
    a.start(args);
  }

  public int start(String... args) {
    Aardvark.setProject(new Project());
    String file = null;
    for (int i = 0; i < args.length; i++) {
      if (args[i].equals("-f") || args[i].equals("--file")) {
        if (i == args.length - 1 || args[i + 1].startsWith("-")) {
          throw new IllegalArgumentException("\"" + args[i] + "\" is expected to be followed by a parameter");
        }
        file = args[++i];
      }
    }

    try {
      _varkFile = findVarkFile(file);
    }
    catch (FileNotFoundException e) {
      log(e.getMessage());
      return EXITCODE_VARKFILE_NOT_FOUND;
    }
    log("Buildfile: " + _varkFile);

    Gosu.initGosu(_varkFile, getSystemClasspath());

    int exitCode = 1;
    try {
      try {
        showEditor();
        exitCode = 0;
      } catch (ExitStatusException ese) {
        exitCode = ese.getStatus();
        if (exitCode != 0) {
          throw ese;
        }
      }
    } catch (BuildException e) {
      log(e.getMessage( ));
    } catch (Throwable e) {
      e.printStackTrace();
      log(e.getMessage());
    }
    return exitCode;
  }

  private void showEditor() throws Exception {

    UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());

    _undoMgr = new AtomicUndoManager();
    _editor = new GosuEditor( null,
                              new StandardSymbolTable( true ),
                              _undoMgr,
                              ScriptabilityModifiers.SCRIPTABLE,
                              new DefaultContextMenuHandler(),
                              false, true );
    _editor.parseAndWaitForParser();
    IGosuClassInternal baseClass = (IGosuClassInternal) Aardvark.getAardvarkFileBaseClass();
    baseClass.putClassMembers((GosuParser) _editor.getParser(), _editor.getSymbolTable(), baseClass, true);

    _editor.setProgramSuperType(baseClass);
    ITypeUsesMap map = CommonServices.getGosuIndustrialPark().createTypeUsesMap(getDefaultTypeUsesPackages());
    _editor.setTypeUsesMap(map);
    _editor.getEditor().addKeyListener(new KeyAdapter() {
      @Override
      public void keyPressed(KeyEvent e) {
        super.keyPressed(e);    //To change body of overridden methods use File | Settings | File Templates.
      }

      @Override
      public void keyReleased(KeyEvent e) {
        super.keyReleased(e);    //To change body of overridden methods use File | Settings | File Templates.
      }

      @Override
      public void keyTyped(KeyEvent e) {
        _mainFrame.getMenuBar().dispatchEvent(e);
      }
    });

    loadFile(false);

    // establish undo manager
    ScriptChangeHandler handler = new ScriptChangeHandler( _undoMgr );
    handler.establishUndoableEditListener( _editor );

    _mainFrame = new JFrame("VEdit");
    _mainFrame.getContentPane().setLayout(new BorderLayout());
    _outputPanel = new JPanel();
    _splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, _editor, _outputPanel );
    makeOutpuPanel();
    _mainFrame.getContentPane().add(_splitPane, BorderLayout.CENTER);
    _mainFrame.setMenuBar(buildMenuBar());
    _mainFrame.addWindowListener(new WindowAdapter() {
      @Override
      public void windowOpened(WindowEvent e) {
        SwingUtilities.invokeLater(new Runnable(){
          @Override
          public void run() {
            _splitPane.setDividerLocation(1.0);
            SwingUtilities.invokeLater(new Runnable() {
              @Override
              public void run() {
                _editor.parseAndWaitForParser();
              }
            });
          }
        });        
      }

      @Override
      public void windowClosing(WindowEvent e) {
        if (needsSaving()) {
          int i = JOptionPane.showConfirmDialog(_mainFrame, "Save Changes?", "Save Changes?", JOptionPane.YES_NO_OPTION);
          if (i == JOptionPane.YES_OPTION) {
            save();
          }
        }
      }
    });
    _mainFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    _mainFrame.pack();
    _mainFrame.setSize(1024, 1000);
    _mainFrame.setVisible(true);
  }

  private void makeOutpuPanel() {
    _outputPanel.setLayout(new BorderLayout());
    JPanel outputTop = new JPanel();
    _outputPanel.add(outputTop, BorderLayout.NORTH);
    outputTop.setBorder(BorderFactory.createEmptyBorder(2, 2, 2, 2));
    outputTop.setLayout(new FlowLayout(FlowLayout.LEFT));
    outputTop.add(new JLabel("Output:"));
    outputTop.add(new JButton(new AbstractAction("Hide") {
      @Override
      public void actionPerformed(ActionEvent e) {
        _splitPane.setDividerLocation(1.0);
      }
    }));
    outputTop.add(new JButton(new AbstractAction("Clear") {
      @Override
      public void actionPerformed(ActionEvent e) {
        _outputArea.setText("");
      }
    }));
    _outputArea = new JTextArea();
    _outputArea.setEditable(false);
    _outputPanel.add(new JScrollPane(_outputArea), BorderLayout.CENTER);
    _outputArea.setLineWrap(true);
    _outputArea.setWrapStyleWord(true);
  }

  private boolean needsSaving() {
    return !_lastSavedContent.equals(_editor.getText());
  }

  private void loadFile(boolean warnOnOverwrite) {
    try {
      String content;
      if (_varkFile.exists()) {
        FileReader in = new FileReader(_varkFile);
        content = StreamUtil.getContent(in);
        in.close();
      } else {
        content = "";
      }

      content = GosuStringUtil.replace(content, "\r\n", "\n");

      if (warnOnOverwrite && _lastSavedContent != null && !_lastSavedContent.equals(content)) {
        if (JOptionPane.showConfirmDialog(null, "Do you want to overwrite local changes?") != JOptionPane.YES_OPTION) {
          return;
        }
      }

      _lastSavedContent = content;
      _editor.read(null, _lastSavedContent, _varkFile.getAbsolutePath());
      _editor.refreshed();
    } catch (IOException e) {
      throw GosuExceptionUtil.forceThrow(e);
    }
  }

  private boolean save() {
    try {
      if (!_varkFile.exists()) {
        _varkFile.createNewFile();
      }
      FileWriter writer = new FileWriter(_varkFile);
      String s = _editor.getText();
      writer.write(s);
      _lastSavedContent = s;
      writer.close();
      return true;
    } catch (Exception e) {
      JOptionPane.showMessageDialog(null, e.getMessage());
      return false;
    }
  }

  private MenuBar buildMenuBar() {
    MenuBar bar = new MenuBar();
    Menu file = new Menu("File");
    bar.add(file);

    MenuItem save = new MenuItem("Save");
    save.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent e) {
        save();
      }
    });
    save.setShortcut(new MenuShortcut(KeyEvent.VK_S));
    file.add(save);

    MenuItem reload = new MenuItem("Reload");
    reload.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent e) {
        if (needsSaving() &&
                JOptionPane.showConfirmDialog(null, "Are you sure you wish to discard your edits?") != JOptionPane.YES_OPTION) {
          return;
        }
        loadFile(true);
      }
    });
    reload.setShortcut(new MenuShortcut(KeyEvent.VK_R, true));
    file.add(reload);

    MenuItem execShellScript = new MenuItem("Execute Shell Script");
    execShellScript.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent e) {
        if (needsSaving()) {
          if (JOptionPane.showConfirmDialog(null, "File needs saving.  Save now?") != JOptionPane.YES_OPTION || !save()) {
            return;
          }
        }
        String shellCommand = JOptionPane.showInputDialog(null, "Enter command to run on file : ");
        String s = Shell.exec(shellCommand + " " + _varkFile.getAbsolutePath());
        System.out.println(s);
        if (contentOnDiskChanged() &&
                JOptionPane.showConfirmDialog(null, "File changed on disk.  Reload?" ) == JOptionPane.YES_OPTION ) {
          loadFile(false);
        }
      }
    });
    execShellScript.setShortcut(new MenuShortcut(KeyEvent.VK_X, true));
    file.add(execShellScript);

    file.addSeparator();

    MenuItem quit = new MenuItem("Quit");
    quit.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent e) {
        if (needsSaving()) {
          if (save()) {
            System.exit(0);
          }
        }
        else
        {
          System.exit(0);
        }
      }
    });
    quit.setShortcut(new MenuShortcut(KeyEvent.VK_Q, true));
    file.add(quit);

    Menu code = new Menu("Code");
    bar.add(code);

    MenuItem runTarget = new MenuItem("Run Target Under Cursor");
    runTarget.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent e) {
        _editor.parseAndWaitForParser();
        IParseTree deepest = _editor.getDeepestLocationAtCaret();
        IFunctionStatement target = findCurrentFunction(deepest);
        if (!isValidTarget(target)) {
          if (target == null) {
            JOptionPane.showMessageDialog(null, "Place the cursor in the target you wish to run.");
          } else {
            JOptionPane.showMessageDialog(null, "The currently selected function " + target.getDynamicFunctionSymbol().getDisplayName() + " is not a valid target.");
          }
          return;
        }
        String command = "java -cp " + makeClasspath() + " " + makeAardvarkDevFlag() + " " + Launcher.class.getName() + " -f " + _varkFile.getAbsolutePath() + " " + target.getFunctionName();
        final ProcessStarter proc = Shell.buildProcess(command)
                .withStdErrHandler(new ProcessStarter.OutputHandler() {
                  @Override
                  public void handleLine(final String line) {
                    SwingUtilities.invokeLater(new Runnable(){
                      @Override
                      public void run() {
                        output(line + "\n");
                      }
                    });
                  }
                }).withStdOutHandler(new ProcessStarter.OutputHandler() {
                  @Override
                  public void handleLine(final String line) {
                    SwingUtilities.invokeLater(new Runnable() {
                      @Override
                      public void run() {
                        output(line + "\n");
                      }
                    });
                  }
                });
        showOutputArea();
        _outputArea.setText("");
        output(command + "\n\n");
        _backgroundThread = new Thread() {
          @Override
          public void run() {
            proc.exec();
          }
        };
        _backgroundThread.start();
      }
    });
    runTarget.setShortcut(new MenuShortcut(KeyEvent.VK_R));
    code.add(runTarget);

    MenuItem showOutputArea = new MenuItem("Show Output");
    showOutputArea.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent e) {
        showOutputArea();
      }
    });
    code.add(showOutputArea);

    return bar;
  }

  private void showOutputArea() {
    _splitPane.setDividerLocation(.8);
  }

  private void output(String line) {
    _outputArea.append(line);
    _outputArea.setCaretPosition(_outputArea.getDocument().getLength());
  }

  private String makeAardvarkDevFlag() {
    if ("true".equals(System.getProperty("aardvark.dev"))) {
      return "-Daardvark.dev=true";
    } else {
      return "";
    }
  }

  private String makeClasspath() {
    return System.getProperty("java.class.path");
  }

  private boolean isValidTarget(IFunctionStatement target) {
    return target != null &&
            target.getDynamicFunctionSymbol() != null &&
            target.getDynamicFunctionSymbol().getModifierInfo() != null &&
            !(Modifier.isPrivate(target.getDynamicFunctionSymbol().getModifierInfo().getModifiers()) ||
                    Modifier.isProtected(target.getDynamicFunctionSymbol().getModifierInfo().getModifiers())) &&
            target.getDynamicFunctionSymbol().getArgs().size() == 0;
  }

  private IFunctionStatement findCurrentFunction(IParseTree deepest) {
    if (deepest == null) {
      return null;
    } else if (deepest.getParsedElement() instanceof IFunctionStatement) {
      return (IFunctionStatement) deepest.getParsedElement();
    } else {
      return findCurrentFunction(deepest.getParent());
    }
  }

  private boolean contentOnDiskChanged() {
    try {
      String content;
      if (_varkFile.exists()) {
        FileReader in = new FileReader(_varkFile);
        content = StreamUtil.getContent(in);
        in.close();
      } else {
        content = "";
      }
      content = GosuStringUtil.replace(content, "\r\n", "\n");
      if (_lastSavedContent != null && !_lastSavedContent.equals(content)) {
        return true;
      } else {
        return false;
      }
    } catch (IOException e) {
      throw GosuExceptionUtil.forceThrow(e);
    }
  }


  private File findVarkFile( String fileFromArgs ) throws FileNotFoundException {
    File varkFile;
    if( fileFromArgs != null )
    {
      varkFile = new File( fileFromArgs );
      if ( !varkFile.exists() )
      {
        throw new FileNotFoundException( "Specified vark buildfile \"" + fileFromArgs + "\" doesn't exist" );
      }
    }
    else {
      varkFile = new File( DEFAULT_BUILD_FILE_NAME );
      if (!varkFile.exists()) {
        log("Could not find a vark file.  One will be created in the current working directory.");
      }
    }
    try {
      return varkFile.getCanonicalFile();
    } catch (IOException e) {
      log("Could not get canonical file (" + varkFile.getPath() + ") - using absolute file instead.");
      return varkFile.getAbsoluteFile();
    }
  }

  static List<File> getSystemClasspath()
  {
    ArrayList<File> files = new ArrayList<File>();
    for( String file : System.getProperty( "java.class.path" ).split( File.pathSeparator ) )
    {
      files.add( new File( file ) );
    }
    return files;
  }

  private static List<String> getDefaultTypeUsesPackages()
  {
    return Arrays.asList( Depends.class.getPackage().getName() + ".*" );
  }

  private void log(String message) {
    System.out.println(message);
  }

}
