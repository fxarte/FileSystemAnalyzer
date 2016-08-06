package com.dscid.filesystemanalyzer.processors;

import com.dscid.filesystemanalyzer.Analyzer;

/**
 * Processors are run after all analyzers, they will use the data generated by
 * them, and will not access the files directly
 * 
 * @author felix
 *
 */
public interface Processors extends Analyzer {
  void analyzeItems();
}
