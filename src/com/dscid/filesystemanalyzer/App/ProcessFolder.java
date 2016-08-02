package com.dscid.filesystemanalyzer.App;

/**
 * http://docs.oracle.com/javase/tutorial/displayCode.html?code=http://docs.oracle.com/javase/tutorial/essential/io/examples/WatchDir.java
 */
import static java.nio.file.LinkOption.NOFOLLOW_LINKS;
import static java.nio.file.StandardWatchEventKinds.ENTRY_CREATE;
import static java.nio.file.StandardWatchEventKinds.ENTRY_DELETE;
import static java.nio.file.StandardWatchEventKinds.ENTRY_MODIFY;
import static java.nio.file.StandardWatchEventKinds.OVERFLOW;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.lang.reflect.InvocationTargetException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.dscid.filesystemanalyzer.SimpleObservableVisitor;
import com.dscid.filesystemanalyzer.analyzers.FileAnalyzer;
import com.dscid.filesystemanalyzer.analyzers.FileAnalyzerAPI;
import com.dscid.filesystemanalyzer.processors.DataProcessorsAPI;

//import org.apache.commons.io.FileUtils;

/**
 * Example to watch a directory (or tree) for changes to files.
 */
public class ProcessFolder {

  private final WatchService watcher;
  private final Map<WatchKey, Path> keys;
  static boolean recursive;
  public static String watchDirectory;
  public static String commandsOutPut;
  public static String commandsOS;
  public static String commandsComment;
  public static String commandsRowOperations;
  public static String commandsOperationSkipRow = "last";
  public static String logFolder = "logs";
  public static String logFile = logFolder + "/output.log";
  public static Integer showBiggestItems;
  public static boolean skipProcessed;

  private boolean trace = true;
  public static final Boolean refreshDB = skipProcessed;
  private static final Logger logger = Logger.getLogger(ProcessFolder.class.getName());
  
  private static void loadProperties() {
    Properties prop = new Properties();
    InputStream input = null;
    try {
      input = new FileInputStream("settings.properties");

      // load a properties file
      prop.load(input);

      // get the property value and print it out
      recursive = "yes".equals(prop.getProperty("recurse").toLowerCase());
      String watchDirectoryPropertyValue = prop.getProperty("watchDirectory");
      commandsOS = prop.getProperty("commandsOS");
      commandsOutPut = prop.getProperty("commandsOutPut", "cmds.log");
      commandsComment = prop.getProperty("commandsComment", "# ");
      commandsRowOperations = prop.getProperty("commandsRowOperations", "# ");
      commandsOperationSkipRow = prop.getProperty("commandsOperationSkipRow", "last");
      showBiggestItems = Integer.valueOf(prop.getProperty("showBiggestItems", "-1"));
      skipProcessed = Boolean.valueOf(prop.getProperty("skipProcessed", "False"));
      

      logFolder = prop.getProperty("logFolder");
      logFile = logFolder + "/" + prop.getProperty("logFile");

      File wf = new File(watchDirectoryPropertyValue);
      if (wf.exists() && wf.isDirectory()) {
        watchDirectory = watchDirectoryPropertyValue;
      }
      else {
        String exceptionMessage = String.format("The provided folder:<%s> is not valid folder or the app has no access", watchDirectoryPropertyValue);
        throw new IllegalArgumentException(exceptionMessage);
      }

    }
    catch (IOException ex) {
      ex.printStackTrace();
    }
    finally {
      if (input != null) {
        try {
          input.close();
        }
        catch (IOException e) {
          e.printStackTrace();
        }
      }
    }
  }

  @SuppressWarnings("unchecked")
  static <T> WatchEvent<T> cast(WatchEvent<?> event) {
    return (WatchEvent<T>) event;
  }

  /**
   * Register the given directory with the WatchService
   */
  private void register(Path dir) throws IOException {
    WatchKey key = dir.register(watcher, ENTRY_CREATE, ENTRY_DELETE, ENTRY_MODIFY);
    if (trace) {
      Path prev = keys.get(key);
      if (prev == null) {
        System.out.format("register: %s\n", dir);
      }
      else {
        if (!dir.equals(prev)) {
          System.out.format("update: %s -> %s\n", prev, dir);
        }
      }
    }

    // this should be in the SQLite fileSystemAnalyzer.DB?
    // here if the item is not registered, do so, and pass it to the
    // registered analyzers
    // based on the type (file or dir) to a
    keys.put(key, dir);
  }

  /**
   * Register the given directory, and all its sub-directories, with the
   * WatchService.
   */
  private void registerAll(final Path start) {
    try {
      // register directory and sub-directories
      Files.walkFileTree(start, new SimpleObservableVisitor());
    }
    catch (IOException ex) {
      System.err.println(ex);
    }
  }

