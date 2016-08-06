package com.dscid.filesystemanalyzer.analyzers;

import java.util.LinkedHashSet;
import java.util.Set;

import com.dscid.filesystemanalyzer.FileContext;
import com.dscid.filesystemanalyzer.ProcessingActions;
import com.dscid.filesystemanalyzer.App.ProcessFolder;
import com.dscid.filesystemanalyzer.DB.DBLayer;
import com.dscid.filesystemanalyzer.DB.DBSingleStorage;

public enum ItemModifiedTimeStamp implements FileAnalyzer, DBSingleStorage {

  INSTANCE;
  private final DBLayer DBInstance;
  private final Set<Class<? extends FileAnalyzer>> dependencies;

  // private static final Logger logger =
  // Logger.getLogger(ItemModifiedTimeStamp.class.getName());

  private ItemModifiedTimeStamp() {
    DBInstance = DBLayer.getDbInstanceOf(ItemModifiedTimeStamp.class);
    dependencies = new LinkedHashSet<Class<? extends FileAnalyzer>>();
    // dependencies.add(ItemCore.class);
  }

  @Override
  public void analyzeItem(FileContext context) {
    long value = context.getFileAttributes().lastModifiedTime().toMillis();
    String path = context.getPath().toString();
    if (ProcessFolder.skipProcessed) {
      String oldTS = getValueOf(path);
      if (oldTS != null && !oldTS.isEmpty()) {
        long db_val = Long.valueOf(oldTS);
        if (db_val == value) {
          context.UpdateAction(ProcessingActions.Skip);
          return;
        }
      }
    }
    String strValue = String.valueOf(value);
    DBInstance.updateValue(path, context.getPath().getParent().toString(), strValue);
  }

  @Override
  public void analyzeItems(FileContext context) {
    String value = String.valueOf(context.getFileAttributes().lastModifiedTime().toMillis());
    DBInstance.updateValue(context.getPath().toString(), context.getPath().getParent().toString(), value);
  }

  /**
   * Returns the size in bytes of a given path
   * 
   * @param path
   * @return
   */
  public String getValueOf(String path) {
    // TODO: forgot behavior with dirs
    String value = DBInstance.selectValueOf(path);

    return value;
  }
}
