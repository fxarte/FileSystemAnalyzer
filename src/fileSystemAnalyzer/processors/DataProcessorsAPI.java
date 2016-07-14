package fileSystemAnalyzer.processors;

import java.util.LinkedHashSet;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;


public enum DataProcessorsAPI {

  INSTANCE;
  private static final Logger logger = Logger.getLogger(DataProcessorsAPI.class.getName());

  Set<Processors> pl = new LinkedHashSet<Processors>();

  Set<Processors> getDataProcessors() {
    pl.add(Duplicates.INSTANCE);
    return pl;
  }

  /**
   * register the providers
   */
  void setAnalyzerProviders() {

  }

  public void processData() {
    logger.info("------------- Processing data...");
    for (Processors p : getDataProcessors()) {
      ((Processors) p).analyzeItems();
    }
  }
}
