package com.dscid.filesystemanalyzer.analyzers;

import java.util.LinkedHashSet;
import java.util.Set;

import com.dscid.filesystemanalyzer.Analyzer;
import com.dscid.filesystemanalyzer.FileContext;
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
}