  /**
   * Creates a WatchService and registers the given directory
   */
  ProcessFolder(Path dir, boolean recursive) throws IOException {
    this.watcher = FileSystems.getDefault().newWatchService();
    this.keys = new HashMap<WatchKey, Path>();
    this.recursive = recursive;

    if (recursive) {
      Date t0 = new Date();
      System.out.format("Scanning %s ... \n", dir);
      registerAll(dir);
      Date t1 = new Date();
      System.out.format("%nDone. (%s ms) %n", (t1.getTime() - t0.getTime()));
    }
    else {
      register(dir);
    }

    // enable trace after initial registration
    this.trace = true;
  }

  /**
   * Process all events for keys queued to the watcher
   */
  void processEvents() {
    for (;;) {

      // wait for key to be signaled
      WatchKey key;
      try {
        key = watcher.take();
      }
      catch (InterruptedException x) {
        return;
      }

      Path dir = keys.get(key);
      if (dir == null) {
        System.err.println("WatchKey not recognized!! " + dir.toString());
        continue;
      }

      for (WatchEvent<?> event : key.pollEvents()) {
        // StandardWatchEventKinds
        WatchEvent.Kind kind = event.kind();

        // TBD - provide example of how OVERFLOW event is handled
        if (kind == OVERFLOW) {
          continue;
        }

        // Context for directory entry event is the file name of entry
        WatchEvent<Path> ev = cast(event);
        Path name = ev.context();
        Path child = dir.resolve(name);

        // print out event
        System.out.format("%s: %s\n", event.kind().name(), child);

        // if directory is created, and watching recursively, then
        // register it and its sub-directories
        if (recursive && (kind == ENTRY_CREATE)) {
          if (Files.isDirectory(child, NOFOLLOW_LINKS)) {
            registerAll(child);
          }
        }
        // After all checks are done: instantiate an analyzer and pass
        // the path
        // if it is a directory then of sub type bundle
        // If a file of type quantum

      }

      // reset key and remove from set if directory no longer accessible
      boolean valid = key.reset();
      if (!valid) {
        keys.remove(key);

        // all directories are inaccessible
        if (keys.isEmpty()) {
          break;
        }
      }
      System.out.println("Done.");
    }
    System.exit(0);
  }

  static void usage() {
    System.err.println("usage: java ProcessFolder [-r] dir");
    System.exit(-1);
  }

  public static void main(String[] args) throws IOException {
    // load properties
    init();

    // parse arguments
    // if (args.length == 0 || args.length > 2) {
    // usage();
    // }

    // boolean recursive = false;
    // int dirArg = 0;
    // if (args[0].equals("-r")) {
    // if (args.length < 2) {
    // usage();
    // }
    // recursive = true;
    // dirArg++;
    // } else if (args[0].equals("-p") || args[0].equals("-p")) {
    // //run all reports, for now
    // processProcessed();
    // }
    Boolean refreshFileData = ProcessFolder.refreshDB;
    try {
      instantiateAnalizers();
    }
    catch (NoSuchMethodException | SecurityException | ClassNotFoundException | IllegalAccessException | IllegalArgumentException | InvocationTargetException ex) {
      System.err.println("An exception occur, these are the details" + ex.getMessage());
    }
    catch (NoSuchFieldException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
    // register directory and process its events
    Path dir = Paths.get(watchDirectory);
    if (/*refreshFileData && */ Files.isWritable(dir)) {
      System.out.println("Analyzing folder " + watchDirectory);
      ProcessFolder p = new ProcessFolder(dir, recursive);
      System.out.println("Start processing folder " + watchDirectory);
      processProcessed();
      // p.processEvents();
    }
    else {
      System.err.println("Cannot access the folder '" + watchDirectory + "' for processing");
    }

  }

  private static void init() {
    loadProperties();

    reRouteOutput();

  }

  private static void reRouteOutput() {
    new File(logFolder).mkdirs();
    try {
      System.setOut(new PrintStream(new BufferedOutputStream(new FileOutputStream(logFile)), true));
    }
    catch (FileNotFoundException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
    System.out.println("=====================================================================================\n" + logFile + "\n=====================================================================================");
    System.err.println("System.out is being sent to:" + logFile);

  }

  private static void processProcessed() {
    DataProcessorsAPI.INSTANCE.processData();
  }

  private static void instantiateAnalizers() throws NoSuchMethodException, SecurityException, ClassNotFoundException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, NoSuchFieldException {
    Set<FileAnalyzer> al = FileAnalyzerAPI.INSTANCE.getAnalyzerProviders();
    System.out.println(al);
    for (FileAnalyzer fa : al) {
      logger.log(Level.INFO, "--------- Using analiser: {0} ", fa.getClass().getName());
    }
    SimpleObservableVisitor.setPluggings(al);
  }
}
