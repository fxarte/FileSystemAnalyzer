package com.dscid.filesystemanalyzer.analyzers;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import org.apache.commons.codec.binary.StringUtils;

import com.dscid.filesystemanalyzer.Analyzer;
import com.dscid.filesystemanalyzer.FileContext;
import com.dscid.filesystemanalyzer.DB.DBLayer;
import com.dscid.filesystemanalyzer.DB.DBSingleStorage;

public enum ItemVector implements FileAnalyzer, DBSingleStorage {
  INSTANCE;
  private final DBLayer DBInstance;
  private final Set<Class<? extends FileAnalyzer>> dependencies;

  ItemVector() {
    this.DBInstance = DBLayer.getDbInstanceOf(ItemVector.class);
    this.dependencies = new LinkedHashSet<Class<? extends FileAnalyzer>>();
    dependencies.add(ItemHash.class);
  }

  Set<Class<? extends Analyzer>> getDependencies() {
    Set<Class<? extends Analyzer>> defensiveCopyMap = new LinkedHashSet<Class<? extends Analyzer>>();
    defensiveCopyMap.addAll(dependencies);
    return defensiveCopyMap;
  }

  @Override
  public void analyzeItem(FileContext context) {
    // TODO Auto-generated method stub
  }


  @Override
  /**
   * Generates a vector representation for each folder
   */
  public void analyzeItems(FileContext context) {
    
  }

  /**
   * Returns the one hot vector representation for a given path
   * @param path
   * @return One hot vector representation as List<Integer>
   */
  public Long[] getValueOf(String path) {
    String sizeString = DBInstance.selectValueOf(path);
    List<Long> vec = new ArrayList<Long>();
    String iStdfdfr = "233457657";
    try{
    for (String iStr : sizeString.split("\t")){
      iStdfdfr = iStr;
      vec.add(Long.parseLong(iStr));
    }
    }catch (Exception e) {
      System.err.println(iStdfdfr);
      e.printStackTrace();
    }
    
    return vec.toArray(new Long[vec.size()]);
  }

  static public String listNumericString(List<Long> list, String glue) {
    StringBuilder strbul = new StringBuilder();
    Iterator<Long> iter = list.iterator();
    while (iter.hasNext()) {
      strbul.append(iter.next());
      if (iter.hasNext()) {
        strbul.append(glue);
      }
    }
    return strbul.toString();
  }

}
