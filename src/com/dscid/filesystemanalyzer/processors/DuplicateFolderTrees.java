package com.dscid.filesystemanalyzer.processors;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.dscid.filesystemanalyzer.DB.DBLayer;
import com.dscid.filesystemanalyzer.DB.DBLayerException;
import com.dscid.filesystemanalyzer.analyzers.ItemCore;
import com.dscid.filesystemanalyzer.analyzers.ItemHash;

public enum DuplicateFolderTrees implements Processors {

  INSTANCE;

  private DuplicateFolderTrees() {
    // TODO Auto-generated constructor stub
  }

  public void analyzeItems() {
    Map<String, List<String>> duplicateHashedFiles = new HashMap<String, List<String>>();
    // TODO: Add the elements to analyze here from the Duplictae processor
    // fileSystemAnalyzer.DB
    // if it is a folder check if it is part of a duplicate tree
    Map<String, List<String>> duplicateTrees = detectDuplicateTrees(duplicateHashedFiles);
    System.out.print(duplicateTrees);
  }

  private static Map<String, List<String>> detectDuplicateTrees(Map<String, List<String>> duplicateHashedFiles) {
    Map<String, List<String>> duplicateTrees = null;
    List<String> treeCommonParent = null;
    Map<String, Set<String>> tree = new HashMap<String, Set<String>>();
    // paths per hash
    Map<String, Set<String>> treeCommonParentSet = new HashMap<String, Set<String>>();
    ;
    System.out.format("----------------------------------------------------------%n");
    System.out.format("Checking trees%n");
    for (Map.Entry<String, List<String>> hashedGroups : duplicateHashedFiles.entrySet()) {
      String hash = hashedGroups.getKey();
      List<String> paths = hashedGroups.getValue();
      // if all paths are of the same type and are folders, and have the
      // same size, the proceed
      if (getPathsType(paths).equals("D") && sizesAreEqual(paths)) {
        if (true) {

        }
        if (paths.size() >= 2) {
          // treeCommonParent = getTreeCommonParents(paths.get(0),
          // paths.get(1));
          // System.out.format("Paths: '%s' and '%s' have parents:%n '%s' and '%s' %n",
          // paths.get(0), paths.get(1), treeCommonParent.get(0),
          // treeCommonParent.get(1));
          Set<String> ids = new HashSet<String>();
          for (String path : paths) {
            String id = DBLayer.selectInstanceValueOf(DBLayer.getDbInstanceOf(ItemCore.class), path, "ID");
            if (!treeCommonParentSet.containsValue(path)) {
              ids.add(id);
            }
          }

          Set<String> t = getTreeCommonParents(ids);

          if (t.size() == 1) {
            String parentId = t.iterator().next();
            tree.put(parentId, ids);
          } else {
            String parentIds = DBLayer.join(t, "|");
            tree.put(parentIds, ids);
          }

          // not working, still confusing. and I'm hungry and sleepy
          for (String id : t) {
            Long _id = Long.parseLong(id);
            // the aprent of the starting folder is always Zero
            // TODO: come up with a better solution
            if (_id > 0) {
              String _path = DBLayer.selectInstanceValueOf(DBLayer.getDbInstanceOf(ItemCore.class), _id, "path");
              String _hash = DBLayer.selectInstanceValueOf(DBLayer.getDbInstanceOf(ItemHash.class), _path, "value");
              Set<String> thisHashPaths = treeCommonParentSet.get(_hash);
              if (_hash.length() == 0) {
                String s = "no hash!";
              }
              if (thisHashPaths != null) {
                thisHashPaths.add(_path);
                treeCommonParentSet.put(_hash, thisHashPaths);
              } else {
                treeCommonParentSet.put(_hash, new HashSet<String>(Arrays.asList(_path)));
              }
            }
          }

        }
      }

    }
    Map<String, Set<String>> dupParents = new HashMap<String, Set<String>>();
    for (Map.Entry<String, Set<String>> entry : tree.entrySet()) {
      String key = entry.getKey();
      // skip multiple parents for now
      if (!key.contains("|")) {
        Set<String> value = entry.getValue();
        String _ParentPath = DBLayer.selectInstanceValueOf(DBLayer.getDbInstanceOf(ItemCore.class), Long.parseLong(key), "path");
        String _ParentHash = DBLayer.selectInstanceValueOf(DBLayer.getDbInstanceOf(ItemHash.class), _ParentPath, "value");
        if (dupParents.containsKey(_ParentHash)) {
          dupParents.get(_ParentHash).add(_ParentPath);
        } else {
          dupParents.put(_ParentHash, new HashSet<String>(Arrays.asList(_ParentPath)));
        }
        System.out.format("'%s' ('%s': '%s')%n", key, _ParentHash, _ParentPath);
        for (String childId : value) {
          String _childPath = DBLayer.selectInstanceValueOf(DBLayer.getDbInstanceOf(ItemCore.class), Long.parseLong(childId), "path");
          String _childHash = DBLayer.selectInstanceValueOf(DBLayer.getDbInstanceOf(ItemHash.class), _ParentPath, "value");
          System.out.format("\t'%s' ('%s': '%s')%n", childId, _childHash, _childPath);
        }
      }
    }

    System.out.format("----------------------------------------------------------------%n");
    for (Map.Entry<String, Set<String>> entry : dupParents.entrySet()) {
      String key = entry.getKey();
      Set<String> value = entry.getValue();
      System.out.format("'%s'%n", key);
      for (String childId : value) {
        System.out.format("\t'%s'%n", childId);
      }
    }

    // System.out.print(treeCommonParent);
    return duplicateTrees;
  }

