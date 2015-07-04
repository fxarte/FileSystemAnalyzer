package fileSystemAnalyzer.processors;

import fileSystemAnalyzer.App.ProcessFolder;
import static fileSystemAnalyzer.App.ProcessFolder.commandsRowOperations;
import fileSystemAnalyzer.DB.DBSingleStorage;
import fileSystemAnalyzer.analyzers.ItemCore;
import fileSystemAnalyzer.analyzers.ItemHash;
import fileSystemAnalyzer.analyzers.ItemSize;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

/**
 * Find duplicates using exact match hash values Have dependencies in the form
 * of class names (from which the proper fileSystemAnalyzer.DB is inferred) IT
 * must know how to handle each fileSystemAnalyzer.DB, so fileSystemAnalyzer.DB
 * must be kept simple.
 *
 * @author felix
 *
 */
public enum Duplicates implements Processors, DBSingleStorage {

  INSTANCE;
  //private final DBLayer DBInstance;

  private Duplicates() {
    // DBInstance = DBLayer.getDbInstanceOf(Duplicates.class);
  }

  /**
   * Processes the hashes for duplicate folders and files type(t1:folder,
   * t2:file)
   */
  @Override
  public void analyzeItems() {

    System.out.println(String.format("%s processing Duplicates ... ", ProcessFolder.commandsComment));
//    System.out.print("building possible duplicate by groups");
    Map<String, Map<Long, Map<String, List<String>>>> groupedSizes = buildHashValues(1);
//    System.out.print("Done %n");

    // NOTE: subfolders of a bigger duplicate folder may be part of another hash group, 
    // for instace 2 empty folder that are not part of similar tress at all.
    // May be is best to leav it as is, as clean the FS iteratively
    for (Map.Entry<String, Map<Long, Map<String, List<String>>>> entry : groupedSizes.entrySet()) {

      String groupType = entry.getKey();
      System.out.println(String.format("%s Type: %s", ProcessFolder.commandsComment, groupType));
      Map<Long, Map<String, List<String>>> sortedBySizeGroup = entry.getValue();
      for (Map.Entry<Long, Map<String, List<String>>> hashedPaths : sortedBySizeGroup.entrySet()) {
        Long groupSize = hashedPaths.getKey();
        System.out.println(String.format("%s\t Size: %d", ProcessFolder.commandsComment, groupSize));
        for (Map.Entry<String, List<String>> hashedValues : hashedPaths.getValue().entrySet()) {
          String groupHash = hashedValues.getKey();
          System.out.println(String.format("%s\t\t Hash: %s", ProcessFolder.commandsComment, groupHash));
          int operationCount = 0;
          int skipRowOperation = ("first".equals(ProcessFolder.commandsOperationSkipRow))?0:hashedValues.getValue().size()-1;
          for (String path : hashedValues.getValue()) {
            if (operationCount == skipRowOperation) {
              System.out.println(String.format("%s %s", ProcessFolder.commandsComment, String.format(ProcessFolder.commandsRowOperations, path)));
            } else {
              System.out.println(String.format(ProcessFolder.commandsRowOperations, path));
            }
            operationCount++;
          }
        }
      }
    }
    System.out.println(String.format("%s %s",ProcessFolder.commandsComment, "Duplicates done!"));
  }

  /**
   * Build a collection of paths grouped by hash and item type Has memory
   * issues, see the DB version
   *
   * @param minCount
   * @return
   */
  Map<String, Map<Long, Map<String, List<String>>>> buildHashValues(int minCount) {
    //itemType, size, hash, paths
    Map<String, Map<Long, Map<String, List<String>>>> results = new TreeMap<>();
    Map<Long, Map<String, List<String>>> filesSortedBySize = new TreeMap<>(Collections.reverseOrder());
    Map<Long, Map<String, List<String>>> directoriesSortedBySize = new TreeMap<>(Collections.reverseOrder());

    Map<String, List<String>> entrySet = ItemHash.INSTANCE.getGroupedValues(minCount);

    for (Map.Entry<String, List<String>> entry : entrySet.entrySet()) {
      String itemsType = getPathsValidatedType(entry.getValue());
      Long size = getPathsValidatedSize(entry.getValue());
      Map<String, List<String>> hashedPaths = new HashMap<>();
      hashedPaths.put(entry.getKey(), entry.getValue());
      if (itemsType.equals("D")) {
        directoriesSortedBySize.put(size, hashedPaths);
      } else {
        filesSortedBySize.put(size, hashedPaths);
      }
    }

    results.put("D", directoriesSortedBySize);
    results.put("F", filesSortedBySize);
    return results;
  }

  /**
   * Builds the tree in the DB
   *
   * @param parent concatenated type[F|D]
   * @param hashedPaths
   */
  void buildDBDuplicateTree() {
    Map<String, List<String>> entrySet = ItemHash.INSTANCE.getGroupedValues(1);
    for (Map.Entry<String, List<String>> entry : entrySet.entrySet()) {
      String itemsType = getPathsValidatedType(entry.getValue());
      Long size = getPathsValidatedSize(entry.getValue());
      //DBInstance.updateValue(itemsType, size.toString(), itemsType);

      for (String path : entry.getValue()) {

      }
    }
    // DBInstance.updateValue(context.getPath().toString(), context.getPath().getParent().toString(), "D");
  }

  /**
   * determines that the paths collection are indeed of the same type, ither
   * forlder or files
   *
   * @param paths
   * @return
   */
  String getPathsValidatedType(List<String> paths) throws IllegalArgumentException {
    Set<String> itemType = new HashSet<>();
    for (String path : paths) {
      itemType.add(ItemCore.INSTANCE.getValueOf(path));
    }
    if (itemType.size() == 1) {
      return itemType.iterator().next();
    } else {
      throw new IllegalArgumentException("The paths do not have the same type, folder or file");
    }
  }

  /**
   * determines that the paths collection are indeed of the same size Adds extra
   * processing, but makes sure we get a correct duplicate collection
   *
   * @param paths
   * @return
   */
  Long getPathsValidatedSize(List<String> paths) throws IllegalArgumentException {
    Set<Long> sizes = new HashSet<>();
    for (String path : paths) {
      sizes.add(ItemSize.INSTANCE.getValueOf(path));
    }
    if (sizes.size() == 1) {
      return sizes.iterator().next();
    } else {
      throw new IllegalArgumentException("The paths do not have the same size");
    }
  }
}
