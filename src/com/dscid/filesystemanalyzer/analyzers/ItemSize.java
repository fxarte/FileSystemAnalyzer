package com.dscid.filesystemanalyzer.analyzers;

import com.dscid.filesystemanalyzer.Analyzer;
import com.dscid.filesystemanalyzer.DB.DBLayer;
import com.dscid.filesystemanalyzer.DB.DBSingleStorage;
import com.dscid.filesystemanalyzer.FileContext;

import java.io.IOException;
import java.nio.file.Files;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

public enum ItemSize implements FileAnalyzer, DBSingleStorage {

  INSTANCE;
  private final DBLayer DBInstance;
  private final Set<Class<? extends FileAnalyzer>> dependencies;

  private ItemSize() {
    DBInstance = DBLayer.getDbInstanceOf(ItemSize.class);
    dependencies = new LinkedHashSet<Class<? extends FileAnalyzer>>();
    dependencies.add(ItemCore.class);
  }

  public void analyzeItem(FileContext context) {
    // TODO: Add proper ItemCore dependency logic, sych the ids
    String size = String.valueOf(context.getFileAttributes().size());
    DBInstance.updateValue(context.getPath().toString(), context.getPath().getParent().toString(), size);
    // System.out.format("processing File: %s\n", filePath);
  }

  public void analyzeItems(FileContext context) {
    // Long size = Files.size(context.getPath());
    String folderPath = context.getPath().toString();
    String results = DBInstance.sumValuesByParent(folderPath);
    Long sum = 0L;
    if (results != null) {
      sum = Long.parseLong(results);
    }

    DBInstance.updateValue(folderPath, context.getPath().getParent().toString(), sum.toString());

  }

  Set<Class<? extends Analyzer>> getDependencies() {
    // TODO Auto-generated method stub
    Set<Class<? extends Analyzer>> defensiveCopyMap = new LinkedHashSet<Class<? extends Analyzer>>();
    defensiveCopyMap.addAll(dependencies);
    return defensiveCopyMap;
  }

  /**
   * Returns the size in bytes of a given path
   * 
   * @param path
   * @return
   */
  public Long getValueOf(String path) {
    // TODO: forgot behavior with dirs
    String sizeString = DBInstance.selectValueOf(path);
    long size = 0L;
    if (sizeString != null && !sizeString.trim().equals("")) {
      size = Long.parseLong(sizeString);
    }
    else {
      String m = String.format("null or empty size for path %s.", path);
      System.out.println(m);
    }
    return new Long(size);
  }

  public Map<Long, List<String>> getGroupedValues(int minCount) {
    Map<String, List<String>> groupedSizes = DBInstance.selectGroupedValues(minCount);
    Map<Long, List<String>> results = new TreeMap<Long, List<String>>(Collections.reverseOrder());
    for (Map.Entry<String, List<String>> entry : groupedSizes.entrySet()) {
      long size = 0L;
      String sizeString = entry.getKey();
      if (sizeString != null && !sizeString.trim().equals("")) {
        size = Long.parseLong(sizeString);
      }
      else {
        String m = "Null or empty string size found during processing grouped sizes";
        System.out.println(m);
      }
      results.put(size, entry.getValue());
    }
    return new HashMap<Long, List<String>>(results);
  }
}
