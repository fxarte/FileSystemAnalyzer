package com.dscid.filesystemanalyzer.analyzers;

import com.dscid.filesystemanalyzer.Analyzer;
import com.dscid.filesystemanalyzer.FileContext;

/**
 * @author felix
 */
public interface FileAnalyzer extends Analyzer<FileContext, FileContext> {
  void analyzeItem(FileContext context);

  void analyzeItems(FileContext context);
}
