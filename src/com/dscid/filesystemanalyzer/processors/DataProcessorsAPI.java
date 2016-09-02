package com.dscid.filesystemanalyzer.processors;

import java.util.LinkedHashSet;
import java.util.Set;
import java.util.logging.Logger;
import java.util.stream.Stream;

import com.dscid.filesystemanalyzer.App.ProcessFolder;

public enum DataProcessorsAPI {

  INSTANCE;
  private static final Logger logger = Logger.getLogger(DataProcessorsAPI.class.getName());

  Set<Processors> pl = new LinkedHashSet<Processors>();

  Set<Processors> getDataProcessors() {
    pl.add(Duplicates.INSTANCE);
//    pl.add(Similarity.INSTANCE);
    return pl;
  }

  /**
   * register the providers
   */
  void setAnalyzerProviders() {

  }

  public void processData() {
    logger.info("------------- Processing data...");
//    Stream.of(ProcessFolder.processors)
//      .map(p -> SupportedProcessors.getProcessor(p))
//      .forEach(p -> p.analyzeItems());
    
    for (Processors p : getDataProcessors()) {
      ((Processors) p).analyzeItems();
    }
  }
}
