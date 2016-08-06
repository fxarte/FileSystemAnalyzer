package com.dscid.filesystemanalyzer.analyzers;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.dscid.filesystemanalyzer.Analyzer;
import com.dscid.filesystemanalyzer.FileContext;
import com.dscid.filesystemanalyzer.App.ProcessFolder;
import com.dscid.filesystemanalyzer.DB.DBLayer;
import com.dscid.filesystemanalyzer.DB.DBSingleStorage;

public enum ItemCore implements FileAnalyzer, DBSingleStorage {

    INSTANCE;
    private final DBLayer DBInstance;
    //core doesn't have dependencies so it should be empty
    // dependencies are the names of the analyzers, 
    // the Analyzer loader in the API? will take care of providing the proper references
    private final Set<Class<? extends FileAnalyzer>> dependencies;
    

    ItemCore() {
        this.DBInstance = DBLayer.getDbInstanceOf(ItemCore.class);
        this.dependencies = new LinkedHashSet<Class<? extends FileAnalyzer>>();
        dependencies.add(ItemModifiedTimeStamp.class);
    }

    public void analyzeItem(FileContext context) {
      DBInstance.updateValue(context.getPath().toString(), context.getPath().getParent().toString(), "F");
    }

    @Override
    public void analyzeItems(FileContext context) {
        DBInstance.updateValue(context.getPath().toString(), context.getPath().getParent().toString(), "D");
    }

    Set<Class<? extends Analyzer>> getDependencies() {
        // TODO remove defensive copy if this class end up used only within the package
        //Make defensive copy here  
        Set<Class<? extends Analyzer>> defensiveCopyMap = new LinkedHashSet<Class<? extends Analyzer>>();
        defensiveCopyMap.addAll(dependencies);
        return defensiveCopyMap;
    }

    public String getValueOf(String path) {
      return DBInstance.selectValueOf(path);
    }

    public Map<String, List<String>> getGroupedValues(int minCount) {
      return DBInstance.selectGroupedValues(minCount);
    }
    public List<String> getChildren(String parent) {
      return DBInstance.getChildrenValues(parent);
    }
    public List<String> getChildrenPaths(String parent) {
      return DBInstance.getChildrenPaths(parent);
    }
    public int getValueInt(String path){
      String type = getValueOf(path);
      if (type.equals("F")) {
        return 3;
      } else if (type.equals("D")) {
        return 5;
      } else {
        return 0;
      }
    }
}
