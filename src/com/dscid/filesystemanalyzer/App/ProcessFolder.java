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
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.dscid.filesystemanalyzer.SimpleObservableVisitor;
import com.dscid.filesystemanalyzer.DB.DBLayer;
import com.dscid.filesystemanalyzer.analyzers.FileAnalyzer;
import com.dscid.filesystemanalyzer.analyzers.FileAnalyzerAPI;
import com.dscid.filesystemanalyzer.processors.DataProcessorsAPI;
import com.dscid.filesystemanalyzer.processors.SupportedProcessors;

//import org.apache.commons.io.FileUtils;

/**
 * Example to watch a directory (or tree) for changes to files.
 * TODO: See possible improvements: http://stackoverflow.com/questions/2056221/recursively-list-files-in-java/24006711
 */
public class ProcessFolder {

  private final WatchService watcher;
  private final Map<WatchKey, Path> keys;
  static boolean recursive;
  public static String watchDirectory;
  public static String duplicatesReport;
  public static String commandsOS;
  public static String commandsComment;
  public static String commandsRowOperations;
  public static String commandsOperationSkipRow = "last";
  public static String logFolder = "logs";
  public static String logFile = logFolder + "/output.log";
  public static Integer showBiggestItems;
  public static String similarFoldersReport;
  public static boolean skipProcessed;
  public static boolean processOnly;
  public static boolean multithread = false;
  private static final long startTime = System.nanoTime();
  

  private boolean trace = true;
  public static Boolean refreshDB = true;
  public static SupportedProcessors[] processors;
  private static final Logger logger = Logger.getLogger(ProcessFolder.class.getName());

  public static long getTimeIntervalMillis() {
    return TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - startTime);
  }
  
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
      duplicatesReport = prop.getProperty("duplicatesReport", "duplicates.log");
      similarFoldersReport = prop.getProperty("similarFoldersReport", "similar_folders.log");
      commandsComment = prop.getProperty("commandsComment", "# ");
      commandsRowOperations = prop.getProperty("commandsRowOperations", "# ");
      commandsOperationSkipRow = prop.getProperty("commandsOperationSkipRow", "last");
      showBiggestItems = Integer.valueOf(prop.getProperty("showBiggestItems", "-1"));
      refreshDB = Boolean.valueOf(prop.getProperty("refreshDB", "True"));
      skipProcessed = Boolean.valueOf(prop.getProperty("skipProcessed", "False"));
      processOnly = Boolean.valueOf(prop.getProperty("processOnly", "False"));
      //TODO If processOnly; then check if the required DBs are available??
      
      
      processors = Stream.of(prop.getProperty("processors").split(" ?, ?"))
          .map(v -> v.toUpperCase())
          .map(SupportedProcessors::valueOf)
          .toArray(size -> new SupportedProcessors[size]);

      logFolder = prop.getProperty("logFolder");
      logFile = logFolder + "/" + prop.getProperty("logFile");

      File wf = new File(watchDirectoryPropertyValue);
      if (wf.exists() && wf.isDirectory()) {
        watchDirectory = watchDirectoryPropertyValue;
      } else {
        String exceptionMessage = String.format("The provided folder:<%s> is not valid folder or the app has no access", watchDirectoryPropertyValue);
        throw new IllegalArgumentException(exceptionMessage);
      }

    } catch (IOException ex) {
      ex.printStackTrace();
    } finally {
      if (input != null) {
        try {
          input.close();
        } catch (IOException e) {
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
      } else {
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
    } catch (IOException ex) {
      System.err.println(ex);
    }
  }

  /**
   * Creates a WatchService and registers the given directory
   */
  ProcessFolder(Path dir, boolean recursive) throws IOException {
    //TODO: Clean up maybe the watch pattern is not needed
    this.watcher = FileSystems.getDefault().newWatchService();
    this.keys = new HashMap<WatchKey, Path>();
    this.recursive = recursive;

    if (recursive) {
      Date t0 = new Date();
      System.out.format("Scanning %s ... \n", dir);
      registerAll(dir);
      Date t1 = new Date();
      System.out.format("%nDone. (%s ms) %n", (t1.getTime() - t0.getTime()));
    } else {
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
      } catch (InterruptedException x) {
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
    PrintStream stdout = null;
    stdout = ProcessFolder.toggleStdOut(new PrintStream(new BufferedOutputStream(new FileOutputStream(logFile)), true));

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
    } catch (NoSuchMethodException | SecurityException | ClassNotFoundException | IllegalAccessException | IllegalArgumentException | InvocationTargetException ex) {
      System.err.println("An exception occur, these are the details" + ex.getMessage());
    } catch (NoSuchFieldException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
    // register directory and process its events
    Path dir = Paths.get(watchDirectory);
    if (/* refreshFileData && */Files.isWritable(dir)) {
      if (!processOnly) {
        System.out.println("Analyzing folder " + watchDirectory);
        ProcessFolder p = new ProcessFolder(dir, recursive);
      } else {
        System.out.println("Skipping analyzing folder " + watchDirectory);
      }
      System.out.println("Start processing folder " + watchDirectory);
      processProcessed();
      System.out.println("Processing done!");
      // p.processEvents();
      stdout = ProcessFolder.toggleStdOut(stdout);
    } else {
      System.err.println("Cannot access the folder '" + watchDirectory + "' for processing");
    }
    System.out.printf("Processing done!\nDuration: %d msecs.\n", getTimeIntervalMillis());
  }

  private static void init() {
    loadProperties();
    new File(logFolder).mkdirs();

//    reRouteOutput();

  }

  static private PrintStream stdout = System.out;;
  public static PrintStream toggleStdOut(PrintStream out) {
    PrintStream prevOut; 
    if (out != null) {
      prevOut = stdout;
      stdout = out;
      System.setOut(stdout);
      return prevOut;
    }
    System.setOut(stdout);
    return stdout;
  }
  
  private static void reRouteOutput() {
    try {
      stdout = System.out;
      System.setOut(new PrintStream(new BufferedOutputStream(new FileOutputStream(logFile)), true));
    } catch (FileNotFoundException e) {
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
