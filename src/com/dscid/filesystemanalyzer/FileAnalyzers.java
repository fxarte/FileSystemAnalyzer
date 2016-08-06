/**
 * Allows the registration of the analysis providers
 * See: Effective Java (2nd Ed): @Item1 
 */
package com.dscid.filesystemanalyzer;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author felix
 *
 */
public final class FileAnalyzers {

  private FileAnalyzers() {
  }

  // Plugins, or Analysis Providers
  private static final Map<String, FileAnalysisProvider> fileAnalysisProviders = new ConcurrentHashMap<String, FileAnalysisProvider>();
  private static final String DEFAULT_FILE_ANALYSIS_PROVIDER_NAME = "ItemCore";

  // Registration API
  public static void registerDefaultAnalysisProvider(FileAnalysisProvider a) {
    registerAnalysisProvider(DEFAULT_FILE_ANALYSIS_PROVIDER_NAME, a);
  }

  private static void registerAnalysisProvider(String name, FileAnalysisProvider a) {
    fileAnalysisProviders.put(name, a);
  }

  // Access API
  public static Analyzer<?, ?> newInstance() {
    return newInstance(DEFAULT_FILE_ANALYSIS_PROVIDER_NAME);
  }

  /**
   * Not available at the moment
   * 
   * @param AnalyzerName
   * @return
   */
  private static Analyzer<?, ?> newInstance(String AnalyzerName) {
    throw new AssertionError();
    /*
     * FileAnalysisProvider ap = fileAnalysisProviders.get(AnalyzerName); if (ap
     * == null) throw new
     * IllegalArgumentException("No Analysis Provider registered with name: " +
     * AnalyzerName); return ap.newAnalyzer();
     */
  }
}