  private static String getPathsType(List<String> paths) {
    List<String> l = DBLayer.selectValuesOf(DBLayer.getDbInstanceOf(ItemCore.class), "value", paths);
    boolean F = l.contains("F");
    boolean D = l.contains("D");

    if (F && D) {
      return "";
    } else if (F) {
      return "F";
    } else {
      return "D";
    }
  }

  /**
   * Check whether the whole collection items their sizes are the same or not,
   * This will avoid extra processing
   *
   * @param paths
   * @return
   */
  private static boolean sizesAreEqual(List<String> paths) {
    // TODO Auto-generated method stub
    return true;
  }

  private static Set<String> getTreeCommonParents(Set<String> ids) {
    List<String> parentIds = new ArrayList<String>();
    Set<String> parentHashes = new HashSet<String>();
    Map<String, String> duplicateParents = new HashMap<String, String>();
    for (String id : ids) {
      String parentId = DBLayer.selectInstanceValueOf(DBLayer.getDbInstanceOf(ItemCore.class), Long.parseLong(id), "parent");
      String parentHash = DBLayer.selectInstanceValueOf(DBLayer.getDbInstanceOf(ItemHash.class), Long.parseLong(id), "value");
      if (!parentHashes.add(parentHash)) {
        duplicateParents.put(parentId, parentHash);
      }
      parentIds.add(parentId);
    }

    // Finding common ancestors: 3 options:
    // 1 no common parent at all, all different: duplicateParents
    // 2 unique parent, all equals:
    // 3 further analysis, mixed: set.size != list.size and set.size>=1 and
    // list.size=n>=3
    if (duplicateParents.size() == 0) {
      // no parents in common, stop and return parent Set
      // OR
      // all have the same parent. Stop and return parent
      // return parentIdSet;
      return new HashSet<String>(parentIds);
    } else if (duplicateParents.size() == 1) {
      return new HashSet<String>(duplicateParents.keySet());
    } else {
      return getTreeCommonParents(duplicateParents.keySet());
    }
  }

  private static List<String> getTreeCommonParents(String path1, String path2) {
    // D:\TEMP\duplitest\containers\MOV02105.AVI === > 3075
    // 3075 ====> D:\TEMP\duplitest\containers
    String parent1Id = DBLayer.selectInstanceValueOf(DBLayer.getDbInstanceOf(ItemCore.class), path1, "parent");
    String parent1Path = DBLayer.selectInstanceValueOf(DBLayer.getDbInstanceOf(ItemCore.class), Long.parseLong(parent1Id), "path");

    String parent2Id = DBLayer.selectInstanceValueOf(DBLayer.getDbInstanceOf(ItemCore.class), path2, "parent");
    String parent2Path = DBLayer.selectInstanceValueOf(DBLayer.getDbInstanceOf(ItemCore.class), Long.parseLong(parent2Id), "path");

    // if we end up in the same folder, return the children
    if (parent1Id.equals(parent2Id)) {
      return new ArrayList<String>(Arrays.asList(parent1Path, parent2Path));
    } else {
      // get parent's hashes and if equal keep going up
      String hash1 = DBLayer.getDbInstanceOf(ItemHash.class).selectValueOf(parent1Path);
      String hash2 = DBLayer.getDbInstanceOf(ItemHash.class).selectValueOf(parent2Path);
      if (hash1.equals(hash2)) {
        return getTreeCommonParents(parent1Path, parent2Path);
      } else {
        return new ArrayList<String>(Arrays.asList(parent1Path, parent2Path));
      }
    }
  }

}
