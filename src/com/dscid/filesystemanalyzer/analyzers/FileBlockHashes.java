package com.dscid.filesystemanalyzer.analyzers;

import com.dscid.filesystemanalyzer.FileContext;
import com.dscid.filesystemanalyzer.DB.DBSingleStorage;
/**
 * Calculates the hash per IO Block
 * Useful for file similarity comparison similar to folders algorthms's
 * @author felix
 *
 */
public enum FileBlockHashes implements FileAnalyzer, DBSingleStorage {
  ;

  @Override
  public void analyzeItem(FileContext context) {
    // TODO Auto-generated method stub
    
  }

  @Override
  public void analyzeItems(FileContext context) {
    // TODO Auto-generated method stub
    
  }

}
